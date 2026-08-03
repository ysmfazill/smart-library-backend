package com.smartlibrary.mapper;

import com.smartlibrary.dto.ReviewRequestDTO;
import com.smartlibrary.dto.ReviewResponseDTO;
import com.smartlibrary.entity.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewResponseDTO toResponseDTO(Review review) {
        if (review == null) {
            return null;
        }
        return ReviewResponseDTO.builder()
                .id(review.getId())
                .userId(review.getUser() != null ? review.getUser().getId() : null)
                .userName(review.getUser() != null ? review.getUser().getFullName() : null)
                .bookId(review.getBook() != null ? review.getBook().getId() : null)
                .bookTitle(review.getBook() != null ? review.getBook().getTitle() : null)
                .rating(review.getRating())
                .comment(review.getComment())
                .reviewDate(review.getReviewDate())
                .userAvatar(review.getUser() != null ? review.getUser().getAvatar() : null)
                .build();
    }

    public Review toEntity(ReviewRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        return Review.builder()
                .rating(dto.getRating())
                .comment(dto.getComment())
                .build();
    }
}
