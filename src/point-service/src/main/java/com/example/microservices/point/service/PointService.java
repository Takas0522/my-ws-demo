package com.example.microservices.point.service;

import com.example.microservices.point.model.Point;
import com.example.microservices.point.model.PointHistory;
import com.example.microservices.point.repository.PointRepository;
import com.example.microservices.point.repository.PointHistoryRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ポイント管理サービス
 */
@ApplicationScoped
public class PointService {

    @Inject
    private PointRepository pointRepository;

    @Inject
    private PointHistoryRepository pointHistoryRepository;

    /**
     * ユーザーのポイント残高を取得
     */
    public Optional<Point> getPointBalance(UUID userId) throws SQLException {
        return pointRepository.findByUserId(userId);
    }

    /**
     * ユーザーのポイント履歴を取得
     */
    public List<PointHistory> getPointHistory(UUID userId, int page, int limit) throws SQLException {
        return pointHistoryRepository.findByUserId(userId, page, limit);
    }

    /**
     * ユーザーのポイント履歴総数を取得
     */
    public int getPointHistoryCount(UUID userId) throws SQLException {
        return pointHistoryRepository.countByUserId(userId);
    }

    /**
     * ポイントを付与
     */
    public Point earnPoints(UUID userId, Integer amount, String description) throws SQLException {
        Optional<Point> existingPoint = pointRepository.findByUserId(userId);
        Point updatedPoint;
        
        if (existingPoint.isPresent()) {
            int newBalance = existingPoint.get().getBalance() + amount;
            updatedPoint = pointRepository.updateBalance(userId, newBalance);
        } else {
            updatedPoint = pointRepository.create(userId, amount);
        }

        // 履歴を記録
        PointHistory history = new PointHistory();
        history.setUserId(userId);
        history.setAmount(amount);
        history.setTransactionType("EARN");
        history.setDescription(description);
        pointHistoryRepository.create(history);

        return updatedPoint;
    }

    /**
     * ポイントを使用
     */
    public Point usePoints(UUID userId, Integer amount, String description) throws SQLException {
        Optional<Point> existingPoint = pointRepository.findByUserId(userId);
        
        if (!existingPoint.isPresent()) {
            throw new IllegalStateException("Point record not found for user: " + userId);
        }

        Point point = existingPoint.get();
        if (point.getBalance() < amount) {
            throw new IllegalArgumentException("Insufficient point balance");
        }

        int newBalance = point.getBalance() - amount;
        Point updatedPoint = pointRepository.updateBalance(userId, newBalance);

        // 履歴を記録
        PointHistory history = new PointHistory();
        history.setUserId(userId);
        history.setAmount(amount);
        history.setTransactionType("USE");
        history.setDescription(description);
        pointHistoryRepository.create(history);

        return updatedPoint;
    }
}
