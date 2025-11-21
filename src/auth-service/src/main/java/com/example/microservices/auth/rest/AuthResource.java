package com.example.microservices.auth.rest;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.microservices.auth.model.LoginRequest;
import com.example.microservices.auth.model.SessionToken;
import com.example.microservices.auth.repository.AuthRepository;
import com.example.microservices.auth.service.AuthService;
import com.example.microservices.auth.util.JwtUtil;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 認証REST API
 */
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    private AuthRepository authRepository;

    @Inject
    private AuthService authService;

    @Inject
    private JwtUtil jwtUtil;

    /**
     * ログイン
     */
    @POST
    @Path("/login")
    public Response login(LoginRequest loginRequest, @Context HttpHeaders headers) {
        try {
            // 入力検証
            if (loginRequest == null || loginRequest.getPassword() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(createErrorResponse("Username and password are required"))
                        .build();
            }

            UUID userId = null;
            String username = null;

            // userId または username のどちらかでログイン
            if (loginRequest.getUserId() != null) {
                // userIdでログイン
                userId = loginRequest.getUserId();
                // ユーザー名を取得（レスポンス用）
                Optional<String> usernameOpt = authRepository.getUsernameByUserId(userId);
                if (usernameOpt.isPresent()) {
                    username = usernameOpt.get();
                }
            } else if (loginRequest.getUsername() != null) {
                // ユーザー名でログイン
                username = loginRequest.getUsername();
                Optional<UUID> userIdOpt = authRepository.getUserIdByUsername(username);
                if (userIdOpt.isPresent()) {
                    userId = userIdOpt.get();
                }
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(createErrorResponse("Username and password are required"))
                        .build();
            }

            if (userId == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(createErrorResponse("Invalid credentials"))
                        .build();
            }

            // パスワードハッシュ取得
            Optional<String> hashedPasswordOpt = authRepository.getPasswordHash(userId);
            if (!hashedPasswordOpt.isPresent()) {
                recordLoginAttempt(userId, headers, false);
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(createErrorResponse("Invalid credentials"))
                        .build();
            }

            // パスワード検証
            boolean isValid = authService.verifyPassword(loginRequest.getPassword(), hashedPasswordOpt.get());
            
            if (!isValid) {
                recordLoginAttempt(userId, headers, false);
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(createErrorResponse("Invalid credentials"))
                        .build();
            }

            // JWT トークン生成
            String jwtToken = jwtUtil.generateToken(userId, username);

            // レガシーのセッショントークンも生成（移行期間中のみ）
            String sessionToken = authService.generateToken();
            LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);
            
            SessionToken session = new SessionToken(userId, sessionToken, expiresAt);
            session = authRepository.createSessionToken(session);

            // ログイン成功記録
            recordLoginAttempt(userId, headers, true);

            Map<String, Object> response = new HashMap<>();
            response.put("token", jwtToken);
            response.put("userId", userId.toString());
            if (username != null) {
                response.put("username", username);
            }
            response.put("expiresAt", expiresAt.toString());

            return Response.ok(response).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse("Login failed: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * トークン検証
     */
    @POST
    @Path("/verify")
    public Response verifyToken(@HeaderParam("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(createErrorResponse("Invalid authorization header"))
                        .build();
            }

            String token = authHeader.substring(7);
            
            // JWT トークン検証を試みる
            try {
                DecodedJWT jwt = jwtUtil.verifyToken(token);
                String userIdStr = jwt.getClaim("userId").asString();
                String username = jwt.getClaim("username").asString();
                
                if (userIdStr == null || userIdStr.isEmpty()) {
                    return Response.status(Response.Status.UNAUTHORIZED)
                            .entity(createErrorResponse("Invalid token: userId claim missing"))
                            .build();
                }
                
                if (username == null || username.isEmpty()) {
                    return Response.status(Response.Status.UNAUTHORIZED)
                            .entity(createErrorResponse("Invalid token: username claim missing"))
                            .build();
                }
                
                Map<String, Object> response = new HashMap<>();
                response.put("valid", true);
                response.put("userId", userIdStr);
                response.put("username", username);

                return Response.ok(response).build();
            } catch (JWTVerificationException e) {
                // JWT 検証失敗の場合、レガシーセッショントークンを確認
                Optional<SessionToken> sessionOpt = authRepository.findSessionByToken(token);

                if (!sessionOpt.isPresent()) {
                    return Response.status(Response.Status.UNAUTHORIZED)
                            .entity(createErrorResponse("Invalid token"))
                            .build();
                }

                SessionToken session = sessionOpt.get();
                if (session.isExpired()) {
                    authRepository.deleteSession(token);
                    return Response.status(Response.Status.UNAUTHORIZED)
                            .entity(createErrorResponse("Token expired"))
                            .build();
                }

                Map<String, Object> response = new HashMap<>();
                response.put("valid", true);
                response.put("userId", session.getUserId().toString());
                response.put("expiresAt", session.getExpiresAt().toString());

                return Response.ok(response).build();
            }
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse("Verification failed: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * ログアウト
     */
    @POST
    @Path("/logout")
    public Response logout(@HeaderParam("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(createErrorResponse("Invalid authorization header"))
                        .build();
            }

            String token = authHeader.substring(7);
            
            // JWT トークンの場合は何もしない（ステートレスなので）
            try {
                jwtUtil.verifyToken(token);
                // JWT トークンは有効だがステートレスなのでサーバー側で削除する必要はない
            } catch (JWTVerificationException e) {
                // レガシーセッショントークンの場合は削除
                authRepository.deleteSession(token);
            }

            Map<String, String> response = new HashMap<>();
            response.put("message", "Logged out successfully");

            return Response.ok(response).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse("Logout failed: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * 期限切れセッションのクリーンアップ
     */
    @POST
    @Path("/cleanup")
    public Response cleanupExpiredSessions() {
        try {
            authRepository.cleanupExpiredSessions();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Expired sessions cleaned up successfully");
            return Response.ok(response).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse("Cleanup failed: " + e.getMessage()))
                    .build();
        }
    }

    private void recordLoginAttempt(UUID userId, HttpHeaders headers, boolean success) {
        try {
            String ipAddress = headers.getHeaderString("X-Forwarded-For");
            if (ipAddress == null) {
                ipAddress = headers.getHeaderString("X-Real-IP");
            }
            if (ipAddress == null) {
                ipAddress = "unknown";
            }

            String userAgent = headers.getHeaderString("User-Agent");
            if (userAgent == null) {
                userAgent = "unknown";
            }

            authRepository.recordLoginHistory(userId, ipAddress, userAgent, success);
        } catch (SQLException e) {
            // ログ記録失敗は致命的なエラーではないので無視
            System.err.println("Failed to record login history: " + e.getMessage());
        }
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}
