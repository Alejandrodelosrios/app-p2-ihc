package com.ihc.mascotas.backend.auth.domain;

import java.time.Instant;
import java.util.UUID;

public record User(
        UUID id,
        String fullName,
        String email,
        String passwordHash,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt) {
  
  public User(UUID id, String fullName, String email, String passwordHash, Instant createdAt, Instant updatedAt){
        this(id,fullName,email,passwordHash,createdAt,updatedAt,null);
  }
  
  public boolean isActive(){
        return deletedAt == null;
  }
}