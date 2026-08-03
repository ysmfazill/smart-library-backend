package com.smartlibrary.service;

import com.smartlibrary.dto.RecommendationDTO;
import java.util.List;

public interface RecommendationService {

    List<RecommendationDTO> getPersonalizedRecommendations(Long userId, int limit);

    List<RecommendationDTO> getRecommendationsByInterests(Long userId, int limit);

    List<RecommendationDTO> getRecommendationsByHistory(Long userId, int limit);

    List<RecommendationDTO> getRecommendationsByFavorites(Long userId, int limit);

    List<RecommendationDTO> getTrendingBooks(int limit);

    List<RecommendationDTO> getPopularBooks(int limit);

    List<RecommendationDTO> getNewArrivals(int limit);
    List<RecommendationDTO> getContinueReading(Long userId, int limit);

    List<RecommendationDTO> getRecentlyViewed(Long userId, int limit);

    List<RecommendationDTO> getHighestRated(int limit);

    List<RecommendationDTO> getReadersAlsoLikedForUser(Long userId, int limit);

    List<RecommendationDTO> getSimilarBooks(Long bookId, int limit);

    List<RecommendationDTO> getReadersAlsoLiked(Long bookId, int limit);
}
