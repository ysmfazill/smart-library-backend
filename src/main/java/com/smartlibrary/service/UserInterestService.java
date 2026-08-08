package com.smartlibrary.service;

import com.smartlibrary.dto.UserInterestRequestDTO;
import com.smartlibrary.dto.UserInterestResponseDTO;

import java.util.List;
import java.util.Set;

public interface UserInterestService {

    UserInterestResponseDTO addInterest(UserInterestRequestDTO request);

    List<UserInterestResponseDTO> getUserInterests(Long userId);

    List<UserInterestResponseDTO> getAllInterests();

    Set<UserInterestResponseDTO> saveUserInterests(Long userId, Set<Long> interestIds);

    Set<UserInterestResponseDTO> updateUserInterests(Long userId, Set<Long> interestIds);
}
