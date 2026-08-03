package com.smartlibrary.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookRequestDTO {

    @NotBlank(message = "Book title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Author is required")
    @Size(max = 255, message = "Author must not exceed 255 characters")
    private String author;

    private String description;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private String isbn;
    private String language;
    private Integer publicationYear;
    private Integer pages;
    private String coverImage;
    private String keywords;


    @Min(value = 0, message = "Total copies cannot be negative")
    @Builder.Default
    private Integer totalCopies = 1;
}
