package com.smartlibrary.service.impl;

import com.smartlibrary.dto.UserInterestRequestDTO;
import com.smartlibrary.dto.UserInterestResponseDTO;
import com.smartlibrary.entity.User;
import com.smartlibrary.entity.UserInterest;
import com.smartlibrary.exception.BadRequestException;
import com.smartlibrary.exception.ResourceNotFoundException;
import com.smartlibrary.mapper.UserInterestMapper;
import com.smartlibrary.repository.UserInterestRepository;
import com.smartlibrary.repository.UserRepository;
import com.smartlibrary.service.UserInterestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserInterestServiceImpl implements UserInterestService {

    private final UserInterestRepository userInterestRepository;
    private final UserRepository userRepository;
    private final UserInterestMapper userInterestMapper;

    public UserInterestServiceImpl(UserInterestRepository userInterestRepository,
                                  UserRepository userRepository,
                                  UserInterestMapper userInterestMapper) {
        this.userInterestRepository = userInterestRepository;
        this.userRepository = userRepository;
        this.userInterestMapper = userInterestMapper;
    }

    @Override
    @Transactional
    public UserInterestResponseDTO addInterest(UserInterestRequestDTO request) {
        log.info("Adding new interest: {}", request.getInterestName());
        if (userInterestRepository.existsByInterestName(request.getInterestName())) {
            throw new BadRequestException("Interest already exists: " + request.getInterestName());
        }

        UserInterest interest = userInterestMapper.toEntity(request);
        UserInterest savedInterest = userInterestRepository.save(interest);
        log.info("Interest ID: {} added successfully", savedInterest.getId());
        return userInterestMapper.toResponseDTO(savedInterest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserInterestResponseDTO> getUserInterests(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return user.getUserInterests().stream()
                .map(userInterestMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Set<UserInterestResponseDTO> saveUserInterests(Long userId, Set<Long> interestIds) {
        return updateUserInterests(userId, interestIds);
    }

    @Override
    @Transactional
    public Set<UserInterestResponseDTO> updateUserInterests(Long userId, Set<Long> interestIds) {
        log.info("Updating interests for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (interestIds != null && !interestIds.isEmpty()) {
            List<UserInterest> interests = userInterestRepository.findAllById(interestIds);
            user.setUserInterests(new HashSet<>(interests));
        } else {
            user.getUserInterests().clear();
        }

        User savedUser = userRepository.save(user);
        log.info("Interests updated for user ID: {}", userId);

        return savedUser.getUserInterests().stream()
                .map(userInterestMapper::toResponseDTO)
                .collect(Collectors.toSet());
    }
}
