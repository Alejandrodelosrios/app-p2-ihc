package com.ihc.mascotas.backend.auth.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, UUID> {
    boolean existsByEmail(String email);
    Optional<UserJpaEntity> findByEmail(String email);
    List<UserJpaEntity> findTop10ByEmailStartingWithIgnoreCaseAndIdNotOrderByEmailAsc(
        String emailPrefix, UUID excludedUserId);
}
