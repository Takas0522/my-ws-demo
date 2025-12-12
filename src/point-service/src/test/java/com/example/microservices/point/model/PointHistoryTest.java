package com.example.microservices.point.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PointHistory Model Tests")
class PointHistoryTest {

    private Long testId;
    private UUID testUserId;
    private Integer testAmount;
    private String testTransactionType;
    private String testDescription;
    private LocalDateTime testCreatedAt;
    private LocalDateTime testExpiresAt;

    @BeforeEach
    void setUp() {
        testId = 1L;
        testUserId = UUID.randomUUID();
        testAmount = 500;
        testTransactionType = "EARN";
        testDescription = "Purchase reward";
        testCreatedAt = LocalDateTime.now();
        testExpiresAt = LocalDateTime.now().plusYears(1);
    }

    @Test
    @DisplayName("Given default constructor, When creating PointHistory, Then all fields should be null")
    void givenDefaultConstructor_whenCreatingPointHistory_thenAllFieldsShouldBeNull() {
        // When
        PointHistory history = new PointHistory();

        // Then
        assertNull(history.getId());
        assertNull(history.getUserId());
        assertNull(history.getAmount());
        assertNull(history.getTransactionType());
        assertNull(history.getDescription());
        assertNull(history.getCreatedAt());
        assertNull(history.getExpiresAt());
    }

    @Test
    @DisplayName("Given all parameters, When creating PointHistory, Then all fields should be set correctly")
    void givenAllParameters_whenCreatingPointHistory_thenAllFieldsShouldBeSetCorrectly() {
        // When
        PointHistory history = new PointHistory(
            testId, testUserId, testAmount, testTransactionType,
            testDescription, testCreatedAt, testExpiresAt
        );

        // Then
        assertEquals(testId, history.getId());
        assertEquals(testUserId, history.getUserId());
        assertEquals(testAmount, history.getAmount());
        assertEquals(testTransactionType, history.getTransactionType());
        assertEquals(testDescription, history.getDescription());
        assertEquals(testCreatedAt, history.getCreatedAt());
        assertEquals(testExpiresAt, history.getExpiresAt());
    }

    @Test
    @DisplayName("Given a PointHistory, When setting id, Then id should be updated")
    void givenAPointHistory_whenSettingId_thenIdShouldBeUpdated() {
        // Given
        PointHistory history = new PointHistory();
        Long newId = 100L;

        // When
        history.setId(newId);

        // Then
        assertEquals(newId, history.getId());
    }

    @Test
    @DisplayName("Given a PointHistory, When setting userId, Then userId should be updated")
    void givenAPointHistory_whenSettingUserId_thenUserIdShouldBeUpdated() {
        // Given
        PointHistory history = new PointHistory();
        UUID newUserId = UUID.randomUUID();

        // When
        history.setUserId(newUserId);

        // Then
        assertEquals(newUserId, history.getUserId());
    }

    @Test
    @DisplayName("Given a PointHistory, When setting amount, Then amount should be updated")
    void givenAPointHistory_whenSettingAmount_thenAmountShouldBeUpdated() {
        // Given
        PointHistory history = new PointHistory();
        Integer newAmount = 1000;

        // When
        history.setAmount(newAmount);

        // Then
        assertEquals(newAmount, history.getAmount());
    }

    @Test
    @DisplayName("Given a PointHistory, When setting transactionType, Then transactionType should be updated")
    void givenAPointHistory_whenSettingTransactionType_thenTransactionTypeShouldBeUpdated() {
        // Given
        PointHistory history = new PointHistory();
        String newType = "USE";

        // When
        history.setTransactionType(newType);

        // Then
        assertEquals(newType, history.getTransactionType());
    }

    @Test
    @DisplayName("Given a PointHistory, When setting description, Then description should be updated")
    void givenAPointHistory_whenSettingDescription_thenDescriptionShouldBeUpdated() {
        // Given
        PointHistory history = new PointHistory();
        String newDescription = "Redeemed for gift card";

        // When
        history.setDescription(newDescription);

        // Then
        assertEquals(newDescription, history.getDescription());
    }

    @Test
    @DisplayName("Given a PointHistory, When setting createdAt, Then createdAt should be updated")
    void givenAPointHistory_whenSettingCreatedAt_thenCreatedAtShouldBeUpdated() {
        // Given
        PointHistory history = new PointHistory();
        LocalDateTime newCreatedAt = LocalDateTime.now().minusDays(1);

        // When
        history.setCreatedAt(newCreatedAt);

        // Then
        assertEquals(newCreatedAt, history.getCreatedAt());
    }

    @Test
    @DisplayName("Given a PointHistory, When setting expiresAt, Then expiresAt should be updated")
    void givenAPointHistory_whenSettingExpiresAt_thenExpiresAtShouldBeUpdated() {
        // Given
        PointHistory history = new PointHistory();
        LocalDateTime newExpiresAt = LocalDateTime.now().plusYears(2);

        // When
        history.setExpiresAt(newExpiresAt);

        // Then
        assertEquals(newExpiresAt, history.getExpiresAt());
    }

    @Test
    @DisplayName("Given EARN transaction type, When creating PointHistory, Then transaction type should be EARN")
    void givenEarnTransactionType_whenCreatingPointHistory_thenTransactionTypeShouldBeEarn() {
        // Given & When
        PointHistory history = new PointHistory(
            testId, testUserId, testAmount, "EARN",
            testDescription, testCreatedAt, testExpiresAt
        );

        // Then
        assertEquals("EARN", history.getTransactionType());
    }

    @Test
    @DisplayName("Given USE transaction type, When creating PointHistory, Then transaction type should be USE")
    void givenUseTransactionType_whenCreatingPointHistory_thenTransactionTypeShouldBeUse() {
        // Given & When
        PointHistory history = new PointHistory(
            testId, testUserId, testAmount, "USE",
            testDescription, testCreatedAt, testExpiresAt
        );

        // Then
        assertEquals("USE", history.getTransactionType());
    }

    @Test
    @DisplayName("Given null values, When setting fields, Then should accept null values")
    void givenNullValues_whenSettingFields_thenShouldAcceptNullValues() {
        // Given
        PointHistory history = new PointHistory(
            testId, testUserId, testAmount, testTransactionType,
            testDescription, testCreatedAt, testExpiresAt
        );

        // When
        history.setId(null);
        history.setUserId(null);
        history.setAmount(null);
        history.setTransactionType(null);
        history.setDescription(null);
        history.setCreatedAt(null);
        history.setExpiresAt(null);

        // Then
        assertNull(history.getId());
        assertNull(history.getUserId());
        assertNull(history.getAmount());
        assertNull(history.getTransactionType());
        assertNull(history.getDescription());
        assertNull(history.getCreatedAt());
        assertNull(history.getExpiresAt());
    }
}
