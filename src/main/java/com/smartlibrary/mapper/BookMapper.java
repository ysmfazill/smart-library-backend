package com.smartlibrary.mapper;

import com.smartlibrary.dto.BookRequestDTO;
import com.smartlibrary.dto.BookResponseDTO;
import com.smartlibrary.dto.BookSummaryDTO;
import com.smartlibrary.entity.Book;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

    private final CategoryMapper categoryMapper;

    public BookMapper(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    private static final String DEFAULT_COVER_IMAGE = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&q=80&w=600";

    public BookResponseDTO toResponseDTO(Book book) {
        if (book == null) {
            return null;
        }
        String cover = (book.getCoverImage() != null && !book.getCoverImage().isBlank()) 
                ? book.getCoverImage() : DEFAULT_COVER_IMAGE;

        return BookResponseDTO.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .description(book.getDescription())
                .category(categoryMapper.toResponseDTO(book.getCategory()))
                .isbn(book.getIsbn())
                .language(book.getLanguage())
                .publicationYear(book.getPublicationYear())
                .pages(book.getPages())
                .rating(book.getRating())
                .coverImage(cover)
                .bookFileUrl(book.getBookFileUrl())
                .bookFileType(book.getBookFileType())
                .bookFileName(book.getBookFileName())
                .keywords(book.getKeywords())

                .availableCopies(book.getAvailableCopies())
                .totalCopies(book.getTotalCopies())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }

    public BookSummaryDTO toSummaryDTO(Book book) {
        if (book == null) {
            return null;
        }
        String cover = (book.getCoverImage() != null && !book.getCoverImage().isBlank()) 
                ? book.getCoverImage() : DEFAULT_COVER_IMAGE;

        return BookSummaryDTO.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .coverImage(cover)
                .rating(book.getRating())
                .categoryName(book.getCategory() != null ? book.getCategory().getName() : null)
                .build();
    }

    public Book toEntity(BookRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        return Book.builder()
                .title(dto.getTitle())
                .author(dto.getAuthor())
                .description(dto.getDescription())
                .isbn(dto.getIsbn())
                .language(dto.getLanguage())
                .publicationYear(dto.getPublicationYear())
                .pages(dto.getPages())
                .coverImage(dto.getCoverImage())
                .bookFileUrl(dto.getBookFileUrl())
                .bookFileType(dto.getBookFileType())
                .bookFileName(dto.getBookFileName())
                .keywords(dto.getKeywords())

                .totalCopies(dto.getTotalCopies())
                .availableCopies(dto.getTotalCopies())
                .build();
    }
}
