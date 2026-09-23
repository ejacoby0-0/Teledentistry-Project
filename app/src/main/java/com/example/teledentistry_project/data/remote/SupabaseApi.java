package com.example.teledentistry_project.data.remote;

import com.example.teledentistry_project.data.model.auth.AuthResponse;
import com.example.teledentistry_project.data.model.auth.SignInRequest;
import com.example.teledentistry_project.data.model.auth.SignUpRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface SupabaseApi {

    @GET("auth/v1/health")
    Call<String> checkAuthHealth();

    @POST("auth/v1/signup")
    Call<AuthResponse> signUp(
            @Body SignUpRequest request
    );

    @POST("auth/v1/token?grant_type=password")
    Call<AuthResponse> signIn(
            @Body SignInRequest request
    );
}