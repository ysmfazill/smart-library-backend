package com.smartlibrary.service.impl;

import com.smartlibrary.dto.LeaderboardEntryDTO;
import com.smartlibrary.dto.UserStatisticsDTO;
import com.smartlibrary.entity.User;
import com.smartlibrary.entity.UserStatistics;
import com.smartlibrary.repository.UserRepository;
import com.smartlibrary.repository.UserStatisticsRepository;
import com.smartlibrary.service.AchievementService;
import com.smartlibrary.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final UserStatisticsRepository statisticsRepository;
    private final UserRepository userRepository;
    private final AchievementService achievementService;
    private final CacheManager cacheManager;

    public StatisticsServiceImpl(UserStatisticsRepository statisticsRepository, UserRepository userRepository, AchievementService achievementService) {
        this(statisticsRepository, userRepository, achievementService, null);
    }

    public StatisticsServiceImpl(UserStatisticsRepository statisticsRepository, UserRepository userRepository, AchievementService achievementService, CacheManager cacheManager) {
        this.statisticsRepository = statisticsRepository;
        this.userRepository = userRepository;
        this.achievementService = achievementService;
        this.cacheManager = cacheManager;
    }

    @Override
    @Transactional(readOnly = true)
    public UserStatisticsDTO getUserStatistics(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        UserStatistics stats = statisticsRepository.findByUserId(userId)
                .orElse(UserStatistics.builder().user(user).build()); // Fallback

        return UserStatisticsDTO.builder()
                .id(stats.getId())
                .userId(userId)
                .booksRead(stats.getBooksRead())
                .pagesRead(stats.getPagesRead())
                .readingHours(stats.getReadingHours())
                .currentStreak(stats.getCurrentStreak())
                .maxStreak(stats.getMaxStreak())
                .lastReadDate(stats.getLastReadDate())
                // In a production scenario, rank could be queried dynamically
                .currentRank(calculateRank(stats))
                .globalRank(calculateRank(stats))
                .build();
    }

    private int calculateRank(UserStatistics stats) {
        if (stats.getBooksRead() == 0) return 0; // Unranked
        // Rough estimation for ranking if we don't query the whole DB.
        // Ideally we would run: SELECT count(*) FROM user_statistics WHERE books_read > ?
        return 1; // Simplified for this prototype
    }

    @Override
    @Transactional
    public void updateReadingStats(Long userId, int pagesRead, boolean completed) {
        User user = userRepository.findById(userId).orElseThrow();
        UserStatistics stats = statisticsRepository.findByUserId(userId)
                .orElse(UserStatistics.builder().user(user).build());

        // Update basic counts
        stats.setPagesRead(stats.getPagesRead() + pagesRead);
        stats.setReadingHours(stats.getPagesRead() / 50); // Assuming 50 pages per hour

        if (completed) {
            stats.setBooksRead(stats.getBooksRead() + 1);
        }

        // Streak logic
        LocalDateTime now = LocalDateTime.now();
        if (stats.getLastReadDate() != null) {
            LocalDate lastReadDay = stats.getLastReadDate().toLocalDate();
            LocalDate today = now.toLocalDate();
            if (lastReadDay.plusDays(1).equals(today)) {
                stats.setCurrentStreak(stats.getCurrentStreak() + 1);
            } else if (lastReadDay.isBefore(today.minusDays(1))) {
                stats.setCurrentStreak(1);
            }
        } else {
            stats.setCurrentStreak(1);
        }

        if (stats.getCurrentStreak() > stats.getMaxStreak()) {
            stats.setMaxStreak(stats.getCurrentStreak());
        }

        stats.setLastReadDate(now);
        statisticsRepository.save(stats);

        // Check for achievements asynchronously or directly
        achievementService.evaluateAchievements(userId, stats);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable("monthlyLeaderboard")
    public List<LeaderboardEntryDTO> getMonthlyLeaderboard() {
        return getLeaderboard(10);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable("allTimeLeaderboard")
    public List<LeaderboardEntryDTO> getAllTimeLeaderboard() {
        return getLeaderboard(10);
    }

    private List<LeaderboardEntryDTO> getLeaderboard(int limit) {
        List<UserStatistics> topReaders = statisticsRepository.findTopReaders(PageRequest.of(0, limit));
        int rank = 1;
        List<LeaderboardEntryDTO> leaderboard = new java.util.ArrayList<>();
        for (UserStatistics stats : topReaders) {
            String topBadge = achievementService.getTopBadge(stats.getUser().getId());
            leaderboard.add(LeaderboardEntryDTO.builder()
                    .rank(rank++)
                    .userId(stats.getUser().getId())
                    .name(stats.getUser().getFullName())
                    .profilePicture("https://api.dicebear.com/7.x/avataaars/svg?seed=" + stats.getUser().getFullName())
                    .booksCompleted(stats.getBooksRead())
                    .pagesRead(stats.getPagesRead())
                    .readingHours(stats.getReadingHours())
                    .readingStreak(stats.getCurrentStreak())
                    .topBadge(topBadge)
                    .build());
        }
        return leaderboard;
    }

    @Scheduled(cron = "0 0 * * * *") // Every hour
    @CacheEvict(value = {"monthlyLeaderboard", "allTimeLeaderboard"}, allEntries = true)
    @Transactional(readOnly = true)
    public void refreshLeaderboards() {
        log.info("Refreshing leaderboards cache...");
        List<LeaderboardEntryDTO> monthly = getMonthlyLeaderboard();
        List<LeaderboardEntryDTO> allTime = getAllTimeLeaderboard();
        if (cacheManager != null) {
            Cache monthlyCache = cacheManager.getCache("monthlyLeaderboard");
            if (monthlyCache != null) {
                monthlyCache.put(SimpleKey.EMPTY, monthly);
            }
            Cache allTimeCache = cacheManager.getCache("allTimeLeaderboard");
            if (allTimeCache != null) {
                allTimeCache.put(SimpleKey.EMPTY, allTime);
            }
        }
    }
}
