package com.smartlibrary.controller;

import com.smartlibrary.dto.UserCollectionDTO;
import com.smartlibrary.security.CustomUserDetails;
import com.smartlibrary.service.CollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    @GetMapping
    public ResponseEntity<List<UserCollectionDTO>> getCollections(@AuthenticationPrincipal CustomUserDetails userDetails) {
        collectionService.ensureSystemCollectionsExist(userDetails.getId());
        return ResponseEntity.ok(collectionService.getUserCollections(userDetails.getId()));
    }

    @PostMapping
    public ResponseEntity<UserCollectionDTO> createCollection(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String name) {
        return ResponseEntity.ok(collectionService.createCollection(userDetails.getId(), name));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCollection(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        collectionService.deleteCollection(id, userDetails.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{collectionId}/books/{bookId}")
    public ResponseEntity<Void> addBook(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long collectionId,
            @PathVariable Long bookId) {
        collectionService.addBookToCollection(collectionId, bookId, userDetails.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{collectionId}/books/{bookId}")
    public ResponseEntity<Void> removeBook(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long collectionId,
            @PathVariable Long bookId) {
        collectionService.removeBookFromCollection(collectionId, bookId, userDetails.getId());
        return ResponseEntity.ok().build();
    }
}
