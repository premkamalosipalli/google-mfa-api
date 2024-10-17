package com.okta.app.model;

public class TOTPRequest {
    private String username;
    private String totp;  // This should be the code from Google Authenticator

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTotp() {
        return totp;
    }

    public void setTotp(String totp) {
        this.totp = totp;
    }
}
