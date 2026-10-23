package com.example.teledentistry_project.data.remote.matrix;

import com.google.gson.annotations.SerializedName;

public class MatrixMessageRequest {

    @SerializedName("msgtype")
    private String msgType = "m.text";

    @SerializedName("body")
    private String body;

    public MatrixMessageRequest(String body) {
        this.body = body;
    }

    public String getMsgType() {
        return msgType;
    }

    public String getBody() {
        return body;
    }
}
