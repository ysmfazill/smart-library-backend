package com.smartlibrary.service;

import com.smartlibrary.dto.*;

public interface AuthService {

    RegisterResponseDTO registerUser(RegisterRequestDTO request);

    LoginResponseDTO loginUser(LoginRequestDTO request);

    boolean checkEmailExists(String email);

    UserResponseDTO getCurrentUser(Long userId);

    UserResponseDTO updateProfile(Long userId, UserRequestDTO request);

    void changePassword(Long userId, String oldPassword, String newPassword);
}
