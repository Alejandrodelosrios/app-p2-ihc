package com.ihc.mascotas.backend.auth.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_user")
class UserJpaEntity {
    @Id
    UUID id;

    @Column(name = "full_name", nullable = false, length = 120)
    String fullName;

    @Column(nullable = false, unique = true, length = 180)
    String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    String passwordHash;

    @Column(name = "created_at", nullable = false)
    Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;

    @Column(name = "deleted_at")
    Instant deletedAt;

    protected UserJpaEntity() { }

    UserJpaEntity(UUID id, String fullName, String email, String passwordHash, Instant createdAt, Instant updatedAt, Instant deletedAt){
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }
}