package com.smartlibrary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportSummaryDTO {
    private int filesProcessed;
    private int totalRowsProcessed;
    private int booksImported;
    private int booksSkipped;
    private int duplicatesSkipped;
    private int invalidRowsSkipped;
    private int categoriesCreated;
    private int authorsCreated;
    private long importDurationMs;
    private double successRate;
    @Builder.Default
    private List<String> logMessages = new ArrayList<>();
    @Builder.Default
    private List<BookPreviewDTO> previewRows = new ArrayList<>();
}
