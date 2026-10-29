package com.ihc.mascotas.backend.auth.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    Optional<User> findById(UUID id);
    List<User> searchByEmailPrefix(String emailPrefix, UUID excludedUserId);
    User save(User user);
}
