package com.smartlibrary.controller;

import com.smartlibrary.dto.ApiResponse;
import com.smartlibrary.dto.FavoriteRequestDTO;
import com.smartlibrary.dto.FavoriteResponseDTO;
import com.smartlibrary.service.FavoriteService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for managing user bookmarked / favorite books.
 */
@RestController
@RequestMapping("/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    /**
     * Retrieves favorite books for a user.
     *
     * @param userId User ID parameter.
     * @param page Page index.
     * @param size Page size.
     * @return Paginated user favorites.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<FavoriteResponseDTO>>> getFavorites(
            @RequestParam(defaultValue = "1") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<FavoriteResponseDTO> favorites = favoriteService.getFavoritesByUser(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Favorites fetched successfully", favorites));
    }

    /**
     * Adds a book to user's favorites list.
     *
     * @param request Favorite request payload.
     * @return Created favorite item.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<FavoriteResponseDTO>> addFavorite(@Valid @RequestBody FavoriteRequestDTO request) {
        FavoriteResponseDTO response = favoriteService.addFavorite(request);
        return new ResponseEntity<>(ApiResponse.success("Book added to favorites", response), HttpStatus.CREATED);
    }

    /**
     * Removes a book from user's favorites list.
     *
     * @param bookId Target Book ID to remove.
     * @param userId User ID parameter.
     * @return Removal confirmation.
     */
    @DeleteMapping("/{bookId}")
    public ResponseEntity<ApiResponse<String>> removeFavorite(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "1") Long userId) {
        favoriteService.removeFavorite(userId, bookId);
        return ResponseEntity.ok(ApiResponse.success("Book removed from favorites", "SUCCESS"));
    }
}
