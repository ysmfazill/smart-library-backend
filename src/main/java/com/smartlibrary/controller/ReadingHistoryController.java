package com.smartlibrary.controller;

import com.smartlibrary.dto.ApiResponse;
import com.smartlibrary.dto.ReadingHistoryRequestDTO;
import com.smartlibrary.dto.ReadingHistoryResponseDTO;
import com.smartlibrary.service.ReadingHistoryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for reading progress tracking and history logs.
 */
@RestController
@RequestMapping("/history")
public class ReadingHistoryController {

    private final ReadingHistoryService readingHistoryService;

    public ReadingHistoryController(ReadingHistoryService readingHistoryService) {
        this.readingHistoryService = readingHistoryService;
    }

    /**
     * Retrieves reading history records for a user.
     *
     * @param userId User ID.
     * @param page Page index.
     * @param size Page size.
     * @return Paginated reading history items.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ReadingHistoryResponseDTO>>> getReadingHistory(
            @RequestParam(defaultValue = "1") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReadingHistoryResponseDTO> history = readingHistoryService.getReadingHistory(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Reading history retrieved successfully", history));
    }

    /**
     * Saves or logs initial reading progress.
     *
     * @param request Progress request payload.
     * @return Created reading history item.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ReadingHistoryResponseDTO>> saveReadingProgress(
            @Valid @RequestBody ReadingHistoryRequestDTO request) {
        ReadingHistoryResponseDTO response = readingHistoryService.saveReadingProgress(request);
        return new ResponseEntity<>(ApiResponse.success("Reading progress saved", response), HttpStatus.CREATED);
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<ApiResponse<ReadingHistoryResponseDTO>> getReadingProgressByBook(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "1") Long userId) {
        ReadingHistoryResponseDTO progress = readingHistoryService.getReadingProgressByBookId(userId, bookId);
        return ResponseEntity.ok(ApiResponse.success("Book reading progress retrieved", progress));
    }

    /**
     * Updates reading progress for a specific book.
     *
     * @param bookId Target Book ID.
     * @param userId User ID parameter.
     * @param progressPercentage Percentage completed (0.0 to 100.0).
     * @param currentPage Optional current page number.
     * @param totalPages Optional total pages count.
     * @return Updated reading history item.
     */
    @PutMapping("/{bookId}")
    public ResponseEntity<ApiResponse<ReadingHistoryResponseDTO>> updateProgress(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "1") Long userId,
            @RequestParam(required = false) Double progressPercentage,
            @RequestParam(required = false) Integer currentPage,
            @RequestParam(required = false) Integer totalPages) {
        ReadingHistoryResponseDTO response = readingHistoryService.updateReadingProgress(userId, bookId, progressPercentage, currentPage, totalPages);
        return ResponseEntity.ok(ApiResponse.success("Reading progress updated successfully", response));
    }
}
