package com.smartlibrary.repository;

import com.smartlibrary.entity.UserInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {

    Optional<UserInterest> findByInterestName(String interestName);

    Optional<UserInterest> findByInterestNameIgnoreCase(String interestName);

    boolean existsByInterestName(String interestName);
}
