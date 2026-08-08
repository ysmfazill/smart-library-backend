package com.smartlibrary.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResponseDTO {

    private Long id;
    private String title;
    private String author;
    private String description;
    private CategoryResponseDTO category;
    private String isbn;
    private String language;
    private Integer publicationYear;
    private Integer pages;
    private Double rating;
    private String coverImage;
    private String bookFileUrl;
    private String bookFileType;
    private String bookFileName;
    private String keywords;

    private Integer availableCopies;
    private Integer totalCopies;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
