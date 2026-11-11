package com.ihc.mascotas.backend.analytics.domain;

import com.ihc.mascotas.backend.shared.exception.NotAllowedException;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public record AnalyticsEvent(
        UUID id,
        String name,
        Map<String, String> props,
        String sessionId,
        Instant occurredAt
) {
    public static final String CARE_OPENED = "cuidado_abierto";
    public static final String CARE_COMPLETED = "cuidado_completado";

    private static final Set<String> ALLOWED_NAMES = Set.of(CARE_OPENED, CARE_COMPLETED);
    private static final Set<String> ALLOWED_PROPS = Set.of("modo", "recurrente", "asignado");
    // Solo "tokens": minúsculas, dígitos y guion bajo. Un correo o una frase no pasan.
    private static final Pattern SAFE_VALUE = Pattern.compile("[a-z0-9_]{1,30}");

    public AnalyticsEvent {
        if (!ALLOWED_NAMES.contains(name)) {
            throw new NotAllowedException("Evento no permitido");
        }
        props = props == null ? Map.of() : Map.copyOf(props);
        for (Map.Entry<String, String> prop : props.entrySet()) {
            if (!ALLOWED_PROPS.contains(prop.getKey())) {
                throw new NotAllowedException("Propiedad no permitida: " + prop.getKey());
            }
            if (prop.getValue() == null || !SAFE_VALUE.matcher(prop.getValue()).matches()) {
                throw new NotAllowedException("Valor no permitido para " + prop.getKey());
            }
        }
    }
}
