package com.smartlibrary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookPreviewDTO {
    private String fileName;
    private int rowNumber;
    private String bookCode;
    private String title;
    private String author;
    private String categoryName;
    private String isbn;
    private Integer publicationYear;
    private Double rating;
    private String language;
    private String description;
    private String shortDescription;
    private String keywords;
    private String coverImage;
    private String status; // VALID, DUPLICATE, INVALID
    private String validationMessage;
}
