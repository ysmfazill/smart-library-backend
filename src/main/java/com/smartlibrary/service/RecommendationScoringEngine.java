package com.smartlibrary.service;

import com.smartlibrary.dto.RecommendationDTO;
import com.smartlibrary.entity.Book;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class RecommendationScoringEngine {

    // Weight factors
    private static final double WEIGHT_HISTORY = 35.0;
    private static final double WEIGHT_FAVORITES = 25.0;
    private static final double WEIGHT_INTERESTS = 20.0;
    private static final double WEIGHT_SIMILAR = 10.0;
    private static final double WEIGHT_TRENDING = 5.0;
    private static final double WEIGHT_NEWEST = 5.0;

    public List<RecommendationDTO> scoreAndSort(
            List<Book> historyBooks,
            List<Book> favoriteBooks,
            List<Book> interestBooks,
            List<Book> similarBooks,
            List<Book> trendingBooks,
            List<Book> newestBooks,
            int limit) {

        Map<Long, ScoredBook> scoreMap = new HashMap<>();

        // Helper to accumulate scores
        addScores(scoreMap, historyBooks, WEIGHT_HISTORY, "Based on books you read");
        addScores(scoreMap, favoriteBooks, WEIGHT_FAVORITES, "Similar to your favorite books");
        addScores(scoreMap, interestBooks, WEIGHT_INTERESTS, "Matches your interests");
        addScores(scoreMap, similarBooks, WEIGHT_SIMILAR, "Similar to books you interacted with");
        addScores(scoreMap, trendingBooks, WEIGHT_TRENDING, "Trending right now");
        addScores(scoreMap, newestBooks, WEIGHT_NEWEST, "Recently added");

        // Convert to list, sort by score descending, limit, and map to DTO
        return scoreMap.values().stream()
                .sorted(Comparator.comparing(ScoredBook::getScore).reversed())
                .limit(limit)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private void addScores(Map<Long, ScoredBook> scoreMap, List<Book> books, double weight, String reason) {
        if (books == null) return;
        for (Book book : books) {
            ScoredBook scoredBook = scoreMap.computeIfAbsent(book.getId(), id -> new ScoredBook(book));
            scoredBook.addScore(weight, reason);
        }
    }

    private RecommendationDTO toDTO(ScoredBook scoredBook) {
        Book book = scoredBook.getBook();
        return RecommendationDTO.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .coverImage(book.getCoverImage())
                .rating(book.getRating())
                .categoryName(book.getCategory() != null ? book.getCategory().getName() : null)
                .recommendationScore(scoredBook.getScore())
                .recommendationReason(scoredBook.getBestReason())
                .build();
    }

    private static class ScoredBook {
        private final Book book;
        private double totalScore = 0.0;
        private double maxWeight = 0.0;
        private String bestReason = "Recommended for you";

        public ScoredBook(Book book) {
            this.book = book;
        }

        public void addScore(double weight, String reason) {
            this.totalScore += weight;
            if (weight > maxWeight) {
                this.maxWeight = weight;
                this.bestReason = reason;
            }
        }

        public Book getBook() {
            return book;
        }

        public double getScore() {
            return totalScore;
        }

        public String getBestReason() {
            return bestReason;
        }
    }
}
