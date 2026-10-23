package com.example.teledentistry_project.data.remote;

import com.example.teledentistry_project.BuildConfig;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PocketBaseClient {

    private static String BASE_URL = BuildConfig.POCKETBASE_URL.endsWith("/")
            ? BuildConfig.POCKETBASE_URL
            : BuildConfig.POCKETBASE_URL + "/";

    public static PocketBaseApi api = createApi(BASE_URL);

    public static void setCustomBaseUrl(String customUrl) {
        if (customUrl != null && !customUrl.isEmpty()) {
            if (!customUrl.endsWith("/")) {
                customUrl += "/";
            }
            BASE_URL = customUrl;
            api = createApi(BASE_URL);
        }
    }

    private static PocketBaseApi createApi(String baseUrl) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PocketBaseApi.class);
    }
}
