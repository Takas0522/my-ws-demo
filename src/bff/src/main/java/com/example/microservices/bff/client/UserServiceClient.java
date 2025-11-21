package com.example.microservices.bff.client;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ユーザーサービスクライアント
 */
@ApplicationScoped
public class UserServiceClient {

    private static final String USER_SERVICE_URL = System.getenv().getOrDefault("USER_SERVICE_URL", "http://localhost:8080");
    private final Client client;

    public UserServiceClient() {
        this.client = ClientBuilder.newClient();
    }

    /**
     * ユーザー情報取得
     */
    public Response getUser(UUID userId) {
        WebTarget target = client.target(USER_SERVICE_URL)
                .path("/user-service/api/users/" + userId);
        return target.request(MediaType.APPLICATION_JSON).get();
    }

    /**
     * ユーザーアカウント情報取得（ユーザー情報 + ポイント）
     */
    public Response getUserAccount(UUID userId) {
        WebTarget target = client.target(USER_SERVICE_URL)
                .path("/user-service/api/users/" + userId + "/account");
        return target.request(MediaType.APPLICATION_JSON).get();
    }

    /**
     * 全ユーザー取得
     */
    public Response getAllUsers() {
        WebTarget target = client.target(USER_SERVICE_URL)
                .path("/user-service/api/users");
        return target.request(MediaType.APPLICATION_JSON).get();
    }

    /**
     * ユーザー作成
     */
    public Response createUser(Map<String, Object> userData) {
        WebTarget target = client.target(USER_SERVICE_URL)
                .path("/user-service/api/users");
        return target.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(userData, MediaType.APPLICATION_JSON));
    }

    /**
     * ユーザー更新
     */
    public Response updateUser(UUID userId, Map<String, Object> userData) {
        WebTarget target = client.target(USER_SERVICE_URL)
                .path("/user-service/api/users/" + userId);
        return target.request(MediaType.APPLICATION_JSON)
                .put(Entity.entity(userData, MediaType.APPLICATION_JSON));
    }

    /**
     * ユーザー削除
     */
    public Response deleteUser(UUID userId) {
        WebTarget target = client.target(USER_SERVICE_URL)
                .path("/user-service/api/users/" + userId);
        return target.request(MediaType.APPLICATION_JSON).delete();
    }
}
