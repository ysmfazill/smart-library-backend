package com.smartlibrary.repository;

import com.smartlibrary.entity.CollectionBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionBookRepository extends JpaRepository<CollectionBook, Long> {
    List<CollectionBook> findByCollectionId(Long collectionId);
    Optional<CollectionBook> findByCollectionIdAndBookId(Long collectionId, Long bookId);
    void deleteByCollectionIdAndBookId(Long collectionId, Long bookId);
}
