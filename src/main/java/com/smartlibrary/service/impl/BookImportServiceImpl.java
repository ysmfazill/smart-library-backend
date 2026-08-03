package com.smartlibrary.service.impl;

import com.smartlibrary.dto.BookPreviewDTO;
import com.smartlibrary.dto.ImportSummaryDTO;
import com.smartlibrary.entity.Book;
import com.smartlibrary.entity.Category;
import com.smartlibrary.repository.BookRepository;
import com.smartlibrary.repository.CategoryRepository;
import com.smartlibrary.service.BookImportService;
import com.smartlibrary.util.ExcelParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import com.smartlibrary.security.CustomUserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.smartlibrary.service.CacheEvictionService;

import java.util.*;

@Slf4j
@Service
public class BookImportServiceImpl implements BookImportService {

    private final ExcelParser excelParser;
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final CacheEvictionService cacheEvictionService;
    private final com.smartlibrary.service.ImportHistoryService importHistoryService;

    public BookImportServiceImpl(ExcelParser excelParser,
                                 BookRepository bookRepository,
                                 CategoryRepository categoryRepository,
                                 CacheEvictionService cacheEvictionService,
                                 com.smartlibrary.service.ImportHistoryService importHistoryService) {
        this.excelParser = excelParser;
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.cacheEvictionService = cacheEvictionService;
        this.importHistoryService = importHistoryService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookPreviewDTO> previewImport(MultipartFile[] files) {
        List<BookPreviewDTO> previewList = new ArrayList<>();
        Set<String> seenIsbns = new HashSet<>();
        Set<String> seenTitleAuthor = new HashSet<>();

        if (files == null || files.length == 0) {
            return previewList;
        }

        for (MultipartFile file : files) {
            List<BookPreviewDTO> parsed = excelParser.parseExcelFile(file);

            for (BookPreviewDTO dto : parsed) {
                // Normalize strings
                if (dto.getAuthor() != null) dto.setAuthor(normalizeName(dto.getAuthor()));
                if (dto.getCategoryName() != null) dto.setCategoryName(normalizeName(dto.getCategoryName()));
                if (dto.getLanguage() != null) dto.setLanguage(normalizeName(dto.getLanguage()));

                // Check required fields
                if (dto.getTitle() == null || dto.getTitle().isBlank() ||
                    dto.getAuthor() == null || dto.getAuthor().isBlank() ||
                    dto.getCategoryName() == null || dto.getCategoryName().isBlank() ||
                    dto.getLanguage() == null || dto.getLanguage().isBlank()) {
                    dto.setStatus("INVALID");
                    dto.setValidationMessage("Missing title, author, category, or language");
                    previewList.add(dto);
                    continue;
                }

                if (dto.getPublicationYear() != null && (dto.getPublicationYear() < 1000 || dto.getPublicationYear() > 2100)) {
                    dto.setStatus("INVALID");
                    dto.setValidationMessage("Invalid publication year");
                    previewList.add(dto);
                    continue;
                }

                if (dto.getRating() != null && (dto.getRating() < 0 || dto.getRating() > 5)) {
                    dto.setStatus("INVALID");
                    dto.setValidationMessage("Rating must be between 0 and 5");
                    previewList.add(dto);
                    continue;
                }

                if (dto.getIsbn() != null && !dto.getIsbn().isBlank() && dto.getIsbn().length() < 10) {
                    dto.setStatus("INVALID");
                    dto.setValidationMessage("Invalid ISBN format");
                    previewList.add(dto);
                    continue;
                }

                String taKey = (dto.getTitle() + "||" + dto.getAuthor()).toLowerCase();

                // Check ISBN duplicate in DB
                boolean isDuplicateIsbn = !dto.getIsbn().isBlank() && bookRepository.existsByIsbn(dto.getIsbn());
                boolean isDuplicateTitleAuthor = bookRepository.existsByTitleIgnoreCaseAndAuthorIgnoreCase(dto.getTitle(), dto.getAuthor());

                if (isDuplicateIsbn || isDuplicateTitleAuthor || seenTitleAuthor.contains(taKey) || (!dto.getIsbn().isBlank() && seenIsbns.contains(dto.getIsbn()))) {
                    dto.setStatus("DUPLICATE");
                    dto.setValidationMessage("Book already exists in database or batch");
                } else {
                    dto.setStatus("VALID");
                    dto.setValidationMessage("Ready to import");
                    if (!dto.getIsbn().isBlank()) seenIsbns.add(dto.getIsbn());
                    seenTitleAuthor.add(taKey);
                }

                previewList.add(dto);
            }
        }

        return previewList;
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) return name;
        String[] words = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                  .append(word.substring(1).toLowerCase())
                  .append(" ");
            }
        }
        return sb.toString().trim();
    }

    @Override
    @Transactional
    public ImportSummaryDTO executeImport(MultipartFile[] files) {
        long startTime = System.currentTimeMillis();
        int totalProcessed = 0;
        int booksImported = 0;
        int duplicatesSkipped = 0;
        int invalidRowsSkipped = 0;
        int categoriesCreated = 0;
        List<String> logs = new ArrayList<>();
        List<BookPreviewDTO> allPreviews = new ArrayList<>();
        List<Book> booksToSave = new ArrayList<>();

        Set<String> seenIsbns = new HashSet<>();
        Set<String> seenTitleAuthor = new HashSet<>();
        Map<String, Category> categoryMap = new HashMap<>();
        Set<String> newAuthors = new HashSet<>();

        // Cache existing categories
        categoryRepository.findAll().forEach(cat -> categoryMap.put(cat.getName().toLowerCase(), cat));

        if (files != null) {
            for (MultipartFile file : files) {
                log.info("Processing import for file: {}", file.getOriginalFilename());
                logs.add("Started processing file: " + file.getOriginalFilename());

                List<BookPreviewDTO> parsed = excelParser.parseExcelFile(file);

                for (BookPreviewDTO dto : parsed) {
                    totalProcessed++;

                    // Normalize strings
                    if (dto.getAuthor() != null) dto.setAuthor(normalizeName(dto.getAuthor()));
                    if (dto.getCategoryName() != null) dto.setCategoryName(normalizeName(dto.getCategoryName()));
                    if (dto.getLanguage() != null) dto.setLanguage(normalizeName(dto.getLanguage()));

                    // 1. Validation
                    if (dto.getTitle() == null || dto.getTitle().isBlank() ||
                        dto.getAuthor() == null || dto.getAuthor().isBlank() ||
                        dto.getCategoryName() == null || dto.getCategoryName().isBlank() ||
                        dto.getLanguage() == null || dto.getLanguage().isBlank()) {
                        invalidRowsSkipped++;
                        dto.setStatus("INVALID");
                        dto.setValidationMessage("Missing title, author, category, or language");
                        allPreviews.add(dto);
                        logs.add(String.format("Row %d in %s skipped: missing required fields", dto.getRowNumber(), dto.getFileName()));
                        continue;
                    }

                    if (dto.getPublicationYear() != null && (dto.getPublicationYear() < 1000 || dto.getPublicationYear() > 2100)) {
                        invalidRowsSkipped++;
                        dto.setStatus("INVALID");
                        dto.setValidationMessage("Invalid publication year");
                        allPreviews.add(dto);
                        logs.add(String.format("Row %d in %s skipped: invalid year", dto.getRowNumber(), dto.getFileName()));
                        continue;
                    }

                    if (dto.getRating() != null && (dto.getRating() < 0 || dto.getRating() > 5)) {
                        invalidRowsSkipped++;
                        dto.setStatus("INVALID");
                        dto.setValidationMessage("Rating must be between 0 and 5");
                        allPreviews.add(dto);
                        logs.add(String.format("Row %d in %s skipped: invalid rating", dto.getRowNumber(), dto.getFileName()));
                        continue;
                    }

                    if (dto.getIsbn() != null && !dto.getIsbn().isBlank() && dto.getIsbn().length() < 10) {
                        invalidRowsSkipped++;
                        dto.setStatus("INVALID");
                        dto.setValidationMessage("Invalid ISBN format");
                        allPreviews.add(dto);
                        logs.add(String.format("Row %d in %s skipped: invalid ISBN", dto.getRowNumber(), dto.getFileName()));
                        continue;
                    }

                    String taKey = (dto.getTitle() + "||" + dto.getAuthor()).toLowerCase();

                    // 2. Duplicate Check
                    boolean isDuplicateIsbn = !dto.getIsbn().isBlank() && (bookRepository.existsByIsbn(dto.getIsbn()) || seenIsbns.contains(dto.getIsbn()));
                    boolean isDuplicateTitleAuthor = bookRepository.existsByTitleIgnoreCaseAndAuthorIgnoreCase(dto.getTitle(), dto.getAuthor()) || seenTitleAuthor.contains(taKey);

                    if (isDuplicateIsbn || isDuplicateTitleAuthor) {
                        duplicatesSkipped++;
                        dto.setStatus("DUPLICATE");
                        dto.setValidationMessage("Skipped duplicate book");
                        allPreviews.add(dto);
                        logs.add(String.format("Row %d in %s skipped: duplicate title/ISBN '%s'", dto.getRowNumber(), dto.getFileName(), dto.getTitle()));
                        continue;
                    }

                    // 3. Category Resolution or Auto-Creation
                    String catName = dto.getCategoryName() != null && !dto.getCategoryName().isBlank() ? dto.getCategoryName() : "General";
                    Category category = categoryMap.get(catName.toLowerCase());

                    if (category == null) {
                        log.info("Creating new category: {}", catName);
                        category = categoryRepository.save(Category.builder()
                                .name(catName)
                                .description("Auto-created category from Excel import")
                                .build());
                        categoryMap.put(catName.toLowerCase(), category);
                        categoriesCreated++;
                        logs.add("Created new category: " + catName);
                    }

                    // 4. Persistence setup
                    Book book = Book.builder()
                            .title(dto.getTitle())
                            .author(dto.getAuthor())
                            .category(category)
                            .isbn(dto.getIsbn().isBlank() ? null : dto.getIsbn())
                            .publicationYear(dto.getPublicationYear())
                            .rating(dto.getRating() != null ? dto.getRating() : 0.0)
                            .language(dto.getLanguage())
                            .description(dto.getDescription())
                            .keywords(dto.getKeywords())
                            .coverImage(dto.getCoverImage())
                            .totalCopies(5)
                            .availableCopies(5)
                            .aiSummary(null)
                            .build();

                    booksToSave.add(book);
                    booksImported++;
                    newAuthors.add(dto.getAuthor().toLowerCase());

                    if (!dto.getIsbn().isBlank()) seenIsbns.add(dto.getIsbn());
                    seenTitleAuthor.add(taKey);

                    dto.setStatus("VALID");
                    dto.setValidationMessage("Successfully imported");
                    allPreviews.add(dto);
                }
            }
        }

        // Execute batch insert
        if (!booksToSave.isEmpty()) {
            bookRepository.saveAll(booksToSave);
        }

        long duration = System.currentTimeMillis() - startTime;
        double successRate = totalProcessed > 0 ? ((double) booksImported / totalProcessed) * 100.0 : 100.0;
        int booksSkipped = duplicatesSkipped + invalidRowsSkipped;

        log.info("Excel book import completed: {} books imported, {} duplicates skipped, {} categories created in {} ms",
                booksImported, duplicatesSkipped, categoriesCreated, duration);

        if (booksImported > 0) {
            cacheEvictionService.evictRecommendationCaches();
        }

        // Save import history
        Long currentUserId = 1L; // Fallback
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            currentUserId = userDetails.getId();
        }

        String filenames = files != null ? Arrays.stream(files).map(MultipartFile::getOriginalFilename).reduce((a, b) -> a + ", " + b).orElse("unknown") : "unknown";
        String status = successRate == 100.0 ? "SUCCESS" : (successRate > 0 ? "PARTIAL" : "FAILED");

        importHistoryService.saveHistory(
                filenames,
                booksImported,
                duplicatesSkipped,
                invalidRowsSkipped,
                duration,
                status,
                currentUserId
        );

        return ImportSummaryDTO.builder()
                .filesProcessed(files != null ? files.length : 0)
                .totalRowsProcessed(totalProcessed)
                .booksImported(booksImported)
                .booksSkipped(booksSkipped)
                .duplicatesSkipped(duplicatesSkipped)
                .invalidRowsSkipped(invalidRowsSkipped)
                .categoriesCreated(categoriesCreated)
                .authorsCreated(newAuthors.size())
                .importDurationMs(duration)
                .successRate(Math.round(successRate * 10.0) / 10.0)
                .logMessages(logs)
                .previewRows(allPreviews)
                .build();
    }
}
