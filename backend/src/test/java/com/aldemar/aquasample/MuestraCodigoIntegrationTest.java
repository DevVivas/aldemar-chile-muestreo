package com.aldemar.aquasample;

import com.aldemar.aquasample.model.EstadoRevision;
import com.aldemar.aquasample.model.Muestra;
import com.aldemar.aquasample.repository.MuestraRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * GET /api/muestras/codigo/{codigo}: buscar una muestra por su ID corto (8 primeros caracteres del UUID).
 * Cada test usa IDs propios (prefijos c0de...) para no chocar con los datos de los otros tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MuestraCodigoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MuestraRepository muestraRepository;

    private void crear(String id) {
        Muestra m = new Muestra();
        m.setId(id);
        m.setCentro("Calbuco Sur");
        m.setTren("Tren 1 - Módulo A");
        m.setLinea("Línea 10");
        m.setTramoCm(100);
        m.setFechaHora(LocalDateTime.of(2026, 11, 20, 10, 0));
        m.setOperador("Operario Demo");
        m.setCantidad(120);
        m.setObservaciones("");
        m.setEstado(EstadoRevision.ENVIADA);
        m.setActualizadoEn(LocalDateTime.of(2026, 11, 20, 10, 0));
        muestraRepository.save(m);
    }

    @Test
    @DisplayName("Código corto en mayúsculas (como lo muestra la app) → 200 con la muestra completa")
    void encontrada200() throws Exception {
        crear("c0de0001-1111-4111-8111-111111111111");

        mockMvc.perform(get("/api/muestras/codigo/C0DE0001").with(httpBasic("an01", "test-analista")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("c0de0001-1111-4111-8111-111111111111"))
                .andExpect(jsonPath("$.centro").value("Calbuco Sur"));
    }

    @Test
    @DisplayName("Código que no existe → 404")
    void noExiste404() throws Exception {
        mockMvc.perform(get("/api/muestras/codigo/c0deffff").with(httpBasic("op01", "test-operario")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe una muestra cuyo ID empiece con C0DEFFFF"));
    }

    @Test
    @DisplayName("Dos muestras con el mismo código corto → 409 pidiendo el UUID completo")
    void ambiguo409() throws Exception {
        crear("c0de0002-1111-4111-8111-111111111111");
        crear("c0de0002-2222-4222-8222-222222222222");

        mockMvc.perform(get("/api/muestras/codigo/c0de0002").with(httpBasic("su01", "test-supervisor")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("Hay 2 muestras")));
    }

    @Test
    @DisplayName("Código con formato incorrecto (largo o caracteres) → 400 con error en el campo codigo")
    void formatoInvalido400() throws Exception {
        for (String malo : new String[]{"A9DD", "A9DDA450X", "ZZZZZZZZ"}) {
            mockMvc.perform(get("/api/muestras/codigo/" + malo).with(httpBasic("op01", "test-operario")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errores.codigo").exists());
        }
    }

    @Test
    @DisplayName("Sin credenciales → 401")
    void sinCredenciales401() throws Exception {
        mockMvc.perform(get("/api/muestras/codigo/C0DE0001"))
                .andExpect(status().isUnauthorized());
    }
}
