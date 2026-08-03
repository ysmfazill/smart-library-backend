package com.smartlibrary.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteResponseDTO {

    private Long id;
    private Long userId;
    private String userName;
    private BookSummaryDTO book;
    private LocalDateTime addedAt;
}
