package com.example.microservices.auth.util;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtUtilのユニットテスト
 * Given-When-Then 形式でテストケースを記述
 */
@DisplayName("JwtUtil Tests")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UUID testUserId;
    private String testUsername;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        testUserId = UUID.randomUUID();
        testUsername = "testuser";
    }

    @Test
    @DisplayName("Given userId and username, When generateToken is called, Then should return a valid JWT token")
    void testGenerateToken() {
        // When
        String token = jwtUtil.generateToken(testUserId, testUsername);

        // Then
        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertTrue(token.contains(".")); // JWT has dots separating sections
    }

    @Test
    @DisplayName("Given valid token, When verifyToken is called, Then should return decoded JWT")
    void testVerifyToken_Success() {
        // Given
        String token = jwtUtil.generateToken(testUserId, testUsername);

        // When
        DecodedJWT decoded = jwtUtil.verifyToken(token);

        // Then
        assertNotNull(decoded);
        assertEquals(testUserId.toString(), decoded.getClaim("userId").asString());
        assertEquals(testUsername, decoded.getClaim("username").asString());
    }

    @Test
    @DisplayName("Given invalid token, When verifyToken is called, Then should throw JWTVerificationException")
    void testVerifyToken_InvalidToken() {
        // Given
        String invalidToken = "invalid.token.string";

        // When & Then
        assertThrows(JWTVerificationException.class, () -> {
            jwtUtil.verifyToken(invalidToken);
        });
    }

    @Test
    @DisplayName("Given valid token, When extractUserId is called, Then should return the userId")
    void testExtractUserId_Success() {
        // Given
        String token = jwtUtil.generateToken(testUserId, testUsername);

        // When
        UUID extractedUserId = jwtUtil.extractUserId(token);

        // Then
        assertEquals(testUserId, extractedUserId);
    }

    @Test
    @DisplayName("Given invalid token, When extractUserId is called, Then should throw exception")
    void testExtractUserId_InvalidToken() {
        // Given
        String invalidToken = "invalid.token.string";

        // When & Then
        assertThrows(Exception.class, () -> {
            jwtUtil.extractUserId(invalidToken);
        });
    }

    @Test
    @DisplayName("Given valid token, When extractUsername is called, Then should return the username")
    void testExtractUsername_Success() {
        // Given
        String token = jwtUtil.generateToken(testUserId, testUsername);

        // When
        String extractedUsername = jwtUtil.extractUsername(token);

        // Then
        assertEquals(testUsername, extractedUsername);
    }

    @Test
    @DisplayName("Given invalid token, When extractUsername is called, Then should throw exception")
    void testExtractUsername_InvalidToken() {
        // Given
        String invalidToken = "invalid.token.string";

        // When & Then
        assertThrows(Exception.class, () -> {
            jwtUtil.extractUsername(invalidToken);
        });
    }

    @Test
    @DisplayName("Given token, When verifyToken is called, Then token should have issued and expiry dates")
    void testGeneratedToken_HasDates() {
        // Given
        String token = jwtUtil.generateToken(testUserId, testUsername);

        // When
        DecodedJWT decoded = jwtUtil.verifyToken(token);

        // Then
        assertNotNull(decoded.getIssuedAt());
        assertNotNull(decoded.getExpiresAt());
        assertTrue(decoded.getExpiresAt().after(decoded.getIssuedAt()));
    }

    @Test
    @DisplayName("Given different user data, When generateToken is called, Then tokens should be different")
    void testGenerateToken_DifferentUsers() {
        // Given
        UUID userId2 = UUID.randomUUID();
        String username2 = "testuser2";

        // When
        String token1 = jwtUtil.generateToken(testUserId, testUsername);
        String token2 = jwtUtil.generateToken(userId2, username2);

        // Then
        assertNotEquals(token1, token2);
    }
}
