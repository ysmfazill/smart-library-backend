package com.smartlibrary.service;

import com.smartlibrary.dto.UserCollectionDTO;

import java.util.List;

public interface CollectionService {
    List<UserCollectionDTO> getUserCollections(Long userId);
    UserCollectionDTO createCollection(Long userId, String name);
    void deleteCollection(Long collectionId, Long userId);
    void addBookToCollection(Long collectionId, Long bookId, Long userId);
    void removeBookFromCollection(Long collectionId, Long bookId, Long userId);
    void ensureSystemCollectionsExist(Long userId);
}
