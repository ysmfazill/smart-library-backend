package com.smartlibrary.service.impl;

import com.smartlibrary.dto.BookResponseDTO;
import com.smartlibrary.dto.UserCollectionDTO;
import com.smartlibrary.entity.Book;
import com.smartlibrary.entity.CollectionBook;
import com.smartlibrary.entity.User;
import com.smartlibrary.entity.UserCollection;
import com.smartlibrary.exception.BadRequestException;
import com.smartlibrary.exception.ResourceNotFoundException;
import com.smartlibrary.mapper.BookMapper;
import com.smartlibrary.repository.BookRepository;
import com.smartlibrary.repository.CollectionBookRepository;
import com.smartlibrary.repository.UserCollectionRepository;
import com.smartlibrary.repository.UserRepository;
import com.smartlibrary.service.CollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollectionServiceImpl implements CollectionService {

    private final UserCollectionRepository collectionRepository;
    private final CollectionBookRepository collectionBookRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    private static final List<String> SYSTEM_COLLECTIONS = Arrays.asList(
            "Want To Read", "Currently Reading", "Completed", "Favorites"
    );

    @Override
    @Transactional
    public void ensureSystemCollectionsExist(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        for (String sysCol : SYSTEM_COLLECTIONS) {
            if (collectionRepository.findByUserIdAndName(userId, sysCol).isEmpty()) {
                UserCollection collection = UserCollection.builder()
                        .user(user)
                        .name(sysCol)
                        .isSystem(true)
                        .build();
                collectionRepository.save(collection);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserCollectionDTO> getUserCollections(Long userId) {
        return collectionRepository.findByUserId(userId).stream().map(col -> {
            List<BookResponseDTO> books = collectionBookRepository.findByCollectionId(col.getId())
                    .stream()
                    .map(cb -> bookMapper.toResponseDTO(cb.getBook()))
                    .collect(Collectors.toList());

            return UserCollectionDTO.builder()
                    .id(col.getId())
                    .userId(col.getUser().getId())
                    .name(col.getName())
                    .isSystem(col.isSystem())
                    .createdAt(col.getCreatedAt())
                    .books(books)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserCollectionDTO createCollection(Long userId, String name) {
        if (collectionRepository.findByUserIdAndName(userId, name).isPresent()) {
            throw new BadRequestException("Collection with name '" + name + "' already exists.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        UserCollection collection = UserCollection.builder()
                .user(user)
                .name(name)
                .isSystem(false)
                .build();
        UserCollection saved = collectionRepository.save(collection);

        return UserCollectionDTO.builder()
                .id(saved.getId())
                .userId(userId)
                .name(saved.getName())
                .isSystem(false)
                .createdAt(saved.getCreatedAt())
                .books(List.of())
                .build();
    }

    @Override
    @Transactional
    public void deleteCollection(Long collectionId, Long userId) {
        UserCollection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Collection", "id", collectionId));

        if (!collection.getUser().getId().equals(userId)) {
            throw new BadRequestException("Not authorized to delete this collection.");
        }
        if (collection.isSystem()) {
            throw new BadRequestException("Cannot delete a system collection.");
        }

        // Delete associated books first (cascade not set up explicitly, so doing it manually or let JPA handle if mapping exists)
        List<CollectionBook> cbs = collectionBookRepository.findByCollectionId(collectionId);
        collectionBookRepository.deleteAll(cbs);
        collectionRepository.delete(collection);
    }

    @Override
    @Transactional
    public void addBookToCollection(Long collectionId, Long bookId, Long userId) {
        UserCollection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Collection", "id", collectionId));
        if (!collection.getUser().getId().equals(userId)) {
            throw new BadRequestException("Not authorized to modify this collection.");
        }
        
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        if (collectionBookRepository.findByCollectionIdAndBookId(collectionId, bookId).isPresent()) {
            return; // Already exists
        }

        CollectionBook cb = CollectionBook.builder()
                .collection(collection)
                .book(book)
                .build();
        collectionBookRepository.save(cb);
    }

    @Override
    @Transactional
    public void removeBookFromCollection(Long collectionId, Long bookId, Long userId) {
        UserCollection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Collection", "id", collectionId));
        if (!collection.getUser().getId().equals(userId)) {
            throw new BadRequestException("Not authorized to modify this collection.");
        }
        collectionBookRepository.deleteByCollectionIdAndBookId(collectionId, bookId);
    }
}
