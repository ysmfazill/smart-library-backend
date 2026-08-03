package com.smartlibrary.service;

import com.smartlibrary.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    UserResponseDTO getUserById(Long userId);

    UserResponseDTO getUserByEmail(String email);

    UserProfileDTO getUserProfile(Long userId);

    List<UserResponseDTO> getAllUsers();

    Page<UserResponseDTO> searchUsersByName(String name, Pageable pageable);

    UserResponseDTO updateUser(Long userId, UserRequestDTO request);

    void deleteUser(Long userId);
}
