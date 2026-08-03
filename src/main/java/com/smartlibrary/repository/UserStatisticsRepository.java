package com.smartlibrary.repository;

import com.smartlibrary.entity.UserStatistics;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserStatisticsRepository extends JpaRepository<UserStatistics, Long> {
    Optional<UserStatistics> findByUserId(Long userId);

    @Query("SELECT us FROM UserStatistics us JOIN us.user u ORDER BY us.booksRead DESC, us.pagesRead DESC, us.readingHours DESC, us.currentStreak DESC")
    List<UserStatistics> findTopReaders(Pageable pageable);
}
