package com.smartlibrary.service;

import com.smartlibrary.dto.ReadingHistoryRequestDTO;
import com.smartlibrary.dto.ReadingHistoryResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReadingHistoryService {

    ReadingHistoryResponseDTO saveReadingProgress(ReadingHistoryRequestDTO request);

    ReadingHistoryResponseDTO updateReadingProgress(Long userId, Long bookId, Double progressPercentage);

    List<ReadingHistoryResponseDTO> getReadingHistory(Long userId);

    Page<ReadingHistoryResponseDTO> getReadingHistory(Long userId, Pageable pageable);

    ReadingHistoryResponseDTO markBookCompleted(Long userId, Long bookId);
}
