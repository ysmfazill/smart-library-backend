package com.smartlibrary.service.impl;

import com.smartlibrary.dto.ReviewRequestDTO;
import com.smartlibrary.dto.ReviewResponseDTO;
import com.smartlibrary.entity.Book;
import com.smartlibrary.entity.Review;
import com.smartlibrary.entity.User;
import com.smartlibrary.exception.BadRequestException;
import com.smartlibrary.exception.ResourceNotFoundException;
import com.smartlibrary.mapper.ReviewMapper;
import com.smartlibrary.repository.BookRepository;
import com.smartlibrary.repository.ReviewRepository;
import com.smartlibrary.repository.ReviewLikeRepository;
import com.smartlibrary.repository.UserRepository;
import com.smartlibrary.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final ReviewMapper reviewMapper;

    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             ReviewLikeRepository reviewLikeRepository,
                             UserRepository userRepository,
                             BookRepository bookRepository,
                             ReviewMapper reviewMapper) {
        this.reviewRepository = reviewRepository;
        this.reviewLikeRepository = reviewLikeRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.reviewMapper = reviewMapper;
    }

    @Override
    @Transactional
    public ReviewResponseDTO addReview(ReviewRequestDTO request) {
        log.info("Adding review for user ID: {}, book ID: {}", request.getUserId(), request.getBookId());

        if (reviewRepository.existsByUserIdAndBookId(request.getUserId(), request.getBookId())) {
            throw new BadRequestException("User has already reviewed this book");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", request.getBookId()));

        Review review = reviewMapper.toEntity(request);
        review.setUser(user);
        review.setBook(book);

        Review savedReview = reviewRepository.save(review);
        updateBookAverageRating(book.getId());

        log.info("Review ID: {} added successfully", savedReview.getId());
        return enrichWithLikes(reviewMapper.toResponseDTO(savedReview), request.getUserId());
    }

    @Override
    @Transactional
    public ReviewResponseDTO updateReview(Long reviewId, ReviewRequestDTO request) {
        log.info("Updating review ID: {}", reviewId);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }
        if (request.getComment() != null) {
            review.setComment(request.getComment());
        }

        Review updatedReview = reviewRepository.save(review);
        updateBookAverageRating(review.getBook().getId());

        log.info("Review ID: {} updated successfully", reviewId);
        // Note: passing null for user ID here since update doesn't usually return full enriched view, 
        // or we could extract it from context. Let's just enrich it assuming the updater is the current user.
        return enrichWithLikes(reviewMapper.toResponseDTO(updatedReview), review.getUser().getId());
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        log.info("Deleting review ID: {}", reviewId);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        Long bookId = review.getBook().getId();
        reviewRepository.delete(review);
        updateBookAverageRating(bookId);

        log.info("Review ID: {} deleted", reviewId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getReviewsByBook(Long bookId, Long currentUserId) {
        if (!bookRepository.existsById(bookId)) {
            throw new ResourceNotFoundException("Book", "id", bookId);
        }
        return reviewRepository.findByBookId(bookId).stream()
                .map(reviewMapper::toResponseDTO)
                .map(dto -> enrichWithLikes(dto, currentUserId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponseDTO> getReviewsByBook(Long bookId, Long currentUserId, Pageable pageable) {
        if (!bookRepository.existsById(bookId)) {
            throw new ResourceNotFoundException("Book", "id", bookId);
        }
        return reviewRepository.findByBookId(bookId, pageable)
                .map(reviewMapper::toResponseDTO)
                .map(dto -> enrichWithLikes(dto, currentUserId));
    }

    private void updateBookAverageRating(Long bookId) {
        Double avgRating = reviewRepository.getAverageRatingByBookId(bookId);
        bookRepository.findById(bookId).ifPresent(book -> {
            book.setRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);
            bookRepository.save(book);
        });
    }

    private ReviewResponseDTO enrichWithLikes(ReviewResponseDTO dto, Long currentUserId) {
        if (dto == null) return null;
        dto.setLikesCount(reviewLikeRepository.countByReviewId(dto.getId()));
        if (currentUserId != null) {
            dto.setLikedByCurrentUser(reviewLikeRepository.findByUserIdAndReviewId(currentUserId, dto.getId()).isPresent());
        } else {
            dto.setLikedByCurrentUser(false);
        }
        return dto;
    }

    @Override
    @Transactional
    public void likeReview(Long reviewId, Long userId) {
        if (reviewLikeRepository.findByUserIdAndReviewId(userId, reviewId).isEmpty()) {
            User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
            Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));
            
            com.smartlibrary.entity.ReviewLike like = com.smartlibrary.entity.ReviewLike.builder()
                    .user(user)
                    .review(review)
                    .build();
            reviewLikeRepository.save(like);
        }
    }

    @Override
    @Transactional
    public void unlikeReview(Long reviewId, Long userId) {
        reviewLikeRepository.findByUserIdAndReviewId(userId, reviewId)
                .ifPresent(reviewLikeRepository::delete);
    }
}
