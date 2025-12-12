package com.example.microservices.user.rest;

import com.example.microservices.user.model.User;
import com.example.microservices.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UserResourceのユニットテスト
 * Given-When-Then 形式でテストケースを記述
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserResource API Tests")
class UserResourceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserResource userResource;

    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = new User(testUserId, "testuser", "test@example.com", "Test User");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Given users exist, When getAllUsers is called, Then return OK with user list")
    void testGetAllUsers_Success() throws SQLException {
        // Given
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);

        // When
        Response response = userResource.getAllUsers();

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Given repository throws SQLException, When getAllUsers is called, Then return INTERNAL_SERVER_ERROR")
    void testGetAllUsers_SQLException() throws SQLException {
        // Given
        when(userRepository.findAll()).thenThrow(new SQLException("Database error"));

        // When
        Response response = userResource.getAllUsers();

        // Then
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Given user exists with id, When getUserById is called, Then return OK with user")
    void testGetUserById_Success() throws SQLException {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // When
        Response response = userResource.getUserById(testUserId);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(userRepository, times(1)).findById(testUserId);
    }

    @Test
    @DisplayName("Given user does not exist, When getUserById is called, Then return NOT_FOUND")
    void testGetUserById_NotFound() throws SQLException {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // When
        Response response = userResource.getUserById(testUserId);

        // Then
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        verify(userRepository, times(1)).findById(testUserId);
    }

    @Test
    @DisplayName("Given repository throws SQLException, When getUserById is called, Then return INTERNAL_SERVER_ERROR")
    void testGetUserById_SQLException() throws SQLException {
        // Given
        when(userRepository.findById(testUserId)).thenThrow(new SQLException("Database error"));

        // When
        Response response = userResource.getUserById(testUserId);

        // Then
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        verify(userRepository, times(1)).findById(testUserId);
    }

    @Test
    @DisplayName("Given user exists with username, When getUserByUsername is called, Then return OK with user")
    void testGetUserByUsername_Success() throws SQLException {
        // Given
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // When
        Response response = userResource.getUserByUsername(username);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("Given user does not exist, When getUserByUsername is called, Then return NOT_FOUND")
    void testGetUserByUsername_NotFound() throws SQLException {
        // Given
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When
        Response response = userResource.getUserByUsername(username);

        // Then
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("Given user exists, When getUserAccount is called, Then return OK with account data")
    void testGetUserAccount_Success() throws SQLException {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // When
        Response response = userResource.getUserAccount(testUserId);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(userRepository, times(1)).findById(testUserId);
    }

    @Test
    @DisplayName("Given user does not exist, When getUserAccount is called, Then return NOT_FOUND")
    void testGetUserAccount_NotFound() throws SQLException {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // When
        Response response = userResource.getUserAccount(testUserId);

        // Then
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        verify(userRepository, times(1)).findById(testUserId);
    }

    @Test
    @DisplayName("Given valid user data, When createUser is called, Then return CREATED with user")
    void testCreateUser_Success() throws SQLException {
        // Given
        User newUser = new User(null, "newuser", "new@example.com", "New User");
        when(userRepository.create(any(User.class))).thenReturn(testUser);

        // When
        Response response = userResource.createUser(newUser);

        // Then
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(userRepository, times(1)).create(any(User.class));
    }

    @Test
    @DisplayName("Given repository throws SQLException, When createUser is called, Then return INTERNAL_SERVER_ERROR")
    void testCreateUser_SQLException() throws SQLException {
        // Given
        User newUser = new User(null, "newuser", "new@example.com", "New User");
        when(userRepository.create(any(User.class))).thenThrow(new SQLException("Database error"));

        // When
        Response response = userResource.createUser(newUser);

        // Then
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        verify(userRepository, times(1)).create(any(User.class));
    }

    @Test
    @DisplayName("Given user exists, When updateUser is called, Then return OK with updated user")
    void testUpdateUser_Success() throws SQLException {
        // Given
        User updateData = new User(testUserId, "updateduser", "updated@example.com", "Updated User");
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.update(any(User.class))).thenReturn(updateData);

        // When
        Response response = userResource.updateUser(testUserId, updateData);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(userRepository, times(1)).findById(testUserId);
        verify(userRepository, times(1)).update(any(User.class));
    }

    @Test
    @DisplayName("Given user does not exist, When updateUser is called, Then return NOT_FOUND")
    void testUpdateUser_NotFound() throws SQLException {
        // Given
        User updateData = new User(testUserId, "updateduser", "updated@example.com", "Updated User");
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // When
        Response response = userResource.updateUser(testUserId, updateData);

        // Then
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        verify(userRepository, times(1)).findById(testUserId);
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("Given repository throws SQLException, When updateUser is called, Then return INTERNAL_SERVER_ERROR")
    void testUpdateUser_SQLException() throws SQLException {
        // Given
        User updateData = new User(testUserId, "updateduser", "updated@example.com", "Updated User");
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.update(any(User.class))).thenThrow(new SQLException("Database error"));

        // When
        Response response = userResource.updateUser(testUserId, updateData);

        // Then
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        verify(userRepository, times(1)).update(any(User.class));
    }

    @Test
    @DisplayName("Given user exists, When deleteUser is called, Then return NO_CONTENT")
    void testDeleteUser_Success() throws SQLException {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(testUserId);

        // When
        Response response = userResource.deleteUser(testUserId);

        // Then
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        verify(userRepository, times(1)).findById(testUserId);
        verify(userRepository, times(1)).delete(testUserId);
    }

    @Test
    @DisplayName("Given user does not exist, When deleteUser is called, Then return NOT_FOUND")
    void testDeleteUser_NotFound() throws SQLException {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // When
        Response response = userResource.deleteUser(testUserId);

        // Then
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        verify(userRepository, times(1)).findById(testUserId);
        verify(userRepository, never()).delete(any(UUID.class));
    }

    @Test
    @DisplayName("Given repository throws SQLException, When deleteUser is called, Then return INTERNAL_SERVER_ERROR")
    void testDeleteUser_SQLException() throws SQLException {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        doThrow(new SQLException("Database error")).when(userRepository).delete(testUserId);

        // When
        Response response = userResource.deleteUser(testUserId);

        // Then
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        verify(userRepository, times(1)).delete(testUserId);
    }
}
