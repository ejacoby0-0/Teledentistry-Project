package com.example.teledentistry_project.data.repository;

import com.example.teledentistry_project.data.remote.SupabaseClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    public void checkSupabaseConnection(
            ConnectionCallback callback
    ) {

        SupabaseClient.api
                .checkAuthHealth()
                .enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(
                            Call<String> call,
                            Response<String> response
                    ) {

                        if (response.isSuccessful()) {

                            callback.onSuccess(
                                    response.body()
                            );

                        } else {

                            callback.onError(
                                    "Supabase returned HTTP "
                                            + response.code()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<String> call,
                            Throwable t
                    ) {

                        callback.onError(
                                t.getMessage()
                        );
                    }
                });
    }

    public interface ConnectionCallback {

        void onSuccess(String response);

        void onError(String error);
    }
}