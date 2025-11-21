package com.example.microservices.point.repository;

import com.example.microservices.point.model.PointHistory;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ポイント履歴リポジトリ
 */
@ApplicationScoped
public class PointHistoryRepository {

    @Resource(lookup = "java:app/jdbc/pointServiceDB")
    private DataSource dataSource;

    /**
     * ユーザーIDでポイント履歴を取得（ページネーション対応）
     */
    public List<PointHistory> findByUserId(UUID userId, int page, int limit) throws SQLException {
        List<PointHistory> histories = new ArrayList<>();
        int offset = (page - 1) * limit;
        
        String sql = "SELECT id, user_id, amount, transaction_type, description, created_at, expires_at " +
                     "FROM point_history WHERE user_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, userId);
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    histories.add(mapResultSetToPointHistory(rs));
                }
            }
        }
        return histories;
    }

    /**
     * ユーザーの履歴総数を取得
     */
    public int countByUserId(UUID userId) throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM point_history WHERE user_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }

    /**
     * 新規履歴を作成
     */
    public PointHistory create(PointHistory history) throws SQLException {
        String sql = "INSERT INTO point_history (user_id, amount, transaction_type, description, created_at, expires_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?) RETURNING id, user_id, amount, transaction_type, description, created_at, expires_at";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, history.getUserId());
            stmt.setInt(2, history.getAmount());
            stmt.setString(3, history.getTransactionType());
            stmt.setString(4, history.getDescription());
            stmt.setTimestamp(5, history.getCreatedAt() != null ? Timestamp.valueOf(history.getCreatedAt()) : new Timestamp(System.currentTimeMillis()));
            stmt.setTimestamp(6, history.getExpiresAt() != null ? Timestamp.valueOf(history.getExpiresAt()) : null);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPointHistory(rs);
                }
            }
        }
        throw new SQLException("Failed to create point history");
    }

    private PointHistory mapResultSetToPointHistory(ResultSet rs) throws SQLException {
        PointHistory history = new PointHistory();
        history.setId(rs.getLong("id"));
        history.setUserId((UUID) rs.getObject("user_id"));
        history.setAmount(rs.getInt("amount"));
        history.setTransactionType(rs.getString("transaction_type"));
        history.setDescription(rs.getString("description"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            history.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp expiresAt = rs.getTimestamp("expires_at");
        if (expiresAt != null) {
            history.setExpiresAt(expiresAt.toLocalDateTime());
        }
        
        return history;
    }
}
