package com.ihc.mascotas.backend.account.application;

import java.util.ArrayList;
import java.util.List;

/** Convierte "Mis datos" a una tabla CSV (UTF-8 con BOM para que Excel respete las tildes). */
public final class AccountDataCsv {

    private static final String BOM = "\uFEFF";
    private static final String HEADER = "tipo,mascota,detalle,fecha,estado,rol,motivo";

    private AccountDataCsv() { }

    public static String toCsv(AccountData data) {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);

        AccountData.Profile profile = data.profile();
        lines.add(row("perfil", "", profile.fullName() + " (" + profile.email() + ")",
                String.valueOf(profile.createdAt()), "", "", ""));

        for (AccountData.PetData pet : data.pets()) {
            String detail = pet.breed() == null || pet.breed().isBlank()
                    ? pet.species()
                    : pet.species() + " - " + pet.breed();
            lines.add(row("mascota", pet.name(), detail, text(pet.birthDate()),
                    "", "dueño", ""));
        }
        for (AccountData.CareData care : data.cares()) {
            lines.add(row("cuidado", care.pet(), care.care(), text(care.date()),
                    care.status(), care.role(), text(care.changeReason())));
        }
        for (AccountData.HistoryData entry : data.history()) {
            lines.add(row("historial", entry.pet(), entry.care(),
                    text(entry.currentDate()), entry.action(), "",
                    text(entry.changeReason())));
        }
        return BOM + String.join("\r\n", lines) + "\r\n";
    }

    private static String row(String... cells) {
        List<String> escaped = new ArrayList<>();
        for (String cell : cells) {
            escaped.add(escape(cell));
        }
        return String.join(",", escaped);
    }

    static String escape(String value) {
        String text = value == null ? "" : value;
        // Evita que Excel interprete texto escrito por el usuario como una fórmula.
        if (!text.isEmpty() && "=+-@\t\r".indexOf(text.charAt(0)) >= 0) {
            text = "'" + text;
        }
        if (text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    private static String text(Object value) {
        return value == null ? "" : value.toString();
    }
}