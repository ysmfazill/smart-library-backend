package com.smartlibrary.service;

import com.smartlibrary.dto.BookRequestDTO;
import com.smartlibrary.dto.BookResponseDTO;
import com.smartlibrary.dto.BookSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookService {

    List<BookResponseDTO> getAllBooks();

    Page<BookResponseDTO> getAllBooks(Pageable pageable);

    BookResponseDTO getBookById(Long bookId);

    Page<BookResponseDTO> searchBooks(String query, Long categoryId, Double minRating, Pageable pageable);

    Page<BookResponseDTO> searchByCategory(String categoryName, Pageable pageable);

    Page<BookResponseDTO> searchByAuthor(String author, Pageable pageable);

    Page<BookResponseDTO> searchByKeyword(String keyword, Pageable pageable);

    BookResponseDTO addBook(BookRequestDTO request);

    BookResponseDTO updateBook(Long bookId, BookRequestDTO request);

    void deleteBook(Long bookId);

    List<BookSummaryDTO> getTrendingBooks(int limit);

    List<BookSummaryDTO> getRecommendedBooks(Long userId, int limit);

    BookResponseDTO uploadBookFile(Long bookId, org.springframework.web.multipart.MultipartFile file);
}
