package com.smartlibrary.service.impl;

import com.smartlibrary.entity.Book;
import com.smartlibrary.entity.CoverSource;
import com.smartlibrary.repository.BookRepository;
import com.smartlibrary.service.BookCoverService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
public class BookCoverServiceImpl implements BookCoverService {

    private final BookRepository bookRepository;
    private final RestTemplate restTemplate;

    public BookCoverServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
        this.restTemplate = new RestTemplate();
    }

    @Override
    @Transactional
    public String resolveCoverUrl(Book book) {
        if (book == null) return null;

        // 1. Check if cover URL is already set and valid
        if (book.getCoverImage() != null && !book.getCoverImage().isBlank() && !book.getCoverImage().contains("placeholder")) {
            if (book.getCoverSource() == null) {
                book.setCoverSource(CoverSource.REAL);
                bookRepository.save(book);
            }
            return book.getCoverImage();
        }

        // 2. Try Open Library / Google Books by ISBN if present
        String officialCover = tryFetchRealCover(book);
        if (officialCover != null && !officialCover.isBlank()) {
            book.setCoverImage(officialCover);
            book.setCoverSource(CoverSource.REAL);
            bookRepository.save(book);
            log.info("Resolved REAL cover for book '{}': {}", book.getTitle(), officialCover);
            return officialCover;
        }

        // 3. Fallback: Generate Custom Professional Book Cover (Data-URI SVG)
        String generatedCover = generateCustomSvgCover(book);
        book.setCoverImage(generatedCover);
        book.setCoverSource(CoverSource.GENERATED);
        bookRepository.save(book);
        log.info("Generated CUSTOM professional cover for book '{}'", book.getTitle());
        return generatedCover;
    }

    private String tryFetchRealCover(Book book) {
        try {
            // Check ISBN via Open Library Covers
            if (book.getIsbn() != null && !book.getIsbn().isBlank()) {
                String cleanIsbn = book.getIsbn().replaceAll("[^0-9X]", "");
                if (!cleanIsbn.isEmpty()) {
                    String openLibraryUrl = "https://covers.openlibrary.org/b/isbn/" + cleanIsbn + "-L.jpg";
                    // Verify if OpenLibrary returns a real image
                    if (isUrlAccessible(openLibraryUrl)) {
                        return openLibraryUrl;
                    }
                }
            }

            // Search Google Books API by Title + Author
            if (book.getTitle() != null && !book.getTitle().isBlank()) {
                String query = URLEncoder.encode("intitle:" + book.getTitle() + (book.getAuthor() != null ? " inauthor:" + book.getAuthor() : ""), StandardCharsets.UTF_8);
                String googleApiUrl = "https://www.googleapis.com/books/v1/volumes?q=" + query + "&maxResults=1";
                Map<?, ?> response = restTemplate.getForObject(googleApiUrl, Map.class);
                if (response != null && response.containsKey("items")) {
                    List<?> items = (List<?>) response.get("items");
                    if (items != null && !items.isEmpty()) {
                        Map<?, ?> firstItem = (Map<?, ?>) items.get(0);
                        Map<?, ?> volumeInfo = (Map<?, ?>) firstItem.get("volumeInfo");
                        if (volumeInfo != null && volumeInfo.containsKey("imageLinks")) {
                            Map<?, ?> imageLinks = (Map<?, ?>) volumeInfo.get("imageLinks");
                            String thumbnail = (String) imageLinks.get("thumbnail");
                            if (thumbnail != null && !thumbnail.isBlank()) {
                                return thumbnail.replace("http://", "https://");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed fetching real cover for '{}': {}", book.getTitle(), e.getMessage());
        }
        return null;
    }

    private boolean isUrlAccessible(String urlString) {
        try {
            URI uri = URI.create(urlString);
            var conn = (java.net.HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("HEAD");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            int code = conn.getResponseCode();
            return code == 200 && conn.getContentLength() > 1000; // OpenLibrary default blank images are small (~807 bytes)
        } catch (Exception e) {
            return false;
        }
    }

    public String generateCustomSvgCover(Book book) {
        String title = book.getTitle() != null ? book.getTitle() : "Untitled";
        String author = book.getAuthor() != null ? book.getAuthor() : "Unknown Author";
        String category = book.getCategory() != null ? book.getCategory().getName() : "General";

        // Determine genre palette
        String[] colors = getGenreColors(category, title);
        String bgGradientStart = colors[0];
        String bgGradientEnd = colors[1];
        String accentColor = colors[2];
        String patternIcon = colors[3];

        // Format title lines
        String titleEscaped = escapeXml(title);
        String authorEscaped = escapeXml(author);
        String categoryEscaped = escapeXml(category.toUpperCase());

        String svg = "<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 400 600' width='400' height='600'>" +
                "<defs>" +
                "<linearGradient id='bg' x1='0%' y1='0%' x2='100%' y2='100%'>" +
                "<stop offset='0%' stop-color='" + bgGradientStart + "'/>" +
                "<stop offset='100%' stop-color='" + bgGradientEnd + "'/>" +
                "</linearGradient>" +
                "<linearGradient id='accent' x1='0%' y1='0%' x2='100%' y2='0%'>" +
                "<stop offset='0%' stop-color='" + accentColor + "' stop-opacity='0.8'/>" +
                "<stop offset='100%' stop-color='" + accentColor + "' stop-opacity='0.2'/>" +
                "</linearGradient>" +
                "</defs>" +
                "<rect width='400' height='600' rx='16' fill='url(#bg)'/>" +
                "<rect x='20' y='20' width='360' height='560' rx='12' fill='none' stroke='rgba(255,255,255,0.15)' stroke-width='2'/>" +
                "<rect x='40' y='60' width='80' height='6' fill='url(#accent)' rx='3'/>" +
                "<text x='40' y='90' fill='" + accentColor + "' font-family='sans-serif' font-size='12' font-weight='700' letter-spacing='2'>" + categoryEscaped + "</text>" +
                "<text x='40' y='160' fill='#ffffff' font-family='Georgia, serif' font-size='28' font-weight='bold' width='320'>" +
                formatTextSvgLines(titleEscaped, 28, 40, 160) +
                "</text>" +
                "<text x='40' y='460' fill='rgba(255,255,255,0.85)' font-family='sans-serif' font-size='16' font-weight='500'>By " + authorEscaped + "</text>" +
                "<path d='" + patternIcon + "' fill='" + accentColor + "' opacity='0.25' transform='translate(260, 440) scale(1.5)'/>" +
                "<rect x='0' y='0' width='16' height='600' fill='rgba(0,0,0,0.25)'/>" +
                "<rect x='16' y='0' width='2' height='600' fill='rgba(255,255,255,0.15)'/>" +
                "</svg>";

        return "data:image/svg+xml;charset=utf-8," + URLEncoder.encode(svg, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private String formatTextSvgLines(String text, int fontSize, int x, int startY) {
        String[] words = text.split(" ");
        StringBuilder lines = new StringBuilder();
        StringBuilder currentLine = new StringBuilder();
        int lineY = startY;
        int maxLineChars = 20;

        for (String word : words) {
            if (currentLine.length() + word.length() + 1 > maxLineChars) {
                lines.append("<tspan x='").append(x).append("' dy='").append(lineY == startY ? 0 : 36).append("'>")
                     .append(currentLine.toString().trim()).append("</tspan>");
                currentLine = new StringBuilder(word).append(" ");
                lineY += 36;
            } else {
                currentLine.append(word).append(" ");
            }
        }
        if (currentLine.length() > 0) {
            lines.append("<tspan x='").append(x).append("' dy='").append(lineY == startY ? 0 : 36).append("'>")
                 .append(currentLine.toString().trim()).append("</tspan>");
        }
        return lines.toString();
    }

    private String[] getGenreColors(String category, String title) {
        String catLower = category.toLowerCase();
        String titleLower = title.toLowerCase();

        if (catLower.contains("artificial") || catLower.contains("ai") || titleLower.contains("neural")) {
            return new String[]{"#0f172a", "#1e1b4b", "#6366f1", "M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"};
        } else if (catLower.contains("machine learning") || titleLower.contains("learning")) {
            return new String[]{"#022c22", "#064e3b", "#10b981", "M4 4h16v16H4z"};
        } else if (catLower.contains("computer science") || titleLower.contains("code") || catLower.contains("web")) {
            return new String[]{"#0f172a", "#1e293b", "#38bdf8", "M8 9l3 3-3 3m5 0h3"};
        } else if (catLower.contains("cyber") || catLower.contains("security")) {
            return new String[]{"#18181b", "#27272a", "#a855f7", "M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"};
        } else if (catLower.contains("business") || catLower.contains("startup")) {
            return new String[]{"#1e293b", "#334155", "#f59e0b", "M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"};
        } else if (catLower.contains("science") || catLower.contains("physics")) {
            return new String[]{"#1a103c", "#2e1065", "#ec4899", "M12 2a10 10 0 100 20 10 10 0 000-20z"};
        } else if (catLower.contains("philosophy") || catLower.contains("psychology")) {
            return new String[]{"#292524", "#44403c", "#f97316", "M12 3v18m9-9H3"};
        } else {
            return new String[]{"#1e1b4b", "#312e81", "#818cf8", "M12 2L2 7l10 5 10-5-10-5z"};
        }
    }

    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }

    @Override
    @Transactional
    public Map<String, Object> auditAndResolveAllCovers() {
        log.info("Starting complete book-cover audit and automatic resolution...");
        List<Book> books = bookRepository.findAll();
        int total = books.size();
        int realCount = 0;
        int generatedCount = 0;

        for (Book book : books) {
            String resolved = resolveCoverUrl(book);
            if (book.getCoverSource() == CoverSource.GENERATED || (resolved != null && resolved.startsWith("data:image"))) {
                generatedCount++;
            } else {
                realCount++;
            }
        }

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("totalBooks", total);
        report.put("realCovers", realCount);
        report.put("generatedCovers", generatedCount);
        report.put("missingCovers", 0);

        log.info("=================================================");
        log.info("READIFY BOOK COVER SYSTEM AUDIT REPORT");
        log.info("Total books: {}", total);
        log.info("Real covers: {}", realCount);
        log.info("Generated covers: {}", generatedCount);
        log.info("Missing covers: 0");
        log.info("=================================================");

        return report;
    }
}
