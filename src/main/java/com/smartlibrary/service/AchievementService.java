package com.smartlibrary.service;

import com.smartlibrary.dto.AchievementDTO;
import com.smartlibrary.entity.UserStatistics;

import java.util.List;

public interface AchievementService {
    void evaluateAchievements(Long userId, UserStatistics stats);
    List<AchievementDTO> getUserAchievements(Long userId);
    String getTopBadge(Long userId);
}
