package com.ihc.mascotas.backend.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DemoDataSeeder implements ApplicationRunner {
    private static final UUID DEMO_USER_ID = UUID.fromString("6f8f8f0d-7a2c-4c1e-9f34-0c1a8b7d2e11");
    private static final String DEMO_EMAIL = "ana@example.com";
    private static final String DEMO_PASSWORD = "password123";
    private static final UUID ALEX_USER_ID = UUID.fromString("2f8e9c1a-3b4d-4e2f-9c1a-8b7d2e11f8f8");
    private static final String ALEX_USER_EMAIL = "alex@example.com";
    private static final String ALEX_USER_PASSWORD = "12345678";

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.update("""
                INSERT INTO app_user (id, full_name, email, password_hash, created_at, updated_at)
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                ON CONFLICT (email) DO NOTHING
                """,
                DEMO_USER_ID,
                "Ana Pérez",
                DEMO_EMAIL,
                passwordEncoder.encode(DEMO_PASSWORD));
            
        jdbcTemplate.update("""
                INSERT INTO app_user (id, full_name, email, password_hash, created_at, updated_at)
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                ON CONFLICT (email) DO NOTHING
                """,
                ALEX_USER_ID,
                "Alex Pérez",
                ALEX_USER_EMAIL,
                passwordEncoder.encode(ALEX_USER_PASSWORD));

        jdbcTemplate.update("""
                INSERT INTO care_record
                    (id, user_id, pet_name, care, animal_type, care_date, created_at, status)
                SELECT ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?
                WHERE EXISTS (SELECT 1 FROM app_user WHERE id = ?)
                  AND NOT EXISTS (SELECT 1 FROM care_record WHERE id = ?)
                """,
                UUID.fromString("f7c1d9e2-42d7-4a2d-b8c1-8c4f6e5a9011"),
                DEMO_USER_ID,
                "Luna",
                "Vacuna antirrábica",
                "Perro",
                java.sql.Date.valueOf("2026-10-15"),
                "COMPLETED",
                DEMO_USER_ID,
                UUID.fromString("f7c1d9e2-42d7-4a2d-b8c1-8c4f6e5a9011"));

        jdbcTemplate.update("""
                INSERT INTO care_record
                    (id, user_id, pet_name, care, animal_type, care_date, created_at, status)
                SELECT ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?
                WHERE EXISTS (SELECT 1 FROM app_user WHERE id = ?)
                  AND NOT EXISTS (SELECT 1 FROM care_record WHERE id = ?)
                """,
                UUID.fromString("2a6e3f91-8b5c-4d72-a0f6-1c9e7b3d5022"),
                DEMO_USER_ID,
                "Milo",
                "Desparasitación",
                "Gato",
                java.sql.Date.valueOf("2026-11-05"),
                "PENDING",
                DEMO_USER_ID,
                UUID.fromString("2a6e3f91-8b5c-4d72-a0f6-1c9e7b3d5022"));

        jdbcTemplate.update("""
                INSERT INTO care_record
                    (id, user_id, pet_name, care, animal_type, care_date, created_at, status)
                SELECT ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?
                WHERE EXISTS (SELECT 1 FROM app_user WHERE id = ?)
                  AND NOT EXISTS (SELECT 1 FROM care_record WHERE id = ?)
                """,
                UUID.fromString("9b4d2c68-1f73-46a9-8e05-3d7c2b6f8044"),
                DEMO_USER_ID,
                "Coco",
                "Control veterinario",
                "Conejo",
                java.sql.Date.valueOf("2026-11-20"),
                "PENDING",
                DEMO_USER_ID,
                UUID.fromString("9b4d2c68-1f73-46a9-8e05-3d7c2b6f8044"));
    }
}
