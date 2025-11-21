package com.example.microservices.bff.rest;

import com.example.microservices.bff.client.AuthServiceClient;
import com.example.microservices.bff.client.UserServiceClient;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * BFF REST API
 * フロントエンドからのリクエストを各マイクロサービスにプロキシ
 */
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BffResource {

    @Inject
    private UserServiceClient userServiceClient;

    @Inject
    private AuthServiceClient authServiceClient;

    // ==================== 認証エンドポイント ====================

    /**
     * ログイン
     */
    @POST
    @Path("/login")
    public Response login(Map<String, Object> loginData) {
        try {
            // リクエストボディの検証とログ出力
            System.out.println("Login request received: " + loginData);
            
            if (loginData == null || !loginData.containsKey("password")) {
                return createErrorResponse("Username and password are required");
            }

            // userId または username を取得
            Object userIdObj = loginData.get("userId");
            Object usernameObj = loginData.get("username");
            String password = (String) loginData.get("password");

            // loginData をそのまま転送
            Response authResponse = authServiceClient.login(loginData);
            String body = authResponse.readEntity(String.class);
            return Response.status(authResponse.getStatus())
                    .entity(body)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("Login failed: " + e.getMessage());
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
                return createErrorResponse("Invalid authorization header");
            }

            String token = authHeader.substring(7);
            Response authResponse = authServiceClient.logout(token);
            String body = authResponse.readEntity(String.class);
            return Response.status(authResponse.getStatus())
                    .entity(body)
                    .build();
        } catch (Exception e) {
            return createErrorResponse("Logout failed: " + e.getMessage());
        }
    }

    /**
     * トークン検証
     */
    @GET
    @Path("/verify")
    public Response verifyToken(@HeaderParam("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return createErrorResponse("Invalid authorization header");
            }

            String token = authHeader.substring(7);
            Response authResponse = authServiceClient.verifyToken(token);
            String body = authResponse.readEntity(String.class);
            return Response.status(authResponse.getStatus())
                    .entity(body)
                    .build();
        } catch (Exception e) {
            return createErrorResponse("Verification failed: " + e.getMessage());
        }
    }

    // ==================== ユーザー情報エンドポイント ====================

    /**
     * 会員画面用：ユーザーアカウント情報取得（認証付き）
     */
    @GET
    @Path("/account")
    public Response getAccount(@HeaderParam("Authorization") String authHeader) {
        try {
            // トークン検証
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(createErrorMap("Invalid authorization header"))
                        .build();
            }

            String token = authHeader.substring(7);
            Response verifyResponse = authServiceClient.verifyToken(token);
            
            if (verifyResponse.getStatus() != 200) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(createErrorMap("Invalid or expired token"))
                        .build();
            }

            // トークンから userId を取得
            String verifyBody = verifyResponse.readEntity(String.class);
            // 簡易的な実装（実際はJSON パースが必要）
            UUID userId = extractUserIdFromVerifyResponse(verifyBody);

            // ユーザーアカウント情報取得
            Response userResponse = userServiceClient.getUserAccount(userId);
            String body = userResponse.readEntity(String.class);
            
            return Response.status(userResponse.getStatus())
                    .entity(body)
                    .build();
        } catch (Exception e) {
            return createErrorResponse("Failed to get account: " + e.getMessage());
        }
    }

    /**
     * ユーザー情報取得
     */
    @GET
    @Path("/users/{id}")
    public Response getUser(@PathParam("id") String idParam, @HeaderParam("Authorization") String authHeader) {
        try {
            // UUIDバリデーション
            UUID id;
            try {
                id = UUID.fromString(idParam);
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(createErrorMap("Invalid UUID format"))
                        .build();
            }

            // 認証チェック
            if (!isAuthenticated(authHeader)) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(createErrorMap("Unauthorized"))
                        .build();
            }

            Response userResponse = userServiceClient.getUser(id);
            String body = userResponse.readEntity(String.class);
            return Response.status(userResponse.getStatus())
                    .entity(body)
                    .build();
        } catch (Exception e) {
            return createErrorResponse("Failed to get user: " + e.getMessage());
        }
    }

    /**
     * 全ユーザー取得
     */
    @GET
    @Path("/users")
    public Response getAllUsers(@HeaderParam("Authorization") String authHeader) {
        try {
            // 認証チェック
            if (!isAuthenticated(authHeader)) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(createErrorMap("Unauthorized"))
                        .build();
            }

            Response userResponse = userServiceClient.getAllUsers();
            String body = userResponse.readEntity(String.class);
            return Response.status(userResponse.getStatus())
                    .entity(body)
                    .build();
        } catch (Exception e) {
            return createErrorResponse("Failed to get users: " + e.getMessage());
        }
    }

    // ==================== ヘルパーメソッド ====================

    private boolean isAuthenticated(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }

        try {
            String token = authHeader.substring(7);
            Response verifyResponse = authServiceClient.verifyToken(token);
            return verifyResponse.getStatus() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    private UUID extractUserIdFromVerifyResponse(String jsonResponse) {
        // 簡易的な実装（実際は JSON ライブラリで適切にパースすべき）
        // 例: {"valid":true,"userId":"550e8400-e29b-41d4-a716-446655440000","expiresAt":"..."}
        try {
            String userIdKey = "\"userId\":";
            int userIdIndex = jsonResponse.indexOf(userIdKey);
            if (userIdIndex == -1) {
                return null;
            }
            // Skip past "userId": to find the opening quote
            String substring = jsonResponse.substring(userIdIndex + userIdKey.length());
            int openQuoteIndex = substring.indexOf("\"");
            if (openQuoteIndex == -1) {
                return null;
            }
            // Find the closing quote after the opening quote
            String afterOpenQuote = substring.substring(openQuoteIndex + 1);
            int closeQuoteIndex = afterOpenQuote.indexOf("\"");
            if (closeQuoteIndex == -1) {
                return null;
            }
            String userIdStr = afterOpenQuote.substring(0, closeQuoteIndex);
            return UUID.fromString(userIdStr);
        } catch (Exception e) {
            return null;
        }
    }

    private Response createErrorResponse(String message) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(createErrorMap(message))
                .build();
    }

    private Map<String, String> createErrorMap(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}
