package com.pium.adapter.outbound.user.persistence.repository;

import com.pium.adapter.outbound.user.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserEntity, String> {
}
