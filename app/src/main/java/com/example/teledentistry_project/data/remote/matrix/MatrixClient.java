package com.example.teledentistry_project.data.remote.matrix;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.teledentistry_project.BuildConfig;
import com.example.teledentistry_project.utils.SessionManager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.security.SecureRandom;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Matrix client (singleton). Session handling, appointment room lookup, sending events,
 * history and a long-poll /sync loop. All callbacks are delivered on the main thread.
 *
 * Usage: MatrixClient.getInstance().init(context) once (idempotent), then
 * getAppointmentRoom(...), sendTextMessage(...), getRecentMessages(...), startSync(...).
 */
public class MatrixClient {

    private static final String TAG = "MatrixClient";
    private static final String PREFS = "matrix_prefs";
    private static final int SYNC_TIMEOUT_MS = 30000;

    private static String normalize(String url) {
        return url.endsWith("/") ? url : url + "/";
    }

    private static MatrixClient instance;

    private final MatrixApi matrixApi;
    private final MatrixSessionApi sessionApi;
    private final Handler main = new Handler(Looper.getMainLooper());

    private Context appContext;
    private SessionManager pbSession;
    private SharedPreferences prefs;

    // Current Matrix session (cached per PocketBase user)
    private String accessToken = "";
    private String userId = "";
    private String sessionPbUserId = "";
    private String deviceId = "";

    private boolean sessionRequestInFlight = false;
    private final List<MatrixCallback<MatrixAuthResponse>> sessionWaiters = new ArrayList<>();

    // Serial send queue so rapid messages keep their order
    private final ArrayDeque<Runnable> sendQueue = new ArrayDeque<>();
    private boolean sending = false;

    public interface MatrixCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    /** Receives events from a sync loop (main thread). */
    public interface SyncListener {
        /**
         * @param roomId         the room these events belong to
         * @param stateEvents    state events (state section; in a sync with a gap these precede the timeline)
         * @param timelineEvents timeline events (messages, call events, state changes), oldest first
         * @param initial        true for the first sync of this loop (state snapshot, empty timeline)
         */
        void onEvents(String roomId, List<MatrixSyncResponse.Event> stateEvents,
                      List<MatrixSyncResponse.Event> timelineEvents, boolean initial);

        /** Called when a sync request failed; the loop retries with backoff. */
        void onSyncError(String error);
    }

    private MatrixClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        logging.redactHeader("Authorization");

        // Read timeout must exceed the 30s sync long-poll
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build();

        matrixApi = new Retrofit.Builder()
                .baseUrl(normalize(BuildConfig.MATRIX_URL))
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MatrixApi.class);

        sessionApi = new Retrofit.Builder()
                .baseUrl(normalize(BuildConfig.POCKETBASE_URL))
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MatrixSessionApi.class);
    }

    public static synchronized MatrixClient getInstance() {
        if (instance == null) {
            instance = new MatrixClient();
        }
        return instance;
    }

    /** Idempotent; must be called before any other method that needs a session. */
    public synchronized void init(Context context) {
        if (appContext != null) return;
        appContext = context.getApplicationContext();
        pbSession = new SessionManager(appContext);
        prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        deviceId = prefs.getString("device_id", "");
        if (deviceId.isEmpty()) {
            byte[] b = new byte[8];
            new SecureRandom().nextBytes(b);
            StringBuilder sb = new StringBuilder("TD");
            for (byte x : b) sb.append(String.format("%02x", x));
            deviceId = sb.toString();
            prefs.edit().putString("device_id", deviceId).apply();
        }
        sessionPbUserId = prefs.getString("pb_user_id", "");
        userId = prefs.getString("mx_user_id", "");
        accessToken = prefs.getString("mx_token", "");
    }

    // ------------------------------------------------------------------ session

    /** Matrix user id of the current session ("" until a session was acquired). */
    public String getUserId() {
        return userId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getDeviceId() {
        return deviceId;
    }

    /** Fallback room id for screens that are not tied to an appointment. Not a real room. */
    public String getDefaultRoomId() {
        return "";
    }

    /** Forget the Matrix session (call on logout). */
    public synchronized void clearSession() {
        accessToken = "";
        userId = "";
        sessionPbUserId = "";
        if (prefs != null) {
            prefs.edit().remove("pb_user_id").remove("mx_user_id").remove("mx_token").apply();
        }
    }

    private synchronized void invalidateToken(String badToken) {
        if (badToken != null && badToken.equals(accessToken)) {
            accessToken = "";
            if (prefs != null) prefs.edit().remove("mx_token").apply();
        }
    }

    /** Ensures a valid Matrix session for the logged-in PocketBase user. */
    public void ensureSession(MatrixCallback<MatrixAuthResponse> callback) {
        final String pbToken;
        final String pbUserId;
        synchronized (this) {
            if (pbSession == null) {
                postError(callback, "MatrixClient not initialised");
                return;
            }
            pbToken = pbSession.getAuthToken();
            pbUserId = pbSession.getUserId();
            if (pbToken == null || pbUserId == null) {
                postError(callback, "Not logged in");
                return;
            }
            if (!accessToken.isEmpty() && pbUserId.equals(sessionPbUserId)) {
                JsonObject o = new JsonObject();
                o.addProperty("user_id", userId);
                o.addProperty("access_token", accessToken);
                o.addProperty("device_id", deviceId);
                final MatrixAuthResponse c = new Gson().fromJson(o, MatrixAuthResponse.class);
                main.post(() -> callback.onSuccess(c));
                return;
            }
            // different PocketBase user (or no session): drop old one
            if (!pbUserId.equals(sessionPbUserId)) {
                accessToken = "";
                userId = "";
            }
            sessionWaiters.add(callback);
            if (sessionRequestInFlight) return;
            sessionRequestInFlight = true;
        }

        JsonObject body = new JsonObject();
        body.addProperty("device_id", deviceId);
        sessionApi.createSession(pbToken, body).enqueue(new Callback<MatrixAuthResponse>() {
            @Override
            public void onResponse(Call<MatrixAuthResponse> call, Response<MatrixAuthResponse> response) {
                MatrixAuthResponse auth = response.body();
                if (response.isSuccessful() && auth != null && auth.getAccessToken() != null) {
                    synchronized (MatrixClient.this) {
                        accessToken = auth.getAccessToken();
                        userId = auth.getUserId();
                        sessionPbUserId = pbUserId;
                        prefs.edit().putString("pb_user_id", pbUserId)
                                .putString("mx_user_id", userId)
                                .putString("mx_token", accessToken).apply();
                    }
                    finishSession(auth, null);
                } else {
                    finishSession(null, "Could not start chat session (HTTP " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<MatrixAuthResponse> call, Throwable t) {
                Log.w(TAG, "Matrix session request failed", t);
                finishSession(null, "Could not reach server: " + t.getMessage());
            }
        });
    }

    private void finishSession(MatrixAuthResponse auth, String error) {
        List<MatrixCallback<MatrixAuthResponse>> waiters;
        synchronized (this) {
            sessionRequestInFlight = false;
            waiters = new ArrayList<>(sessionWaiters);
            sessionWaiters.clear();
        }
        for (MatrixCallback<MatrixAuthResponse> w : waiters) {
            if (auth != null) w.onSuccess(auth);
            else w.onError(error);
        }
    }

    // ------------------------------------------------------------ authed calls

    private interface CallFactory<T> {
        Call<T> create(String authHeader);
    }

    private static String errorText(Response<?> response) {
        String detail = "";
        try {
            if (response.errorBody() != null) {
                String s = response.errorBody().string();
                JsonObject o = new Gson().fromJson(s, JsonObject.class);
                if (o != null && o.has("error")) detail = ": " + o.get("error").getAsString();
            }
        } catch (Exception ignored) {}
        return "HTTP " + response.code() + detail;
    }

    private static boolean isUnknownToken(Response<?> response) {
        if (response.code() != 401) return false;
        try {
            if (response.errorBody() != null) {
                String s = response.errorBody().string();
                return s.contains("M_UNKNOWN_TOKEN");
            }
        } catch (Exception ignored) {}
        return true;
    }

    private <T> void execute(CallFactory<T> factory, MatrixCallback<T> cb) {
        execute(factory, cb, false, null);
    }

    /** Runs the call with the current session; on M_UNKNOWN_TOKEN re-acquires the session once. */
    private <T> void execute(CallFactory<T> factory, MatrixCallback<T> cb, boolean retried, Holder holder) {
        ensureSession(new MatrixCallback<MatrixAuthResponse>() {
            @Override
            public void onSuccess(MatrixAuthResponse auth) {
                final String token = auth.getAccessToken();
                Call<T> call = factory.create("Bearer " + token);
                if (holder != null) {
                    if (holder.cancelled) return;
                    holder.call = call;
                }
                call.enqueue(new Callback<T>() {
                    @Override
                    public void onResponse(Call<T> c, Response<T> response) {
                        if (response.isSuccessful()) {
                            cb.onSuccess(response.body());
                        } else if (!retried && isUnknownToken(response)) {
                            invalidateToken(token);
                            execute(factory, cb, true, holder);
                        } else {
                            cb.onError(errorText(response));
                        }
                    }

                    @Override
                    public void onFailure(Call<T> c, Throwable t) {
                        if (c.isCanceled()) return;
                        cb.onError(t.getMessage() != null ? t.getMessage() : t.toString());
                    }
                });
            }

            @Override
            public void onError(String error) {
                cb.onError(error);
            }
        });
    }

    private static class Holder {
        volatile boolean cancelled;
        volatile Call<?> call;
    }

    private <T> void postError(MatrixCallback<T> cb, String error) {
        main.post(() -> cb.onError(error));
    }

    // -------------------------------------------------------------------- rooms

    /** Looks up (creating server-side if needed) the Matrix room for an appointment. */
    public void getAppointmentRoom(String appointmentId, MatrixCallback<String> callback) {
        if (pbSession == null || pbSession.getAuthToken() == null) {
            postError(callback, "Not logged in");
            return;
        }
        sessionApi.appointmentRoom(pbSession.getAuthToken(), appointmentId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject body = response.body();
                if (response.isSuccessful() && body != null && body.has("room_id")) {
                    callback.onSuccess(body.get("room_id").getAsString());
                } else if (response.code() == 403) {
                    callback.onError("You are not a participant of this appointment");
                } else {
                    callback.onError("Could not open chat room (" + errorText(response) + ")");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                callback.onError("Could not reach server: " + t.getMessage());
            }
        });
    }

    // ------------------------------------------------------------------ sending

    public static String newTxnId() {
        return "m" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Sends an m.text message. Messages are sent one at a time in call order. Re-sending with the
     * same txnId is idempotent on the server. Callback gets the event id.
     */
    public void sendTextMessage(String roomId, String txnId, String body, MatrixCallback<String> callback) {
        JsonObject content = new JsonObject();
        content.addProperty("msgtype", "m.text");
        content.addProperty("body", body);
        sendEvent(roomId, "m.room.message", txnId, content, callback);
    }

    /** Sends an arbitrary room event (serialised with other sends). Callback gets the event id. */
    public void sendEvent(String roomId, String eventType, String txnId, JsonObject content,
                          MatrixCallback<String> callback) {
        enqueueSend(done -> execute(
                auth -> matrixApi.sendEvent(auth, roomId, eventType, txnId, content),
                new MatrixCallback<JsonObject>() {
                    @Override
                    public void onSuccess(JsonObject r) {
                        done.run();
                        if (r != null && r.has("event_id")) callback.onSuccess(r.get("event_id").getAsString());
                        else callback.onError("No event id in response");
                    }

                    @Override
                    public void onError(String error) {
                        done.run();
                        callback.onError(error);
                    }
                }));
    }

    /** TURN credentials from the homeserver (username, password, ttl, uris). */
    public void getTurnServers(MatrixCallback<JsonObject> callback) {
        execute(auth -> matrixApi.turnServer(auth), callback);
    }

    /** Sends a state event. Callback gets the event id. */
    public void sendStateEvent(String roomId, String eventType, String stateKey, JsonObject content,
                               MatrixCallback<String> callback) {
        execute(auth -> matrixApi.sendStateEvent(auth, roomId, eventType, stateKey, content),
                new MatrixCallback<JsonObject>() {
                    @Override
                    public void onSuccess(JsonObject r) {
                        callback.onSuccess(r != null && r.has("event_id") ? r.get("event_id").getAsString() : "");
                    }

                    @Override
                    public void onError(String error) {
                        callback.onError(error);
                    }
                });
    }

    private interface SendTask {
        void run(Runnable done);
    }

    private void enqueueSend(SendTask task) {
        synchronized (sendQueue) {
            sendQueue.add(() -> task.run(this::sendDone));
        }
        runNextSend();
    }

    private void sendDone() {
        synchronized (sendQueue) {
            sending = false;
        }
        runNextSend();
    }

    private void runNextSend() {
        Runnable next;
        synchronized (sendQueue) {
            if (sending) return;
            next = sendQueue.poll();
            if (next == null) return;
            sending = true;
        }
        next.run();
    }

    /** Stage 3 helper: overlay state event (org.teledentistry.teeth_overlay / active_session). */
    public void sendTeethOverlayState(String roomId, boolean isVisible, String practitionerName,
                                      Map<String, String> toothConditions, String selectedTooth,
                                      MatrixCallback<Boolean> callback) {
        if (roomId == null || roomId.isEmpty()) {
            if (callback != null) postError(callback, "No Matrix room");
            return;
        }
        TeethOverlayEvent event = new TeethOverlayEvent(isVisible, practitionerName, toothConditions, selectedTooth);
        JsonObject content = new Gson().toJsonTree(event).getAsJsonObject();
        sendStateEvent(roomId, "org.teledentistry.teeth_overlay", "active_session", content,
                new MatrixCallback<String>() {
                    @Override
                    public void onSuccess(String id) {
                        if (callback != null) callback.onSuccess(true);
                    }

                    @Override
                    public void onError(String error) {
                        if (callback != null) callback.onError(error);
                    }
                });
    }

    // ------------------------------------------------------------------ history

    /** Fetches the latest events of a room, returned oldest first. */
    /**
     * Latest events of the given types (wildcards like "m.call.*" allowed), oldest first.
     * Filtering matters: one video call adds dozens of m.call.* events, which would otherwise
     * push chat messages out of the window (and vice versa).
     */
    public void getRecentMessages(String roomId, int limit, List<String> types,
                                  MatrixCallback<List<MatrixSyncResponse.Event>> callback) {
        JsonObject filter = new JsonObject();
        JsonArray typeArray = new JsonArray();
        for (String t : types) typeArray.add(t);
        filter.add("types", typeArray);
        String filterJson = filter.toString();

        execute(auth -> matrixApi.messages(auth, roomId, "b", limit, filterJson),
                new MatrixCallback<MatrixMessagesResponse>() {
                    @Override
                    public void onSuccess(MatrixMessagesResponse r) {
                        List<MatrixSyncResponse.Event> chunk = new ArrayList<>();
                        if (r != null && r.getChunk() != null) chunk.addAll(r.getChunk());
                        Collections.reverse(chunk);
                        callback.onSuccess(chunk);
                    }

                    @Override
                    public void onError(String error) {
                        callback.onError(error);
                    }
                });
    }

    // --------------------------------------------------------------------- sync

    /** Handle to a running sync loop; call stop() (e.g. in onPause/onDestroy). */
    public class SyncHandle {
        private final String roomId;
        private final SyncListener listener;
        private final Holder holder = new Holder();
        private String since = null;
        private int failures = 0;
        private volatile boolean stopped = false;

        private SyncHandle(String roomId, SyncListener listener) {
            this.roomId = roomId;
            this.listener = listener;
        }

        public void stop() {
            stopped = true;
            holder.cancelled = true;
            Call<?> c = holder.call;
            if (c != null) c.cancel();
            main.removeCallbacksAndMessages(this);
        }

        public boolean isStopped() {
            return stopped;
        }

        private String filter(boolean initial) {
            JsonObject timeline = new JsonObject();
            timeline.addProperty("limit", initial ? 1 : 50);
            JsonObject room = new JsonObject();
            JsonArray rooms = new JsonArray();
            rooms.add(roomId);
            room.add("rooms", rooms);
            room.add("timeline", timeline);
            JsonObject none = new JsonObject();
            JsonArray star = new JsonArray();
            star.add("*");
            none.add("not_types", star);
            room.add("ephemeral", none);
            room.add("account_data", none);
            JsonObject filter = new JsonObject();
            filter.add("room", room);
            filter.add("presence", none);
            filter.add("account_data", none);
            return filter.toString();
        }

        private void poll() {
            if (stopped) return;
            final boolean initial = since == null;
            final String sinceNow = since;
            holder.cancelled = false;
            execute(auth -> matrixApi.sync(auth, sinceNow, initial ? 0 : SYNC_TIMEOUT_MS, filter(initial), "offline"),
                    new MatrixCallback<MatrixSyncResponse>() {
                        @Override
                        public void onSuccess(MatrixSyncResponse r) {
                            if (stopped) return;
                            failures = 0;
                            if (r != null) {
                                since = r.getNextBatch();
                                dispatch(r, initial);
                            }
                            poll();
                        }

                        @Override
                        public void onError(String error) {
                            if (stopped) return;
                            failures++;
                            Log.w(TAG, "sync failed (" + failures + "): " + error);
                            if (error != null && error.contains("HTTP 400")) since = null; // bad token, resync
                            listener.onSyncError(error);
                            long delay = Math.min(15000L, 1000L << Math.min(failures - 1, 4));
                            main.postAtTime(SyncHandle.this::poll, SyncHandle.this, android.os.SystemClock.uptimeMillis() + delay);
                        }
                    }, false, holder);
        }

        private void dispatch(MatrixSyncResponse r, boolean initial) {
            if (r.getRooms() == null || r.getRooms().getJoin() == null) {
                if (initial) listener.onEvents(roomId, new ArrayList<>(), new ArrayList<>(), true);
                return;
            }
            MatrixSyncResponse.JoinedRoom jr = r.getRooms().getJoin().get(roomId);
            List<MatrixSyncResponse.Event> state = new ArrayList<>();
            List<MatrixSyncResponse.Event> timeline = new ArrayList<>();
            if (jr != null) {
                if (jr.getState() != null && jr.getState().getEvents() != null) state.addAll(jr.getState().getEvents());
                if (jr.getTimeline() != null && jr.getTimeline().getEvents() != null) {
                    timeline.addAll(jr.getTimeline().getEvents());
                }
            }
            if (initial || !state.isEmpty() || !timeline.isEmpty()) {
                listener.onEvents(roomId, state, timeline, initial);
            }
        }
    }

    /**
     * Starts a long-poll sync loop limited to one room. The first sync returns the state snapshot
     * (and at most the latest event); later syncs deliver new events of any type.
     */
    public SyncHandle startSync(String roomId, SyncListener listener) {
        SyncHandle h = new SyncHandle(roomId, listener);
        h.poll();
        return h;
    }
}
