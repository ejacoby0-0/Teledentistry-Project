package com.example.teledentistry_project.data.remote;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PocketBaseClient {

    private static final String BASE_URL =
            "http://127.0.0.1:8090/";

    private static final Retrofit retrofit =
            new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

    public static final PocketBaseApi api =
            retrofit.create(PocketBaseApi.class);
}
