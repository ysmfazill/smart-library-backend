package com.smartlibrary.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CacheEvictionService {

    @CacheEvict(value = {"trendingBooks", "popularBooks", "newestBooks"}, allEntries = true)
    public void evictRecommendationCaches() {
        // Caches cleared
    }

    @CacheEvict(value = {"trendingBooks", "popularBooks"}, allEntries = true)
    public void evictTrendingAndPopularCaches() {
        // Caches cleared
    }

    @Scheduled(fixedRate = 3600000) // Every hour
    @CacheEvict(value = {"trendingBooks", "popularBooks", "newestBooks"}, allEntries = true)
    public void scheduledCacheEviction() {
        // Caches will be cleared every hour and rebuilt on next request
    }
}
