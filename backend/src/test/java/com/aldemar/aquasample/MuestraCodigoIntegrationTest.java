package com.aldemar.aquasample;

import com.aldemar.aquasample.model.EstadoRevision;
import com.aldemar.aquasample.model.Muestra;
import com.aldemar.aquasample.repository.MuestraRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ID corto (8 primeros caracteres del UUID): GET /api/muestras/codigo/{codigo} y todos los endpoints
 * con {id}, que aceptan el UUID completo o el ID corto.
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

    private static String muestraJson(String id, String estado) {
        return """
                {"id": "%s", "centro": "Calbuco Sur", "tren": "Tren 1 - Módulo A", "linea": "Línea 10",
                 "tramoCm": 100, "fechaHora": "2026-11-20T10:00:00", "operador": "Operario Demo",
                 "cantidad": 130, "observaciones": "", "estado": "%s"}
                """.formatted(id, estado);
    }

    @Test
    @DisplayName("GET /api/muestras/{id} con el ID corto → 200 con la muestra")
    void getConIdCorto200() throws Exception {
        crear("c0de0003-1111-4111-8111-111111111111");

        mockMvc.perform(get("/api/muestras/C0DE0003").with(httpBasic("an01", "test-analista")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("c0de0003-1111-4111-8111-111111111111"));
    }

    @Test
    @DisplayName("PATCH /{id}/estado con el ID corto → el supervisor valida la muestra")
    void patchConIdCorto200() throws Exception {
        crear("c0de0004-1111-4111-8111-111111111111");

        mockMvc.perform(patch("/api/muestras/c0de0004/estado")
                        .with(httpBasic("su01", "test-supervisor"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"estado": "VALIDADA"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("c0de0004-1111-4111-8111-111111111111"))
                .andExpect(jsonPath("$.estado").value("VALIDADA"));
    }

    @Test
    @DisplayName("PUT /{id} con el ID corto → actualiza la existente (200) o crea una nueva (201)")
    void putConIdCorto() throws Exception {
        String existente = "c0de0005-1111-4111-8111-111111111111";
        crear(existente);
        mockMvc.perform(put("/api/muestras/C0DE0005")
                        .with(httpBasic("op01", "test-operario"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(muestraJson(existente, "ENVIADA")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad").value(130));

        String nueva = "c0de0006-1111-4111-8111-111111111111";
        mockMvc.perform(put("/api/muestras/c0de0006")
                        .with(httpBasic("op01", "test-operario"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(muestraJson(nueva, "BORRADOR")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(nueva));
    }

    @Test
    @DisplayName("DELETE /{id} con el ID corto → 204 y la muestra ya no existe")
    void deleteConIdCorto204() throws Exception {
        crear("c0de0007-1111-4111-8111-111111111111");

        mockMvc.perform(delete("/api/muestras/C0DE0007").with(httpBasic("su01", "test-supervisor")))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/muestras/c0de0007-1111-4111-8111-111111111111").with(httpBasic("su01", "test-supervisor")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /{id}/foto con el ID corto de una muestra sin foto → 404 de foto (la muestra sí se encontró)")
    void fotoConIdCorto404() throws Exception {
        crear("c0de0008-1111-4111-8111-111111111111");

        mockMvc.perform(get("/api/muestras/c0de0008/foto").with(httpBasic("an01", "test-analista")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.ruta").value("/api/muestras/c0de0008/foto"));
    }

    @Test
    @DisplayName("DELETE con un ID corto ambiguo → 409 y no se borra ninguna")
    void deleteAmbiguo409() throws Exception {
        crear("c0de0009-1111-4111-8111-111111111111");
        crear("c0de0009-2222-4222-8222-222222222222");

        mockMvc.perform(delete("/api/muestras/c0de0009").with(httpBasic("su01", "test-supervisor")))
                .andExpect(status().isConflict());
        mockMvc.perform(get("/api/muestras/c0de0009-1111-4111-8111-111111111111").with(httpBasic("su01", "test-supervisor")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/muestras/c0de0009-2222-4222-8222-222222222222").with(httpBasic("su01", "test-supervisor")))
                .andExpect(status().isOk());
    }
}
