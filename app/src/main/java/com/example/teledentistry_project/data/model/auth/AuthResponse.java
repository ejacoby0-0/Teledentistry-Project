package com.example.teledentistry_project.data.model.auth;

public class AuthResponse {

    private String access_token;
    private String refresh_token;
    private String token_type;
    private Integer expires_in;
    private Integer expires_at;

    private SupabaseUser user;

    public String getAccessToken() {
        return access_token;
    }

    public String getRefreshToken() {
        return refresh_token;
    }

    public String getTokenType() {
        return token_type;
    }

    public Integer getExpiresIn() {
        return expires_in;
    }

    public Integer getExpiresAt() {
        return expires_at;
    }

    public SupabaseUser getUser() {
        return user;
    }
}
