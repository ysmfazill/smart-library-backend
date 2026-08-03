package com.smartlibrary.mapper;

import com.smartlibrary.dto.FavoriteResponseDTO;
import com.smartlibrary.entity.Favorite;
import org.springframework.stereotype.Component;

@Component
public class FavoriteMapper {

    private final BookMapper bookMapper;

    public FavoriteMapper(BookMapper bookMapper) {
        this.bookMapper = bookMapper;
    }

    public FavoriteResponseDTO toResponseDTO(Favorite favorite) {
        if (favorite == null) {
            return null;
        }
        return FavoriteResponseDTO.builder()
                .id(favorite.getId())
                .userId(favorite.getUser() != null ? favorite.getUser().getId() : null)
                .userName(favorite.getUser() != null ? favorite.getUser().getFullName() : null)
                .book(bookMapper.toSummaryDTO(favorite.getBook()))
                .addedAt(favorite.getAddedAt())
                .build();
    }
}
