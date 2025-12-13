package com.example.microservices.auth.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import jakarta.enterprise.context.ApplicationScoped;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 認証ユーティリティサービス
 */
@ApplicationScoped
public class AuthService {

    private static final int TOKEN_LENGTH = 32;
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * パスワードをハッシュ化
     */
    public String hashPassword(String password) {
        return BCrypt.withDefaults().hashToString(10, password.toCharArray());
    }

    /**
     * パスワード検証
     */
    public boolean verifyPassword(String password, String hashedPassword) {
        try {
            BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), hashedPassword);
            return result.verified;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * ランダムなセッショントークン生成
     */
    public String generateToken() {
        byte[] randomBytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
