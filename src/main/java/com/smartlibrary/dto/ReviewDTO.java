package com.smartlibrary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDTO {
    private Long id;
    private Long userId;
    private String userName;
    private String userAvatar;
    private Long bookId;
    private Integer rating;
    private String comment;
    private LocalDateTime reviewDate;
    private long likesCount;
    private boolean isLikedByCurrentUser;
}
