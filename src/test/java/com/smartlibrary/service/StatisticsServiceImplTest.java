package com.smartlibrary.service;

import com.smartlibrary.dto.LeaderboardEntryDTO;
import com.smartlibrary.entity.User;
import com.smartlibrary.entity.UserStatistics;
import com.smartlibrary.repository.UserRepository;
import com.smartlibrary.repository.UserStatisticsRepository;
import com.smartlibrary.service.impl.StatisticsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceImplTest {

    @Mock
    private UserStatisticsRepository statisticsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AchievementService achievementService;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    private StatisticsServiceImpl statisticsService;

    private User testUser;
    private UserStatistics testStats;

    @BeforeEach
    void setUp() {
        statisticsService = new StatisticsServiceImpl(statisticsRepository, userRepository, achievementService, cacheManager);

        testUser = User.builder()
                .id(2L)
                .fullName("John Doe")
                .email("john@example.com")
                .build();

        testStats = UserStatistics.builder()
                .id(1L)
                .user(testUser)
                .booksRead(15)
                .pagesRead(750)
                .readingHours(15)
                .currentStreak(5)
                .maxStreak(10)
                .build();
    }

    @Test
    void testGetMonthlyLeaderboard_Success() {
        when(statisticsRepository.findTopReaders(any())).thenReturn(List.of(testStats));
        when(achievementService.getTopBadge(2L)).thenReturn("🏅");

        List<LeaderboardEntryDTO> leaderboard = statisticsService.getMonthlyLeaderboard();

        assertNotNull(leaderboard);
        assertEquals(1, leaderboard.size());

        LeaderboardEntryDTO entry = leaderboard.get(0);
        assertEquals(1, entry.getRank());
        assertEquals(2L, entry.getUserId());
        assertEquals("John Doe", entry.getName());
        assertEquals(15, entry.getBooksCompleted());
        assertEquals(750, entry.getPagesRead());
        assertEquals("🏅", entry.getTopBadge());

        verify(statisticsRepository, times(1)).findTopReaders(any());
    }

    @Test
    void testGetAllTimeLeaderboard_Success() {
        when(statisticsRepository.findTopReaders(any())).thenReturn(List.of(testStats));
        when(achievementService.getTopBadge(2L)).thenReturn("🏅");

        List<LeaderboardEntryDTO> leaderboard = statisticsService.getAllTimeLeaderboard();

        assertNotNull(leaderboard);
        assertEquals(1, leaderboard.size());
        assertEquals("John Doe", leaderboard.get(0).getName());

        verify(statisticsRepository, times(1)).findTopReaders(any());
    }

    @Test
    void testRefreshLeaderboards_Success() {
        when(statisticsRepository.findTopReaders(any())).thenReturn(List.of(testStats));
        when(achievementService.getTopBadge(2L)).thenReturn("🏅");
        when(cacheManager.getCache(anyString())).thenReturn(cache);

        assertDoesNotThrow(() -> statisticsService.refreshLeaderboards());

        verify(cacheManager, times(2)).getCache(anyString());
        verify(cache, times(2)).put(any(), any());
    }
}
