package com.smartlibrary.dto;

import com.smartlibrary.entity.Role;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDTO {

    private Long id;
    private String fullName;
    private String email;
    private Role role;
    private String avatar;
    private LocalDateTime createdAt;
    private int totalFavorites;
    private int totalReadingHistory;
    private int totalReviews;
    private Set<UserInterestResponseDTO> interests;
}
