package com.smartlibrary.service.impl;

import com.smartlibrary.dto.ImportHistoryDTO;
import com.smartlibrary.entity.ImportHistory;
import com.smartlibrary.entity.User;
import com.smartlibrary.repository.ImportHistoryRepository;
import com.smartlibrary.repository.UserRepository;
import com.smartlibrary.service.ImportHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
public class ImportHistoryServiceImpl implements ImportHistoryService {

    private final ImportHistoryRepository importHistoryRepository;
    private final UserRepository userRepository;

    public ImportHistoryServiceImpl(ImportHistoryRepository importHistoryRepository, UserRepository userRepository) {
        this.importHistoryRepository = importHistoryRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ImportHistoryDTO> getImportHistory(int page, int size) {
        return importHistoryRepository.findAllByOrderByImportDateDesc(PageRequest.of(page, size))
                .map(this::mapToDTO);
    }

    @Override
    @Transactional
    public void saveHistory(String filename, int booksImported, int duplicatesSkipped, int invalidRows, long durationMs, String status, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        ImportHistory history = ImportHistory.builder()
                .filename(filename)
                .booksImported(booksImported)
                .duplicatesSkipped(duplicatesSkipped)
                .invalidRows(invalidRows)
                .importDurationMs(durationMs)
                .status(status)
                .importedBy(user)
                .importDate(LocalDateTime.now())
                .build();
        importHistoryRepository.save(history);
        log.info("Saved import history for file {}", filename);
    }

    private ImportHistoryDTO mapToDTO(ImportHistory history) {
        return ImportHistoryDTO.builder()
                .id(history.getId())
                .importDate(history.getImportDate())
                .filename(history.getFilename())
                .booksImported(history.getBooksImported())
                .duplicatesSkipped(history.getDuplicatesSkipped())
                .invalidRows(history.getInvalidRows())
                .importDurationMs(history.getImportDurationMs())
                .status(history.getStatus())
                .importedBy(history.getImportedBy().getFullName())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateCsvReport() {
        List<ImportHistory> allHistory = importHistoryRepository.findAllByOrderByImportDateDesc(PageRequest.of(0, 10000)).getContent();
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Filename,Import Date,Imported By,Books Imported,Duplicates Skipped,Failed Rows,Total Books,Duration (ms),Status\n");
        for (ImportHistory h : allHistory) {
            int total = h.getBooksImported() + h.getDuplicatesSkipped() + h.getInvalidRows();
            sb.append(h.getId()).append(",")
              .append("\"").append(h.getFilename()).append("\",")
              .append(h.getImportDate()).append(",")
              .append("\"").append(h.getImportedBy().getFullName()).append("\",")
              .append(h.getBooksImported()).append(",")
              .append(h.getDuplicatesSkipped()).append(",")
              .append(h.getInvalidRows()).append(",")
              .append(total).append(",")
              .append(h.getImportDurationMs()).append(",")
              .append(h.getStatus()).append("\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }
}
