package com.smartlibrary.service.impl;

import com.smartlibrary.dto.BookRequestDTO;
import com.smartlibrary.dto.BookResponseDTO;
import com.smartlibrary.dto.BookSummaryDTO;
import com.smartlibrary.entity.Book;
import com.smartlibrary.entity.Category;
import com.smartlibrary.exception.BadRequestException;
import com.smartlibrary.exception.ResourceNotFoundException;
import com.smartlibrary.mapper.BookMapper;
import com.smartlibrary.repository.BookRepository;
import com.smartlibrary.repository.CategoryRepository;
import com.smartlibrary.service.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final BookMapper bookMapper;
    private final com.smartlibrary.service.FileStorageService fileStorageService;

    public BookServiceImpl(BookRepository bookRepository,
                           CategoryRepository categoryRepository,
                           BookMapper bookMapper,
                           com.smartlibrary.service.FileStorageService fileStorageService) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.bookMapper = bookMapper;
        this.fileStorageService = fileStorageService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponseDTO> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(bookMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookResponseDTO> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable).map(bookMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponseDTO getBookById(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));
        return bookMapper.toResponseDTO(book);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookResponseDTO> searchBooks(String query, Long categoryId, Double minRating, Pageable pageable) {
        return bookRepository.searchBooks(query, categoryId, minRating, pageable)
                .map(bookMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookResponseDTO> searchByCategory(String categoryName, Pageable pageable) {
        return bookRepository.findByCategory_Name(categoryName, pageable)
                .map(bookMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookResponseDTO> searchByAuthor(String author, Pageable pageable) {
        return bookRepository.findByAuthorContainingIgnoreCase(author, pageable)
                .map(bookMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookResponseDTO> searchByKeyword(String keyword, Pageable pageable) {
        return bookRepository.findByKeywordsContainingIgnoreCase(keyword, pageable)
                .map(bookMapper::toResponseDTO);
    }

    @Override
    @Transactional
    public BookResponseDTO addBook(BookRequestDTO request) {
        log.info("Adding new book with title: {}", request.getTitle());

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        if (request.getIsbn() != null && !request.getIsbn().isBlank()) {
            if (bookRepository.findByIsbn(request.getIsbn()).isPresent()) {
                throw new BadRequestException("Book with ISBN " + request.getIsbn() + " already exists");
            }
        }

        Book book = bookMapper.toEntity(request);
        book.setCategory(category);

        if (book.getCoverImage() == null || book.getCoverImage().isBlank()) {
            book.setCoverImage("https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&q=80&w=600");
        }

        Book savedBook = bookRepository.save(book);
        log.info("Successfully added book ID: {}", savedBook.getId());
        return bookMapper.toResponseDTO(savedBook);
    }

    @Override
    @Transactional
    public BookResponseDTO updateBook(Long bookId, BookRequestDTO request) {
        log.info("Updating book ID: {}", bookId);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            book.setCategory(category);
        }

        if (request.getTitle() != null) book.setTitle(request.getTitle());
        if (request.getAuthor() != null) book.setAuthor(request.getAuthor());
        if (request.getDescription() != null) book.setDescription(request.getDescription());
        if (request.getIsbn() != null) book.setIsbn(request.getIsbn());
        if (request.getLanguage() != null) book.setLanguage(request.getLanguage());
        if (request.getPublicationYear() != null) book.setPublicationYear(request.getPublicationYear());
        if (request.getPages() != null) book.setPages(request.getPages());
        if (request.getCoverImage() != null) book.setCoverImage(request.getCoverImage());
        if (request.getBookFileUrl() != null) book.setBookFileUrl(request.getBookFileUrl());
        if (request.getBookFileType() != null) book.setBookFileType(request.getBookFileType());
        if (request.getBookFileName() != null) book.setBookFileName(request.getBookFileName());
        if (request.getKeywords() != null) book.setKeywords(request.getKeywords());

        if (request.getTotalCopies() != null) {
            book.setTotalCopies(request.getTotalCopies());
            if (book.getAvailableCopies() == null || book.getAvailableCopies() > request.getTotalCopies()) {
                book.setAvailableCopies(request.getTotalCopies());
            }
        }

        Book updatedBook = bookRepository.save(book);
        log.info("Book ID: {} updated successfully", bookId);
        return bookMapper.toResponseDTO(updatedBook);
    }

    @Override
    @Transactional
    public BookResponseDTO uploadBookFile(Long bookId, org.springframework.web.multipart.MultipartFile file) {
        log.info("Uploading book file for book ID: {}", bookId);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file cannot be empty");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.toLowerCase().endsWith(".pdf") && !"application/pdf".equalsIgnoreCase(file.getContentType()))) {
            throw new BadRequestException("Invalid book file format. Only PDF files are supported");
        }

        // 50 MB max limit
        if (file.getSize() > 50 * 1024 * 1024) {
            throw new BadRequestException("Book file size exceeds maximum limit of 50MB");
        }

        // Delete previous stored file if it exists
        if (book.getBookFileName() != null && book.getBookFileUrl() != null && !book.getBookFileUrl().startsWith("http")) {
            fileStorageService.deleteFile(book.getBookFileUrl());
        }

        String storedPath = fileStorageService.storeFile(file, "books");
        book.setBookFileUrl(storedPath);
        book.setBookFileName(filename);
        book.setBookFileType("application/pdf");

        Book savedBook = bookRepository.save(book);
        log.info("Successfully saved digital book file for book ID: {}", bookId);
        return bookMapper.toResponseDTO(savedBook);
    }

    @Override
    @Transactional
    public void deleteBook(Long bookId) {
        log.info("Deleting book ID: {}", bookId);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        if (book.getBookFileUrl() != null && !book.getBookFileUrl().startsWith("http")) {
            fileStorageService.deleteFile(book.getBookFileUrl());
        }

        bookRepository.delete(book);
        log.info("Book ID: {} deleted", bookId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookSummaryDTO> getTrendingBooks(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "rating"));
        return bookRepository.findAll(pageable).stream()
                .map(bookMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookSummaryDTO> getRecommendedBooks(Long userId, int limit) {
        // AI/Preference based fallback logic sorting by rating
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "rating"));
        return bookRepository.findAll(pageable).stream()
                .map(bookMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }
}
