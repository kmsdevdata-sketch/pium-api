package com.pium.adapter.outbound.user.persistence.repository;

import com.pium.adapter.outbound.user.persistence.entity.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileJpaRepository extends JpaRepository<UserProfileEntity, String> {

    Optional<UserProfileEntity> findByUserId(String userId);
}
