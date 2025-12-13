package com.example.microservices.bff.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

/**
 * 認証サービスクライアント
 */
@ApplicationScoped
public class AuthServiceClient {

    private static final String AUTH_SERVICE_URL = System.getenv().getOrDefault("AUTH_SERVICE_URL", "http://localhost:8081");
    private final Client client;

    public AuthServiceClient() {
        this.client = ClientBuilder.newClient();
    }

    /**
     * ログイン
     */
    public Response login(Map<String, Object> loginData) {
        WebTarget target = client.target(AUTH_SERVICE_URL)
                .path("/auth-service/api/auth/login");
        return target.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(loginData, MediaType.APPLICATION_JSON));
    }

    /**
     * トークン検証
     */
    public Response verifyToken(String token) {
        WebTarget target = client.target(AUTH_SERVICE_URL)
                .path("/auth-service/api/auth/verify");
        return target.request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .post(Entity.json("{}"));
    }

    /**
     * ログアウト
     */
    public Response logout(String token) {
        WebTarget target = client.target(AUTH_SERVICE_URL)
                .path("/auth-service/api/auth/logout");
        return target.request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .post(Entity.json("{}"));
    }
}
