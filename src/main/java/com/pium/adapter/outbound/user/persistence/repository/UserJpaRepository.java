package com.pium.adapter.outbound.user.persistence.repository;

import com.pium.adapter.outbound.user.persistence.entity.UserEntity;
import com.pium.domain.user.enumtype.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserJpaRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByUserIdAndStatus(String value, UserStatus userStatus);
}
