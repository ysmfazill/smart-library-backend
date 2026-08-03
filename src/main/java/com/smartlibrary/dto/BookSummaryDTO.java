package com.smartlibrary.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookSummaryDTO {

    private Long id;
    private String title;
    private String author;
    private String coverImage;
    private Double rating;
    private String categoryName;
    private String matchReason;
}
