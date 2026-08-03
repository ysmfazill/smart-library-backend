-- ===================================================================
-- AI-Powered Smart Library Recommendation System
-- Complete Production Database Schema (MySQL 8.0+)
-- ===================================================================

-- CREATE DATABASE IF NOT EXISTS smart_library_db
--     CHARACTER SET utf8mb4
--     COLLATE utf8mb4_unicode_ci;
-- USE smart_library_db;

-- -------------------------------------------------------------------
-- 1. CATEGORIES TABLE
-- Represents book genres, subjects, and topics in a hierarchical structure.
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    parent_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_categories_parent 
        FOREIGN KEY (parent_id) 
        REFERENCES categories(id) 
        ON DELETE SET NULL 
        ON UPDATE CASCADE,
        
    INDEX idx_categories_slug (slug),
    INDEX idx_categories_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------------
-- 2. USERS TABLE
-- Stores general user accounts, profile details, and account status.
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    avatar_url VARCHAR(512) NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_users_email (email),
    INDEX idx_users_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------------
-- 3. ADMINS TABLE
-- Dedicated administrative details linked to user accounts for role isolation.
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS admins (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    admin_level ENUM('SUPER_ADMIN', 'LIBRARIAN', 'CONTENT_MANAGER') DEFAULT 'LIBRARIAN',
    department VARCHAR(100) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_admins_user 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
        
    INDEX idx_admins_user_id (user_id),
    INDEX idx_admins_level (admin_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------------
-- 4. BOOKS TABLE
-- Core catalog of books containing metadata, availability, and stats.
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    isbn VARCHAR(20) UNIQUE NULL,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    publisher VARCHAR(255) NULL,
    publication_year SMALLINT NULL,
    category_id BIGINT NOT NULL,
    description TEXT NULL,
    cover_image_url VARCHAR(512) NULL,
    file_url VARCHAR(512) NULL,
    average_rating DECIMAL(3, 2) DEFAULT 0.00,
    ratings_count INT UNSIGNED DEFAULT 0,
    views_count INT UNSIGNED DEFAULT 0,
    total_copies INT UNSIGNED DEFAULT 1,
    available_copies INT UNSIGNED DEFAULT 1,
    status ENUM('AVAILABLE', 'OUT_OF_STOCK', 'ARCHIVED') DEFAULT 'AVAILABLE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_books_category 
        FOREIGN KEY (category_id) 
        REFERENCES categories(id) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE,
        
    INDEX idx_books_title (title),
    INDEX idx_books_author (author),
    INDEX idx_books_category (category_id),
    INDEX idx_books_isbn (isbn),
    INDEX idx_books_rating (average_rating DESC),
    FULLTEXT INDEX idx_books_fulltext (title, author, description)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------------
-- 5. USER INTERESTS TABLE
-- User topic/genre preferences used for AI cold-start recommendation engine.
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_interests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    weight DECIMAL(3, 2) DEFAULT 1.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uq_user_category UNIQUE (user_id, category_id),
    
    CONSTRAINT fk_user_interests_user 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
        
    CONSTRAINT fk_user_interests_category 
        FOREIGN KEY (category_id) 
        REFERENCES categories(id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
        
    INDEX idx_user_interests_user (user_id),
    INDEX idx_user_interests_category (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------------
-- 6. FAVORITES TABLE
-- Tracks user saved / bookmarked books.
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uq_user_book_favorite UNIQUE (user_id, book_id),
    
    CONSTRAINT fk_favorites_user 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
        
    CONSTRAINT fk_favorites_book 
        FOREIGN KEY (book_id) 
        REFERENCES books(id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
        
    INDEX idx_favorites_user (user_id),
    INDEX idx_favorites_book (book_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------------
-- 7. READING HISTORY TABLE
-- Tracks reading activity, progress metrics, and reading statuses.
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS reading_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    status ENUM('WANT_TO_READ', 'READING', 'COMPLETED', 'DROPPED') DEFAULT 'READING',
    last_page_read INT UNSIGNED DEFAULT 0,
    total_pages INT UNSIGNED DEFAULT 0,
    progress_percentage DECIMAL(5, 2) GENERATED ALWAYS AS (
        CASE 
            WHEN total_pages > 0 THEN LEAST(100.00, ROUND((last_page_read / total_pages) * 100, 2)) 
            ELSE 0.00 
        END
    ) STORED,
    started_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    last_read_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT uq_user_book_reading UNIQUE (user_id, book_id),
    
    CONSTRAINT fk_reading_history_user 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
        
    CONSTRAINT fk_reading_history_book 
        FOREIGN KEY (book_id) 
        REFERENCES books(id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
        
    INDEX idx_reading_user (user_id),
    INDEX idx_reading_book (book_id),
    INDEX idx_reading_status (status),
    INDEX idx_reading_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------------
-- 8. REVIEWS TABLE
-- Stores user ratings (1-5), textual reviews, and spoiler flags.
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    rating TINYINT UNSIGNED NOT NULL,
    review_text TEXT NULL,
    likes_count INT UNSIGNED DEFAULT 0,
    is_spoiler BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT uq_user_book_review UNIQUE (user_id, book_id),
    
    CONSTRAINT chk_review_rating CHECK (rating BETWEEN 1 AND 5),
    
    CONSTRAINT fk_reviews_user 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
        
    CONSTRAINT fk_reviews_book 
        FOREIGN KEY (book_id) 
        REFERENCES books(id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
        
    INDEX idx_reviews_book (book_id),
    INDEX idx_reviews_user (user_id),
    INDEX idx_reviews_rating (rating)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 10. IMPORT HISTORY TABLE
-- Logs Excel bulk book imports by administrators.
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS import_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    imported_by BIGINT NOT NULL,
    import_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    filename VARCHAR(255) NOT NULL,
    books_imported INT DEFAULT 0,
    duplicates_skipped INT DEFAULT 0,
    invalid_rows INT DEFAULT 0,
    import_duration_ms BIGINT DEFAULT 0,
    status VARCHAR(50) NOT NULL,
    
    CONSTRAINT fk_import_user 
        FOREIGN KEY (imported_by) 
        REFERENCES users(id) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE,
        
    INDEX idx_import_date (import_date),
    INDEX idx_import_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
