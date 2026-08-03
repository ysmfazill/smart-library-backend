package com.smartlibrary.service;

import com.smartlibrary.dto.LeaderboardEntryDTO;
import com.smartlibrary.dto.UserStatisticsDTO;

import java.util.List;

public interface StatisticsService {
    UserStatisticsDTO getUserStatistics(Long userId);
    void updateReadingStats(Long userId, int pagesRead, boolean completed);
    List<LeaderboardEntryDTO> getMonthlyLeaderboard();
    List<LeaderboardEntryDTO> getAllTimeLeaderboard();
    void refreshLeaderboards();
}
