package com.smartlibrary.controller;

import com.smartlibrary.dto.*;
import com.smartlibrary.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication and current user profile management.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new user account.
     *
     * @param request User registration details.
     * @return ResponseEntity with created user data.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponseDTO>> register(@Valid @RequestBody RegisterRequestDTO request) {
        RegisterResponseDTO response = authService.registerUser(request);
        return new ResponseEntity<>(ApiResponse.success("User registered successfully", response), HttpStatus.CREATED);
    }

    /**
     * Authenticates user and returns JWT access token.
     *
     * @param request Login credentials.
     * @return ResponseEntity with access token and user metadata.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(@Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = authService.loginUser(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    /**
     * Retrieves profile information for the authenticated user.
     *
     * @param userId User ID parameter.
     * @return ResponseEntity with user details.
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getProfile(@RequestParam(defaultValue = "1") Long userId) {
        UserResponseDTO response = authService.getCurrentUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Profile fetched successfully", response));
    }

    /**
     * Updates profile details for the authenticated user.
     *
     * @param userId User ID parameter.
     * @param request Updated profile fields.
     * @return ResponseEntity with updated user details.
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateProfile(
            @RequestParam(defaultValue = "1") Long userId,
            @Valid @RequestBody UserRequestDTO request) {
        UserResponseDTO response = authService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    /**
     * Modifies current user password.
     *
     * @param userId User ID.
     * @param oldPassword Current password.
     * @param newPassword New password.
     * @return ResponseEntity with success status message.
     */
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @RequestParam(defaultValue = "1") Long userId,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        authService.changePassword(userId, oldPassword, newPassword);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", "SUCCESS"));
    }
}
