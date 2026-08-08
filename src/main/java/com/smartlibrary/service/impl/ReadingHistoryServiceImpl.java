package com.smartlibrary.service.impl;

import com.smartlibrary.dto.ReadingHistoryRequestDTO;
import com.smartlibrary.dto.ReadingHistoryResponseDTO;
import com.smartlibrary.entity.Book;
import com.smartlibrary.entity.ReadingHistory;
import com.smartlibrary.entity.User;
import com.smartlibrary.exception.ResourceNotFoundException;
import com.smartlibrary.mapper.ReadingHistoryMapper;
import com.smartlibrary.repository.BookRepository;
import com.smartlibrary.repository.ReadingHistoryRepository;
import com.smartlibrary.repository.UserRepository;
import com.smartlibrary.service.CacheEvictionService;
import com.smartlibrary.service.ReadingHistoryService;
import com.smartlibrary.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReadingHistoryServiceImpl implements ReadingHistoryService {

    private final ReadingHistoryRepository readingHistoryRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final ReadingHistoryMapper readingHistoryMapper;
    private final CacheEvictionService cacheEvictionService;
    private final StatisticsService statisticsService;

    public ReadingHistoryServiceImpl(ReadingHistoryRepository readingHistoryRepository,
                                     UserRepository userRepository,
                                     BookRepository bookRepository,
                                     ReadingHistoryMapper readingHistoryMapper,
                                     CacheEvictionService cacheEvictionService,
                                     StatisticsService statisticsService) {
        this.readingHistoryRepository = readingHistoryRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.readingHistoryMapper = readingHistoryMapper;
        this.cacheEvictionService = cacheEvictionService;
        this.statisticsService = statisticsService;
    }

    @Override
    @Transactional
    public ReadingHistoryResponseDTO saveReadingProgress(ReadingHistoryRequestDTO request) {
        log.info("Saving reading progress for user ID: {}, book ID: {}", request.getUserId(), request.getBookId());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", request.getBookId()));

        Optional<ReadingHistory> existingHistory = readingHistoryRepository
                .findByUserIdAndBookId(request.getUserId(), request.getBookId());

        ReadingHistory history;
        double oldProgress = 0.0;
        boolean wasCompleted = false;

        if (existingHistory.isPresent()) {
            history = existingHistory.get();
            oldProgress = history.getProgressPercentage() != null ? history.getProgressPercentage() : 0.0;
            wasCompleted = Boolean.TRUE.equals(history.getCompleted());

            if (request.getProgressPercentage() != null) {
                history.setProgressPercentage(request.getProgressPercentage());
            }
            if (request.getCurrentPage() != null) {
                history.setCurrentPage(request.getCurrentPage());
            }
            if (request.getTotalPages() != null) {
                history.setTotalPages(request.getTotalPages());
            }
            if (request.getStatus() != null) {
                history.setStatus(request.getStatus());
            }
            if (request.getCompleted() != null) {
                history.setCompleted(request.getCompleted());
            }
        } else {
            history = readingHistoryMapper.toEntity(request);
            history.setUser(user);
            history.setBook(book);
        }

        ReadingHistory savedHistory = readingHistoryRepository.save(history);
        log.info("Saved reading progress ID: {}", savedHistory.getId());
        cacheEvictionService.evictTrendingAndPopularCaches();

        updateStats(savedHistory, oldProgress, wasCompleted);

        return readingHistoryMapper.toResponseDTO(savedHistory);
    }

    @Override
    @Transactional
    public ReadingHistoryResponseDTO updateReadingProgress(Long userId, Long bookId, Double progressPercentage) {
        return updateReadingProgress(userId, bookId, progressPercentage, null, null);
    }

    @Override
    @Transactional
    public ReadingHistoryResponseDTO updateReadingProgress(Long userId, Long bookId, Double progressPercentage, Integer currentPage, Integer totalPages) {
        log.info("Updating reading progress for user ID: {}, book ID: {} to {}% page: {}", userId, bookId, progressPercentage, currentPage);

        ReadingHistory history = readingHistoryRepository.findByUserIdAndBookId(userId, bookId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
                    Book book = bookRepository.findById(bookId)
                            .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));
                    return ReadingHistory.builder()
                            .user(user)
                            .book(book)
                            .progressPercentage(0.0)
                            .currentPage(1)
                            .status("READING")
                            .completed(false)
                            .build();
                });

        double oldProgress = history.getProgressPercentage() != null ? history.getProgressPercentage() : 0.0;
        boolean wasCompleted = Boolean.TRUE.equals(history.getCompleted());

        if (progressPercentage != null) {
            history.setProgressPercentage(progressPercentage);
        }
        if (currentPage != null) {
            history.setCurrentPage(currentPage);
        }
        if (totalPages != null) {
            history.setTotalPages(totalPages);
        }

        if (history.getProgressPercentage() != null && history.getProgressPercentage() >= 100.0) {
            history.setCompleted(true);
            history.setStatus("COMPLETED");
        }

        ReadingHistory savedHistory = readingHistoryRepository.save(history);
        cacheEvictionService.evictTrendingAndPopularCaches();

        updateStats(savedHistory, oldProgress, wasCompleted);

        return readingHistoryMapper.toResponseDTO(savedHistory);
    }

    @Override
    @Transactional(readOnly = true)
    public ReadingHistoryResponseDTO getReadingProgressByBookId(Long userId, Long bookId) {
        Optional<ReadingHistory> history = readingHistoryRepository.findByUserIdAndBookId(userId, bookId);
        return history.map(readingHistoryMapper::toResponseDTO).orElse(null);
    }

    private void updateStats(ReadingHistory history, double oldProgress, boolean wasCompleted) {
        double newProgress = history.getProgressPercentage() != null ? history.getProgressPercentage() : 0.0;
        boolean isCompleted = history.getCompleted() != null ? history.getCompleted() : false;

        double progressDiff = newProgress - oldProgress;
        if (progressDiff < 0) progressDiff = 0;

        int totalPages = history.getBook().getPages() != null ? history.getBook().getPages() : 300;
        int pagesRead = (int) Math.round((progressDiff / 100.0) * totalPages);

        boolean newlyCompleted = isCompleted && !wasCompleted;

        if (pagesRead > 0 || newlyCompleted) {
            statisticsService.updateReadingStats(history.getUser().getId(), pagesRead, newlyCompleted);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReadingHistoryResponseDTO> getReadingHistory(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        return readingHistoryRepository.findByUserIdOrderByLastReadDateDesc(userId).stream()
                .map(readingHistoryMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReadingHistoryResponseDTO> getReadingHistory(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        return readingHistoryRepository.findByUserIdOrderByLastReadDateDesc(userId, pageable)
                .map(readingHistoryMapper::toResponseDTO);
    }

    @Override
    @Transactional
    public ReadingHistoryResponseDTO markBookCompleted(Long userId, Long bookId) {
        log.info("Marking book ID: {} as completed for user ID: {}", bookId, userId);
        return updateReadingProgress(userId, bookId, 100.0);
    }
}
