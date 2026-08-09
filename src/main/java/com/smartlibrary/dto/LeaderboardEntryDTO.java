package com.smartlibrary.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardEntryDTO {
    private int rank;
    private Long userId;
    private String name;
    private String avatar;
    private String profilePicture;
    private int booksCompleted;
    private int pagesRead;
    private int readingHours;
    private int readingStreak;
    private String topBadge;
}
