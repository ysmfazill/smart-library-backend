package com.smartlibrary.controller;

import com.smartlibrary.dto.ApiResponse;
import com.smartlibrary.dto.ImportHistoryDTO;
import com.smartlibrary.service.ImportHistoryService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/imports/history")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class ImportHistoryController {

    private final ImportHistoryService importHistoryService;

    public ImportHistoryController(ImportHistoryService importHistoryService) {
        this.importHistoryService = importHistoryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ImportHistoryDTO>>> getImportHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ImportHistoryDTO> history = importHistoryService.getImportHistory(page, size);
        return ResponseEntity.ok(ApiResponse.success("Import history retrieved", history));
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadImportReport() {
        byte[] csvData = importHistoryService.generateCsvReport();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "import_history_report.csv");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(csvData);
    }
}
