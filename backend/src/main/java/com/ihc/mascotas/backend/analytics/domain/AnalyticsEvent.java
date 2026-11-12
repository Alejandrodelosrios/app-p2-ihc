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

    // autenticacion
    public static final String REGISTER_OPENED = "registro_abierto";
    public static final String REGISTER_COMPLETED = "registro_completado";
    public static final String LOGIN_ATTEMPTED = "login_intentado";
    public static final String LOGIN_COMPLETED = "login_completado";
    public static final String PASSWORD_RECOVERY_REQUESTED = "recuperacion_solicitada";
    public static final String PASSWORD_RESET_COMPLETED = "contrasena_restablecida";

    // mascotas
    public static final String PET_FORM_OPENED = "mascota_formulario_abierto";
    public static final String PET_CREATED = "mascota_creada";
    public static final String PET_EDIT_OPENED = "mascota_edicion_abierta";
    public static final String PET_UPDATED = "mascota_actualizada";
    public static final String PET_DELETED = "mascota_eliminada";

    // cuidados
    public static final String CARE_EDIT_OPENED = "cuidado_edicion_abierta";
    public static final String CARE_UPDATED = "cuidado_actualizado";
    public static final String CARE_DELETED = "cuidado_eliminado";
    public static final String CARE_RESCHEDULED = "cuidado_reprogramado";
    public static final String CARE_MARKED_DONE = "cuidado_realizado";

    // funcionalidades adicionales
    public static final String CARE_ASSIGNED = "cuidado_asignado";
    public static final String RECURRENCE_CONFIGURED = "recurrencia_configurada";

    private static final Set<String> ALLOWED_NAMES = Set.of(
        CARE_OPENED,
        CARE_COMPLETED,
        
        REGISTER_OPENED,
        REGISTER_COMPLETED,
        LOGIN_ATTEMPTED,
        LOGIN_COMPLETED,
        PASSWORD_RECOVERY_REQUESTED,
        PASSWORD_RESET_COMPLETED,

        PET_FORM_OPENED,
        PET_CREATED,
        PET_EDIT_OPENED,
        PET_UPDATED,
        PET_DELETED,

        CARE_EDIT_OPENED,
        CARE_UPDATED,
        CARE_DELETED,
        CARE_RESCHEDULED,
        CARE_MARKED_DONE,

        CARE_ASSIGNED,
        RECURRENCE_CONFIGURED

    );
    private static final Set<String> ALLOWED_PROPS = Set.of(
        "modo",
        "recurrente",
        "asignado",
        "especie",
        "origen"
    );
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
