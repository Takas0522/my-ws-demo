package com.example.microservices.bff.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * ポイントサービスクライアント
 */
@ApplicationScoped
public class PointServiceClient {

    private static final String POINT_SERVICE_URL = System.getenv().getOrDefault("POINT_SERVICE_URL", "http://localhost:8084");
    private final Client client;

    public PointServiceClient() {
        this.client = ClientBuilder.newClient();
    }

    /**
     * ポイント残高取得
     * GET /api/points
     */
    public Response getPoints(String jwtToken) {
        WebTarget target = client.target(POINT_SERVICE_URL)
                .path("/point-service/api/points");
        return target.request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .get();
    }

    /**
     * ポイント履歴取得
     * GET /api/points/history?page={page}&limit={limit}
     */
    public Response getPointHistory(String jwtToken, int page, int limit) {
        WebTarget target = client.target(POINT_SERVICE_URL)
                .path("/point-service/api/points/history")
                .queryParam("page", page)
                .queryParam("limit", limit);
        return target.request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .get();
    }
}
