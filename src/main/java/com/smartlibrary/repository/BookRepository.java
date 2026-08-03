package com.smartlibrary.repository;

import com.smartlibrary.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    boolean existsByTitleIgnoreCaseAndAuthorIgnoreCase(String title, String author);

    List<Book> findByTitleContainingIgnoreCase(String title);

    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    List<Book> findByAuthorContainingIgnoreCase(String author);

    Page<Book> findByAuthorContainingIgnoreCase(String author, Pageable pageable);

    List<Book> findByCategory_Name(String categoryName);

    Page<Book> findByCategory_Name(String categoryName, Pageable pageable);

    Page<Book> findByCategoryId(Long categoryId, Pageable pageable);

    List<Book> findByLanguage(String language);

    List<Book> findByPublicationYear(Integer year);

    List<Book> findByRatingGreaterThanEqual(Double rating);

    Page<Book> findByRatingGreaterThanEqual(Double rating, Pageable pageable);

    List<Book> findByKeywordsContainingIgnoreCase(String keyword);

    Page<Book> findByKeywordsContainingIgnoreCase(String keyword, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE " +
           "(:query IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.keywords) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:categoryId IS NULL OR b.category.id = :categoryId) AND " +
           "(:minRating IS NULL OR b.rating >= :minRating)")
    Page<Book> searchBooks(@Param("query") String query,
                           @Param("categoryId") Long categoryId,
                           @Param("minRating") Double minRating,
                           Pageable pageable);

    @Query("SELECT b FROM Book b JOIN FETCH b.category ORDER BY b.createdAt DESC")
    Page<Book> findNewArrivals(Pageable pageable);

    @Query("SELECT b FROM Book b JOIN FETCH b.category ORDER BY b.rating DESC")
    Page<Book> findPopularBooks(Pageable pageable);

    @Query("SELECT b FROM Book b JOIN FETCH b.category WHERE b.category.id IN :categoryIds")
    Page<Book> findByCategoryIdIn(@Param("categoryIds") List<Long> categoryIds, Pageable pageable);
    
    @Query("SELECT b FROM Book b JOIN FETCH b.category WHERE b.id IN :bookIds")
    List<Book> findAllByIdInWithCategory(@Param("bookIds") List<Long> bookIds);

    @Query("SELECT b.author as author, COUNT(b.id) as count FROM Book b GROUP BY b.author ORDER BY COUNT(b.id) DESC")
    Page<Object[]> findPopularAuthors(Pageable pageable);
}
