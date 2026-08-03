package com.smartlibrary.service;

import com.smartlibrary.dto.ReviewRequestDTO;
import com.smartlibrary.dto.ReviewResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReviewService {

    ReviewResponseDTO addReview(ReviewRequestDTO request);

    ReviewResponseDTO updateReview(Long reviewId, ReviewRequestDTO request);

    void deleteReview(Long reviewId);

    List<ReviewResponseDTO> getReviewsByBook(Long bookId, Long currentUserId);

    Page<ReviewResponseDTO> getReviewsByBook(Long bookId, Long currentUserId, Pageable pageable);

    void likeReview(Long reviewId, Long userId);

    void unlikeReview(Long reviewId, Long userId);
}
