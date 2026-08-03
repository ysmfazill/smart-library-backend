package com.smartlibrary.service;

import com.smartlibrary.dto.ImportHistoryDTO;
import org.springframework.data.domain.Page;

public interface ImportHistoryService {
    Page<ImportHistoryDTO> getImportHistory(int page, int size);
    void saveHistory(String filename, int booksImported, int duplicatesSkipped, int invalidRows, long durationMs, String status, Long userId);
    byte[] generateCsvReport();
}
