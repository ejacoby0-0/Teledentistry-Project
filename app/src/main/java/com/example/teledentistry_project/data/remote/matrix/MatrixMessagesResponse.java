package com.example.teledentistry_project.data.remote.matrix;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/** Response of GET /rooms/{roomId}/messages. */
public class MatrixMessagesResponse {

    @SerializedName("start")
    private String start;

    @SerializedName("end")
    private String end;

    @SerializedName("chunk")
    private List<MatrixSyncResponse.Event> chunk;

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    public List<MatrixSyncResponse.Event> getChunk() {
        return chunk;
    }
}
