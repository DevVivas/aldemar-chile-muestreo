package com.aldemar.aquasample;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de integración del CRUD de usuarios (/api/usuarios), con la app completa y H2.
 * Cada test usa un username distinto, porque la base se comparte entre los tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UsuarioIntegrationTest {

    private static final String SUPERVISOR = "su01";
    private static final String CLAVE_SUPERVISOR = "test-supervisor";

    @Autowired
    private MockMvc mockMvc;

    private static String usuarioJson(String username, String password, String rol) {
        return """
                {"username": "%s", "nombre": "Usuario de prueba", "password": "%s", "rol": "%s"}
                """.formatted(username, password, rol);
    }

    private void crear(String username, String password, String rol) throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .with(httpBasic(SUPERVISOR, CLAVE_SUPERVISOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(usuarioJson(username, password, rol)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("OPERARIO listando o creando usuarios → 403")
    void operario403() throws Exception {
        mockMvc.perform(get("/api/usuarios").with(httpBasic("op01", "test-operario")))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/usuarios")
                        .with(httpBasic("op01", "test-operario"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(usuarioJson("intruso1", "ClaveSegura123", "SUPERVISOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SUPERVISOR crea un usuario → 201, y el nuevo usuario puede autenticarse en /api/auth/me")
    void crearYLogin() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .with(httpBasic(SUPERVISOR, CLAVE_SUPERVISOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(usuarioJson("opnuevo", "ClaveSegura123", "OPERARIO")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/usuarios/opnuevo"))
                .andExpect(jsonPath("$.username").value("opnuevo"))
                .andExpect(jsonPath("$.rol").value("OPERARIO"))
                .andExpect(jsonPath("$.activo").value(true));

        mockMvc.perform(get("/api/auth/me").with(httpBasic("opnuevo", "ClaveSegura123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("opnuevo"))
                .andExpect(jsonPath("$.rol").value("OPERARIO"));
    }

    @Test
    @DisplayName("Username duplicado → 409")
    void duplicado409() throws Exception {
        crear("opduplicado", "ClaveSegura123", "OPERARIO");

        mockMvc.perform(post("/api/usuarios")
                        .with(httpBasic(SUPERVISOR, CLAVE_SUPERVISOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(usuarioJson("opduplicado", "OtraClave456", "ANALISTA")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Ya existe el usuario opduplicado"));
    }

    @Test
    @DisplayName("Datos inválidos (username corto, contraseña corta, sin rol) → 400 con error por campo")
    void validaciones400() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .with(httpBasic(SUPERVISOR, CLAVE_SUPERVISOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "a!", "nombre": " ", "password": "corta"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.username").exists())
                .andExpect(jsonPath("$.errores.nombre").exists())
                .andExpect(jsonPath("$.errores.password").exists())
                .andExpect(jsonPath("$.errores.rol").exists());
    }

    @Test
    @DisplayName("DELETE desactiva: el usuario ya no puede autenticarse (401) pero sigue existiendo")
    void desactivar401() throws Exception {
        crear("opbaja", "ClaveSegura123", "OPERARIO");
        mockMvc.perform(get("/api/auth/me").with(httpBasic("opbaja", "ClaveSegura123")))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/usuarios/opbaja").with(httpBasic(SUPERVISOR, CLAVE_SUPERVISOR)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/auth/me").with(httpBasic("opbaja", "ClaveSegura123")))
                .andExpect(status().isUnauthorized());
        // La fila se conserva (trazabilidad), solo queda inactiva
        mockMvc.perform(get("/api/usuarios/opbaja").with(httpBasic(SUPERVISOR, CLAVE_SUPERVISOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
    }

    @Test
    @DisplayName("Supervisor que intenta desactivarse o quitarse el rol a sí mismo → 409")
    void supervisorASiMismo409() throws Exception {
        mockMvc.perform(delete("/api/usuarios/" + SUPERVISOR).with(httpBasic(SUPERVISOR, CLAVE_SUPERVISOR)))
                .andExpect(status().isConflict());

        mockMvc.perform(put("/api/usuarios/" + SUPERVISOR)
                        .with(httpBasic(SUPERVISOR, CLAVE_SUPERVISOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "Supervisor Demo", "rol": "ANALISTA"}
                                """))
                .andExpect(status().isConflict());

        // Sigue siendo supervisor activo
        mockMvc.perform(get("/api/auth/me").with(httpBasic(SUPERVISOR, CLAVE_SUPERVISOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("SUPERVISOR"));
    }

    @Test
    @DisplayName("PUT con password nueva: la anterior deja de servir y la nueva funciona")
    void cambiarPassword() throws Exception {
        crear("opclave", "ClaveSegura123", "OPERARIO");

        mockMvc.perform(put("/api/usuarios/opclave")
                        .with(httpBasic(SUPERVISOR, CLAVE_SUPERVISOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "Operario Clave", "rol": "ANALISTA", "password": "NuevaClave789"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Operario Clave"))
                .andExpect(jsonPath("$.rol").value("ANALISTA"));

        mockMvc.perform(get("/api/auth/me").with(httpBasic("opclave", "ClaveSegura123")))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/auth/me").with(httpBasic("opclave", "NuevaClave789")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Usuario que no existe → 404")
    void noExiste404() throws Exception {
        mockMvc.perform(get("/api/usuarios/nadie123").with(httpBasic(SUPERVISOR, CLAVE_SUPERVISOR)))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/usuarios/nadie123").with(httpBasic(SUPERVISOR, CLAVE_SUPERVISOR)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Las respuestas nunca incluyen la contraseña ni el hash")
    void sinPasswordEnRespuestas() throws Exception {
        String creado = mockMvc.perform(post("/api/usuarios")
                        .with(httpBasic(SUPERVISOR, CLAVE_SUPERVISOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(usuarioJson("opsecreto", "ClaveSecreta123", "OPERARIO")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String lista = mockMvc.perform(get("/api/usuarios").with(httpBasic(SUPERVISOR, CLAVE_SUPERVISOR)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String me = mockMvc.perform(get("/api/auth/me").with(httpBasic("opsecreto", "ClaveSecreta123")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        for (String cuerpo : new String[]{creado, lista, me}) {
            assertThat(cuerpo)
                    .doesNotContain("ClaveSecreta123")
                    .doesNotContainIgnoringCase("password")
                    .doesNotContain("$2a$") // prefijo de un hash BCrypt
                    .doesNotContain("$2b$");
        }
    }

    @Test
    @DisplayName("Filtros: rol y activo")
    void filtros() throws Exception {
        mockMvc.perform(get("/api/usuarios").param("rol", "SUPERVISOR").param("activo", "true")
                        .with(httpBasic(SUPERVISOR, CLAVE_SUPERVISOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].rol").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("SUPERVISOR"))))
                .andExpect(jsonPath("$[?(@.username == 'su01')]").exists());

        mockMvc.perform(get("/api/usuarios").param("rol", "JEFE").with(httpBasic(SUPERVISOR, CLAVE_SUPERVISOR)))
                .andExpect(status().isBadRequest());
    }
}
