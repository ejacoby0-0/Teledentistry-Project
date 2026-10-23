package com.example.teledentistry_project.data.remote.matrix;

import com.google.gson.annotations.SerializedName;

public class MatrixLoginRequest {

    @SerializedName("type")
    private String type = "m.login.password";

    @SerializedName("identifier")
    private Identifier identifier;

    @SerializedName("password")
    private String password;

    public MatrixLoginRequest(String username, String password) {
        this.identifier = new Identifier(username);
        this.password = password;
    }

    public static class Identifier {
        @SerializedName("type")
        private String type = "m.id.user";

        @SerializedName("user")
        private String user;

        public Identifier(String user) {
            this.user = user;
        }
    }
}
