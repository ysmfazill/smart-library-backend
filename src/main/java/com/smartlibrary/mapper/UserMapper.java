package com.smartlibrary.mapper;

import com.smartlibrary.dto.*;
import com.smartlibrary.entity.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {

    private final UserInterestMapper userInterestMapper;

    public UserMapper(UserInterestMapper userInterestMapper) {
        this.userInterestMapper = userInterestMapper;
    }

    public UserResponseDTO toResponseDTO(User user) {
        if (user == null) {
            return null;
        }
        return UserResponseDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .avatar(user.getAvatar())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .interests(user.getUserInterests() != null ?
                        user.getUserInterests().stream()
                                .map(userInterestMapper::toResponseDTO)
                                .collect(Collectors.toSet()) : null)
                .build();
    }

    public UserProfileDTO toProfileDTO(User user) {
        if (user == null) {
            return null;
        }
        return UserProfileDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .avatar(user.getAvatar())
                .createdAt(user.getCreatedAt())
                .totalFavorites(user.getFavorites() != null ? user.getFavorites().size() : 0)
                .totalReadingHistory(user.getReadingHistories() != null ? user.getReadingHistories().size() : 0)
                .totalReviews(user.getReviews() != null ? user.getReviews().size() : 0)
                .interests(user.getUserInterests() != null ?
                        user.getUserInterests().stream()
                                .map(userInterestMapper::toResponseDTO)
                                .collect(Collectors.toSet()) : null)
                .build();
    }

    public User toEntity(UserRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        return User.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .password(dto.getPassword())
                .avatar(dto.getAvatar())
                .build();
    }

    public User toEntity(RegisterRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        return User.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .password(dto.getPassword())
                .avatar(dto.getAvatar())
                .build();
    }

    public RegisterResponseDTO toRegisterResponseDTO(User user) {
        if (user == null) {
            return null;
        }
        return RegisterResponseDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
