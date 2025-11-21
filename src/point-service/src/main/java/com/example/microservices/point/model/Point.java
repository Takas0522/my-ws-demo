package com.example.microservices.point.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ポイント残高エンティティ
 */
public class Point implements Serializable {
    private UUID userId;
    private Integer balance;
    private LocalDateTime lastUpdated;

    // Constructors
    public Point() {
    }

    public Point(UUID userId, Integer balance, LocalDateTime lastUpdated) {
        this.userId = userId;
        this.balance = balance;
        this.lastUpdated = lastUpdated;
    }

    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Integer getBalance() {
        return balance;
    }

    public void setBalance(Integer balance) {
        this.balance = balance;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
