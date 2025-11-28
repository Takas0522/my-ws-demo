package com.example.microservices.user.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;

import org.postgresql.ds.PGSimpleDataSource;

/**
 * Integration Test用のベースクラス
 * TestContainersを使用してPostgreSQLコンテナを起動し、
 * テストデータのセットアップとクリーンアップを行います。
 */
@Testcontainers
public abstract class BaseIntegrationTest {

    @Container
    protected static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("test_db")
            .withUsername("test_user")
            .withPassword("test_password");

    protected static DataSource dataSource;

    @BeforeAll
    static void initializeDataSource() {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setUrl(postgres.getJdbcUrl());
        ds.setUser(postgres.getUsername());
        ds.setPassword(postgres.getPassword());
        dataSource = ds;
    }

    @BeforeEach
    void setUp() throws Exception {
        // スキーマの初期化
        executeSqlScript("test-schema.sql");
        // テストデータの投入
        executeSqlScript("test-data.sql");
    }

    @AfterEach
    void tearDown() throws Exception {
        // テストデータのクリーンアップ
        executeSqlScript("cleanup-data.sql");
    }

    /**
     * SQLスクリプトファイルを実行します
     * 
     * @param scriptName スクリプトファイル名
     * @throws Exception SQL実行エラー
     */
    protected void executeSqlScript(String scriptName) throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(scriptName)) {
            if (is == null) {
                throw new IllegalArgumentException("Script not found: " + scriptName);
            }

            String script;
            try (InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                 BufferedReader br = new BufferedReader(isr)) {
                script = br.lines().collect(Collectors.joining("\n"));
            }

            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute(script);
            }
        }
    }

    /**
     * DataSourceを取得します
     * 
     * @return DataSource
     */
    protected DataSource getDataSource() {
        return dataSource;
    }
}
