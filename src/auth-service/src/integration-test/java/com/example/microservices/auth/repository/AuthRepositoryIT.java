package com.example.microservices.auth.repository;

import com.example.microservices.auth.model.SessionToken;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.postgresql.ds.PGSimpleDataSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AuthRepository統合テスト
 * TestContainersを使用してPostgreSQLコンテナでテストを実行
 */
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthRepositoryIT {

    @Container
    private static final PostgreSQLContainer<?> authPostgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:15-alpine")
    )
            .withDatabaseName("auth_service_db")
            .withUsername("testuser")
            .withPassword("testpass");

    @Container
    private static final PostgreSQLContainer<?> userPostgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:15-alpine")
    )
            .withDatabaseName("user_service_db")
            .withUsername("testuser")
            .withPassword("testpass");

    private static AuthRepository authRepository;
    private static DataSource authDataSource;
    private static DataSource userDataSource;

    @BeforeAll
    static void setUp() throws Exception {
        // Auth Service DataSourceの設定
        PGSimpleDataSource authDs = new PGSimpleDataSource();
        authDs.setURL(authPostgres.getJdbcUrl());
        authDs.setUser(authPostgres.getUsername());
        authDs.setPassword(authPostgres.getPassword());
        authDataSource = authDs;

        // User Service DataSourceの設定
        PGSimpleDataSource userDs = new PGSimpleDataSource();
        userDs.setURL(userPostgres.getJdbcUrl());
        userDs.setUser(userPostgres.getUsername());
        userDs.setPassword(userPostgres.getPassword());
        userDataSource = userDs;

        // システムプロパティを設定（AuthRepositoryがUser Serviceに接続するため）
        System.setProperty("USER_SERVICE_DB_HOST", userPostgres.getHost());
        System.setProperty("USER_SERVICE_DB_PORT", String.valueOf(userPostgres.getFirstMappedPort()));
        System.setProperty("USER_SERVICE_DB_NAME", userPostgres.getDatabaseName());
        System.setProperty("USER_SERVICE_DB_USER", userPostgres.getUsername());
        System.setProperty("USER_SERVICE_DB_PASSWORD", userPostgres.getPassword());

        // AuthRepositoryのインスタンス化とDataSourceの注入
        authRepository = new AuthRepository();
        Field dataSourceField = AuthRepository.class.getDeclaredField("dataSource");
        dataSourceField.setAccessible(true);
        dataSourceField.set(authRepository, authDataSource);

        // スキーマとシードデータの投入
        initializeDatabase();
    }

    /**
     * データベーススキーマとシードデータの初期化
     */
    private static void initializeDatabase() throws Exception {
        // User Serviceデータベース初期化
        try (Connection conn = userDataSource.getConnection()) {
            // スキーマ作成
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                        "id UUID PRIMARY KEY," +
                        "username VARCHAR(50) UNIQUE NOT NULL," +
                        "email VARCHAR(100) UNIQUE NOT NULL," +
                        "full_name VARCHAR(100) NOT NULL," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_users_username ON users(username)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_users_email ON users(email)");
            }

            // シードデータ挿入
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO users (id, username, email, full_name) VALUES (?, ?, ?, ?)")) {
                // User 1
                pstmt.setObject(1, UUID.fromString("05c66ceb-6ddc-4ada-b736-08702615ff48"));
                pstmt.setString(2, "tanaka_taro");
                pstmt.setString(3, "tanaka.taro@example.com");
                pstmt.setString(4, "田中太郎");
                pstmt.addBatch();

                // User 2
                pstmt.setObject(1, UUID.fromString("4f4777e4-dd9c-4d5b-a928-19a59b1d3ead"));
                pstmt.setString(2, "suzuki_hanako");
                pstmt.setString(3, "suzuki.hanako@example.com");
                pstmt.setString(4, "鈴木花子");
                pstmt.addBatch();

                // User 3
                pstmt.setObject(1, UUID.fromString("7bd6e35b-9c8e-4635-a47d-f7adce5c8ed9"));
                pstmt.setString(2, "yamada_jiro");
                pstmt.setString(3, "yamada.jiro@example.com");
                pstmt.setString(4, "山田次郎");
                pstmt.addBatch();

                // User 4
                pstmt.setObject(1, UUID.fromString("233c99d5-41ba-42f3-89fa-eb34644fe3b5"));
                pstmt.setString(2, "sato_yuki");
                pstmt.setString(3, "sato.yuki@example.com");
                pstmt.setString(4, "佐藤優希");
                pstmt.addBatch();

                // User 5
                pstmt.setObject(1, UUID.fromString("8a17f2c2-c1c8-4fee-ae95-8a483127bf1f"));
                pstmt.setString(2, "takahashi_mai");
                pstmt.setString(3, "takahashi.mai@example.com");
                pstmt.setString(4, "高橋舞");
                pstmt.addBatch();

                pstmt.executeBatch();
            }
        }

        // Auth Serviceデータベース初期化
        try (Connection conn = authDataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            String schemaSql = loadResourceFile("/database/schema.sql");
            schemaSql = schemaSql.replaceAll("\\\\c\\s+\\w+;", "");
            stmt.execute(schemaSql);

            String seedSql = loadResourceFile("/database/seed.sql");
            seedSql = seedSql.replaceAll("\\\\c\\s+\\w+;", "");
            stmt.execute(seedSql);
        }
    }

    /**
     * リソースファイルの読み込み
     */
    private static String loadResourceFile(String path) throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(AuthRepositoryIT.class.getResourceAsStream(path)))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    @Test
    @Order(1)
    @DisplayName("ユーザー名からユーザーIDを取得できること")
    void testGetUserIdByUsername() throws SQLException {
        // 実行
        Optional<UUID> userIdOpt = authRepository.getUserIdByUsername("tanaka_taro");

        // 検証
        assertTrue(userIdOpt.isPresent(), "ユーザーIDが取得できること");
        assertEquals(UUID.fromString("05c66ceb-6ddc-4ada-b736-08702615ff48"), userIdOpt.get());
    }

    @Test
    @Order(2)
    @DisplayName("存在しないユーザー名で検索した場合はEmptyが返ること")
    void testGetUserIdByUsernameNotFound() throws SQLException {
        // 実行
        Optional<UUID> userIdOpt = authRepository.getUserIdByUsername("nonexistent_user");

        // 検証
        assertFalse(userIdOpt.isPresent(), "ユーザーIDが見つからないこと");
    }

    @Test
    @Order(3)
    @DisplayName("ユーザーIDからユーザー名を取得できること")
    void testGetUsernameByUserId() throws SQLException {
        // 準備
        UUID userId = UUID.fromString("4f4777e4-dd9c-4d5b-a928-19a59b1d3ead");

        // 実行
        Optional<String> usernameOpt = authRepository.getUsernameByUserId(userId);

        // 検証
        assertTrue(usernameOpt.isPresent(), "ユーザー名が取得できること");
        assertEquals("suzuki_hanako", usernameOpt.get());
    }

    @Test
    @Order(4)
    @DisplayName("存在しないユーザーIDで検索した場合はEmptyが返ること")
    void testGetUsernameByUserIdNotFound() throws SQLException {
        // 準備
        UUID nonExistentId = UUID.randomUUID();

        // 実行
        Optional<String> usernameOpt = authRepository.getUsernameByUserId(nonExistentId);

        // 検証
        assertFalse(usernameOpt.isPresent(), "ユーザー名が見つからないこと");
    }

    @Test
    @Order(5)
    @DisplayName("パスワードハッシュを取得できること")
    void testGetPasswordHash() throws SQLException {
        // 準備
        UUID userId = UUID.fromString("05c66ceb-6ddc-4ada-b736-08702615ff48");

        // 実行
        Optional<String> passwordHashOpt = authRepository.getPasswordHash(userId);

        // 検証
        assertTrue(passwordHashOpt.isPresent(), "パスワードハッシュが取得できること");
        assertTrue(passwordHashOpt.get().startsWith("$2a$"), "BCryptハッシュであること");
    }

    @Test
    @Order(6)
    @DisplayName("存在しないユーザーIDでパスワードハッシュ検索した場合はEmptyが返ること")
    void testGetPasswordHashNotFound() throws SQLException {
        // 準備
        UUID nonExistentId = UUID.randomUUID();

        // 実行
        Optional<String> passwordHashOpt = authRepository.getPasswordHash(nonExistentId);

        // 検証
        assertFalse(passwordHashOpt.isPresent(), "パスワードハッシュが見つからないこと");
    }

    @Test
    @Order(7)
    @DisplayName("認証情報の検証が正しく動作すること")
    void testVerifyCredentials() throws SQLException {
        // 準備
        UUID userId = UUID.fromString("05c66ceb-6ddc-4ada-b736-08702615ff48");
        String correctHash = "$2a$10$OSuuVFLoafV6AKzzptdQSeGXQIqx0rU53gtZvYwZ07Des/5txWC6q";

        // 実行と検証
        assertTrue(authRepository.verifyCredentials(userId, correctHash), "正しい認証情報で検証成功");
        
        // 誤ったハッシュでの検証
        String wrongHash = "$2a$10$wronghash";
        assertFalse(authRepository.verifyCredentials(userId, wrongHash), "誤った認証情報で検証失敗");
    }

    @Test
    @Order(8)
    @DisplayName("セッショントークンを作成できること")
    void testCreateSessionToken() throws SQLException {
        // 準備
        UUID userId = UUID.fromString("7bd6e35b-9c8e-4635-a47d-f7adce5c8ed9");
        SessionToken token = new SessionToken();
        token.setUserId(userId);
        token.setToken("test_token_123456");
        token.setExpiresAt(LocalDateTime.now().plusDays(7));

        // 実行
        SessionToken createdToken = authRepository.createSessionToken(token);

        // 検証
        assertNotNull(createdToken.getId(), "IDが自動生成されること");
        assertEquals(userId, createdToken.getUserId());
        assertEquals("test_token_123456", createdToken.getToken());
        assertNotNull(createdToken.getCreatedAt(), "作成日時が設定されること");
        assertNotNull(createdToken.getExpiresAt(), "有効期限が設定されること");
    }

    @Test
    @Order(9)
    @DisplayName("トークンからセッション情報を取得できること")
    void testFindSessionByToken() throws SQLException {
        // 準備: 既存のトークン
        String token = "token_tanaka_123456";

        // 実行
        Optional<SessionToken> tokenOpt = authRepository.findSessionByToken(token);

        // 検証
        assertTrue(tokenOpt.isPresent(), "トークンが見つかること");
        SessionToken sessionToken = tokenOpt.get();
        assertEquals(UUID.fromString("05c66ceb-6ddc-4ada-b736-08702615ff48"), sessionToken.getUserId());
        assertEquals(token, sessionToken.getToken());
    }

    @Test
    @Order(10)
    @DisplayName("存在しないトークンで検索した場合はEmptyが返ること")
    void testFindSessionByTokenNotFound() throws SQLException {
        // 実行
        Optional<SessionToken> tokenOpt = authRepository.findSessionByToken("nonexistent_token");

        // 検証
        assertFalse(tokenOpt.isPresent(), "トークンが見つからないこと");
    }

    @Test
    @Order(11)
    @DisplayName("セッショントークンを削除できること")
    void testDeleteSession() throws SQLException {
        // 準備: まず新規トークンを作成
        UUID userId = UUID.fromString("233c99d5-41ba-42f3-89fa-eb34644fe3b5");
        SessionToken token = new SessionToken();
        token.setUserId(userId);
        token.setToken("delete_test_token");
        token.setExpiresAt(LocalDateTime.now().plusDays(7));
        SessionToken createdToken = authRepository.createSessionToken(token);

        // 削除前に存在確認
        assertTrue(authRepository.findSessionByToken("delete_test_token").isPresent(), 
                   "削除前はトークンが存在すること");

        // 実行
        authRepository.deleteSession("delete_test_token");

        // 検証
        assertFalse(authRepository.findSessionByToken("delete_test_token").isPresent(),
                    "削除後はトークンが存在しないこと");
    }

    @Test
    @Order(12)
    @DisplayName("ログイン履歴を記録できること")
    void testRecordLoginHistory() throws SQLException {
        // 準備
        UUID userId = UUID.fromString("8a17f2c2-c1c8-4fee-ae95-8a483127bf1f");
        String ipAddress = "192.168.1.200";
        String userAgent = "Test Browser";
        boolean success = true;

        // 実行
        assertDoesNotThrow(() -> {
            authRepository.recordLoginHistory(userId, ipAddress, userAgent, success);
        }, "ログイン履歴の記録が成功すること");
    }

    @Test
    @Order(13)
    @DisplayName("期限切れトークンを削除できること")
    void testCleanupExpiredSessions() throws SQLException {
        // 準備: 期限切れトークンを作成
        UUID userId = UUID.fromString("05c66ceb-6ddc-4ada-b736-08702615ff48");
        SessionToken expiredToken = new SessionToken();
        expiredToken.setUserId(userId);
        expiredToken.setToken("expired_token_test");
        expiredToken.setExpiresAt(LocalDateTime.now().minusDays(1)); // 既に期限切れ
        authRepository.createSessionToken(expiredToken);

        // 実行
        assertDoesNotThrow(() -> {
            authRepository.cleanupExpiredSessions();
        }, "期限切れトークンの削除が成功すること");
        
        // 検証
        assertFalse(authRepository.findSessionByToken("expired_token_test").isPresent(),
                    "期限切れトークンが削除されていること");
    }
}
