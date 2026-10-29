package com.ihc.mascotas.backend.auth.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.ihc.mascotas.backend.auth.domain.User;
import com.ihc.mascotas.backend.auth.domain.UserRepository;

@Repository
class JpaUserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository repository;

    JpaUserRepositoryAdapter(SpringDataUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(JpaUserRepositoryAdapter::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return repository.findById(id).map(JpaUserRepositoryAdapter::toDomain);
    }

    @Override
    public List<User> searchByEmailPrefix(String emailPrefix, UUID excludedUserId) {
        return repository.findTop10ByEmailStartingWithIgnoreCaseAndIdNotOrderByEmailAsc(
                        emailPrefix, excludedUserId)
                .stream()
                .map(JpaUserRepositoryAdapter::toDomain)
                .toList();
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = new UserJpaEntity(
                user.id(), user.fullName(), user.email(), user.passwordHash(), user.createdAt(), user.updatedAt());
        return toDomain(repository.save(entity));
    }

    private static User toDomain(UserJpaEntity entity) {
        return new User(entity.id, entity.fullName, entity.email, entity.passwordHash, entity.createdAt, entity.updatedAt);
    }
}
