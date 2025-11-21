package com.example.microservices.user.rest;

import com.example.microservices.user.model.User;
import com.example.microservices.user.repository.UserRepository;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * ユーザー管理REST API
 */
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    private UserRepository userRepository;

    /**
     * 全ユーザー取得
     */
    @GET
    public Response getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            return Response.ok(users).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse("Failed to fetch users: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * ユーザーID指定取得
     */
    @GET
    @Path("/{id}")
    public Response getUserById(@PathParam("id") UUID id) {
        try {
            Optional<User> user = userRepository.findById(id);
            if (user.isPresent()) {
                return Response.ok(user.get()).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(createErrorResponse("User not found with id: " + id))
                        .build();
            }
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse("Failed to fetch user: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * ユーザー名指定取得
     */
    @GET
    @Path("/username/{username}")
    public Response getUserByUsername(@PathParam("username") String username) {
        try {
            Optional<User> user = userRepository.findByUsername(username);
            if (user.isPresent()) {
                return Response.ok(user.get()).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(createErrorResponse("User not found with username: " + username))
                        .build();
            }
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse("Failed to fetch user: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * ユーザーアカウント情報を取得
     */
    @GET
    @Path("/{id}/account")
    public Response getUserAccount(@PathParam("id") UUID id) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (!userOpt.isPresent()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(createErrorResponse("User not found with id: " + id))
                        .build();
            }

            // フロントエンド用にuserオブジェクトをラップして返す
            Map<String, Object> accountData = new HashMap<>();
            accountData.put("user", userOpt.get());

            return Response.ok(accountData).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse("Failed to fetch account: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * ユーザー作成
     */
    @POST
    public Response createUser(User user) {
        try {
            User createdUser = userRepository.create(user);
            return Response.status(Response.Status.CREATED).entity(createdUser).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse("Failed to create user: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * ユーザー更新
     */
    @PUT
    @Path("/{id}")
    public Response updateUser(@PathParam("id") UUID id, User user) {
        try {
            Optional<User> existingUser = userRepository.findById(id);
            if (!existingUser.isPresent()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(createErrorResponse("User not found with id: " + id))
                        .build();
            }

            user.setId(id);
            User updatedUser = userRepository.update(user);
            return Response.ok(updatedUser).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse("Failed to update user: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * ユーザー削除
     */
    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") UUID id) {
        try {
            Optional<User> user = userRepository.findById(id);
            if (!user.isPresent()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(createErrorResponse("User not found with id: " + id))
                        .build();
            }

            userRepository.delete(id);
            return Response.noContent().build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse("Failed to delete user: " + e.getMessage()))
                    .build();
        }
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}
