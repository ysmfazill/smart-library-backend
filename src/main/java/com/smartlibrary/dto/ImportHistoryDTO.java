package com.smartlibrary.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportHistoryDTO {
    private Long id;
    private String importedBy;
    private LocalDateTime importDate;
    private String filename;
    private Integer booksImported;
    private Integer duplicatesSkipped;
    private Integer invalidRows;
    private Long importDurationMs;
    private String status;
}
