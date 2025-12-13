package com.example.microservices.point.repository;

import com.example.microservices.point.model.Point;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;
import java.util.UUID;

/**
 * ポイント残高リポジトリ
 */
@ApplicationScoped
public class PointRepository {

    @Resource(lookup = "java:app/jdbc/pointServiceDB")
    private DataSource dataSource;

    /**
     * ユーザーIDでポイント残高を取得
     */
    public Optional<Point> findByUserId(UUID userId) throws SQLException {
        String sql = "SELECT user_id, balance, last_updated FROM points WHERE user_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPoint(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * ポイント残高を更新
     */
    public Point updateBalance(UUID userId, Integer newBalance) throws SQLException {
        String sql = "UPDATE points SET balance = ?, last_updated = NOW() WHERE user_id = ? RETURNING user_id, balance, last_updated";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, newBalance);
            stmt.setObject(2, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPoint(rs);
                }
            }
        }
        throw new SQLException("Failed to update balance for user: " + userId);
    }

    /**
     * 新規ポイント残高を作成
     */
    public Point create(UUID userId, Integer initialBalance) throws SQLException {
        String sql = "INSERT INTO points (user_id, balance, last_updated) VALUES (?, ?, NOW()) RETURNING user_id, balance, last_updated";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, userId);
            stmt.setInt(2, initialBalance);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPoint(rs);
                }
            }
        }
        throw new SQLException("Failed to create point record for user: " + userId);
    }

    private Point mapResultSetToPoint(ResultSet rs) throws SQLException {
        Point point = new Point();
        point.setUserId((UUID) rs.getObject("user_id"));
        point.setBalance(rs.getInt("balance"));
        Timestamp timestamp = rs.getTimestamp("last_updated");
        if (timestamp != null) {
            point.setLastUpdated(timestamp.toLocalDateTime());
        }
        return point;
    }
}
