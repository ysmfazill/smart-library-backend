package com.smartlibrary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCollectionDTO {
    private Long id;
    private Long userId;
    private String name;
    private boolean isSystem;
    private LocalDateTime createdAt;
    private List<BookResponseDTO> books;
}
