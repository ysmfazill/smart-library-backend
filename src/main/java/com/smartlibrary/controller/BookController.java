package com.smartlibrary.controller;

import com.smartlibrary.dto.ApiResponse;
import com.smartlibrary.dto.BookRequestDTO;
import com.smartlibrary.dto.BookResponseDTO;
import com.smartlibrary.dto.BookSummaryDTO;
import com.smartlibrary.service.BookService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for book management, search, and recommendation operations.
 */
@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;
    private final com.smartlibrary.service.FileStorageService fileStorageService;

    public BookController(BookService bookService, com.smartlibrary.service.FileStorageService fileStorageService) {
        this.bookService = bookService;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Retrieves all books with pagination support.
     *
     * @param page Page index (0-indexed).
     * @param size Page size.
     * @return Paginated book records.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<BookResponseDTO>>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BookResponseDTO> books = bookService.getAllBooks(pageable);
        return ResponseEntity.ok(ApiResponse.success("Books retrieved successfully", books));
    }

    /**
     * Fetches book details by ID.
     *
     * @param id Book primary key.
     * @return Book details.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponseDTO>> getBookById(@PathVariable Long id) {
        BookResponseDTO book = bookService.getBookById(id);
        return ResponseEntity.ok(ApiResponse.success("Book retrieved successfully", book));
    }

    /**
     * Streams digital book content file (PDF) for a book.
     */
    @GetMapping("/{id}/file")
    public ResponseEntity<org.springframework.core.io.Resource> getBookFile(@PathVariable Long id) {
        BookResponseDTO book = bookService.getBookById(id);
        if (book.getBookFileUrl() == null || book.getBookFileUrl().isBlank()) {
            return ResponseEntity.notFound().build();
        }

        // If URL is external (http/https), redirect or return not found for stream
        if (book.getBookFileUrl().startsWith("http://") || book.getBookFileUrl().startsWith("https://")) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FOUND)
                    .location(java.net.URI.create(book.getBookFileUrl()))
                    .build();
        }

        org.springframework.core.io.Resource resource = fileStorageService.loadFileAsResource(book.getBookFileUrl());
        String contentType = book.getBookFileType() != null ? book.getBookFileType() : "application/pdf";
        String filename = book.getBookFileName() != null ? book.getBookFileName() : "book.pdf";

        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .header(org.springframework.http.HttpHeaders.ACCEPT_RANGES, "bytes")
                .body(resource);
    }

    /**
     * Dynamic search endpoint supporting text query, category ID, and minimum rating filters.
     *
     * @param query Search query string.
     * @param categoryId Optional Category ID.
     * @param minRating Optional minimum rating score.
     * @param page Page index.
     * @param size Page size.
     * @return Paginated search results.
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<BookResponseDTO>>> searchBooks(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BookResponseDTO> results = bookService.searchBooks(query, categoryId, minRating, pageable);
        return ResponseEntity.ok(ApiResponse.success("Search completed successfully", results));
    }

    /**
     * Retrieves books belonging to a specific category.
     *
     * @param category Category name.
     * @param page Page index.
     * @param size Page size.
     * @return Paginated books by category.
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<Page<BookResponseDTO>>> getBooksByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BookResponseDTO> books = bookService.searchByCategory(category, pageable);
        return ResponseEntity.ok(ApiResponse.success("Category books retrieved successfully", books));
    }

    /**
     * Retrieves top trending books based on popularity and ratings.
     *
     * @param limit Maximum number of books to retrieve.
     * @return List of trending book summaries.
     */
    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<List<BookSummaryDTO>>> getTrendingBooks(
            @RequestParam(defaultValue = "10") int limit) {
        List<BookSummaryDTO> books = bookService.getTrendingBooks(limit);
        return ResponseEntity.ok(ApiResponse.success("Trending books retrieved successfully", books));
    }

    /**
     * Gets personalized recommended books for a specific user.
     *
     * @param userId User ID parameter.
     * @param limit Limit count.
     * @return List of recommended book summaries.
     */
    @GetMapping("/recommended")
    public ResponseEntity<ApiResponse<List<BookSummaryDTO>>> getRecommendedBooks(
            @RequestParam(defaultValue = "1") Long userId,
            @RequestParam(defaultValue = "10") int limit) {
        List<BookSummaryDTO> books = bookService.getRecommendedBooks(userId, limit);
        return ResponseEntity.ok(ApiResponse.success("Recommended books retrieved successfully", books));
    }

    /**
     * Adds a new book entry to the library.
     *
     * @param request Book payload.
     * @return Created book details.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BookResponseDTO>> addBook(@Valid @RequestBody BookRequestDTO request) {
        BookResponseDTO createdBook = bookService.addBook(request);
        return new ResponseEntity<>(ApiResponse.success("Book created successfully", createdBook), HttpStatus.CREATED);
    }

    /**
     * Updates an existing book entry.
     *
     * @param id Book ID.
     * @param request Book payload.
     * @return Updated book details.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponseDTO>> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookRequestDTO request) {
        BookResponseDTO updatedBook = bookService.updateBook(id, request);
        return ResponseEntity.ok(ApiResponse.success("Book updated successfully", updatedBook));
    }

    /**
     * Removes a book entry from the library.
     *
     * @param id Book ID.
     * @return Deletion status response.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.ok(ApiResponse.success("Book deleted successfully", "SUCCESS"));
    }
}
