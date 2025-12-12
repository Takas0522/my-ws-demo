package com.example.microservices.auth.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LoginRequestモデルのユニットテスト
 * Given-When-Then 形式でテストケースを記述
 */
@DisplayName("LoginRequest Model Tests")
class LoginRequestTest {

    private LoginRequest loginRequest;
    private UUID testUserId;
    private String testUsername;
    private String testPassword;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUsername = "testuser";
        testPassword = "password123";
        loginRequest = new LoginRequest();
    }

    @Test
    @DisplayName("Given default constructor, When creating LoginRequest, Then all fields should be null")
    void testDefaultConstructor() {
        // Then
        assertNull(loginRequest.getUserId());
        assertNull(loginRequest.getUsername());
        assertNull(loginRequest.getPassword());
    }

    @Test
    @DisplayName("Given username and password, When using constructor, Then fields should be initialized")
    void testConstructorWithUsernameAndPassword() {
        // When
        LoginRequest request = new LoginRequest(testUsername, testPassword);

        // Then
        assertNull(request.getUserId());
        assertEquals(testUsername, request.getUsername());
        assertEquals(testPassword, request.getPassword());
    }

    @Test
    @DisplayName("Given userId and password, When using constructor, Then fields should be initialized")
    void testConstructorWithUserIdAndPassword() {
        // When
        LoginRequest request = new LoginRequest(testUserId, testPassword);

        // Then
        assertEquals(testUserId, request.getUserId());
        assertNull(request.getUsername());
        assertEquals(testPassword, request.getPassword());
    }

    @Test
    @DisplayName("Given LoginRequest object, When setting userId, Then userId should be stored correctly")
    void testSetAndGetUserId() {
        // When
        loginRequest.setUserId(testUserId);

        // Then
        assertEquals(testUserId, loginRequest.getUserId());
    }

    @Test
    @DisplayName("Given LoginRequest object, When setting username, Then username should be stored correctly")
    void testSetAndGetUsername() {
        // When
        loginRequest.setUsername(testUsername);

        // Then
        assertEquals(testUsername, loginRequest.getUsername());
    }

    @Test
    @DisplayName("Given LoginRequest object, When setting password, Then password should be stored correctly")
    void testSetAndGetPassword() {
        // When
        loginRequest.setPassword(testPassword);

        // Then
        assertEquals(testPassword, loginRequest.getPassword());
    }

    @Test
    @DisplayName("Given LoginRequest, When checking if it implements Serializable, Then it should be serializable")
    void testSerializable() {
        // Then
        assertTrue(loginRequest instanceof java.io.Serializable);
    }
}
