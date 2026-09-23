package com.example.teledentistry_project.data.remote;

import com.example.teledentistry_project.BuildConfig;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SupabaseClient {

    private static final OkHttpClient httpClient =
            new OkHttpClient.Builder()
                    .addInterceptor(new SupabaseAuthInterceptor())
                    .build();

    private static final Retrofit retrofit =
            new Retrofit.Builder()
                    .baseUrl(BuildConfig.SUPABASE_URL + "/")
                    .client(httpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

    public static final SupabaseApi api =
            retrofit.create(SupabaseApi.class);
}