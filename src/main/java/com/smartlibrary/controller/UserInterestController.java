package com.smartlibrary.controller;

import com.smartlibrary.dto.ApiResponse;
import com.smartlibrary.dto.UserInterestRequestDTO;
import com.smartlibrary.dto.UserInterestResponseDTO;
import com.smartlibrary.service.UserInterestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * REST Controller for managing user recommendation topic/genre interests.
 */
@RestController
@RequestMapping("/interests")
public class UserInterestController {

    private final UserInterestService userInterestService;

    public UserInterestController(UserInterestService userInterestService) {
        this.userInterestService = userInterestService;
    }

    /**
     * Gets recommendation interest topics associated with a user.
     *
     * @param userId User ID parameter.
     * @return List of user interest DTOs.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserInterestResponseDTO>>> getUserInterests(
            @RequestParam(defaultValue = "1") Long userId) {
        List<UserInterestResponseDTO> interests = userInterestService.getUserInterests(userId);
        return ResponseEntity.ok(ApiResponse.success("User interests retrieved successfully", interests));
    }

    /**
     * Registers a new interest category in the library catalog.
     *
     * @param request Interest request payload.
     * @return Created interest DTO.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserInterestResponseDTO>> addInterest(
            @Valid @RequestBody UserInterestRequestDTO request) {
        UserInterestResponseDTO response = userInterestService.addInterest(request);
        return new ResponseEntity<>(ApiResponse.success("Interest category created successfully", response), HttpStatus.CREATED);
    }

    /**
     * Updates interest preferences mapped to a user account.
     *
     * @param userId User ID parameter.
     * @param interestIds Set of target interest IDs.
     * @return Set of updated user interest DTOs.
     */
    @PutMapping
    public ResponseEntity<ApiResponse<Set<UserInterestResponseDTO>>> updateUserInterests(
            @RequestParam(defaultValue = "1") Long userId,
            @RequestBody Set<Long> interestIds) {
        Set<UserInterestResponseDTO> response = userInterestService.updateUserInterests(userId, interestIds);
        return ResponseEntity.ok(ApiResponse.success("User interest preferences updated successfully", response));
    }
}
