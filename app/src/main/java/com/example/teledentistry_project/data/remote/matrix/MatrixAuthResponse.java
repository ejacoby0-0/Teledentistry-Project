package com.example.teledentistry_project.data.remote.matrix;

import com.google.gson.annotations.SerializedName;

public class MatrixAuthResponse {

    @SerializedName("user_id")
    private String userId;

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("home_server")
    private String homeServer;

    @SerializedName("device_id")
    private String deviceId;

    public String getUserId() {
        return userId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getHomeServer() {
        return homeServer;
    }

    public String getDeviceId() {
        return deviceId;
    }
}
