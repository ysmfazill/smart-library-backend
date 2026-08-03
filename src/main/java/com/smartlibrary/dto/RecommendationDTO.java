package com.smartlibrary.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationDTO {
    private Long id;
    private String title;
    private String author;
    private String coverImage;
    private Double rating;
    private String categoryName;
    private Double recommendationScore;
    private String recommendationReason;
}
