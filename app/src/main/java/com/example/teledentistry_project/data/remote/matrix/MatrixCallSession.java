package com.example.teledentistry_project.data.remote.matrix;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStreamTrack;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpTransceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * One 1:1 video call in an appointment room, signalled with Matrix VoIP v1 events
 * (m.call.invite / answer / candidates / select_answer / hangup / reject) and carried by WebRTC.
 *
 * Roles / glare: whoever starts the session first sends the invite. If a fresh, unanswered,
 * not-hung-up invite from the other participant is found in the room history (or arrives while we
 * ring) we answer it instead. If both invite simultaneously, the participant with the
 * lexicographically smaller Matrix user id stays caller and the other one withdraws and answers.
 *
 * Threading: everything runs on the main thread (WebRTC callbacks are re-posted).
 * Lifecycle: attachRenderers() -> start() -> onRoomEvents() for live events -> hangup()/release().
 */
public class MatrixCallSession {

    private static final String TAG = "MatrixCallSession";

    /**
     * Only use TURN relay candidates. The dev setup reaches the PC through "adb reverse" (TCP only)
     * so host/srflx candidates of two devices can never connect directly; media must go through
     * coturn over TCP. Set to false on a real network to allow direct (host/srflx) connections.
     */
    public static final boolean FORCE_RELAY = true;

    private static final long INVITE_LIFETIME_MS = 60000;
    private static final long CANDIDATE_BATCH_MS = 150;
    private static final String VERSION = "1";
    private static final int CAPTURE_WIDTH = 640;
    private static final int CAPTURE_HEIGHT = 480;
    private static final int CAPTURE_FPS = 24;

    public enum Status { PREPARING, RINGING, CONNECTING, CONNECTED, ENDED, FAILED }

    public interface Listener {
        void onStatus(Status status, String detail);
        void onRemoteVideoAvailable(boolean available);
    }

    private enum Phase { IDLE, OUTGOING, INCOMING, ACTIVE, ENDED }

    private static boolean factoryInitialised = false;

    private final Context appContext;
    private final MatrixClient matrix;
    private final String roomId;
    private final Listener listener;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final String partyId = "td" + UUID.randomUUID().toString().substring(0, 8);

    private EglBase eglBase;
    private PeerConnectionFactory factory;
    private SurfaceTextureHelper textureHelper;
    private CameraVideoCapturer capturer;
    private VideoSource videoSource;
    private VideoTrack localVideoTrack;
    private AudioSource audioSource;
    private AudioTrack audioTrack;
    private SurfaceViewRenderer localRenderer;
    private SurfaceViewRenderer remoteRenderer;
    private VideoTrack remoteVideoTrack;
    private boolean capturing = false;
    private boolean cameraEnabled = true;
    private boolean micEnabled = true;

    private PeerConnection pc;
    private List<PeerConnection.IceServer> iceServers = new ArrayList<>();
    private Phase phase = Phase.IDLE;
    private String callId = "";
    private String remotePartyId = "";
    private long inviteSentAt = 0;
    private boolean remoteDescriptionSet = false;
    private final List<IceCandidate> pendingRemoteCandidates = new ArrayList<>();
    private final List<IceCandidate> outgoingCandidates = new ArrayList<>();
    private boolean candidateFlushScheduled = false;

    private boolean ready = false; // history processed
    private final List<MatrixSyncResponse.Event> bufferedLive = new ArrayList<>();
    private final Set<String> seenEvents = new HashSet<>();

    private boolean started = false;
    private boolean released = false;
    private boolean hungUpSent = false;
    private int previousAudioMode = AudioManager.MODE_NORMAL;
    private boolean previousSpeaker = false;
    private boolean audioConfigured = false;

    public MatrixCallSession(Context context, String roomId, Listener listener) {
        this.appContext = context.getApplicationContext();
        this.matrix = MatrixClient.getInstance();
        this.roomId = roomId;
        this.listener = listener;
    }

    // ------------------------------------------------------------------ setup

    /** Initialises the renderers (must be called before start()). */
    public void attachRenderers(SurfaceViewRenderer local, SurfaceViewRenderer remote) {
        eglBase = EglBase.create();
        localRenderer = local;
        remoteRenderer = remote;
        local.init(eglBase.getEglBaseContext(), null);
        local.setMirror(true);
        local.setEnableHardwareScaler(true);
        local.setZOrderMediaOverlay(true);
        remote.init(eglBase.getEglBaseContext(), null);
        remote.setEnableHardwareScaler(true);
        remote.setScalingType(org.webrtc.RendererCommon.ScalingType.SCALE_ASPECT_FILL);
    }

    /**
     * Opens camera/mic (preview shows immediately), fetches TURN credentials, looks at the room
     * history and then either answers a pending invite or sends one.
     */
    public void start() {
        if (started || released) return;
        started = true;
        status(Status.PREPARING, "Connecting…");
        try {
            initFactoryAndMedia();
        } catch (Exception e) {
            Log.e(TAG, "media init failed", e);
            fail("Could not start camera: " + e.getMessage());
            return;
        }
        configureAudio();
        matrix.getTurnServers(new MatrixClient.MatrixCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject r) {
                if (released) return;
                parseIceServers(r);
                loadHistory();
            }

            @Override
            public void onError(String error) {
                if (released) return;
                Log.w(TAG, "TURN fetch failed: " + error);
                if (FORCE_RELAY) {
                    fail("Could not get TURN credentials: " + error);
                } else {
                    iceServers.add(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer());
                    loadHistory();
                }
            }
        });
    }

    private void initFactoryAndMedia() {
        if (!factoryInitialised) {
            PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions
                    .builder(appContext).createInitializationOptions());
            factoryInitialised = true;
        }
        if (eglBase == null) eglBase = EglBase.create();
        factory = PeerConnectionFactory.builder()
                .setVideoEncoderFactory(new DefaultVideoEncoderFactory(eglBase.getEglBaseContext(), true, true))
                .setVideoDecoderFactory(new DefaultVideoDecoderFactory(eglBase.getEglBaseContext()))
                .createPeerConnectionFactory();

        audioSource = factory.createAudioSource(new MediaConstraints());
        audioTrack = factory.createAudioTrack("td_audio", audioSource);
        audioTrack.setEnabled(micEnabled);

        capturer = createCapturer();
        if (capturer == null) {
            Log.w(TAG, "No camera available, continuing audio-only");
        } else {
            textureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.getEglBaseContext());
            videoSource = factory.createVideoSource(capturer.isScreencast());
            capturer.initialize(textureHelper, appContext, videoSource.getCapturerObserver());
            localVideoTrack = factory.createVideoTrack("td_video", videoSource);
            localVideoTrack.setEnabled(cameraEnabled);
            if (localRenderer != null) localVideoTrack.addSink(localRenderer);
            if (cameraEnabled) startCapture();
        }
    }

    private CameraVideoCapturer createCapturer() {
        CameraEnumerator enumerator = Camera2Enumerator.isSupported(appContext)
                ? new Camera2Enumerator(appContext) : new Camera1Enumerator(false);
        CameraVideoCapturer c = tryCreate(enumerator, true);
        if (c == null && enumerator instanceof Camera2Enumerator) {
            c = tryCreate(new Camera1Enumerator(false), true);
        }
        return c;
    }

    private CameraVideoCapturer tryCreate(CameraEnumerator enumerator, boolean preferFront) {
        String[] names = enumerator.getDeviceNames();
        for (String n : names) {
            if (enumerator.isFrontFacing(n) == preferFront) {
                CameraVideoCapturer c = enumerator.createCapturer(n, null);
                if (c != null) return c;
            }
        }
        for (String n : names) {
            CameraVideoCapturer c = enumerator.createCapturer(n, null);
            if (c != null) return c;
        }
        return null;
    }

    private void startCapture() {
        if (capturer != null && !capturing) {
            capturer.startCapture(CAPTURE_WIDTH, CAPTURE_HEIGHT, CAPTURE_FPS);
            capturing = true;
        }
    }

    private void stopCapture() {
        if (capturer != null && capturing) {
            try {
                capturer.stopCapture();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            capturing = false;
        }
    }

    private void configureAudio() {
        try {
            AudioManager am = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
            if (am == null) return;
            previousAudioMode = am.getMode();
            previousSpeaker = am.isSpeakerphoneOn();
            am.setMode(AudioManager.MODE_IN_COMMUNICATION);
            am.setSpeakerphoneOn(true);
            audioConfigured = true;
        } catch (Exception e) {
            Log.w(TAG, "audio config failed", e);
        }
    }

    private void restoreAudio() {
        if (!audioConfigured) return;
        audioConfigured = false;
        try {
            AudioManager am = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
            if (am != null) {
                am.setSpeakerphoneOn(previousSpeaker);
                am.setMode(previousAudioMode);
            }
        } catch (Exception ignored) {}
    }

    private void parseIceServers(JsonObject r) {
        iceServers = new ArrayList<>();
        if (r == null || !r.has("uris")) return;
        String user = r.has("username") ? r.get("username").getAsString() : "";
        String pass = r.has("password") ? r.get("password").getAsString() : "";
        for (JsonElement e : r.getAsJsonArray("uris")) {
            iceServers.add(PeerConnection.IceServer.builder(e.getAsString())
                    .setUsername(user).setPassword(pass).createIceServer());
        }
    }

    // ------------------------------------------------------------ controls

    public void setMicEnabled(boolean enabled) {
        micEnabled = enabled;
        if (audioTrack != null) audioTrack.setEnabled(enabled);
    }

    /** Turns the camera off (capture stopped, camera released) or back on. */
    public void setCameraEnabled(boolean enabled) {
        cameraEnabled = enabled;
        if (localVideoTrack != null) localVideoTrack.setEnabled(enabled);
        if (enabled) startCapture();
        else stopCapture();
    }

    /** Sends m.call.hangup if a call is in progress, and ends the session. */
    public void hangup() {
        if (phase == Phase.OUTGOING || phase == Phase.INCOMING || phase == Phase.ACTIVE) {
            sendHangup("user_hangup");
        }
        endCall(Status.ENDED, "Call ended");
    }

    // ---------------------------------------------------------- history/decide

    private void loadHistory() {
        matrix.getRecentMessages(roomId, 60, Collections.singletonList("m.call.*"), new MatrixClient.MatrixCallback<List<MatrixSyncResponse.Event>>() {
            @Override
            public void onSuccess(List<MatrixSyncResponse.Event> events) {
                if (released) return;
                decide(events);
            }

            @Override
            public void onError(String error) {
                if (released) return;
                Log.w(TAG, "history failed: " + error);
                decide(new ArrayList<>());
            }
        });
    }

    private static class CallInfo {
        MatrixSyncResponse.Event invite;
        boolean answered;
        boolean ended;
        final List<MatrixSyncResponse.Event> others = new ArrayList<>();
    }

    private void decide(List<MatrixSyncResponse.Event> history) {
        String me = matrix.getUserId();
        Map<String, CallInfo> calls = new LinkedHashMap<>();
        for (MatrixSyncResponse.Event ev : history) {
            if (ev.getType() == null || !ev.getType().startsWith("m.call.") || ev.getContent() == null) continue;
            String cid = str(ev.getContent(), "call_id");
            if (cid.isEmpty()) continue;
            CallInfo ci = calls.get(cid);
            if (ci == null) {
                ci = new CallInfo();
                calls.put(cid, ci);
            }
            switch (ev.getType()) {
                case "m.call.invite":
                    ci.invite = ev;
                    break;
                case "m.call.answer":
                    ci.answered = true;
                    ci.others.add(ev);
                    break;
                case "m.call.hangup":
                case "m.call.reject":
                    ci.ended = true;
                    break;
                default:
                    ci.others.add(ev);
            }
            if (ev.getEventId() != null) seenEvents.add(ev.getEventId());
        }

        MatrixSyncResponse.Event chosen = null;
        CallInfo chosenInfo = null;
        for (CallInfo ci : calls.values()) {
            if (ci.invite == null || ci.answered || ci.ended) continue;
            if (me.equals(ci.invite.getSender())) continue;
            if (isStale(ci.invite)) continue;
            chosen = ci.invite;
            chosenInfo = ci;
        }

        ready = true;
        if (chosen != null) {
            Log.i(TAG, "answering existing invite " + str(chosen.getContent(), "call_id"));
            acceptInvite(chosen);
            for (MatrixSyncResponse.Event o : chosenInfo.others) handleCallEvent(o);
        } else {
            placeCall();
        }
        List<MatrixSyncResponse.Event> buffered = new ArrayList<>(bufferedLive);
        bufferedLive.clear();
        for (MatrixSyncResponse.Event ev : buffered) handleCallEvent(ev);
    }

    private boolean isStale(MatrixSyncResponse.Event ev) {
        long lifetime = INVITE_LIFETIME_MS;
        JsonObject c = ev.getContent();
        if (c != null && c.has("lifetime") && c.get("lifetime").isJsonPrimitive()) {
            lifetime = c.get("lifetime").getAsLong();
        }
        long age = -1;
        JsonObject u = ev.getUnsigned();
        if (u != null && u.has("age") && u.get("age").isJsonPrimitive()) age = u.get("age").getAsLong();
        // "age" is server-relative so it is robust against a wrong device clock
        if (age < 0) age = System.currentTimeMillis() - ev.getOriginServerTs();
        return age > lifetime;
    }

    // ------------------------------------------------------------- live events

    /** Feed events from the room sync loop (timeline events; non-call events are ignored). */
    public void onRoomEvents(List<MatrixSyncResponse.Event> timeline) {
        if (released || timeline == null) return;
        for (MatrixSyncResponse.Event ev : timeline) {
            if (ev.getType() == null || !ev.getType().startsWith("m.call.")) continue;
            if (!started) continue;
            if (!ready) {
                bufferedLive.add(ev);
            } else {
                handleCallEvent(ev);
            }
        }
    }

    private void handleCallEvent(MatrixSyncResponse.Event ev) {
        if (released || ev.getContent() == null || ev.getType() == null) return;
        if (matrix.getUserId().equals(ev.getSender())) return; // own echo
        String id = ev.getEventId();
        String cid = str(ev.getContent(), "call_id");
        switch (ev.getType()) {
            case "m.call.invite":
                if (id != null && !seenEvents.add(id)) return;
                onInvite(ev);
                break;
            case "m.call.answer":
                if (cid.equals(callId)) onAnswer(ev);
                break;
            case "m.call.candidates":
                if (cid.equals(callId)) onRemoteCandidates(ev);
                break;
            case "m.call.hangup":
            case "m.call.reject":
                if (cid.equals(callId) && (phase == Phase.OUTGOING || phase == Phase.INCOMING || phase == Phase.ACTIVE)) {
                    boolean reject = "m.call.reject".equals(ev.getType());
                    endCall(Status.ENDED, reject ? "Call declined" : "The other participant left the call");
                }
                break;
            default:
                break;
        }
    }

    private void onInvite(MatrixSyncResponse.Event ev) {
        String cid = str(ev.getContent(), "call_id");
        if (cid.equals(callId) || isStale(ev)) return;
        switch (phase) {
            case OUTGOING: {
                boolean ownExpired = System.currentTimeMillis() - inviteSentAt > INVITE_LIFETIME_MS;
                boolean iYield = ownExpired || matrix.getUserId().compareTo(ev.getSender()) > 0;
                if (!iYield) return; // we stay caller, the other side will yield to us
                Log.i(TAG, "glare: withdrawing own invite, answering " + cid);
                sendHangup("invite_timeout");
                closePeerConnection();
                acceptInvite(ev);
                break;
            }
            case IDLE:
                acceptInvite(ev);
                break;
            case ACTIVE:
            case INCOMING: {
                JsonObject c = base(cid);
                c.addProperty("reason", "user_busy");
                matrix.sendEvent(roomId, "m.call.reject", MatrixClient.newTxnId(), c, ignoreResult());
                break;
            }
            default:
                break;
        }
    }

    // --------------------------------------------------------------- caller

    private void placeCall() {
        phase = Phase.OUTGOING;
        inviteSentAt = 0;
        callId = "c" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6);
        remotePartyId = "";
        status(Status.RINGING, "Ringing…");
        createPeerConnection();
        if (pc == null) return;
        pc.createOffer(new SimpleSdp() {
            @Override
            public void onCreateSuccess(SessionDescription sdp) {
                main.post(() -> {
                    if (released || pc == null || phase != Phase.OUTGOING) return;
                    pc.setLocalDescription(new SimpleSdp() {
                        @Override
                        public void onSetSuccess() {
                            main.post(() -> sendInvite(sdp));
                        }

                        @Override
                        public void onSetFailure(String s) {
                            main.post(() -> fail("Could not set local offer: " + s));
                        }
                    }, sdp);
                });
            }

            @Override
            public void onCreateFailure(String s) {
                main.post(() -> fail("Could not create offer: " + s));
            }
        }, new MediaConstraints());
    }

    private void sendInvite(SessionDescription sdp) {
        if (released || phase != Phase.OUTGOING) return;
        JsonObject c = base(callId);
        c.addProperty("lifetime", INVITE_LIFETIME_MS);
        JsonObject offer = new JsonObject();
        offer.addProperty("type", "offer");
        offer.addProperty("sdp", sdp.description);
        c.add("offer", offer);
        inviteSentAt = System.currentTimeMillis();
        matrix.sendEvent(roomId, "m.call.invite", MatrixClient.newTxnId(), c, new MatrixClient.MatrixCallback<String>() {
            @Override
            public void onSuccess(String id) {
                if (id != null) seenEvents.add(id);
            }

            @Override
            public void onError(String error) {
                main.post(() -> fail("Could not send call invite: " + error));
            }
        });
        flushCandidates(); // candidates gathered so far (queue keeps them after the invite)
    }

    private void onAnswer(MatrixSyncResponse.Event ev) {
        if (phase != Phase.OUTGOING || remoteDescriptionSet || pc == null) return;
        JsonObject ans = ev.getContent().has("answer") && ev.getContent().get("answer").isJsonObject()
                ? ev.getContent().getAsJsonObject("answer") : null;
        if (ans == null) return;
        remotePartyId = str(ev.getContent(), "party_id");
        phase = Phase.ACTIVE;
        status(Status.CONNECTING, "Connecting…");
        SessionDescription sdp = new SessionDescription(SessionDescription.Type.ANSWER, str(ans, "sdp"));
        pc.setRemoteDescription(new SimpleSdp() {
            @Override
            public void onSetSuccess() {
                main.post(() -> {
                    remoteDescriptionSet = true;
                    drainRemoteCandidates();
                });
            }

            @Override
            public void onSetFailure(String s) {
                main.post(() -> fail("Could not apply answer: " + s));
            }
        }, sdp);

        JsonObject sel = base(callId);
        sel.addProperty("selected_party_id", remotePartyId);
        matrix.sendEvent(roomId, "m.call.select_answer", MatrixClient.newTxnId(), sel, ignoreResult());
    }

    // --------------------------------------------------------------- callee

    private void acceptInvite(MatrixSyncResponse.Event ev) {
        JsonObject c = ev.getContent();
        JsonObject offer = c.has("offer") && c.get("offer").isJsonObject() ? c.getAsJsonObject("offer") : null;
        if (offer == null) {
            Log.w(TAG, "invite without offer");
            if (phase == Phase.IDLE) placeCall();
            return;
        }
        phase = Phase.INCOMING;
        callId = str(c, "call_id");
        remotePartyId = str(c, "party_id");
        status(Status.CONNECTING, "Connecting…");
        createPeerConnection();
        if (pc == null) return;
        SessionDescription remote = new SessionDescription(SessionDescription.Type.OFFER, str(offer, "sdp"));
        pc.setRemoteDescription(new SimpleSdp() {
            @Override
            public void onSetSuccess() {
                main.post(() -> {
                    if (released || pc == null) return;
                    remoteDescriptionSet = true;
                    drainRemoteCandidates();
                    pc.createAnswer(new SimpleSdp() {
                        @Override
                        public void onCreateSuccess(SessionDescription sdp) {
                            main.post(() -> {
                                if (released || pc == null) return;
                                pc.setLocalDescription(new SimpleSdp() {
                                    @Override
                                    public void onSetSuccess() {
                                        main.post(() -> sendAnswer(sdp));
                                    }

                                    @Override
                                    public void onSetFailure(String s) {
                                        main.post(() -> fail("Could not set local answer: " + s));
                                    }
                                }, sdp);
                            });
                        }

                        @Override
                        public void onCreateFailure(String s) {
                            main.post(() -> fail("Could not create answer: " + s));
                        }
                    }, new MediaConstraints());
                });
            }

            @Override
            public void onSetFailure(String s) {
                main.post(() -> fail("Could not apply offer: " + s));
            }
        }, remote);
    }

    private void sendAnswer(SessionDescription sdp) {
        if (released || phase != Phase.INCOMING) return;
        phase = Phase.ACTIVE;
        JsonObject c = base(callId);
        JsonObject answer = new JsonObject();
        answer.addProperty("type", "answer");
        answer.addProperty("sdp", sdp.description);
        c.add("answer", answer);
        matrix.sendEvent(roomId, "m.call.answer", MatrixClient.newTxnId(), c, new MatrixClient.MatrixCallback<String>() {
            @Override
            public void onSuccess(String id) {
                if (id != null) seenEvents.add(id);
            }

            @Override
            public void onError(String error) {
                main.post(() -> fail("Could not send answer: " + error));
            }
        });
        flushCandidates();
    }

    // ------------------------------------------------------------ peer connection

    private void createPeerConnection() {
        PeerConnection.RTCConfiguration cfg = new PeerConnection.RTCConfiguration(iceServers);
        cfg.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN;
        cfg.iceTransportsType = FORCE_RELAY
                ? PeerConnection.IceTransportsType.RELAY : PeerConnection.IceTransportsType.ALL;
        cfg.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_ONCE;
        remoteDescriptionSet = false;
        pendingRemoteCandidates.clear();
        outgoingCandidates.clear();
        pc = factory.createPeerConnection(cfg, new PeerConnection.Observer() {
            @Override
            public void onIceCandidate(IceCandidate c) {
                main.post(() -> queueLocalCandidate(c));
            }

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState s) {
                main.post(() -> onIceState(s));
            }

            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState s) {
                if (s == PeerConnection.IceGatheringState.COMPLETE) main.post(() -> sendEndOfCandidates());
            }

            @Override
            public void onTrack(RtpTransceiver t) {
                main.post(() -> {
                    if (t.getReceiver() == null) return;
                    MediaStreamTrack track = t.getReceiver().track();
                    if (track instanceof VideoTrack && remoteRenderer != null) {
                        remoteVideoTrack = (VideoTrack) track;
                        remoteVideoTrack.addSink(remoteRenderer);
                        listener.onRemoteVideoAvailable(true);
                    }
                });
            }

            @Override public void onSignalingChange(PeerConnection.SignalingState s) {}
            @Override public void onIceConnectionReceivingChange(boolean b) {}
            @Override public void onIceCandidatesRemoved(IceCandidate[] c) {}
            @Override public void onAddStream(org.webrtc.MediaStream m) {}
            @Override public void onRemoveStream(org.webrtc.MediaStream m) {}
            @Override public void onDataChannel(org.webrtc.DataChannel d) {}
            @Override public void onRenegotiationNeeded() {}
        });
        if (pc == null) {
            fail("Could not create the call connection");
            return;
        }
        List<String> streamIds = Collections.singletonList("td_stream");
        if (audioTrack != null) pc.addTrack(audioTrack, streamIds);
        if (localVideoTrack != null) pc.addTrack(localVideoTrack, streamIds);
    }

    private void closePeerConnection() {
        if (remoteVideoTrack != null && remoteRenderer != null) {
            try { remoteVideoTrack.removeSink(remoteRenderer); } catch (Exception ignored) {}
        }
        remoteVideoTrack = null;
        if (pc != null) {
            try { pc.close(); pc.dispose(); } catch (Exception e) { Log.w(TAG, "pc close", e); }
            pc = null;
        }
        remoteDescriptionSet = false;
        pendingRemoteCandidates.clear();
        outgoingCandidates.clear();
        callId = "";
        phase = Phase.IDLE;
        listener.onRemoteVideoAvailable(false);
    }

    private void onIceState(PeerConnection.IceConnectionState s) {
        if (released || phase == Phase.ENDED) return;
        Log.i(TAG, "ICE state " + s);
        switch (s) {
            case CONNECTED:
            case COMPLETED:
                status(Status.CONNECTED, "Connected");
                break;
            case DISCONNECTED:
                status(Status.CONNECTING, "Connection interrupted, reconnecting…");
                break;
            case FAILED:
                sendHangup("ice_failed");
                endCall(Status.FAILED, "Connection failed");
                break;
            default:
                break;
        }
    }

    // ----------------------------------------------------------- ICE candidates

    private void queueLocalCandidate(IceCandidate c) {
        if (released || pc == null) return;
        Log.i(TAG, "local candidate: " + c.sdp);
        outgoingCandidates.add(c);
        if (!candidateFlushScheduled) {
            candidateFlushScheduled = true;
            main.postDelayed(this::flushCandidates, CANDIDATE_BATCH_MS);
        }
    }

    private void flushCandidates() {
        candidateFlushScheduled = false;
        if (released || outgoingCandidates.isEmpty() || callId.isEmpty()) return;
        if (phase != Phase.OUTGOING && phase != Phase.ACTIVE && phase != Phase.INCOMING) return;
        // The callee must not trickle before its answer is queued; flushCandidates() is called from
        // sendAnswer()/sendInvite() so the serial send queue keeps the order.
        if (phase == Phase.INCOMING) return;
        if (phase == Phase.OUTGOING && inviteSentAt == 0) return;
        JsonArray arr = new JsonArray();
        for (IceCandidate c : outgoingCandidates) arr.add(candidateJson(c.sdp, c.sdpMid, c.sdpMLineIndex));
        outgoingCandidates.clear();
        JsonObject content = base(callId);
        content.add("candidates", arr);
        matrix.sendEvent(roomId, "m.call.candidates", MatrixClient.newTxnId(), content, ignoreResult());
    }

    private void sendEndOfCandidates() {
        if (released || callId.isEmpty()) return;
        flushCandidates();
        if (phase == Phase.INCOMING || (phase == Phase.OUTGOING && inviteSentAt == 0)) return;
        JsonArray arr = new JsonArray();
        arr.add(candidateJson("", "", 0));
        JsonObject content = base(callId);
        content.add("candidates", arr);
        matrix.sendEvent(roomId, "m.call.candidates", MatrixClient.newTxnId(), content, ignoreResult());
    }

    private static JsonObject candidateJson(String sdp, String mid, int index) {
        JsonObject o = new JsonObject();
        o.addProperty("candidate", sdp);
        o.addProperty("sdpMid", mid == null ? "" : mid);
        o.addProperty("sdpMLineIndex", index);
        return o;
    }

    private void onRemoteCandidates(MatrixSyncResponse.Event ev) {
        JsonObject c = ev.getContent();
        if (!c.has("candidates") || !c.get("candidates").isJsonArray()) return;
        for (JsonElement e : c.getAsJsonArray("candidates")) {
            if (!e.isJsonObject()) continue;
            JsonObject o = e.getAsJsonObject();
            String cand = str(o, "candidate");
            if (cand.isEmpty()) continue; // end-of-candidates
            int idx = o.has("sdpMLineIndex") && o.get("sdpMLineIndex").isJsonPrimitive()
                    ? o.get("sdpMLineIndex").getAsInt() : 0;
            IceCandidate ic = new IceCandidate(str(o, "sdpMid"), idx, cand);
            if (remoteDescriptionSet && pc != null) pc.addIceCandidate(ic);
            else pendingRemoteCandidates.add(ic);
        }
    }

    private void drainRemoteCandidates() {
        if (pc == null) return;
        for (IceCandidate c : pendingRemoteCandidates) pc.addIceCandidate(c);
        pendingRemoteCandidates.clear();
    }

    // ----------------------------------------------------------------- teardown

    private void sendHangup(String reason) {
        if (callId.isEmpty() || hungUpSent && phase == Phase.ENDED) return;
        JsonObject c = base(callId);
        c.addProperty("reason", reason);
        matrix.sendEvent(roomId, "m.call.hangup", MatrixClient.newTxnId(), c, ignoreResult());
    }

    private void endCall(Status status, String detail) {
        if (phase == Phase.ENDED) return;
        phase = Phase.ENDED;
        hungUpSent = true;
        status(status, detail);
        closePeerConnection();
        phase = Phase.ENDED;
        stopCapture(); // camera off as soon as the call is over
    }

    private void fail(String message) {
        Log.w(TAG, message);
        if (phase == Phase.OUTGOING || phase == Phase.INCOMING || phase == Phase.ACTIVE) sendHangup("unknown_error");
        endCall(Status.FAILED, message);
    }

    /** Releases camera, microphone, peer connection, factory and renderers. Idempotent. */
    public void release() {
        if (released) return;
        if (phase == Phase.OUTGOING || phase == Phase.INCOMING || phase == Phase.ACTIVE) {
            sendHangup("user_hangup");
        }
        released = true;
        phase = Phase.ENDED;
        main.removeCallbacksAndMessages(null);
        closePeerConnection();
        stopCapture();
        if (localVideoTrack != null && localRenderer != null) {
            try { localVideoTrack.removeSink(localRenderer); } catch (Exception ignored) {}
        }
        if (capturer != null) {
            try { capturer.dispose(); } catch (Exception ignored) {}
            capturer = null;
        }
        if (localVideoTrack != null) { localVideoTrack.dispose(); localVideoTrack = null; }
        if (videoSource != null) { videoSource.dispose(); videoSource = null; }
        if (textureHelper != null) { textureHelper.dispose(); textureHelper = null; }
        if (audioTrack != null) { audioTrack.dispose(); audioTrack = null; }
        if (audioSource != null) { audioSource.dispose(); audioSource = null; }
        if (factory != null) { factory.dispose(); factory = null; }
        if (localRenderer != null) { localRenderer.release(); localRenderer = null; }
        if (remoteRenderer != null) { remoteRenderer.release(); remoteRenderer = null; }
        if (eglBase != null) { eglBase.release(); eglBase = null; }
        restoreAudio();
    }

    // ------------------------------------------------------------------ helpers

    private void status(Status s, String detail) {
        if (!released) listener.onStatus(s, detail);
    }

    private JsonObject base(String cid) {
        JsonObject o = new JsonObject();
        o.addProperty("call_id", cid);
        o.addProperty("party_id", partyId);
        o.addProperty("version", VERSION);
        return o;
    }

    private static String str(JsonObject o, String key) {
        if (o != null && o.has(key) && o.get(key).isJsonPrimitive()) return o.get(key).getAsString();
        return "";
    }

    private MatrixClient.MatrixCallback<String> ignoreResult() {
        return new MatrixClient.MatrixCallback<String>() {
            @Override
            public void onSuccess(String id) {}

            @Override
            public void onError(String error) {
                Log.w(TAG, "send failed: " + error);
            }
        };
    }

    /** SdpObserver with no-op defaults. */
    private static class SimpleSdp implements SdpObserver {
        @Override public void onCreateSuccess(SessionDescription sdp) {}
        @Override public void onSetSuccess() {}
        @Override public void onCreateFailure(String s) {}
        @Override public void onSetFailure(String s) {}
    }
}
