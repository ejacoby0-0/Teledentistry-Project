package com.example.teledentistry_project.data.remote;

import com.example.teledentistry_project.BuildConfig;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class SupabaseAuthInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {

        Request originalRequest = chain.request();

        Request request = originalRequest.newBuilder()
                .addHeader(
                        "apikey",
                        BuildConfig.SUPABASE_PUBLISHABLE_KEY
                )
                .addHeader(
                        "Content-Type",
                        "application/json"
                )
                .build();

        return chain.proceed(request);
    }
}