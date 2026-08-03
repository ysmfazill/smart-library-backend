package com.smartlibrary.service.impl;

import com.smartlibrary.dto.FavoriteRequestDTO;
import com.smartlibrary.dto.FavoriteResponseDTO;
import com.smartlibrary.entity.Book;
import com.smartlibrary.entity.Favorite;
import com.smartlibrary.entity.User;
import com.smartlibrary.exception.BadRequestException;
import com.smartlibrary.exception.ResourceNotFoundException;
import com.smartlibrary.mapper.FavoriteMapper;
import com.smartlibrary.repository.BookRepository;
import com.smartlibrary.repository.FavoriteRepository;
import com.smartlibrary.repository.UserRepository;
import com.smartlibrary.service.CacheEvictionService;
import com.smartlibrary.service.FavoriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final FavoriteMapper favoriteMapper;
    private final CacheEvictionService cacheEvictionService;

    public FavoriteServiceImpl(FavoriteRepository favoriteRepository,
                               UserRepository userRepository,
                               BookRepository bookRepository,
                               FavoriteMapper favoriteMapper,
                               CacheEvictionService cacheEvictionService) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.favoriteMapper = favoriteMapper;
        this.cacheEvictionService = cacheEvictionService;
    }

    @Override
    @Transactional
    public FavoriteResponseDTO addFavorite(FavoriteRequestDTO request) {
        log.info("Adding favorite for user ID: {}, book ID: {}", request.getUserId(), request.getBookId());

        if (favoriteRepository.existsByUserIdAndBookId(request.getUserId(), request.getBookId())) {
            throw new BadRequestException("Book is already in user's favorites");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", request.getBookId()));

        Favorite favorite = Favorite.builder()
                .user(user)
                .book(book)
                .build();

        Favorite savedFavorite = favoriteRepository.save(favorite);
        log.info("Favorite ID: {} added successfully", savedFavorite.getId());
        cacheEvictionService.evictTrendingAndPopularCaches();
        return favoriteMapper.toResponseDTO(savedFavorite);
    }

    @Override
    @Transactional
    public void removeFavorite(Long userId, Long bookId) {
        log.info("Removing favorite for user ID: {}, book ID: {}", userId, bookId);
        if (!favoriteRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new ResourceNotFoundException("Favorite not found for user ID " + userId + " and book ID " + bookId);
        }
        favoriteRepository.deleteByUserIdAndBookId(userId, bookId);
        log.info("Removed favorite for user ID: {}, book ID: {}", userId, bookId);
        cacheEvictionService.evictTrendingAndPopularCaches();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FavoriteResponseDTO> getFavoritesByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        return favoriteRepository.findByUserId(userId).stream()
                .map(favoriteMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FavoriteResponseDTO> getFavoritesByUser(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        return favoriteRepository.findByUserId(userId, pageable)
                .map(favoriteMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFavorite(Long userId, Long bookId) {
        return favoriteRepository.existsByUserIdAndBookId(userId, bookId);
    }
}
