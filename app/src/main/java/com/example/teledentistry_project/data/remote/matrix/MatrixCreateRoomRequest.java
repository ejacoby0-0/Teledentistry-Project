package com.example.teledentistry_project.data.remote.matrix;

import com.google.gson.annotations.SerializedName;

public class MatrixCreateRoomRequest {

    @SerializedName("preset")
    private String preset = "private_chat";

    @SerializedName("name")
    private String name;

    @SerializedName("topic")
    private String topic;

    @SerializedName("visibility")
    private String visibility = "private";

    public MatrixCreateRoomRequest(String name, String topic) {
        this.name = name;
        this.topic = topic;
    }
}
