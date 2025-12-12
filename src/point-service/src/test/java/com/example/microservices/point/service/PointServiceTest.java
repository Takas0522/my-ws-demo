package com.example.microservices.point.service;

import com.example.microservices.point.model.Point;
import com.example.microservices.point.model.PointHistory;
import com.example.microservices.point.repository.PointRepository;
import com.example.microservices.point.repository.PointHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PointService Tests")
class PointServiceTest {

    @Mock
    private PointRepository pointRepository;

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @InjectMocks
    private PointService pointService;

    private UUID testUserId;
    private Point testPoint;
    private PointHistory testHistory;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testPoint = new Point(testUserId, 1000, LocalDateTime.now());
        
        testHistory = new PointHistory();
        testHistory.setId(1L);
        testHistory.setUserId(testUserId);
        testHistory.setAmount(100);
        testHistory.setTransactionType("EARN");
        testHistory.setDescription("Test transaction");
        testHistory.setCreatedAt(LocalDateTime.now());
    }

    // ==================== getPointBalance Tests ====================

    @Test
    @DisplayName("Given existing point balance, When getting point balance, Then should return point")
    void givenExistingPointBalance_whenGettingPointBalance_thenShouldReturnPoint() throws SQLException {
        // Given
        when(pointRepository.findByUserId(testUserId)).thenReturn(Optional.of(testPoint));

        // When
        Optional<Point> result = pointService.getPointBalance(testUserId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testPoint, result.get());
        verify(pointRepository, times(1)).findByUserId(testUserId);
    }

    @Test
    @DisplayName("Given no existing point balance, When getting point balance, Then should return empty")
    void givenNoExistingPointBalance_whenGettingPointBalance_thenShouldReturnEmpty() throws SQLException {
        // Given
        when(pointRepository.findByUserId(testUserId)).thenReturn(Optional.empty());

        // When
        Optional<Point> result = pointService.getPointBalance(testUserId);

        // Then
        assertFalse(result.isPresent());
        verify(pointRepository, times(1)).findByUserId(testUserId);
    }

    @Test
    @DisplayName("Given repository throws SQLException, When getting point balance, Then should throw SQLException")
    void givenRepositoryThrowsSQLException_whenGettingPointBalance_thenShouldThrowSQLException() throws SQLException {
        // Given
        when(pointRepository.findByUserId(testUserId)).thenThrow(new SQLException("Database error"));

        // When & Then
        assertThrows(SQLException.class, () -> pointService.getPointBalance(testUserId));
        verify(pointRepository, times(1)).findByUserId(testUserId);
    }

    // ==================== getPointHistory Tests ====================

    @Test
    @DisplayName("Given existing point history, When getting point history, Then should return history list")
    void givenExistingPointHistory_whenGettingPointHistory_thenShouldReturnHistoryList() throws SQLException {
        // Given
        List<PointHistory> historyList = Arrays.asList(testHistory);
        when(pointHistoryRepository.findByUserId(testUserId, 1, 10)).thenReturn(historyList);

        // When
        List<PointHistory> result = pointService.getPointHistory(testUserId, 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testHistory, result.get(0));
        verify(pointHistoryRepository, times(1)).findByUserId(testUserId, 1, 10);
    }

    @Test
    @DisplayName("Given no point history, When getting point history, Then should return empty list")
    void givenNoPointHistory_whenGettingPointHistory_thenShouldReturnEmptyList() throws SQLException {
        // Given
        when(pointHistoryRepository.findByUserId(testUserId, 1, 10)).thenReturn(Arrays.asList());

        // When
        List<PointHistory> result = pointService.getPointHistory(testUserId, 1, 10);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(pointHistoryRepository, times(1)).findByUserId(testUserId, 1, 10);
    }

    // ==================== getPointHistoryCount Tests ====================

    @Test
    @DisplayName("Given point history exists, When getting count, Then should return correct count")
    void givenPointHistoryExists_whenGettingCount_thenShouldReturnCorrectCount() throws SQLException {
        // Given
        when(pointHistoryRepository.countByUserId(testUserId)).thenReturn(5);

        // When
        int result = pointService.getPointHistoryCount(testUserId);

        // Then
        assertEquals(5, result);
        verify(pointHistoryRepository, times(1)).countByUserId(testUserId);
    }

    @Test
    @DisplayName("Given no point history, When getting count, Then should return zero")
    void givenNoPointHistory_whenGettingCount_thenShouldReturnZero() throws SQLException {
        // Given
        when(pointHistoryRepository.countByUserId(testUserId)).thenReturn(0);

        // When
        int result = pointService.getPointHistoryCount(testUserId);

        // Then
        assertEquals(0, result);
        verify(pointHistoryRepository, times(1)).countByUserId(testUserId);
    }

    // ==================== earnPoints Tests ====================

    @Test
    @DisplayName("Given existing point balance, When earning points, Then should update balance and create history")
    void givenExistingPointBalance_whenEarningPoints_thenShouldUpdateBalanceAndCreateHistory() throws SQLException {
        // Given
        int earnAmount = 500;
        int expectedNewBalance = 1500;
        Point updatedPoint = new Point(testUserId, expectedNewBalance, LocalDateTime.now());
        
        when(pointRepository.findByUserId(testUserId)).thenReturn(Optional.of(testPoint));
        when(pointRepository.updateBalance(testUserId, expectedNewBalance)).thenReturn(updatedPoint);
        when(pointHistoryRepository.create(any(PointHistory.class))).thenReturn(testHistory);

        // When
        Point result = pointService.earnPoints(testUserId, earnAmount, "Bonus points");

        // Then
        assertNotNull(result);
        assertEquals(expectedNewBalance, result.getBalance());
        verify(pointRepository, times(1)).findByUserId(testUserId);
        verify(pointRepository, times(1)).updateBalance(testUserId, expectedNewBalance);
        verify(pointHistoryRepository, times(1)).create(argThat(history -> 
            history.getUserId().equals(testUserId) &&
            history.getAmount().equals(earnAmount) &&
            history.getTransactionType().equals("EARN") &&
            history.getDescription().equals("Bonus points")
        ));
    }

    @Test
    @DisplayName("Given no existing point balance, When earning points, Then should create new balance and history")
    void givenNoExistingPointBalance_whenEarningPoints_thenShouldCreateNewBalanceAndHistory() throws SQLException {
        // Given
        int earnAmount = 500;
        Point newPoint = new Point(testUserId, earnAmount, LocalDateTime.now());
        
        when(pointRepository.findByUserId(testUserId)).thenReturn(Optional.empty());
        when(pointRepository.create(testUserId, earnAmount)).thenReturn(newPoint);
        when(pointHistoryRepository.create(any(PointHistory.class))).thenReturn(testHistory);

        // When
        Point result = pointService.earnPoints(testUserId, earnAmount, "Initial points");

        // Then
        assertNotNull(result);
        assertEquals(earnAmount, result.getBalance());
        verify(pointRepository, times(1)).findByUserId(testUserId);
        verify(pointRepository, times(1)).create(testUserId, earnAmount);
        verify(pointRepository, never()).updateBalance(any(), anyInt());
        verify(pointHistoryRepository, times(1)).create(argThat(history -> 
            history.getUserId().equals(testUserId) &&
            history.getAmount().equals(earnAmount) &&
            history.getTransactionType().equals("EARN")
        ));
    }

    @Test
    @DisplayName("Given zero amount, When earning points, Then should handle correctly")
    void givenZeroAmount_whenEarningPoints_thenShouldHandleCorrectly() throws SQLException {
        // Given
        int earnAmount = 0;
        Point updatedPoint = new Point(testUserId, 1000, LocalDateTime.now());
        
        when(pointRepository.findByUserId(testUserId)).thenReturn(Optional.of(testPoint));
        when(pointRepository.updateBalance(testUserId, 1000)).thenReturn(updatedPoint);
        when(pointHistoryRepository.create(any(PointHistory.class))).thenReturn(testHistory);

        // When
        Point result = pointService.earnPoints(testUserId, earnAmount, "Zero points");

        // Then
        assertNotNull(result);
        verify(pointHistoryRepository, times(1)).create(any(PointHistory.class));
    }

    // ==================== usePoints Tests ====================

    @Test
    @DisplayName("Given sufficient balance, When using points, Then should deduct balance and create history")
    void givenSufficientBalance_whenUsingPoints_thenShouldDeductBalanceAndCreateHistory() throws SQLException {
        // Given
        int useAmount = 300;
        int expectedNewBalance = 700;
        Point updatedPoint = new Point(testUserId, expectedNewBalance, LocalDateTime.now());
        
        when(pointRepository.findByUserId(testUserId)).thenReturn(Optional.of(testPoint));
        when(pointRepository.updateBalance(testUserId, expectedNewBalance)).thenReturn(updatedPoint);
        when(pointHistoryRepository.create(any(PointHistory.class))).thenReturn(testHistory);

        // When
        Point result = pointService.usePoints(testUserId, useAmount, "Product purchase");

        // Then
        assertNotNull(result);
        assertEquals(expectedNewBalance, result.getBalance());
        verify(pointRepository, times(1)).findByUserId(testUserId);
        verify(pointRepository, times(1)).updateBalance(testUserId, expectedNewBalance);
        verify(pointHistoryRepository, times(1)).create(argThat(history -> 
            history.getUserId().equals(testUserId) &&
            history.getAmount().equals(useAmount) &&
            history.getTransactionType().equals("USE") &&
            history.getDescription().equals("Product purchase")
        ));
    }

    @Test
    @DisplayName("Given insufficient balance, When using points, Then should throw IllegalArgumentException")
    void givenInsufficientBalance_whenUsingPoints_thenShouldThrowIllegalArgumentException() throws SQLException {
        // Given
        int useAmount = 1500; // More than available balance (1000)
        when(pointRepository.findByUserId(testUserId)).thenReturn(Optional.of(testPoint));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> pointService.usePoints(testUserId, useAmount, "Large purchase"));
        
        assertEquals("Insufficient point balance", exception.getMessage());
        verify(pointRepository, times(1)).findByUserId(testUserId);
        verify(pointRepository, never()).updateBalance(any(), anyInt());
        verify(pointHistoryRepository, never()).create(any());
    }

    @Test
    @DisplayName("Given no existing point record, When using points, Then should throw IllegalStateException")
    void givenNoExistingPointRecord_whenUsingPoints_thenShouldThrowIllegalStateException() throws SQLException {
        // Given
        when(pointRepository.findByUserId(testUserId)).thenReturn(Optional.empty());

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> pointService.usePoints(testUserId, 100, "Purchase attempt"));
        
        assertTrue(exception.getMessage().contains("Point record not found"));
        verify(pointRepository, times(1)).findByUserId(testUserId);
        verify(pointRepository, never()).updateBalance(any(), anyInt());
        verify(pointHistoryRepository, never()).create(any());
    }

    @Test
    @DisplayName("Given exact balance amount, When using points, Then should set balance to zero")
    void givenExactBalanceAmount_whenUsingPoints_thenShouldSetBalanceToZero() throws SQLException {
        // Given
        int useAmount = 1000; // Exact balance
        Point updatedPoint = new Point(testUserId, 0, LocalDateTime.now());
        
        when(pointRepository.findByUserId(testUserId)).thenReturn(Optional.of(testPoint));
        when(pointRepository.updateBalance(testUserId, 0)).thenReturn(updatedPoint);
        when(pointHistoryRepository.create(any(PointHistory.class))).thenReturn(testHistory);

        // When
        Point result = pointService.usePoints(testUserId, useAmount, "Full redemption");

        // Then
        assertNotNull(result);
        assertEquals(0, result.getBalance());
        verify(pointRepository, times(1)).updateBalance(testUserId, 0);
    }

    @Test
    @DisplayName("Given using one point less than balance, When using points, Then should leave one point")
    void givenUsingOnePointLessThanBalance_whenUsingPoints_thenShouldLeaveOnePoint() throws SQLException {
        // Given
        int useAmount = 999;
        Point updatedPoint = new Point(testUserId, 1, LocalDateTime.now());
        
        when(pointRepository.findByUserId(testUserId)).thenReturn(Optional.of(testPoint));
        when(pointRepository.updateBalance(testUserId, 1)).thenReturn(updatedPoint);
        when(pointHistoryRepository.create(any(PointHistory.class))).thenReturn(testHistory);

        // When
        Point result = pointService.usePoints(testUserId, useAmount, "Almost full redemption");

        // Then
        assertNotNull(result);
        assertEquals(1, result.getBalance());
        verify(pointRepository, times(1)).updateBalance(testUserId, 1);
    }
}
