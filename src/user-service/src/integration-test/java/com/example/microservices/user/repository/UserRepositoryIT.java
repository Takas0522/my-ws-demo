package com.example.microservices.user.repository;

import com.example.microservices.user.model.User;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.postgresql.ds.PGSimpleDataSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserRepository統合テスト
 * TestContainersを使用してPostgreSQLコンテナでテストを実行
 */
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserRepositoryIT {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:15-alpine")
    )
            .withDatabaseName("user_service_db")
            .withUsername("testuser")
            .withPassword("testpass");

    private static UserRepository userRepository;
    private static DataSource dataSource;

    @BeforeAll
    static void setUp() throws Exception {
        // DataSourceの設定
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setURL(postgres.getJdbcUrl());
        ds.setUser(postgres.getUsername());
        ds.setPassword(postgres.getPassword());
        dataSource = ds;

        // UserRepositoryのインスタンス化とDataSourceの注入
        userRepository = new UserRepository();
        Field dataSourceField = UserRepository.class.getDeclaredField("dataSource");
        dataSourceField.setAccessible(true);
        dataSourceField.set(userRepository, dataSource);

        // スキーマとシードデータの投入
        initializeDatabase();
    }

    /**
     * データベーススキーマとシードデータの初期化
     */
    private static void initializeDatabase() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // スキーマSQL読み込みと実行
            String schemaSql = loadResourceFile("/database/schema.sql");
            // \c コマンド削除（TestContainersでは不要）
            schemaSql = schemaSql.replaceAll("\\\\c\\s+\\w+;", "");
            stmt.execute(schemaSql);

            // シードデータSQL読み込みと実行
            String seedSql = loadResourceFile("/database/seed.sql");
            // \c コマンド削除
            seedSql = seedSql.replaceAll("\\\\c\\s+\\w+;", "");
            stmt.execute(seedSql);
        }
    }

    /**
     * リソースファイルの読み込み
     */
    private static String loadResourceFile(String path) throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(UserRepositoryIT.class.getResourceAsStream(path)))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    @Test
    @Order(1)
    @DisplayName("全ユーザーを取得できること")
    void testFindAll() throws SQLException {
        // 実行
        List<User> users = userRepository.findAll();

        // 検証
        assertNotNull(users);
        assertEquals(5, users.size(), "シードデータから5件のユーザーが取得できること");
        
        // 最初のユーザーの確認
        User firstUser = users.get(0);
        assertNotNull(firstUser.getId());
        assertNotNull(firstUser.getUsername());
        assertNotNull(firstUser.getEmail());
        assertNotNull(firstUser.getFullName());
    }

    @Test
    @Order(2)
    @DisplayName("IDでユーザーを取得できること")
    void testFindById() throws SQLException {
        // 準備
        UUID userId = UUID.fromString("05c66ceb-6ddc-4ada-b736-08702615ff48");

        // 実行
        Optional<User> userOpt = userRepository.findById(userId);

        // 検証
        assertTrue(userOpt.isPresent(), "ユーザーが見つかること");
        User user = userOpt.get();
        assertEquals(userId, user.getId());
        assertEquals("tanaka_taro", user.getUsername());
        assertEquals("tanaka.taro@example.com", user.getEmail());
        assertEquals("田中太郎", user.getFullName());
    }

    @Test
    @Order(3)
    @DisplayName("存在しないIDで検索した場合はEmptyが返ること")
    void testFindByIdNotFound() throws SQLException {
        // 準備
        UUID nonExistentId = UUID.randomUUID();

        // 実行
        Optional<User> userOpt = userRepository.findById(nonExistentId);

        // 検証
        assertFalse(userOpt.isPresent(), "ユーザーが見つからないこと");
    }

    @Test
    @Order(4)
    @DisplayName("ユーザー名でユーザーを取得できること")
    void testFindByUsername() throws SQLException {
        // 準備
        String username = "suzuki_hanako";

        // 実行
        Optional<User> userOpt = userRepository.findByUsername(username);

        // 検証
        assertTrue(userOpt.isPresent(), "ユーザーが見つかること");
        User user = userOpt.get();
        assertEquals(username, user.getUsername());
        assertEquals("suzuki.hanako@example.com", user.getEmail());
        assertEquals("鈴木花子", user.getFullName());
    }

    @Test
    @Order(5)
    @DisplayName("存在しないユーザー名で検索した場合はEmptyが返ること")
    void testFindByUsernameNotFound() throws SQLException {
        // 準備
        String nonExistentUsername = "nonexistent_user";

        // 実行
        Optional<User> userOpt = userRepository.findByUsername(nonExistentUsername);

        // 検証
        assertFalse(userOpt.isPresent(), "ユーザーが見つからないこと");
    }

    @Test
    @Order(6)
    @DisplayName("新規ユーザーを作成できること")
    void testCreate() throws SQLException {
        // 準備
        User newUser = new User();
        newUser.setUsername("test_user");
        newUser.setEmail("test.user@example.com");
        newUser.setFullName("テストユーザー");

        // 実行
        User createdUser = userRepository.create(newUser);

        // 検証
        assertNotNull(createdUser.getId(), "IDが自動生成されること");
        assertEquals("test_user", createdUser.getUsername());
        assertEquals("test.user@example.com", createdUser.getEmail());
        assertEquals("テストユーザー", createdUser.getFullName());
        assertNotNull(createdUser.getCreatedAt(), "作成日時が設定されること");
        assertNotNull(createdUser.getUpdatedAt(), "更新日時が設定されること");

        // データベースから取得して確認
        Optional<User> foundUser = userRepository.findById(createdUser.getId());
        assertTrue(foundUser.isPresent(), "作成したユーザーが取得できること");
        assertEquals(createdUser.getUsername(), foundUser.get().getUsername());
    }

    @Test
    @Order(7)
    @DisplayName("ユーザー情報を更新できること")
    void testUpdate() throws SQLException {
        // 準備: まず新規ユーザーを作成
        User user = new User();
        user.setUsername("update_test_user");
        user.setEmail("update@example.com");
        user.setFullName("更新テスト");
        User createdUser = userRepository.create(user);

        // 更新
        createdUser.setUsername("updated_user");
        createdUser.setEmail("updated@example.com");
        createdUser.setFullName("更新後ユーザー");

        // 実行
        User updatedUser = userRepository.update(createdUser);

        // 検証
        assertEquals("updated_user", updatedUser.getUsername());
        assertEquals("updated@example.com", updatedUser.getEmail());
        assertEquals("更新後ユーザー", updatedUser.getFullName());
        assertNotNull(updatedUser.getUpdatedAt(), "更新日時が設定されること");

        // データベースから取得して確認
        Optional<User> foundUser = userRepository.findById(createdUser.getId());
        assertTrue(foundUser.isPresent());
        assertEquals("updated_user", foundUser.get().getUsername());
    }

    @Test
    @Order(8)
    @DisplayName("ユーザーを削除できること")
    void testDelete() throws SQLException {
        // 準備: まず新規ユーザーを作成
        User user = new User();
        user.setUsername("delete_test_user");
        user.setEmail("delete@example.com");
        user.setFullName("削除テスト");
        User createdUser = userRepository.create(user);
        UUID userId = createdUser.getId();

        // 削除前に存在確認
        assertTrue(userRepository.findById(userId).isPresent(), "削除前はユーザーが存在すること");

        // 実行
        userRepository.delete(userId);

        // 検証
        Optional<User> deletedUser = userRepository.findById(userId);
        assertFalse(deletedUser.isPresent(), "削除後はユーザーが存在しないこと");
    }

    @Test
    @Order(9)
    @DisplayName("重複するユーザー名での作成はエラーになること")
    void testCreateDuplicateUsername() {
        // 準備
        User user = new User();
        user.setUsername("tanaka_taro"); // 既存のユーザー名
        user.setEmail("duplicate@example.com");
        user.setFullName("重複テスト");

        // 実行と検証
        assertThrows(SQLException.class, () -> {
            userRepository.create(user);
        }, "重複するユーザー名での作成は失敗すること");
    }

    @Test
    @Order(10)
    @DisplayName("重複するメールアドレスでの作成はエラーになること")
    void testCreateDuplicateEmail() {
        // 準備
        User user = new User();
        user.setUsername("unique_username");
        user.setEmail("tanaka.taro@example.com"); // 既存のメールアドレス
        user.setFullName("重複テスト");

        // 実行と検証
        assertThrows(SQLException.class, () -> {
            userRepository.create(user);
        }, "重複するメールアドレスでの作成は失敗すること");
    }
}
