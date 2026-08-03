package com.smartlibrary.service;

import com.smartlibrary.dto.BookPreviewDTO;
import com.smartlibrary.dto.ImportSummaryDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BookImportService {
    List<BookPreviewDTO> previewImport(MultipartFile[] files);
    ImportSummaryDTO executeImport(MultipartFile[] files);
}
