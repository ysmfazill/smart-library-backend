package com.smartlibrary.service.impl;

import com.smartlibrary.dto.RecommendationDTO;
import com.smartlibrary.entity.*;
import com.smartlibrary.repository.*;
import com.smartlibrary.service.RecommendationScoringEngine;
import com.smartlibrary.service.RecommendationService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    private final BookRepository bookRepository;
    private final ReadingHistoryRepository readingHistoryRepository;
    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final RecommendationScoringEngine scoringEngine;

    public RecommendationServiceImpl(BookRepository bookRepository,
                                     ReadingHistoryRepository readingHistoryRepository,
                                     FavoriteRepository favoriteRepository,
                                     UserRepository userRepository,
                                     RecommendationScoringEngine scoringEngine) {
        this.bookRepository = bookRepository;
        this.readingHistoryRepository = readingHistoryRepository;
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.scoringEngine = scoringEngine;
    }

    @Override
    public List<RecommendationDTO> getPersonalizedRecommendations(Long userId, int limit) {
        List<Book> historyBooks = fetchHistoryBooks(userId);
        List<Book> favoriteBooks = fetchFavoriteBooks(userId);
        List<Book> interestBooks = fetchInterestBooks(userId);
        
        List<Book> similarBooks = new ArrayList<>();
        if (!favoriteBooks.isEmpty()) {
            similarBooks.addAll(bookRepository.findByCategoryId(favoriteBooks.get(0).getCategory().getId(), PageRequest.of(0, 10)).getContent());
        }

        List<Book> trendingBooks = fetchTrendingBooks(10);
        List<Book> newestBooks = fetchNewestBooks(10);

        List<RecommendationDTO> scored = scoringEngine.scoreAndSort(
                historyBooks, favoriteBooks, interestBooks, similarBooks, trendingBooks, newestBooks, limit * 2);

        // Deduplicate and remove already read
        List<Long> readIds = readingHistoryRepository.findByUserId(userId).stream()
                .map(rh -> rh.getBook().getId()).collect(Collectors.toList());

        return scored.stream()
                .filter(dto -> !readIds.contains(dto.getId()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecommendationDTO> getRecommendationsByInterests(Long userId, int limit) {
        List<Book> interestBooks = fetchInterestBooks(userId);
        return scoringEngine.scoreAndSort(null, null, interestBooks, null, fetchTrendingBooks(10), fetchNewestBooks(10), limit);
    }

    @Override
    public List<RecommendationDTO> getRecommendationsByHistory(Long userId, int limit) {
        List<Book> historyBooks = fetchHistoryBooks(userId);
        return scoringEngine.scoreAndSort(historyBooks, null, null, null, null, null, limit);
    }

    @Override
    public List<RecommendationDTO> getRecommendationsByFavorites(Long userId, int limit) {
        List<Book> favoriteBooks = fetchFavoriteBooks(userId);
        List<Book> similarBooks = new ArrayList<>();
        for (Book fav : favoriteBooks) {
            similarBooks.addAll(bookRepository.findByCategoryId(fav.getCategory().getId(), PageRequest.of(0, 5)).getContent());
        }
        return scoringEngine.scoreAndSort(null, favoriteBooks, null, similarBooks, null, null, limit);
    }

    @Override
    @Cacheable("trendingBooks")
    public List<RecommendationDTO> getTrendingBooks(int limit) {
        return scoringEngine.scoreAndSort(null, null, null, null, fetchTrendingBooks(limit), null, limit);
    }

    @Override
    @Cacheable("popularBooks")
    public List<RecommendationDTO> getPopularBooks(int limit) {
        List<Book> popular = bookRepository.findPopularBooks(PageRequest.of(0, limit)).getContent();
        return scoringEngine.scoreAndSort(null, null, null, null, null, popular, limit);
    }

    @Override
    @Cacheable("newestBooks")
    public List<RecommendationDTO> getNewArrivals(int limit) {
        return scoringEngine.scoreAndSort(null, null, null, null, null, fetchNewestBooks(limit), limit);
    }

    @Override
    public List<RecommendationDTO> getContinueReading(Long userId, int limit) {
        List<Book> continueBooks = readingHistoryRepository.findByUserId(userId).stream()
                .filter(rh -> rh.getCompleted() != null && !rh.getCompleted())
                .sorted(Comparator.comparing(ReadingHistory::getLastReadDate).reversed())
                .map(ReadingHistory::getBook)
                .collect(Collectors.toList());
        
        return scoreAndReason(continueBooks, "Continue where you left off", limit);
    }

    @Override
    public List<RecommendationDTO> getRecentlyViewed(Long userId, int limit) {
        List<Book> recentBooks = readingHistoryRepository.findByUserIdOrderByLastReadDateDesc(userId, PageRequest.of(0, limit)).getContent().stream()
                .map(ReadingHistory::getBook)
                .collect(Collectors.toList());
        
        return scoreAndReason(recentBooks, "Recently viewed", limit);
    }

    @Override
    public List<RecommendationDTO> getHighestRated(int limit) {
        List<Book> highestRated = bookRepository.findByRatingGreaterThanEqual(0.0, PageRequest.of(0, limit, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "rating"))).getContent();
        return scoreAndReason(highestRated, "Highest rated overall", limit);
    }

    @Override
    public List<RecommendationDTO> getReadersAlsoLikedForUser(Long userId, int limit) {
        List<ReadingHistory> history = readingHistoryRepository.findByUserIdOrderByLastReadDateDesc(userId, PageRequest.of(0, 1)).getContent();
        if (history.isEmpty()) return Collections.emptyList();
        
        Book lastRead = history.get(0).getBook();
        List<Book> similar = bookRepository.findByCategoryId(lastRead.getCategory().getId(), PageRequest.of(0, limit * 2)).getContent();
        
        return scoreAndReason(
            similar.stream().filter(b -> !b.getId().equals(lastRead.getId())).collect(Collectors.toList()), 
            "Because you read " + lastRead.getTitle(), 
            limit
        );
    }
    
    private List<RecommendationDTO> scoreAndReason(List<Book> books, String reason, int limit) {
        return books.stream().limit(limit).map(book -> RecommendationDTO.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .coverImage(book.getCoverImage())
                .rating(book.getRating())
                .categoryName(book.getCategory() != null ? book.getCategory().getName() : null)
                .recommendationScore(100.0)
                .recommendationReason(reason)
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    public List<RecommendationDTO> getSimilarBooks(Long bookId, int limit) {
        return getReadersAlsoLiked(bookId, limit);
    }

    @Override
    public List<RecommendationDTO> getReadersAlsoLiked(Long bookId, int limit) {
        Optional<Book> targetOpt = bookRepository.findById(bookId);
        if (targetOpt.isEmpty()) return Collections.emptyList();
        Book target = targetOpt.get();

        List<Book> similar = bookRepository.findByCategoryId(target.getCategory().getId(), PageRequest.of(0, limit * 2)).getContent();
        return scoringEngine.scoreAndSort(null, null, null, similar, null, null, limit).stream()
                .filter(dto -> !dto.getId().equals(bookId))
                .limit(limit)
                .collect(Collectors.toList());
    }

    // --- Helper fetch methods ---

    private List<Book> fetchHistoryBooks(Long userId) {
        return readingHistoryRepository.findByUserId(userId).stream()
                .map(ReadingHistory::getBook).collect(Collectors.toList());
    }

    private List<Book> fetchFavoriteBooks(Long userId) {
        return favoriteRepository.findByUserId(userId).stream()
                .map(Favorite::getBook).collect(Collectors.toList());
    }

    private List<Book> fetchInterestBooks(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            List<Long> interestIds = userOpt.get().getUserInterests().stream()
                    .map(UserInterest::getId).collect(Collectors.toList());
            if (!interestIds.isEmpty()) {
                return bookRepository.findByCategoryIdIn(interestIds, PageRequest.of(0, 20)).getContent();
            }
        }
        return new ArrayList<>();
    }

    private List<Book> fetchTrendingBooks(int limit) {
        List<Long> trendingIds = readingHistoryRepository.findTrendingBookIds(PageRequest.of(0, limit)).getContent();
        if (trendingIds.isEmpty()) return new ArrayList<>();
        return bookRepository.findAllByIdInWithCategory(trendingIds);
    }

    private List<Book> fetchNewestBooks(int limit) {
        return bookRepository.findNewArrivals(PageRequest.of(0, limit)).getContent();
    }
}
