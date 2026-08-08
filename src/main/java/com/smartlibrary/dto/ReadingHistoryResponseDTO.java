package com.smartlibrary.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadingHistoryResponseDTO {

    private Long id;
    private Long userId;
    private String userName;
    private BookSummaryDTO book;
    private Double progressPercentage;
    private Integer currentPage;
    private Integer totalPages;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime lastReadDate;
    private Boolean completed;
}
