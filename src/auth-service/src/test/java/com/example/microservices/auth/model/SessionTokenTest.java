package com.example.microservices.auth.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SessionTokenモデルのユニットテスト
 * Given-When-Then 形式でテストケースを記述
 */
@DisplayName("SessionToken Model Tests")
class SessionTokenTest {

    private SessionToken sessionToken;
    private UUID testUserId;
    private String testToken;
    private LocalDateTime testExpiresAt;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testToken = "test-token-string";
        testExpiresAt = LocalDateTime.now().plusDays(7);
        sessionToken = new SessionToken();
    }

    @Test
    @DisplayName("Given default constructor, When creating SessionToken, Then all fields should be null")
    void testDefaultConstructor() {
        // Then
        assertNull(sessionToken.getId());
        assertNull(sessionToken.getUserId());
        assertNull(sessionToken.getToken());
        assertNull(sessionToken.getExpiresAt());
        assertNull(sessionToken.getCreatedAt());
    }

    @Test
    @DisplayName("Given userId, token and expiresAt, When using constructor, Then fields should be initialized")
    void testConstructorWithParameters() {
        // When
        SessionToken token = new SessionToken(testUserId, testToken, testExpiresAt);

        // Then
        assertNull(token.getId());
        assertEquals(testUserId, token.getUserId());
        assertEquals(testToken, token.getToken());
        assertEquals(testExpiresAt, token.getExpiresAt());
        assertNull(token.getCreatedAt());
    }

    @Test
    @DisplayName("Given SessionToken object, When setting id, Then id should be stored correctly")
    void testSetAndGetId() {
        // Given
        Long testId = 123L;

        // When
        sessionToken.setId(testId);

        // Then
        assertEquals(testId, sessionToken.getId());
    }

    @Test
    @DisplayName("Given SessionToken object, When setting userId, Then userId should be stored correctly")
    void testSetAndGetUserId() {
        // When
        sessionToken.setUserId(testUserId);

        // Then
        assertEquals(testUserId, sessionToken.getUserId());
    }

    @Test
    @DisplayName("Given SessionToken object, When setting token, Then token should be stored correctly")
    void testSetAndGetToken() {
        // When
        sessionToken.setToken(testToken);

        // Then
        assertEquals(testToken, sessionToken.getToken());
    }

    @Test
    @DisplayName("Given SessionToken object, When setting expiresAt, Then expiresAt should be stored correctly")
    void testSetAndGetExpiresAt() {
        // When
        sessionToken.setExpiresAt(testExpiresAt);

        // Then
        assertEquals(testExpiresAt, sessionToken.getExpiresAt());
    }

    @Test
    @DisplayName("Given SessionToken object, When setting createdAt, Then createdAt should be stored correctly")
    void testSetAndGetCreatedAt() {
        // Given
        LocalDateTime testCreatedAt = LocalDateTime.now();

        // When
        sessionToken.setCreatedAt(testCreatedAt);

        // Then
        assertEquals(testCreatedAt, sessionToken.getCreatedAt());
    }

    @Test
    @DisplayName("Given SessionToken, When checking if it implements Serializable, Then it should be serializable")
    void testSerializable() {
        // Then
        assertTrue(sessionToken instanceof java.io.Serializable);
    }
}
