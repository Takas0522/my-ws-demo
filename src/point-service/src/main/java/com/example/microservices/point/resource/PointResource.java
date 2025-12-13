package com.example.microservices.point.resource;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.microservices.point.model.Point;
import com.example.microservices.point.model.PointHistory;
import com.example.microservices.point.service.PointService;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.*;

/**
 * ポイント管理REST API
 */
@Path("/points")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PointResource {

    @Inject
    private PointService pointService;

    // JWT検証用のシークレットキー（Auth Serviceと同じ値を使用）
    // SECURITY NOTE: 本番環境では環境変数や暗号化された設定から取得すべき
    // 環境変数例: System.getenv("JWT_SECRET_KEY")
    // または AWS Secrets Manager, HashiCorp Vault などのシークレット管理サービスを使用
    private static final String SECRET_KEY = "your-secret-key-change-this-in-production";
    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET_KEY);

    /**
     * ポイント残高取得
     * GET /api/points
     */
    @GET
    public Response getPointBalance(@Context HttpHeaders headers) {
        try {
            // JWTトークンを検証してuserIdを取得
            UUID userId = extractUserIdFromToken(headers);
            if (userId == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(createErrorResponse("Invalid or missing JWT token"))
                        .build();
            }

            // ポイント残高を取得
            Optional<Point> point = pointService.getPointBalance(userId);
            if (point.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("userId", point.get().getUserId().toString());
                response.put("balance", point.get().getBalance());
                response.put("lastUpdated", point.get().getLastUpdated());
                return Response.ok(response).build();
            } else {
                // ポイント残高がない場合は0として返す
                Map<String, Object> response = new HashMap<>();
                response.put("userId", userId.toString());
                response.put("balance", 0);
                response.put("lastUpdated", null);
                return Response.ok(response).build();
            }
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse("Failed to fetch point balance: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * ポイント履歴取得
     * GET /api/points/history?page=1&limit=10
     */
    @GET
    @Path("/history")
    public Response getPointHistory(
            @Context HttpHeaders headers,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("limit") @DefaultValue("10") int limit) {
        
        try {
            // JWTトークンを検証してuserIdを取得
            UUID userId = extractUserIdFromToken(headers);
            if (userId == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(createErrorResponse("Invalid or missing JWT token"))
                        .build();
            }

            // ページネーションのバリデーション
            if (page < 1) page = 1;
            if (limit < 1) limit = 10;
            if (limit > 100) limit = 100;

            // ポイント履歴を取得
            List<PointHistory> histories = pointService.getPointHistory(userId, page, limit);
            int total = pointService.getPointHistoryCount(userId);
            
            // 現在のポイント残高を取得
            int currentBalance = pointService.getPointBalance(userId)
                    .map(point -> point.getBalance())
                    .orElse(0);

            // 現在のページの最初の履歴時点での残高を計算
            int balanceAtPageStart = currentBalance;
            if (page > 1) {
                // page=1からpage-1までの全履歴を取得して残高を逆算
                int offset = 0;
                int itemsToSkip = (page - 1) * limit;
                List<PointHistory> previousHistories = pointService.getPointHistory(userId, 1, itemsToSkip);
                for (PointHistory h : previousHistories) {
                    if ("EARN".equals(h.getTransactionType())) {
                        balanceAtPageStart -= Math.abs(h.getAmount());
                    } else if ("USE".equals(h.getTransactionType()) || "SPEND".equals(h.getTransactionType())) {
                        balanceAtPageStart += Math.abs(h.getAmount());
                    }
                }
            }

            // レスポンスを構築（ページネーション情報を含む）
            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId.toString());
            response.put("history", convertHistoriesToMap(histories, balanceAtPageStart));
            
            // ページネーション情報
            Map<String, Object> pagination = new HashMap<>();
            pagination.put("currentPage", page);
            pagination.put("limit", limit);
            pagination.put("totalItems", total);
            pagination.put("totalPages", (int) Math.ceil((double) total / limit));
            response.put("pagination", pagination);

            return Response.ok(response).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse("Failed to fetch point history: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * JWTトークンからuserIdを抽出
     */
    private UUID extractUserIdFromToken(HttpHeaders headers) {
        try {
            String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return null;
            }

            String token = authHeader.substring(7); // "Bearer " を除去
            JWTVerifier verifier = JWT.require(ALGORITHM).build();
            DecodedJWT jwt = verifier.verify(token);
            
            String userIdStr = jwt.getClaim("userId").asString();
            if (userIdStr == null || userIdStr.isEmpty()) {
                return null;
            }

            return UUID.fromString(userIdStr);
        } catch (JWTVerificationException | IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * PointHistoryのリストをMap形式に変換
     * balanceAfterを計算するために、指定された開始残高から逆算していく
     */
    private List<Map<String, Object>> convertHistoriesToMap(List<PointHistory> histories, int startingBalance) {
        List<Map<String, Object>> result = new ArrayList<>();
        int balanceAfter = startingBalance;
        
        for (PointHistory history : histories) {
            Map<String, Object> historyMap = new HashMap<>();
            historyMap.put("id", history.getId());
            historyMap.put("amount", Math.abs(history.getAmount())); // 常に正の値
            historyMap.put("type", history.getTransactionType()); // EARN or USE -> そのまま使用
            historyMap.put("transactionType", history.getTransactionType()); // 互換性のため残す
            historyMap.put("description", history.getDescription());
            historyMap.put("createdAt", history.getCreatedAt());
            historyMap.put("expiresAt", history.getExpiresAt());
            historyMap.put("balanceAfter", balanceAfter);
            
            // 次の履歴の残高を計算（古い方向に遡る）
            if ("EARN".equals(history.getTransactionType())) {
                balanceAfter -= Math.abs(history.getAmount());
            } else if ("USE".equals(history.getTransactionType()) || "SPEND".equals(history.getTransactionType())) {
                balanceAfter += Math.abs(history.getAmount());
            }
            
            result.add(historyMap);
        }
        return result;
    }

    /**
     * エラーレスポンスを生成
     */
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}
