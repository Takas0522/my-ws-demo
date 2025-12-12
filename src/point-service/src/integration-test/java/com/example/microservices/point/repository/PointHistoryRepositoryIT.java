package com.example.microservices.point.repository;

import com.example.microservices.point.model.PointHistory;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.postgresql.ds.PGSimpleDataSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PointHistoryRepository統合テスト
 * TestContainersを使用してPostgreSQLコンテナでテストを実行
 */
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PointHistoryRepositoryIT {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:15-alpine")
    )
            .withDatabaseName("point_service_db")
            .withUsername("testuser")
            .withPassword("testpass");

    private static PointHistoryRepository pointHistoryRepository;
    private static DataSource dataSource;
    private static final UUID TEST_USER_ID = UUID.fromString("05c66ceb-6ddc-4ada-b736-08702615ff48");

    @BeforeAll
    static void setUp() throws Exception {
        // DataSourceの設定
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setURL(postgres.getJdbcUrl());
        ds.setUser(postgres.getUsername());
        ds.setPassword(postgres.getPassword());
        dataSource = ds;

        // PointHistoryRepositoryのインスタンス化とDataSourceの注入
        pointHistoryRepository = new PointHistoryRepository();
        Field dataSourceField = PointHistoryRepository.class.getDeclaredField("dataSource");
        dataSourceField.setAccessible(true);
        dataSourceField.set(pointHistoryRepository, dataSource);

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

            // points seed data
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO points (user_id, balance) VALUES (?, ?)")) {
                pstmt.setObject(1, TEST_USER_ID);
                pstmt.setInt(2, 1500);
                pstmt.executeUpdate();
            }

            // point_history seed data
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO point_history (user_id, amount, transaction_type, description, created_at, expires_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?)")) {
                
                // 履歴1
                pstmt.setObject(1, TEST_USER_ID);
                pstmt.setInt(2, 1000);
                pstmt.setString(3, "EARN");
                pstmt.setString(4, "新規登録ボーナス");
                pstmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now().minusDays(30)));
                pstmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now().plusDays(150)));
                pstmt.addBatch();

                // 履歴2
                pstmt.setObject(1, TEST_USER_ID);
                pstmt.setInt(2, 500);
                pstmt.setString(3, "EARN");
                pstmt.setString(4, "購入特典");
                pstmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now().minusDays(10)));
                pstmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now().plusDays(170)));
                pstmt.addBatch();

                pstmt.executeBatch();
            }
        }
    }

    @Test
    @Order(1)
    @DisplayName("ユーザーIDでポイント履歴を取得できること")
    void testFindByUserId() throws SQLException {
        // 実行
        List<PointHistory> histories = pointHistoryRepository.findByUserId(TEST_USER_ID, 1, 10);

        // 検証
        assertNotNull(histories);
        assertEquals(2, histories.size(), "シードデータから2件の履歴が取得できること");
        
        // 新しい履歴が先に来ることを確認（DESC順）
        PointHistory firstHistory = histories.get(0);
        assertEquals(TEST_USER_ID, firstHistory.getUserId());
        assertEquals(500, firstHistory.getAmount());
        assertEquals("EARN", firstHistory.getTransactionType());
        assertEquals("購入特典", firstHistory.getDescription());
    }

    @Test
    @Order(2)
    @DisplayName("ページネーションが正しく動作すること")
    void testFindByUserIdPagination() throws SQLException {
        // ページ1
        List<PointHistory> page1 = pointHistoryRepository.findByUserId(TEST_USER_ID, 1, 1);
        assertEquals(1, page1.size());
        assertEquals(500, page1.get(0).getAmount());

        // ページ2
        List<PointHistory> page2 = pointHistoryRepository.findByUserId(TEST_USER_ID, 2, 1);
        assertEquals(1, page2.size());
        assertEquals(1000, page2.get(0).getAmount());
    }

    @Test
    @Order(3)
    @DisplayName("存在しないユーザーIDで検索した場合は空のリストが返ること")
    void testFindByUserIdNotFound() throws SQLException {
        // 準備
        UUID nonExistentId = UUID.randomUUID();

        // 実行
        List<PointHistory> histories = pointHistoryRepository.findByUserId(nonExistentId, 1, 10);

        // 検証
        assertNotNull(histories);
        assertTrue(histories.isEmpty(), "履歴が見つからないこと");
    }

    @Test
    @Order(4)
    @DisplayName("ユーザーの履歴総数を取得できること")
    void testCountByUserId() throws SQLException {
        // 実行
        int count = pointHistoryRepository.countByUserId(TEST_USER_ID);

        // 検証
        assertEquals(2, count, "シードデータから2件の履歴があること");
    }

    @Test
    @Order(5)
    @DisplayName("存在しないユーザーIDの履歴総数は0になること")
    void testCountByUserIdNotFound() throws SQLException {
        // 準備
        UUID nonExistentId = UUID.randomUUID();

        // 実行
        int count = pointHistoryRepository.countByUserId(nonExistentId);

        // 検証
        assertEquals(0, count, "履歴が見つからないこと");
    }

    @Test
    @Order(6)
    @DisplayName("EARN取引タイプの履歴を作成できること")
    void testCreateEarnHistory() throws SQLException {
        // 準備
        PointHistory history = new PointHistory();
        history.setUserId(TEST_USER_ID);
        history.setAmount(300);
        history.setTransactionType("EARN");
        history.setDescription("キャンペーン特典");
        history.setExpiresAt(LocalDateTime.now().plusDays(180));

        // 実行
        PointHistory createdHistory = pointHistoryRepository.create(history);

        // 検証
        assertNotNull(createdHistory.getId(), "IDが自動生成されること");
        assertEquals(TEST_USER_ID, createdHistory.getUserId());
        assertEquals(300, createdHistory.getAmount());
        assertEquals("EARN", createdHistory.getTransactionType());
        assertEquals("キャンペーン特典", createdHistory.getDescription());
        assertNotNull(createdHistory.getCreatedAt(), "作成日時が設定されること");
        assertNotNull(createdHistory.getExpiresAt(), "有効期限が設定されること");

        // データベースから総数を確認
        int count = pointHistoryRepository.countByUserId(TEST_USER_ID);
        assertEquals(3, count, "履歴が増えていること");
    }

    @Test
    @Order(7)
    @DisplayName("USE取引タイプの履歴を作成できること")
    void testCreateUseHistory() throws SQLException {
        // 準備
        PointHistory history = new PointHistory();
        history.setUserId(TEST_USER_ID);
        history.setAmount(200);
        history.setTransactionType("USE");
        history.setDescription("商品購入");
        history.setExpiresAt(null); // USE取引は有効期限なし

        // 実行
        PointHistory createdHistory = pointHistoryRepository.create(history);

        // 検証
        assertNotNull(createdHistory.getId());
        assertEquals(TEST_USER_ID, createdHistory.getUserId());
        assertEquals(200, createdHistory.getAmount());
        assertEquals("USE", createdHistory.getTransactionType());
        assertEquals("商品購入", createdHistory.getDescription());
        assertNotNull(createdHistory.getCreatedAt());
        assertNull(createdHistory.getExpiresAt(), "USE取引は有効期限がないこと");
    }

    @Test
    @Order(8)
    @DisplayName("created_atを指定して履歴を作成できること")
    void testCreateWithSpecificCreatedAt() throws SQLException {
        // 準備
        LocalDateTime specificTime = LocalDateTime.now().minusDays(5);
        PointHistory history = new PointHistory();
        history.setUserId(TEST_USER_ID);
        history.setAmount(100);
        history.setTransactionType("EARN");
        history.setDescription("テスト");
        history.setCreatedAt(specificTime);

        // 実行
        PointHistory createdHistory = pointHistoryRepository.create(history);

        // 検証
        assertNotNull(createdHistory.getCreatedAt());
        // 作成日時が指定した時刻の近くにあることを確認（ミリ秒の誤差を考慮）
        assertTrue(Math.abs(java.time.Duration.between(specificTime, createdHistory.getCreatedAt()).toSeconds()) < 2);
    }
}
