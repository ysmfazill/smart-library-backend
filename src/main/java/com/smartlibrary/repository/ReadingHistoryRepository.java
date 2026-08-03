package com.smartlibrary.repository;

import com.smartlibrary.entity.ReadingHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReadingHistoryRepository extends JpaRepository<ReadingHistory, Long> {

    List<ReadingHistory> findByUserId(Long userId);

    List<ReadingHistory> findByUserIdOrderByLastReadDateDesc(Long userId);

    Page<ReadingHistory> findByUserIdOrderByLastReadDateDesc(Long userId, Pageable pageable);

    Optional<ReadingHistory> findByUserIdAndBookId(Long userId, Long bookId);

    List<ReadingHistory> findByUserIdAndCompletedTrue(Long userId);

    @org.springframework.data.jpa.repository.Query("SELECT rh.book.id FROM ReadingHistory rh GROUP BY rh.book.id ORDER BY COUNT(rh.id) DESC")
    Page<Long> findTrendingBookIds(Pageable pageable);
}
