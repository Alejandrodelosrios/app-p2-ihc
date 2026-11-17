package com.ihc.mascotas.backend.account.application;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountDataCsvTest {

    private AccountData data(String careText) {
        return new AccountData(
                Instant.parse("2026-11-15T12:00:00Z"),
                new AccountData.Profile(UUID.randomUUID(), "Ana Pérez", "ana@example.com",
                        Instant.parse("2026-01-01T00:00:00Z")),
                List.of(new AccountData.PetData("Luna", "CAT", "Siamés", LocalDate.of(2022, 5, 10))),
                List.of(new AccountData.CareData("Luna", careText, LocalDate.of(2026, 12, 1),
                        "PENDING", "gestor", null, null)),
                List.of());
    }

    @Test
    void startsWithBomAndHeader() {
        String csv = AccountDataCsv.toCsv(data("Vacuna"));

        assertTrue(csv.startsWith("\uFEFFtipo,mascota,detalle,fecha,estado,rol,motivo\r\n"));
        assertTrue(csv.contains("mascota,Luna,CAT - Siamés,2022-05-10,,dueño,"));
        assertTrue(csv.contains("cuidado,Luna,Vacuna,2026-12-01,PENDING,gestor,"));
    }

    @Test
    void quotesCommasAndQuotes() {
        assertEquals("\"a, \"\"b\"\"\"", AccountDataCsv.escape("a, \"b\""));
    }

    @Test
    void neutralizesSpreadsheetFormulas() {
        assertEquals("'=SUMA(A1)", AccountDataCsv.escape("=SUMA(A1)"));
        assertEquals("'@usuario", AccountDataCsv.escape("@usuario"));
    }
}
