package com.smartlibrary.service.impl;

import com.smartlibrary.dto.AchievementDTO;
import com.smartlibrary.entity.AchievementType;
import com.smartlibrary.entity.User;
import com.smartlibrary.entity.UserAchievement;
import com.smartlibrary.entity.UserStatistics;
import com.smartlibrary.repository.UserAchievementRepository;
import com.smartlibrary.repository.UserRepository;
import com.smartlibrary.service.AchievementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AchievementServiceImpl implements AchievementService {

    private final UserAchievementRepository achievementRepository;
    private final UserRepository userRepository;

    public AchievementServiceImpl(UserAchievementRepository achievementRepository, UserRepository userRepository) {
        this.achievementRepository = achievementRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void evaluateAchievements(Long userId, UserStatistics stats) {
        User user = userRepository.findById(userId).orElseThrow();

        if (stats.getBooksRead() >= 1) unlock(user, AchievementType.FIRST_BOOK);
        if (stats.getBooksRead() >= 10) unlock(user, AchievementType.BOOKS_10);
        if (stats.getCurrentStreak() >= 7) unlock(user, AchievementType.STREAK_7);
        if (stats.getPagesRead() >= 1000) unlock(user, AchievementType.PAGES_1000);
        if (stats.getBooksRead() >= 50) unlock(user, AchievementType.CHAMPION);
    }

    private void unlock(User user, AchievementType type) {
        if (!achievementRepository.existsByUserIdAndAchievementType(user.getId(), type)) {
            UserAchievement achievement = UserAchievement.builder()
                    .user(user)
                    .achievementType(type)
                    .build();
            achievementRepository.save(achievement);
            log.info("User {} unlocked achievement {}", user.getId(), type);
        }
    }

    @Override
    public List<AchievementDTO> getUserAchievements(Long userId) {
        return achievementRepository.findByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public String getTopBadge(Long userId) {
        List<UserAchievement> achievements = achievementRepository.findByUserId(userId);
        if (achievements.isEmpty()) return null;
        // Simple logic: return the latest unlocked achievement's icon or name
        return mapToDTO(achievements.get(achievements.size() - 1)).getIcon();
    }

    private AchievementDTO mapToDTO(UserAchievement achievement) {
        String name = "";
        String desc = "";
        String icon = "";

        switch (achievement.getAchievementType()) {
            case FIRST_BOOK:
                name = "First Book"; desc = "Completed your first book"; icon = "📖"; break;
            case STREAK_7:
                name = "7 Day Streak"; desc = "Read for 7 consecutive days"; icon = "🔥"; break;
            case BOOKS_10:
                name = "10 Books"; desc = "Completed 10 books"; icon = "📚"; break;
            case TOP_READER:
                name = "Top Reader"; desc = "Ranked top 10 on the leaderboard"; icon = "🏅"; break;
            case PAGES_1000:
                name = "1000 Pages"; desc = "Read over 1000 pages"; icon = "⭐"; break;
            case CHAMPION:
                name = "Reading Champion"; desc = "Completed 50 books"; icon = "🎯"; break;
        }

        return AchievementDTO.builder()
                .id(achievement.getId())
                .type(achievement.getAchievementType())
                .name(name)
                .description(desc)
                .icon(icon)
                .unlockedAt(achievement.getUnlockedAt())
                .build();
    }
}
