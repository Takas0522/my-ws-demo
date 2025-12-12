package com.example.microservices.point.repository;

import com.example.microservices.point.model.Point;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.Optional;
import java.util.UUID;

import org.postgresql.ds.PGSimpleDataSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PointRepository統合テスト
 * TestContainersを使用してPostgreSQLコンテナでテストを実行
 */
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PointRepositoryIT {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:15-alpine")
    )
            .withDatabaseName("point_service_db")
            .withUsername("testuser")
            .withPassword("testpass");

    private static PointRepository pointRepository;
    private static DataSource dataSource;

    @BeforeAll
    static void setUp() throws Exception {
        // DataSourceの設定
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setURL(postgres.getJdbcUrl());
        ds.setUser(postgres.getUsername());
        ds.setPassword(postgres.getPassword());
        dataSource = ds;

        // PointRepositoryのインスタンス化とDataSourceの注入
        pointRepository = new PointRepository();
        Field dataSourceField = PointRepository.class.getDeclaredField("dataSource");
        dataSourceField.setAccessible(true);
        dataSourceField.set(pointRepository, dataSource);

        // スキーマとシードデータの投入
        initializeDatabase();
    }

    /**
     * データベーススキーマとシードデータの初期化
     */
    private static void initializeDatabase() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // pointsテーブル作成
            stmt.execute("CREATE TABLE IF NOT EXISTS points (" +
                    "user_id UUID PRIMARY KEY," +
                    "balance INTEGER NOT NULL DEFAULT 0," +
                    "last_updated TIMESTAMP DEFAULT NOW())");

            // point_historyテーブル作成
            stmt.execute("CREATE TABLE IF NOT EXISTS point_history (" +
                    "id SERIAL PRIMARY KEY," +
                    "user_id UUID NOT NULL," +
                    "amount INTEGER NOT NULL," +
                    "transaction_type VARCHAR(20) NOT NULL," +
                    "description TEXT," +
                    "created_at TIMESTAMP DEFAULT NOW()," +
                    "expires_at TIMESTAMP," +
                    "FOREIGN KEY (user_id) REFERENCES points(user_id) ON DELETE CASCADE)");

            // インデックス作成
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_point_history_user_id ON point_history(user_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_point_history_created_at ON point_history(created_at)");

            // Seed data
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO points (user_id, balance, last_updated) VALUES (?, ?, NOW())")) {
                
                pstmt.setObject(1, UUID.fromString("05c66ceb-6ddc-4ada-b736-08702615ff48"));
                pstmt.setInt(2, 1500);
                pstmt.addBatch();

                pstmt.setObject(1, UUID.fromString("4f4777e4-dd9c-4d5b-a928-19a59b1d3ead"));
                pstmt.setInt(2, 3200);
                pstmt.addBatch();

                pstmt.setObject(1, UUID.fromString("7bd6e35b-9c8e-4635-a47d-f7adce5c8ed9"));
                pstmt.setInt(2, 500);
                pstmt.addBatch();

                pstmt.setObject(1, UUID.fromString("233c99d5-41ba-42f3-89fa-eb34644fe3b5"));
                pstmt.setInt(2, 2100);
                pstmt.addBatch();

                pstmt.setObject(1, UUID.fromString("8a17f2c2-c1c8-4fee-ae95-8a483127bf1f"));
                pstmt.setInt(2, 750);
                pstmt.addBatch();

                pstmt.executeBatch();
            }
        }
    }

    @Test
    @Order(1)
    @DisplayName("ユーザーIDでポイント残高を取得できること")
    void testFindByUserId() throws SQLException {
        // 準備
        UUID userId = UUID.fromString("05c66ceb-6ddc-4ada-b736-08702615ff48");

        // 実行
        Optional<Point> pointOpt = pointRepository.findByUserId(userId);

        // 検証
        assertTrue(pointOpt.isPresent(), "ポイント情報が見つかること");
        Point point = pointOpt.get();
        assertEquals(userId, point.getUserId());
        assertEquals(1500, point.getBalance());
        assertNotNull(point.getLastUpdated(), "最終更新日時が設定されること");
    }

    @Test
    @Order(2)
    @DisplayName("存在しないユーザーIDで検索した場合はEmptyが返ること")
    void testFindByUserIdNotFound() throws SQLException {
        // 準備
        UUID nonExistentId = UUID.randomUUID();

        // 実行
        Optional<Point> pointOpt = pointRepository.findByUserId(nonExistentId);

        // 検証
        assertFalse(pointOpt.isPresent(), "ポイント情報が見つからないこと");
    }

    @Test
    @Order(3)
    @DisplayName("ポイント残高を更新できること")
    void testUpdateBalance() throws SQLException {
        // 準備
        UUID userId = UUID.fromString("4f4777e4-dd9c-4d5b-a928-19a59b1d3ead");
        Integer newBalance = 4000;

        // 実行
        Point updatedPoint = pointRepository.updateBalance(userId, newBalance);

        // 検証
        assertEquals(userId, updatedPoint.getUserId());
        assertEquals(newBalance, updatedPoint.getBalance());
        assertNotNull(updatedPoint.getLastUpdated(), "最終更新日時が設定されること");

        // データベースから取得して確認
        Optional<Point> pointOpt = pointRepository.findByUserId(userId);
        assertTrue(pointOpt.isPresent());
        assertEquals(newBalance, pointOpt.get().getBalance());
    }

    @Test
    @Order(4)
    @DisplayName("存在しないユーザーの残高を更新しようとした場合はエラーになること")
    void testUpdateBalanceNotFound() {
        // 準備
        UUID nonExistentId = UUID.randomUUID();
        Integer newBalance = 1000;

        // 実行と検証
        assertThrows(SQLException.class, () -> {
            pointRepository.updateBalance(nonExistentId, newBalance);
        }, "存在しないユーザーの残高更新は失敗すること");
    }

    @Test
    @Order(5)
    @DisplayName("新規ポイント残高を作成できること")
    void testCreate() throws SQLException {
        // 準備
        UUID newUserId = UUID.randomUUID();
        Integer initialBalance = 1000;

        // 実行
        Point createdPoint = pointRepository.create(newUserId, initialBalance);

        // 検証
        assertEquals(newUserId, createdPoint.getUserId());
        assertEquals(initialBalance, createdPoint.getBalance());
        assertNotNull(createdPoint.getLastUpdated(), "最終更新日時が設定されること");

        // データベースから取得して確認
        Optional<Point> pointOpt = pointRepository.findByUserId(newUserId);
        assertTrue(pointOpt.isPresent(), "作成したポイント残高が取得できること");
        assertEquals(initialBalance, pointOpt.get().getBalance());
    }

    @Test
    @Order(6)
    @DisplayName("重複するユーザーIDでポイント作成はエラーになること")
    void testCreateDuplicateUserId() {
        // 準備
        UUID existingUserId = UUID.fromString("7bd6e35b-9c8e-4635-a47d-f7adce5c8ed9");
        Integer balance = 1000;

        // 実行と検証
        assertThrows(SQLException.class, () -> {
            pointRepository.create(existingUserId, balance);
        }, "重複するユーザーIDでの作成は失敗すること");
    }

    @Test
    @Order(7)
    @DisplayName("負のポイント残高を設定できること（マイナスポイント許容）")
    void testUpdateBalanceNegative() throws SQLException {
        // 準備
        UUID userId = UUID.fromString("233c99d5-41ba-42f3-89fa-eb34644fe3b5");
        Integer negativeBalance = -100;

        // 実行
        Point updatedPoint = pointRepository.updateBalance(userId, negativeBalance);

        // 検証
        assertEquals(negativeBalance, updatedPoint.getBalance());

        // データベースから取得して確認
        Optional<Point> pointOpt = pointRepository.findByUserId(userId);
        assertTrue(pointOpt.isPresent());
        assertEquals(negativeBalance, pointOpt.get().getBalance());
    }

    @Test
    @Order(8)
    @DisplayName("ゼロポイント残高を設定できること")
    void testUpdateBalanceZero() throws SQLException {
        // 準備
        UUID userId = UUID.fromString("8a17f2c2-c1c8-4fee-ae95-8a483127bf1f");
        Integer zeroBalance = 0;

        // 実行
        Point updatedPoint = pointRepository.updateBalance(userId, zeroBalance);

        // 検証
        assertEquals(zeroBalance, updatedPoint.getBalance());

        // データベースから取得して確認
        Optional<Point> pointOpt = pointRepository.findByUserId(userId);
        assertTrue(pointOpt.isPresent());
        assertEquals(zeroBalance, pointOpt.get().getBalance());
    }
}
