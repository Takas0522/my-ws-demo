package com.example.microservices.user.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Userモデルのユニットテスト
 * Given-When-Then 形式でテストケースを記述
 */
@DisplayName("User Model Tests")
class UserTest {

    private User user;
    private UUID testId;
    private String testUsername;
    private String testEmail;
    private String testFullName;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testUsername = "testuser";
        testEmail = "test@example.com";
        testFullName = "Test User";
        user = new User();
    }

    @Test
    @DisplayName("Given a new User object, When setting id, Then id should be correctly stored")
    void testSetAndGetId() {
        // When
        user.setId(testId);

        // Then
        assertEquals(testId, user.getId());
    }

    @Test
    @DisplayName("Given a new User object, When setting username, Then username should be correctly stored")
    void testSetAndGetUsername() {
        // When
        user.setUsername(testUsername);

        // Then
        assertEquals(testUsername, user.getUsername());
    }

    @Test
    @DisplayName("Given a new User object, When setting email, Then email should be correctly stored")
    void testSetAndGetEmail() {
        // When
        user.setEmail(testEmail);

        // Then
        assertEquals(testEmail, user.getEmail());
    }

    @Test
    @DisplayName("Given a new User object, When setting full name, Then full name should be correctly stored")
    void testSetAndGetFullName() {
        // When
        user.setFullName(testFullName);

        // Then
        assertEquals(testFullName, user.getFullName());
    }

    @Test
    @DisplayName("Given a new User object, When setting createdAt, Then createdAt should be correctly stored")
    void testSetAndGetCreatedAt() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        // When
        user.setCreatedAt(now);

        // Then
        assertEquals(now, user.getCreatedAt());
    }

    @Test
    @DisplayName("Given a new User object, When setting updatedAt, Then updatedAt should be correctly stored")
    void testSetAndGetUpdatedAt() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        // When
        user.setUpdatedAt(now);

        // Then
        assertEquals(now, user.getUpdatedAt());
    }

    @Test
    @DisplayName("Given constructor with parameters, When creating User, Then all fields should be initialized correctly")
    void testConstructorWithParameters() {
        // When
        User newUser = new User(testId, testUsername, testEmail, testFullName);

        // Then
        assertEquals(testId, newUser.getId());
        assertEquals(testUsername, newUser.getUsername());
        assertEquals(testEmail, newUser.getEmail());
        assertEquals(testFullName, newUser.getFullName());
    }

    @Test
    @DisplayName("Given default constructor, When creating User, Then User should be instantiated with null fields")
    void testDefaultConstructor() {
        // When
        User newUser = new User();

        // Then
        assertNull(newUser.getId());
        assertNull(newUser.getUsername());
        assertNull(newUser.getEmail());
        assertNull(newUser.getFullName());
        assertNull(newUser.getCreatedAt());
        assertNull(newUser.getUpdatedAt());
    }

    @Test
    @DisplayName("Given a User object, When checking if it implements Serializable, Then it should be serializable")
    void testSerializable() {
        // Then
        assertTrue(user instanceof java.io.Serializable);
    }
}
