package com.smartlibrary.controller;

import com.smartlibrary.dto.ApiResponse;
import com.smartlibrary.dto.BookRequestDTO;
import com.smartlibrary.dto.BookResponseDTO;
import com.smartlibrary.dto.UserResponseDTO;
import com.smartlibrary.service.BookService;
import com.smartlibrary.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for library administration dashboard and management endpoints.
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final BookService bookService;
    private final com.smartlibrary.repository.BookRepository bookRepository;
    private final com.smartlibrary.repository.ReadingHistoryRepository readingHistoryRepository;
    private final com.smartlibrary.repository.CategoryRepository categoryRepository;

    public AdminController(UserService userService, BookService bookService,
                           com.smartlibrary.repository.BookRepository bookRepository,
                           com.smartlibrary.repository.ReadingHistoryRepository readingHistoryRepository,
                           com.smartlibrary.repository.CategoryRepository categoryRepository) {
        this.userService = userService;
        this.bookService = bookService;
        this.bookRepository = bookRepository;
        this.readingHistoryRepository = readingHistoryRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Retrieves administrative dashboard summary analytics.
     *
     * @return Dashboard analytics summary.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userService.getAllUsers().size());
        stats.put("totalBooks", bookService.getAllBooks().size());
        stats.put("systemStatus", "OPERATIONAL");
        return ResponseEntity.ok(ApiResponse.success("Admin dashboard stats retrieved successfully", stats));
    }

    /**
     * Retrieves all registered library users for administration management.
     *
     * @return List of all users.
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getAllUsers() {
        List<UserResponseDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("Users retrieved for admin successfully", users));
    }

    /**
     * Retrieves paginated list of all books for administration inventory.
     *
     * @param page Page index.
     * @param size Page size.
     * @return Paginated book records.
     */
    @GetMapping("/books")
    public ResponseEntity<ApiResponse<Page<BookResponseDTO>>> getAllBooks(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BookResponseDTO> books;
        if (search != null && !search.trim().isEmpty()) {
            books = bookService.searchBooks(search, null, null, pageable);
        } else {
            books = bookService.getAllBooks(pageable);
        }
        return ResponseEntity.ok(ApiResponse.success("Books retrieved for admin successfully", books));
    }

    /**
     * Retrieves global library metrics and usage statistics.
     *
     * @return System statistics payload.
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("activeBorrowers", userService.getAllUsers().size());
        stats.put("inventoryTotal", bookService.getAllBooks().size());
        stats.put("recommendationEngineStatus", "READY");
        return ResponseEntity.ok(ApiResponse.success("System statistics retrieved successfully", stats));
    }

    @GetMapping("/statistics/recommendations")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRecommendationStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRecommendationsGenerated", readingHistoryRepository.count() * 3); // Dummy metric based on history
        stats.put("activeAlgorithm", "Aggregator Pattern (History + Favorites + Interests)");
        stats.put("cacheStatus", "ENABLED");
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/statistics/popular-categories")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPopularCategories() {
        // Just return top categories based on total books in those categories for now
        List<Map<String, Object>> result = categoryRepository.findAll().stream()
            .map(c -> {
                Map<String, Object> map = new HashMap<>();
                map.put("name", c.getName());
                map.put("count", bookRepository.findByCategory_Name(c.getName()).size());
                return map;
            })
            .sorted((a, b) -> Integer.compare((Integer) b.get("count"), (Integer) a.get("count")))
            .limit(5)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/statistics/popular-authors")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPopularAuthors() {
        List<Map<String, Object>> result = bookRepository.findPopularAuthors(PageRequest.of(0, 5)).getContent().stream()
            .map(obj -> {
                Map<String, Object> map = new HashMap<>();
                map.put("author", obj[0]);
                map.put("count", obj[1]);
                return map;
            }).toList();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Administrative endpoint to add a new book to the library inventory.
     *
     * @param request Book payload.
     * @return Created book response DTO.
     */
    @PostMapping("/books")
    public ResponseEntity<ApiResponse<BookResponseDTO>> addBook(@Valid @RequestBody BookRequestDTO request) {
        BookResponseDTO createdBook = bookService.addBook(request);
        return new ResponseEntity<>(ApiResponse.success("Book added by admin successfully", createdBook), HttpStatus.CREATED);
    }

    /**
     * Administrative endpoint to update a book in inventory.
     *
     * @param id Book ID.
     * @param request Update payload.
     * @return Updated book response DTO.
     */
    @PutMapping("/books/{id}")
    public ResponseEntity<ApiResponse<BookResponseDTO>> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookRequestDTO request) {
        BookResponseDTO updatedBook = bookService.updateBook(id, request);
        return ResponseEntity.ok(ApiResponse.success("Book updated by admin successfully", updatedBook));
    }

    /**
     * Administrative endpoint to remove a book from inventory.
     *
     * @param id Book ID.
     * @return Deletion status message.
     */
    @DeleteMapping("/books/{id}")
    public ResponseEntity<ApiResponse<String>> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.ok(ApiResponse.success("Book deleted by admin successfully", "SUCCESS"));
    }
}
