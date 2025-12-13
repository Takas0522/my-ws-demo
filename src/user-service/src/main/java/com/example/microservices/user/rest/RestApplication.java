package com.example.microservices.user.rest;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS Application Configuration
 */
@ApplicationPath("/api")
public class RestApplication extends Application {
    // エンドポイント: /api/users
}
