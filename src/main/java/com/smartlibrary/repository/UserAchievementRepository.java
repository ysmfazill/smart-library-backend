package com.smartlibrary.repository;

import com.smartlibrary.entity.AchievementType;
import com.smartlibrary.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    List<UserAchievement> findByUserId(Long userId);
    boolean existsByUserIdAndAchievementType(Long userId, AchievementType achievementType);
}
