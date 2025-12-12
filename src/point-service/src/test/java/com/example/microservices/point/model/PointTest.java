package com.example.microservices.point.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Point Model Tests")
class PointTest {

    private UUID testUserId;
    private Integer testBalance;
    private LocalDateTime testLastUpdated;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testBalance = 1000;
        testLastUpdated = LocalDateTime.now();
    }

    @Test
    @DisplayName("Given default constructor, When creating Point, Then all fields should be null")
    void givenDefaultConstructor_whenCreatingPoint_thenAllFieldsShouldBeNull() {
        // When
        Point point = new Point();

        // Then
        assertNull(point.getUserId());
        assertNull(point.getBalance());
        assertNull(point.getLastUpdated());
    }

    @Test
    @DisplayName("Given all parameters, When creating Point, Then all fields should be set correctly")
    void givenAllParameters_whenCreatingPoint_thenAllFieldsShouldBeSetCorrectly() {
        // When
        Point point = new Point(testUserId, testBalance, testLastUpdated);

        // Then
        assertEquals(testUserId, point.getUserId());
        assertEquals(testBalance, point.getBalance());
        assertEquals(testLastUpdated, point.getLastUpdated());
    }

    @Test
    @DisplayName("Given a Point, When setting userId, Then userId should be updated")
    void givenAPoint_whenSettingUserId_thenUserIdShouldBeUpdated() {
        // Given
        Point point = new Point();
        UUID newUserId = UUID.randomUUID();

        // When
        point.setUserId(newUserId);

        // Then
        assertEquals(newUserId, point.getUserId());
    }

    @Test
    @DisplayName("Given a Point, When setting balance, Then balance should be updated")
    void givenAPoint_whenSettingBalance_thenBalanceShouldBeUpdated() {
        // Given
        Point point = new Point();
        Integer newBalance = 5000;

        // When
        point.setBalance(newBalance);

        // Then
        assertEquals(newBalance, point.getBalance());
    }

    @Test
    @DisplayName("Given a Point, When setting lastUpdated, Then lastUpdated should be updated")
    void givenAPoint_whenSettingLastUpdated_thenLastUpdatedShouldBeUpdated() {
        // Given
        Point point = new Point();
        LocalDateTime newLastUpdated = LocalDateTime.now().plusDays(1);

        // When
        point.setLastUpdated(newLastUpdated);

        // Then
        assertEquals(newLastUpdated, point.getLastUpdated());
    }

    @Test
    @DisplayName("Given a Point with zero balance, When getting balance, Then should return zero")
    void givenAPointWithZeroBalance_whenGettingBalance_thenShouldReturnZero() {
        // Given
        Point point = new Point(testUserId, 0, testLastUpdated);

        // Then
        assertEquals(0, point.getBalance());
    }

    @Test
    @DisplayName("Given a Point with negative balance, When getting balance, Then should return negative value")
    void givenAPointWithNegativeBalance_whenGettingBalance_thenShouldReturnNegativeValue() {
        // Given
        Integer negativeBalance = -100;
        Point point = new Point(testUserId, negativeBalance, testLastUpdated);

        // Then
        assertEquals(negativeBalance, point.getBalance());
    }

    @Test
    @DisplayName("Given null values, When setting fields, Then should accept null values")
    void givenNullValues_whenSettingFields_thenShouldAcceptNullValues() {
        // Given
        Point point = new Point(testUserId, testBalance, testLastUpdated);

        // When
        point.setUserId(null);
        point.setBalance(null);
        point.setLastUpdated(null);

        // Then
        assertNull(point.getUserId());
        assertNull(point.getBalance());
        assertNull(point.getLastUpdated());
    }
}
