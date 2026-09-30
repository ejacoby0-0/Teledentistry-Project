package com.example.teledentistry_project.data.remote;

import com.example.teledentistry_project.data.model.auth.PocketBaseAuthResponse;
import com.example.teledentistry_project.data.model.auth.PocketBaseLoginRequest;
import com.example.teledentistry_project.data.model.auth.PocketBaseRegisterRequest;
import com.example.teledentistry_project.data.model.auth.PocketBaseUser;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Header;

public interface PocketBaseApi {

    @POST("api/collections/users/records")
    Call<PocketBaseUser> register(
            @Body PocketBaseRegisterRequest request
    );

    @POST("api/collections/users/auth-with-password")
    Call<PocketBaseAuthResponse> login(
            @Body PocketBaseLoginRequest request
    );

    //Verifies he current authentication state and returns a new auth
    //response/token when successful.
    @POST("api/collections/users/auth-refresh")
    Call<PocketBaseAuthResponse> refreshAuth(
            @Header("Authorization") String authToken
    );
}
