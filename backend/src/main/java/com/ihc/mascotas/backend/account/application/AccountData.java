package com.ihc.mascotas.backend.account.application;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Copia de los datos de una persona ("Mis datos"). Solo incluye lo suyo: no trae
 * correos ni nombres de otras personas (minimización de datos).
 */
public record AccountData(
        Instant generatedAt,
        Profile profile,
        List<PetData> pets,
        List<CareData> cares,
        List<HistoryData> history) {

    public record Profile(UUID id, String fullName, String email, Instant createdAt) { }

    public record PetData(String name, String species, String breed, LocalDate birthDate) { }

    /** role: "gestor" si el cuidado lo creó la persona, "asignado" si se lo asignaron. */
    public record CareData(
            String pet,
            String care,
            LocalDate date,
            String status,
            String role,
            Integer recurrenceIntervalDays,
            String changeReason) { }

    public record HistoryData(
            String pet,
            String care,
            String action,
            LocalDate previousDate,
            LocalDate currentDate,
            Instant occurredAt,
            String changeReason) { }
}
