package com.smartlibrary.mapper;

import com.smartlibrary.dto.ReadingHistoryRequestDTO;
import com.smartlibrary.dto.ReadingHistoryResponseDTO;
import com.smartlibrary.entity.ReadingHistory;
import org.springframework.stereotype.Component;

@Component
public class ReadingHistoryMapper {

    private final BookMapper bookMapper;

    public ReadingHistoryMapper(BookMapper bookMapper) {
        this.bookMapper = bookMapper;
    }

    public ReadingHistoryResponseDTO toResponseDTO(ReadingHistory history) {
        if (history == null) {
            return null;
        }
        return ReadingHistoryResponseDTO.builder()
                .id(history.getId())
                .userId(history.getUser() != null ? history.getUser().getId() : null)
                .userName(history.getUser() != null ? history.getUser().getFullName() : null)
                .book(bookMapper.toSummaryDTO(history.getBook()))
                .progressPercentage(history.getProgressPercentage())
                .lastReadDate(history.getLastReadDate())
                .completed(history.getCompleted())
                .build();
    }

    public ReadingHistory toEntity(ReadingHistoryRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        return ReadingHistory.builder()
                .progressPercentage(dto.getProgressPercentage())
                .completed(dto.getCompleted() != null ? dto.getCompleted() : false)
                .build();
    }
}
