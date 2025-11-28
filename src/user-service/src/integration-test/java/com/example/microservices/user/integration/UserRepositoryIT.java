package com.example.microservices.user.integration;

import com.example.microservices.user.model.User;
import com.example.microservices.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserRepositoryのIntegration Test
 * TestContainersを使用してPostgreSQLコンテナに対してテストを実行します。
 */
class UserRepositoryIT extends BaseIntegrationTest {

    private UserRepository userRepository;

    @BeforeEach
    void setUpRepository() throws Exception {
        userRepository = new UserRepository();
        
        // リフレクションを使用してDataSourceをインジェクト
        Field dataSourceField = UserRepository.class.getDeclaredField("dataSource");
        dataSourceField.setAccessible(true);
        dataSourceField.set(userRepository, getDataSource());
    }

    @Test
    void testFindAll_ShouldReturnAllUsers() throws SQLException {
        // Act
        List<User> users = userRepository.findAll();

        // Assert
        assertNotNull(users);
        assertEquals(3, users.size());
        
        // ユーザー名でソートされていることを確認
        assertEquals("testuser1", users.get(0).getUsername());
        assertEquals("testuser2", users.get(1).getUsername());
        assertEquals("testuser3", users.get(2).getUsername());
    }

    @Test
    void testFindById_WithValidId_ShouldReturnUser() throws SQLException {
        // Arrange
        UUID testUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        // Act
        Optional<User> userOpt = userRepository.findById(testUserId);

        // Assert
        assertTrue(userOpt.isPresent());
        User user = userOpt.get();
        assertEquals(testUserId, user.getId());
        assertEquals("testuser1", user.getUsername());
        assertEquals("test1@example.com", user.getEmail());
        assertEquals("Test User One", user.getFullName());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
    }

    @Test
    void testFindById_WithInvalidId_ShouldReturnEmpty() throws SQLException {
        // Arrange
        UUID nonExistentId = UUID.fromString("00000000-0000-0000-0000-000000000000");

        // Act
        Optional<User> userOpt = userRepository.findById(nonExistentId);

        // Assert
        assertFalse(userOpt.isPresent());
    }

    @Test
    void testFindByUsername_WithValidUsername_ShouldReturnUser() throws SQLException {
        // Arrange
        String testUsername = "testuser2";

        // Act
        Optional<User> userOpt = userRepository.findByUsername(testUsername);

        // Assert
        assertTrue(userOpt.isPresent());
        User user = userOpt.get();
        assertEquals(UUID.fromString("123e4567-e89b-12d3-a456-426614174001"), user.getId());
        assertEquals(testUsername, user.getUsername());
        assertEquals("test2@example.com", user.getEmail());
        assertEquals("Test User Two", user.getFullName());
    }

    @Test
    void testFindByUsername_WithInvalidUsername_ShouldReturnEmpty() throws SQLException {
        // Arrange
        String nonExistentUsername = "nonexistent";

        // Act
        Optional<User> userOpt = userRepository.findByUsername(nonExistentUsername);

        // Assert
        assertFalse(userOpt.isPresent());
    }

    @Test
    void testCreate_ShouldInsertNewUser() throws SQLException {
        // Arrange
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("newuser@example.com");
        newUser.setFullName("New User");

        // Act
        User createdUser = userRepository.create(newUser);

        // Assert
        assertNotNull(createdUser.getId());
        assertEquals("newuser", createdUser.getUsername());
        assertEquals("newuser@example.com", createdUser.getEmail());
        assertEquals("New User", createdUser.getFullName());
        assertNotNull(createdUser.getCreatedAt());
        assertNotNull(createdUser.getUpdatedAt());

        // DBから取得して確認
        Optional<User> foundUser = userRepository.findById(createdUser.getId());
        assertTrue(foundUser.isPresent());
        assertEquals("newuser", foundUser.get().getUsername());
    }

    @Test
    void testUpdate_ShouldUpdateExistingUser() throws SQLException {
        // Arrange
        UUID testUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174002");
        Optional<User> existingUserOpt = userRepository.findById(testUserId);
        assertTrue(existingUserOpt.isPresent());
        
        User existingUser = existingUserOpt.get();
        existingUser.setEmail("updated@example.com");
        existingUser.setFullName("Updated Name");

        // Act
        User updatedUser = userRepository.update(existingUser);

        // Assert
        assertEquals(testUserId, updatedUser.getId());
        assertEquals("testuser3", updatedUser.getUsername());
        assertEquals("updated@example.com", updatedUser.getEmail());
        assertEquals("Updated Name", updatedUser.getFullName());
        assertNotNull(updatedUser.getUpdatedAt());

        // DBから取得して確認
        Optional<User> foundUser = userRepository.findById(testUserId);
        assertTrue(foundUser.isPresent());
        assertEquals("updated@example.com", foundUser.get().getEmail());
        assertEquals("Updated Name", foundUser.get().getFullName());
    }

    @Test
    void testDelete_ShouldRemoveUser() throws SQLException {
        // Arrange
        UUID testUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
        
        // 削除前に存在確認
        Optional<User> beforeDelete = userRepository.findById(testUserId);
        assertTrue(beforeDelete.isPresent());

        // Act
        userRepository.delete(testUserId);

        // Assert
        Optional<User> afterDelete = userRepository.findById(testUserId);
        assertFalse(afterDelete.isPresent());
        
        // 他のユーザーは残っていることを確認
        List<User> remainingUsers = userRepository.findAll();
        assertEquals(2, remainingUsers.size());
    }
}
