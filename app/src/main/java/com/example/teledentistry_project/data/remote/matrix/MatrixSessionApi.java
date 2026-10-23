package com.example.teledentistry_project.data.remote.matrix;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

/** PocketBase endpoints (pb_hooks/matrix.pb.js) that hand out Matrix sessions and appointment rooms. */
public interface MatrixSessionApi {

    @POST("api/matrix/session")
    Call<MatrixAuthResponse> createSession(
            @Header("Authorization") String pocketBaseToken,
            @Body JsonObject body
    );

    @POST("api/matrix/appointment-room/{appointmentId}")
    Call<JsonObject> appointmentRoom(
            @Header("Authorization") String pocketBaseToken,
            @Path("appointmentId") String appointmentId
    );
}
