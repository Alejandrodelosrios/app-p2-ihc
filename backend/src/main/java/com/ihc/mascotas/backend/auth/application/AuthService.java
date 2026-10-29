package com.ihc.mascotas.backend.auth.application;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ihc.mascotas.backend.auth.domain.User;
import com.ihc.mascotas.backend.auth.domain.UserRepository;
import com.ihc.mascotas.backend.shared.exception.ConflictException;
import com.ihc.mascotas.backend.shared.exception.NotFoundException;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final Clock clock;

    @Autowired
    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this(users, passwordEncoder, tokenService, Clock.systemUTC());
    }

    AuthService(UserRepository users, PasswordEncoder passwordEncoder, TokenService tokenService, Clock clock) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.clock = clock;
    }

    @Transactional
    public AuthResult register(String fullName, String email, String rawPassword) {
        String normalizedEmail = normalizeEmail(email);
        if (users.existsByEmail(normalizedEmail)) {
            throw new ConflictException("Ya existe una cuenta con ese correo electrónico");
        }

        Instant now = Instant.now(clock);
        User saved = users.save(new User(
                UUID.randomUUID(),
                fullName.trim(),
                normalizedEmail,
                passwordEncoder.encode(rawPassword),
                now,
                now));
        return toResult(saved);
    }

    @Transactional(readOnly = true)
    public AuthResult login(String email, String rawPassword) {
        User user = users.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));
        if (!passwordEncoder.matches(rawPassword, user.passwordHash())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }
        return toResult(user);
    }

    @Transactional(readOnly = true)
    public UserView currentUser(String email) {
        return users.findByEmail(normalizeEmail(email))
                .map(AuthService::toView)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }

    @Transactional(readOnly = true)
    public List<UserView> searchUsersByEmail(String currentEmail, String emailPrefix) {
        String prefix = normalizeEmail(emailPrefix == null ? "" : emailPrefix);
        if (prefix.length() < 3) {
            return List.of();
        }

        User currentUser = users.findByEmail(normalizeEmail(currentEmail))
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        return users.searchByEmailPrefix(prefix, currentUser.id())
                .stream()
                .map(AuthService::toView)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserView findPublicUserById(UUID id) {
        return users.findById(id)
                .map(AuthService::toView)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }

    private AuthResult toResult(User user) {
        TokenService.IssuedToken token = tokenService.issue(user);
        return new AuthResult(token.value(), token.expiresAt(), toView(user));
    }

    private static UserView toView(User user) {
        return new UserView(user.id(), user.fullName(), user.email());
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public record AuthResult(String accessToken, Instant expiresAt, UserView user) { }
    public record UserView(UUID id, String fullName, String email) { }
}
