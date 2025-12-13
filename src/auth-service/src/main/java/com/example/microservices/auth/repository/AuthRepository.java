package com.example.microservices.auth.repository;

import com.example.microservices.auth.model.SessionToken;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;
import java.util.UUID;

/**
 * 認証リポジトリ
 */
@ApplicationScoped
public class AuthRepository {

    @Resource(lookup = "java:app/jdbc/authServiceDB")
    private DataSource dataSource;

    static {
        try {
            // PostgreSQLドライバーを明示的にロード
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL JDBC Driver not found", e);
        }
    }

    /**
     * 設定値を取得（システムプロパティ優先、次に環境変数、最後にデフォルト値）
     * テスト時にはシステムプロパティで動的な値を設定可能
     */
    private String getConfigValue(String key, String defaultValue) {
        // システムプロパティを優先（テスト用）
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.isEmpty()) {
            return sysProp;
        }
        // 次に環境変数
        String envVar = System.getenv(key);
        if (envVar != null && !envVar.isEmpty()) {
            return envVar;
        }
        // デフォルト値
        return defaultValue;
    }

    /**
     * ユーザー名からユーザーIDを取得
     */
    public Optional<UUID> getUserIdByUsername(String username) throws SQLException {
        // ユーザーサービスのデータベースに接続してユーザーIDを取得
        // 本来はマイクロサービス間通信で取得すべきですが、簡易的にDB直接接続
        // 設定値を取得（システムプロパティまたは環境変数から）
        String host = getConfigValue("USER_SERVICE_DB_HOST", "localhost");
        String port = getConfigValue("USER_SERVICE_DB_PORT", "5432");
        String dbName = getConfigValue("USER_SERVICE_DB_NAME", "user_service_db");
        String dbUser = getConfigValue("USER_SERVICE_DB_USER", "postgres");
        String dbPassword = getConfigValue("USER_SERVICE_DB_PASSWORD", "postgres");
        
        String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", host, port, dbName);
        String sql = "SELECT id FROM users WHERE username = ?";
        
        try (Connection conn = java.sql.DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of((UUID) rs.getObject("id"));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * ユーザーIDからユーザー名を取得
     */
    public Optional<String> getUsernameByUserId(UUID userId) throws SQLException {
        // ユーザーサービスのデータベースに接続してユーザー名を取得
        // 設定値を取得（システムプロパティまたは環境変数から）
        String host = getConfigValue("USER_SERVICE_DB_HOST", "localhost");
        String port = getConfigValue("USER_SERVICE_DB_PORT", "5432");
        String dbName = getConfigValue("USER_SERVICE_DB_NAME", "user_service_db");
        String dbUser = getConfigValue("USER_SERVICE_DB_USER", "postgres");
        String dbPassword = getConfigValue("USER_SERVICE_DB_PASSWORD", "postgres");
        
        String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", host, port, dbName);
        String sql = "SELECT username FROM users WHERE id = ?";
        
        try (Connection conn = java.sql.DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getString("username"));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * ユーザー認証情報の検証
     */
    public boolean verifyCredentials(UUID userId, String passwordHash) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user_credentials WHERE user_id = ? AND password_hash = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, userId);
            stmt.setString(2, passwordHash);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * パスワードハッシュ取得
     */
    public Optional<String> getPasswordHash(UUID userId) throws SQLException {
        String sql = "SELECT password_hash FROM user_credentials WHERE user_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getString("password_hash"));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * セッショントークン作成
     */
    public SessionToken createSessionToken(SessionToken token) throws SQLException {
        String sql = "INSERT INTO session_tokens (user_id, token, expires_at) " +
                    "VALUES (?, ?, ?) RETURNING id, created_at";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, token.getUserId());
            stmt.setString(2, token.getToken());
            stmt.setTimestamp(3, Timestamp.valueOf(token.getExpiresAt()));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    token.setId(rs.getLong("id"));
                    token.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                }
            }
        }
        return token;
    }

    /**
     * トークンによるセッション取得
     */
    public Optional<SessionToken> findSessionByToken(String token) throws SQLException {
        String sql = "SELECT id, user_id, token, expires_at, created_at " +
                    "FROM session_tokens WHERE token = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, token);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    SessionToken sessionToken = new SessionToken();
                    sessionToken.setId(rs.getLong("id"));
                    sessionToken.setUserId((UUID) rs.getObject("user_id"));
                    sessionToken.setToken(rs.getString("token"));
                    sessionToken.setExpiresAt(rs.getTimestamp("expires_at").toLocalDateTime());
                    sessionToken.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    return Optional.of(sessionToken);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * セッション削除
     */
    public void deleteSession(String token) throws SQLException {
        String sql = "DELETE FROM session_tokens WHERE token = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, token);
            stmt.executeUpdate();
        }
    }

    /**
     * ログイン履歴記録
     */
    public void recordLoginHistory(UUID userId, String ipAddress, String userAgent, boolean success) throws SQLException {
        String sql = "INSERT INTO login_history (user_id, ip_address, user_agent, success) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, userId);
            stmt.setString(2, ipAddress);
            stmt.setString(3, userAgent);
            stmt.setBoolean(4, success);
            stmt.executeUpdate();
        }
    }

    /**
     * 期限切れセッションの削除
     */
    public void cleanupExpiredSessions() throws SQLException {
        String sql = "DELETE FROM session_tokens WHERE expires_at < CURRENT_TIMESTAMP";
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }
}
