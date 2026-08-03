package com.smartlibrary.controller;

import com.smartlibrary.dto.AchievementDTO;
import com.smartlibrary.dto.ApiResponse;
import com.smartlibrary.dto.LeaderboardEntryDTO;
import com.smartlibrary.dto.UserStatisticsDTO;
import com.smartlibrary.service.AchievementService;
import com.smartlibrary.service.StatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("")
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final AchievementService achievementService;

    public StatisticsController(StatisticsService statisticsService, AchievementService achievementService) {
        this.statisticsService = statisticsService;
        this.achievementService = achievementService;
    }

    @GetMapping("/leaderboard/monthly")
    public ResponseEntity<ApiResponse<List<LeaderboardEntryDTO>>> getMonthlyLeaderboard() {
        return ResponseEntity.ok(ApiResponse.success("Monthly leaderboard fetched", statisticsService.getMonthlyLeaderboard()));
    }

    @GetMapping("/leaderboard/all-time")
    public ResponseEntity<ApiResponse<List<LeaderboardEntryDTO>>> getAllTimeLeaderboard() {
        return ResponseEntity.ok(ApiResponse.success("All-time leaderboard fetched", statisticsService.getAllTimeLeaderboard()));
    }

    @GetMapping("/statistics/user/{userId}")
    public ResponseEntity<ApiResponse<UserStatisticsDTO>> getUserStatistics(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("User statistics fetched", statisticsService.getUserStatistics(userId)));
    }

    @GetMapping("/achievements/{userId}")
    public ResponseEntity<ApiResponse<List<AchievementDTO>>> getUserAchievements(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("User achievements fetched", achievementService.getUserAchievements(userId)));
    }
}
