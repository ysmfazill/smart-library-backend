package com.smartlibrary.service;

import com.smartlibrary.dto.FavoriteRequestDTO;
import com.smartlibrary.dto.FavoriteResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FavoriteService {

    FavoriteResponseDTO addFavorite(FavoriteRequestDTO request);

    void removeFavorite(Long userId, Long bookId);

    List<FavoriteResponseDTO> getFavoritesByUser(Long userId);

    Page<FavoriteResponseDTO> getFavoritesByUser(Long userId, Pageable pageable);

    boolean isFavorite(Long userId, Long bookId);
}
