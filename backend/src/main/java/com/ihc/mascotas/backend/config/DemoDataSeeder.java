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

    private static final UUID LUNA_PET_ID = UUID.fromString("c3a1f5d2-7b84-4e19-a6c0-5d2e8f1b9a01");
    private static final UUID MILO_PET_ID = UUID.fromString("d4b2a6e3-8c95-4f2a-b7d1-6e3f9a2c0b02");
    private static final UUID COCO_PET_ID = UUID.fromString("e5c3b7f4-9da6-4a3b-88e2-7f4a0b3d1c03");

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

        // Primero las mascotas (care_record.pet_id tiene FK hacia pet).
        seedPet(LUNA_PET_ID, "Luna", "DOG");
        seedPet(MILO_PET_ID, "Milo", "CAT");
        seedPet(COCO_PET_ID, "Coco", "RABBIT");

        seedCare(
                UUID.fromString("f7c1d9e2-42d7-4a2d-b8c1-8c4f6e5a9011"),
                LUNA_PET_ID,
                "Vacuna antirrábica",
                "2026-10-15",
                "COMPLETED");

        seedCare(
                UUID.fromString("2a6e3f91-8b5c-4d72-a0f6-1c9e7b3d5022"),
                MILO_PET_ID,
                "Desparasitación",
                "2026-11-05",
                "PENDING");

        seedCare(
                UUID.fromString("9b4d2c68-1f73-46a9-8e05-3d7c2b6f8044"),
                COCO_PET_ID,
                "Control veterinario",
                "2026-11-20",
                "PENDING");
    }

    private void seedPet(UUID petId, String name, String species) {
        jdbcTemplate.update("""
                INSERT INTO pet (id, owner_id, name, species, created_at, updated_at)
                SELECT ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                WHERE EXISTS (SELECT 1 FROM app_user WHERE id = ?)
                  AND NOT EXISTS (SELECT 1 FROM pet WHERE id = ?)
                """,
                petId,
                DEMO_USER_ID,
                name,
                species,
                DEMO_USER_ID,
                petId);
    }

    private void seedCare(UUID careId, UUID petId, String care, String careDate, String status) {
        jdbcTemplate.update("""
                INSERT INTO care_record
                    (id, user_id, pet_id, care, care_date, created_at, status)
                SELECT ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?
                WHERE EXISTS (SELECT 1 FROM pet WHERE id = ?)
                  AND NOT EXISTS (SELECT 1 FROM care_record WHERE id = ?)
                """,
                careId,
                DEMO_USER_ID,
                petId,
                care,
                java.sql.Date.valueOf(careDate),
                status,
                petId,
                careId);
    }
}