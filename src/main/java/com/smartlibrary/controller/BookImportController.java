package com.smartlibrary.controller;

import com.smartlibrary.dto.ApiResponse;
import com.smartlibrary.dto.BookPreviewDTO;
import com.smartlibrary.dto.ImportSummaryDTO;
import com.smartlibrary.service.BookImportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/books/import")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class BookImportController {

    private final BookImportService bookImportService;

    public BookImportController(BookImportService bookImportService) {
        this.bookImportService = bookImportService;
    }

    @PostMapping("/preview")
    public ResponseEntity<ApiResponse<List<BookPreviewDTO>>> previewImport(
            @RequestParam("files") MultipartFile[] files) {
        log.info("REST request to preview Excel book import with {} file(s)", files != null ? files.length : 0);
        List<BookPreviewDTO> previewList = bookImportService.previewImport(files);
        return ResponseEntity.ok(ApiResponse.success("Excel parsing preview generated", previewList));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ImportSummaryDTO>> executeImport(
            @RequestParam("files") MultipartFile[] files) {
        log.info("REST request to execute Excel book import with {} file(s)", files != null ? files.length : 0);
        ImportSummaryDTO summary = bookImportService.executeImport(files);
        return ResponseEntity.ok(ApiResponse.success("Excel book import process completed successfully", summary));
    }
}
