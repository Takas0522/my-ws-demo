package com.example.microservices.point.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ポイント履歴エンティティ
 */
public class PointHistory implements Serializable {
    private Long id;
    private UUID userId;
    private Integer amount;
    private String transactionType; // 'EARN' or 'USE'
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    // Constructors
    public PointHistory() {
    }

    public PointHistory(Long id, UUID userId, Integer amount, String transactionType, 
                       String description, LocalDateTime createdAt, LocalDateTime expiresAt) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.description = description;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
