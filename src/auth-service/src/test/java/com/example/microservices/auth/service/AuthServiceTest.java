package com.example.microservices.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AuthServiceのユニットテスト
 * Given-When-Then 形式でテストケースを記述
 */
@DisplayName("AuthService Tests")
class AuthServiceTest {

    private AuthService authService;
    private String testPassword;
    private String hashedPassword;

    @BeforeEach
    void setUp() {
        authService = new AuthService();
        testPassword = "testPassword123";
    }

    @Test
    @DisplayName("Given a plain password, When hashPassword is called, Then should return a hashed password")
    void testHashPassword() {
        // When
        String hash = authService.hashPassword(testPassword);

        // Then
        assertNotNull(hash);
        assertNotEquals(testPassword, hash);
        assertTrue(hash.startsWith("$2a$")); // BCrypt hash prefix
    }

    @Test
    @DisplayName("Given matching password and hash, When verifyPassword is called, Then should return true")
    void testVerifyPassword_Success() {
        // Given
        hashedPassword = authService.hashPassword(testPassword);

        // When
        boolean result = authService.verifyPassword(testPassword, hashedPassword);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("Given non-matching password and hash, When verifyPassword is called, Then should return false")
    void testVerifyPassword_Failure() {
        // Given
        hashedPassword = authService.hashPassword(testPassword);
        String wrongPassword = "wrongPassword123";

        // When
        boolean result = authService.verifyPassword(wrongPassword, hashedPassword);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Given invalid hash format, When verifyPassword is called, Then should return false")
    void testVerifyPassword_InvalidHash() {
        // Given
        String invalidHash = "invalid-hash-format";

        // When
        boolean result = authService.verifyPassword(testPassword, invalidHash);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Given generateToken is called, When token is generated, Then should return a valid token")
    void testGenerateToken() {
        // When
        String token1 = authService.generateToken();
        String token2 = authService.generateToken();

        // Then
        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2); // Each token should be unique
        assertTrue(token1.length() > 0);
    }

    @Test
    @DisplayName("Given multiple calls to generateToken, When tokens are generated, Then each should be unique")
    void testGenerateToken_Uniqueness() {
        // When
        String token1 = authService.generateToken();
        String token2 = authService.generateToken();
        String token3 = authService.generateToken();

        // Then
        assertNotEquals(token1, token2);
        assertNotEquals(token2, token3);
        assertNotEquals(token1, token3);
    }

    @Test
    @DisplayName("Given same password hashed twice, When comparing hashes, Then they should be different (salt)")
    void testHashPassword_DifferentSalts() {
        // When
        String hash1 = authService.hashPassword(testPassword);
        String hash2 = authService.hashPassword(testPassword);

        // Then
        assertNotEquals(hash1, hash2); // Different salts should produce different hashes
        // But both should verify the same password
        assertTrue(authService.verifyPassword(testPassword, hash1));
        assertTrue(authService.verifyPassword(testPassword, hash2));
    }
}
