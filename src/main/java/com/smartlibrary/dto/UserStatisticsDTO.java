package com.smartlibrary.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatisticsDTO {
    private Long id;
    private Long userId;
    private int booksRead;
    private int pagesRead;
    private int readingHours;
    private int currentStreak;
    private int maxStreak;
    private LocalDateTime lastReadDate;
    private Integer currentRank;
    private Integer globalRank;
}
