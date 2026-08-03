package com.smartlibrary.repository;

import com.smartlibrary.entity.UserCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCollectionRepository extends JpaRepository<UserCollection, Long> {
    List<UserCollection> findByUserId(Long userId);
    Optional<UserCollection> findByUserIdAndName(Long userId, String name);
}
