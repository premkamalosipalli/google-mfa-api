package com.okta.app.model;


import jakarta.persistence.*;

import java.util.ArrayList;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String secretKey;
    private boolean isMfaEnabled;

    public User(String username, String password, String secretKey) {
        this.username = username;
        this.password = password;
        this.secretKey = secretKey;
    }

    public User() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public boolean isMfaEnabled() {
        return isMfaEnabled;
    }

    public void setMfaEnabled(boolean mfaEnabled) {
        isMfaEnabled = mfaEnabled;
    }
}
