package com.example.microservices.auth.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Date;
import java.util.UUID;

/**
 * JWT生成と検証のユーティリティクラス
 */
@ApplicationScoped
public class JwtUtil {
    
    // SECURITY NOTE: 本番環境では環境変数や暗号化された設定から取得すべき
    // 環境変数例: System.getenv("JWT_SECRET_KEY")
    // または AWS Secrets Manager, HashiCorp Vault などのシークレット管理サービスを使用
    private static final String SECRET_KEY = "your-secret-key-change-this-in-production";
    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET_KEY);
    private static final long EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000; // 7 days in milliseconds
    
    /**
     * JWT トークンを生成
     * 
     * @param userId ユーザーID (UUID)
     * @param username ユーザー名
     * @return 生成されたJWTトークン
     */
    public String generateToken(UUID userId, String username) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + EXPIRATION_TIME);
        
        return JWT.create()
                .withClaim("userId", userId.toString())
                .withClaim("username", username)
                .withIssuedAt(now)
                .withExpiresAt(expiresAt)
                .sign(ALGORITHM);
    }
    
    /**
     * JWT トークンを検証してデコード
     * 
     * @param token JWTトークン
     * @return デコードされたJWT
     * @throws JWTVerificationException トークンが無効な場合
     */
    public DecodedJWT verifyToken(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(ALGORITHM).build();
        return verifier.verify(token);
    }
    
    /**
     * トークンからユーザーIDを抽出
     * 
     * @param token JWTトークン
     * @return ユーザーID (UUID)
     * @throws JWTVerificationException トークンが無効な場合
     * @throws IllegalArgumentException claimが存在しないかnullの場合
     */
    public UUID extractUserId(String token) throws JWTVerificationException {
        DecodedJWT jwt = verifyToken(token);
        String userIdStr = jwt.getClaim("userId").asString();
        if (userIdStr == null || userIdStr.isEmpty()) {
            throw new IllegalArgumentException("userId claim is missing or invalid");
        }
        return UUID.fromString(userIdStr);
    }
    
    /**
     * トークンからユーザー名を抽出
     * 
     * @param token JWTトークン
     * @return ユーザー名
     * @throws JWTVerificationException トークンが無効な場合
     * @throws IllegalArgumentException claimが存在しないかnullの場合
     */
    public String extractUsername(String token) throws JWTVerificationException {
        DecodedJWT jwt = verifyToken(token);
        String username = jwt.getClaim("username").asString();
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("username claim is missing or invalid");
        }
        return username;
    }
}
