package com.smartlibrary.controller;

import com.smartlibrary.dto.ApiResponse;
import com.smartlibrary.dto.ReviewRequestDTO;
import com.smartlibrary.dto.ReviewResponseDTO;
import com.smartlibrary.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.smartlibrary.security.CustomUserDetails;

/**
 * REST Controller for book user ratings and reviews.
 */
@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Retrieves all reviews for a specific book.
     *
     * @param bookId Target Book ID.
     * @param page Page index.
     * @param size Page size.
     * @return Paginated book reviews.
     */
    @GetMapping("/book/{bookId}")
    public ResponseEntity<ApiResponse<Page<ReviewResponseDTO>>> getReviewsByBook(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Pageable pageable = PageRequest.of(page, size);
        Long userId = userDetails != null ? userDetails.getId() : null;
        Page<ReviewResponseDTO> reviews = reviewService.getReviewsByBook(bookId, userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Book reviews retrieved successfully", reviews));
    }

    /**
     * Submits a new review for a book.
     *
     * @param request Review request payload.
     * @return Created review response.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponseDTO>> addReview(@Valid @RequestBody ReviewRequestDTO request) {
        ReviewResponseDTO response = reviewService.addReview(request);
        return new ResponseEntity<>(ApiResponse.success("Review submitted successfully", response), HttpStatus.CREATED);
    }

    /**
     * Updates an existing review rating or comment.
     *
     * @param id Review primary key ID.
     * @param request Review update payload.
     * @return Updated review response.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ReviewResponseDTO>> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequestDTO request) {
        ReviewResponseDTO response = reviewService.updateReview(id, request);
        return ResponseEntity.ok(ApiResponse.success("Review updated successfully", response));
    }

    /**
     * Deletes a review.
     *
     * @param id Review ID.
     * @return Deletion status message.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok(ApiResponse.success("Review deleted successfully", "SUCCESS"));
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Void> likeReview(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails != null) {
            reviewService.likeReview(id, userDetails.getId());
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<Void> unlikeReview(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails != null) {
            reviewService.unlikeReview(id, userDetails.getId());
        }
        return ResponseEntity.ok().build();
    }
}
