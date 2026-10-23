package com.example.teledentistry_project.data.remote.matrix;

import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MatrixApi {

    @POST("_matrix/client/v3/login")
    Call<MatrixAuthResponse> login(@Body MatrixLoginRequest request);

    @GET("_matrix/client/v3/voip/turnServer")
    Call<JsonObject> turnServer(@Header("Authorization") String token);

    @POST("_matrix/client/v3/register")
    Call<MatrixAuthResponse> register(@Body JsonObject request);

    @POST("_matrix/client/v3/createRoom")
    Call<JsonObject> createRoom(
            @Header("Authorization") String token,
            @Body MatrixCreateRoomRequest request
    );

    @GET("_matrix/client/v3/sync")
    Call<MatrixSyncResponse> sync(
            @Header("Authorization") String token,
            @Query("since") String since,
            @Query("timeout") long timeout,
            @Query("filter") String filter,
            @Query("set_presence") String setPresence
    );

    @GET("_matrix/client/v3/rooms/{roomId}/messages")
    Call<MatrixMessagesResponse> messages(
            @Header("Authorization") String token,
            @Path("roomId") String roomId,
            @Query("dir") String dir,
            @Query("limit") int limit,
            @Query("filter") String filter
    );

    @PUT("_matrix/client/v3/rooms/{roomId}/send/{eventType}/{txnId}")
    Call<JsonObject> sendEvent(
            @Header("Authorization") String token,
            @Path("roomId") String roomId,
            @Path("eventType") String eventType,
            @Path("txnId") String txnId,
            @Body JsonObject content
    );

    @PUT("_matrix/client/v3/rooms/{roomId}/state/{eventType}/{stateKey}")
    Call<JsonObject> sendStateEvent(
            @Header("Authorization") String token,
            @Path("roomId") String roomId,
            @Path("eventType") String eventType,
            @Path("stateKey") String stateKey,
            @Body JsonObject stateContent
    );
}
