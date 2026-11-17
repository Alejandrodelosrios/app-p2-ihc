package com.ihc.mascotas.backend.account.integration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:accountdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=update",
        "app.security.jwt.secret=integration-test-secret-with-at-least-32-bytes",
        "app.security.jwt.issuer=mascotas-al-dia-test"
})
@AutoConfigureMockMvc
class AccountDeletionIntegrationTest {

    @Autowired
    MockMvc mvc;

    private String register(String email) throws Exception {
        String body = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Eva Borrar\",\"email\":\"" + email + "\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Matcher matcher = Pattern.compile("\"accessToken\":\"([^\"]+)\"").matcher(body);
        if (!matcher.find()) {
            throw new AssertionError("La respuesta no contiene accessToken");
        }
        return matcher.group(1);
    }

    @Test
    void downloadIsAvailableInJsonAndCsv() throws Exception {
        String token = register("datos.integration@example.com");

        mvc.perform(get("/api/me/datos").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("mis-datos-mascotas-al-dia.json")))
                .andExpect(jsonPath("$.profile.email").value("datos.integration@example.com"));

        mvc.perform(get("/api/me/datos").param("formato", "csv").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("tipo,mascota,detalle,fecha,estado,rol,motivo")));

        mvc.perform(get("/api/me/datos").param("formato", "xml").header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deletedAccountCannotUseItsTokenNorLogInAgain() throws Exception {
        String token = register("borrar.integration@example.com");

        mvc.perform(get("/api/me/eliminacion/resumen").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activePets").value(0));

        mvc.perform(delete("/api/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // El token todavía no venció, pero la cuenta ya no existe.
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"borrar.integration@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isUnauthorized());

        // El correo queda libre: se puede volver a registrar.
        register("borrar.integration@example.com");
    }
}
