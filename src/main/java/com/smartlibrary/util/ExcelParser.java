package com.smartlibrary.util;

import com.smartlibrary.dto.BookPreviewDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

@Slf4j
@Component
public class ExcelParser {

    public List<BookPreviewDTO> parseExcelFile(MultipartFile file) {
        List<BookPreviewDTO> parsedRows = new ArrayList<>();
        String fileName = file.getOriginalFilename();

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                return parsedRows;
            }

            // Detect Header Row (scan first 5 rows)
            int headerRowIndex = findHeaderRow(sheet);
            if (headerRowIndex == -1) {
                log.warn("Could not detect valid Excel header in file: {}", fileName);
                return parsedRows;
            }

            Row headerRow = sheet.getRow(headerRowIndex);
            Map<String, Integer> colMap = buildColumnMap(headerRow);

            log.info("Detected header at row {} for file {}. Mapped columns: {}", headerRowIndex, fileName, colMap.keySet());

            // Iterate over data rows
            for (int r = headerRowIndex + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                BookPreviewDTO dto = extractBookFromRow(row, colMap, fileName, r + 1);
                if (dto != null) {
                    parsedRows.add(dto);
                }
            }

        } catch (Exception e) {
            log.error("Error parsing Excel file {}: {}", fileName, e.getMessage(), e);
        }

        return parsedRows;
    }

    private int findHeaderRow(Sheet sheet) {
        for (int r = 0; r < Math.min(10, sheet.getPhysicalNumberOfRows()); r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;
            for (Cell cell : row) {
                String val = getCellValueAsString(cell).toLowerCase().trim();
                if (val.contains("title") || val.contains("author") || val.contains("isbn") || val.contains("book id")) {
                    return r;
                }
            }
        }
        return 0; // fallback to first row
    }

    private Map<String, Integer> buildColumnMap(Row headerRow) {
        Map<String, Integer> map = new HashMap<>();
        if (headerRow == null) return map;

        for (int c = 0; c < headerRow.getLastCellNum(); c++) {
            Cell cell = headerRow.getCell(c);
            String label = getCellValueAsString(cell).toLowerCase().trim();

            if (label.contains("full description")) {
                map.put("full_description", c);
            } else if (label.contains("short description")) {
                map.put("short_description", c);
            } else if (label.contains("description") || label.contains("desc")) {
                if (!map.containsKey("description")) {
                    map.put("description", c);
                }
            } else if (label.contains("title")) {
                map.put("title", c);
            } else if (label.contains("author")) {
                map.put("author", c);
            } else if (label.contains("category")) {
                map.put("category", c);
            } else if (label.contains("isbn")) {
                map.put("isbn", c);
            } else if (label.contains("pub year") || label.contains("publication year") || label.contains("year")) {
                map.put("year", c);
            } else if (label.contains("rating")) {
                map.put("rating", c);
            } else if (label.contains("language") || label.contains("lang")) {
                map.put("language", c);
            } else if (label.contains("keyword")) {
                map.put("keywords", c);
            } else if (label.contains("cover") || label.contains("image")) {
                map.put("cover", c);
            } else if (label.equals("id") || label.contains("book id")) {
                map.put("bookCode", c);
            }
        }
        return map;
    }

    private BookPreviewDTO extractBookFromRow(Row row, Map<String, Integer> colMap, String fileName, int rowNum) {
        String title = getMappedValue(row, colMap, "title");
        String author = getMappedValue(row, colMap, "author");
        String category = getMappedValue(row, colMap, "category");

        if (title.isBlank() && author.isBlank()) {
            return null; // empty row
        }

        String description = getMappedValue(row, colMap, "full_description");
        if (description.isBlank()) {
            description = getMappedValue(row, colMap, "description");
        }
        
        String shortDescription = getMappedValue(row, colMap, "short_description");
        String bookCode = getMappedValue(row, colMap, "bookCode");

        String isbn = cleanIsbn(getMappedValue(row, colMap, "isbn"));
        String language = getMappedValue(row, colMap, "language");
        String keywords = getMappedValue(row, colMap, "keywords");
        String cover = getMappedValue(row, colMap, "cover");

        Integer pubYear = parseInteger(getMappedValue(row, colMap, "year"), 2024);
        Double rating = parseDouble(getMappedValue(row, colMap, "rating"), 4.5);

        if (language.isBlank()) language = "English";
        if (category.isBlank()) category = "General";

        return BookPreviewDTO.builder()
                .fileName(fileName)
                .rowNumber(rowNum)
                .title(title.trim())
                .author(author.trim())
                .categoryName(category.trim())
                .bookCode(bookCode.trim())
                .isbn(isbn)
                .publicationYear(pubYear)
                .rating(rating)
                .language(language.trim())
                .description(description.trim())
                .shortDescription(shortDescription.trim())
                .keywords(keywords.trim())
                .coverImage(cover.isBlank() ? "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&q=80&w=600" : cover.trim())
                .status("VALID")
                .validationMessage("Ready for import")
                .build();
    }

    private String getMappedValue(Row row, Map<String, Integer> map, String key) {
        Integer colIdx = map.get(key);
        if (colIdx == null) return "";
        Cell cell = row.getCell(colIdx);
        return getCellValueAsString(cell);
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().getYear() + "";
                }
                double num = cell.getNumericCellValue();
                if (num == Math.floor(num)) {
                    return String.format("%.0f", num);
                }
                return String.valueOf(num);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }

    private String cleanIsbn(String raw) {
        if (raw == null) return "";
        return raw.replaceAll("[^0-9X-]", "").trim();
    }

    private Integer parseInteger(String str, int fallback) {
        try {
            if (str == null || str.isBlank()) return fallback;
            return Integer.parseInt(str.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return fallback;
        }
    }

    private Double parseDouble(String str, double fallback) {
        try {
            if (str == null || str.isBlank()) return fallback;
            double d = Double.parseDouble(str);
            return Math.min(5.0, Math.max(1.0, d));
        } catch (Exception e) {
            return fallback;
        }
    }

    private boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK && !getCellValueAsString(cell).isBlank()) {
                return false;
            }
        }
        return true;
    }
}
