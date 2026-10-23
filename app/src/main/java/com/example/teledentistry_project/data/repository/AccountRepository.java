package com.example.teledentistry_project.data.repository;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AccountRepository {

    private static final String POCKETBASE_URL =
            "http://10.0.2.2:8090/";

    private static final String USERS_COLLECTION = "users";

    private final OkHttpClient httpClient = new OkHttpClient();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface DeleteAccountCallback {
        void onSuccess();

        void onError(String message);
    }

    public void deleteAccount(
            String authToken,
            String userId,
            DeleteAccountCallback callback
    ) {
        if (authToken == null || authToken.trim().isEmpty()) {
            callback.onError("Your session has expired. Please sign in again.");
            return;
        }

        if (userId == null || userId.trim().isEmpty()) {
            callback.onError("User ID is missing.");
            return;
        }

        String url = POCKETBASE_URL
                + "/api/collections/"
                + USERS_COLLECTION
                + "/records/"
                + userId;

        Request request = new Request.Builder()
                .url(url)
                .delete()
                .addHeader("Authorization", authToken)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() ->
                        callback.onError(
                                "Could not connect to the server. Please try again."
                        )
                );
            }

            @Override
            public void onResponse(Call call, Response response)
                    throws IOException {

                try (Response responseToClose = response) {
                    if (responseToClose.isSuccessful()) {
                        mainHandler.post(callback::onSuccess);
                    } else {
                        int statusCode = responseToClose.code();

                        mainHandler.post(() ->
                                callback.onError(
                                        "Account deletion failed (HTTP "
                                                + statusCode
                                                + "). Check your permissions "
                                                + "and related records."
                                )
                        );
                    }
                }
            }
        });
    }
}