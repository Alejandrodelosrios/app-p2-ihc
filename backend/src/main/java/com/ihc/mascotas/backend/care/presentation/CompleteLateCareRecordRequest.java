package com.ihc.mascotas.backend.care.presentation;

import java.time.LocalDate;

/** Fecha real en que se hizo el cuidado. Si es null se usa hoy. */
public record CompleteLateCareRecordRequest(LocalDate completedOn) {
}
