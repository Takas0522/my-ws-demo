package com.example.microservices.auth.model;

import java.io.Serializable;
import java.util.UUID;

/**
 * ログインリクエストDTO
 */
public class LoginRequest implements Serializable {
    private UUID userId;
    private String username;
    private String password;

    // Constructors
    public LoginRequest() {
    }

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public LoginRequest(UUID userId, String password) {
        this.userId = userId;
        this.password = password;
    }

    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
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
}
