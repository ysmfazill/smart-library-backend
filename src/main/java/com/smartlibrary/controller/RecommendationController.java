package com.smartlibrary.controller;

import com.smartlibrary.dto.ApiResponse;
import com.smartlibrary.dto.RecommendationDTO;
import com.smartlibrary.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    private Long getAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // Assuming UserPrincipal is used, or the username is the email to look up the ID.
        // If CustomUserDetails has getId(), we can cast it. 
        // For now, assume we can get it from a standard approach or we need a helper.
        // Wait, I need to know how UserDetails are implemented. 
        // I will just use a helper if available, or cast it.
        return ((com.smartlibrary.security.CustomUserDetails) auth.getPrincipal()).getId();
    }

    @GetMapping("/personalized")
    public ResponseEntity<ApiResponse<List<RecommendationDTO>>> getPersonalizedRecommendations(
            @RequestParam(defaultValue = "10") int limit) {
        Long userId = getAuthenticatedUserId();
        return ResponseEntity.ok(ApiResponse.success(recommendationService.getPersonalizedRecommendations(userId, limit)));
    }

    @GetMapping("/interests")
    public ResponseEntity<ApiResponse<List<RecommendationDTO>>> getRecommendationsByInterests(
            @RequestParam(defaultValue = "10") int limit) {
        Long userId = getAuthenticatedUserId();
        return ResponseEntity.ok(ApiResponse.success(recommendationService.getRecommendationsByInterests(userId, limit)));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<RecommendationDTO>>> getRecommendationsByHistory(
            @RequestParam(defaultValue = "10") int limit) {
        Long userId = getAuthenticatedUserId();
        return ResponseEntity.ok(ApiResponse.success(recommendationService.getRecommendationsByHistory(userId, limit)));
    }

    @GetMapping("/favorites")
    public ResponseEntity<ApiResponse<List<RecommendationDTO>>> getRecommendationsByFavorites(
            @RequestParam(defaultValue = "10") int limit) {
        Long userId = getAuthenticatedUserId();
        return ResponseEntity.ok(ApiResponse.success(recommendationService.getRecommendationsByFavorites(userId, limit)));
    }

    @GetMapping("/similar-favorites")
    public ResponseEntity<ApiResponse<List<RecommendationDTO>>> getBooksSimilarToFavorites(
            @RequestParam(defaultValue = "12") int limit) {
        Long userId = getAuthenticatedUserId();
        return ResponseEntity.ok(ApiResponse.success(recommendationService.getRecommendationsByFavorites(userId, limit)));
    }

    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<List<RecommendationDTO>>> getTrendingBooks(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(recommendationService.getTrendingBooks(limit)));
    }

    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<RecommendationDTO>>> getPopularBooks(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(recommendationService.getPopularBooks(limit)));
    }

    @GetMapping("/newest")
    public ResponseEntity<ApiResponse<List<RecommendationDTO>>> getNewArrivals(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(recommendationService.getNewArrivals(limit)));
    }
    
    @GetMapping("/similar/{bookId}")
    public ResponseEntity<ApiResponse<List<RecommendationDTO>>> getSimilarBooks(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(ApiResponse.success(recommendationService.getSimilarBooks(bookId, limit)));
    }

    @GetMapping("/readers-also-liked")
    public ResponseEntity<ApiResponse<List<RecommendationDTO>>> getReadersAlsoLikedForUser(
            @RequestParam(defaultValue = "12") int limit) {
        Long userId = getAuthenticatedUserId();
        return ResponseEntity.ok(ApiResponse.success(recommendationService.getReadersAlsoLikedForUser(userId, limit)));
    }
    
    @GetMapping("/continue-reading")
    public ResponseEntity<ApiResponse<List<RecommendationDTO>>> getContinueReading(
            @RequestParam(defaultValue = "12") int limit) {
        Long userId = getAuthenticatedUserId();
        return ResponseEntity.ok(ApiResponse.success(recommendationService.getContinueReading(userId, limit)));
    }

    @GetMapping("/recently-viewed")
    public ResponseEntity<ApiResponse<List<RecommendationDTO>>> getRecentlyViewed(
            @RequestParam(defaultValue = "12") int limit) {
        Long userId = getAuthenticatedUserId();
        return ResponseEntity.ok(ApiResponse.success(recommendationService.getRecentlyViewed(userId, limit)));
    }

    @GetMapping("/highest-rated")
    public ResponseEntity<ApiResponse<List<RecommendationDTO>>> getHighestRated(
            @RequestParam(defaultValue = "12") int limit) {
        return ResponseEntity.ok(ApiResponse.success(recommendationService.getHighestRated(limit)));
    }
}
