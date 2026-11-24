package com.aldemar.aquasample;

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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de integración: levantan la aplicación completa con H2 (perfil "test"),
 * con los usuarios y muestras de DatosIniciales, y hacen peticiones HTTP reales
 * (HTTP Basic incluido) con MockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SeguridadIntegrationTest {

    // Muestra de ejemplo en estado ENVIADA (ver DatosIniciales)
    private static final String ID_ENVIADA = "a0000000-0000-4000-8000-000000000005";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MuestraRepository muestraRepository;

    private static String muestraJson(String id, String estado) {
        return """
                {
                  "id": "%s",
                  "centro": "Calbuco Sur",
                  "tren": "Tren 1 - Módulo A",
                  "linea": "Línea 10",
                  "tramoCm": 100,
                  "fechaHora": "2026-11-20T10:00:00",
                  "operador": "Operario Demo",
                  "cantidad": 120,
                  "observaciones": "",
                  "estado": "%s"
                }
                """.formatted(id, estado);
    }

    @Test
    @DisplayName("GET sin credenciales → 401 con JSON")
    void sinCredenciales401() throws Exception {
        mockMvc.perform(get("/api/muestras"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.ruta").value("/api/muestras"));
    }

    @Test
    @DisplayName("ANALISTA haciendo PUT → 403")
    void analistaPut403() throws Exception {
        String id = "b0000000-0000-4000-8000-000000000001";
        mockMvc.perform(put("/api/muestras/" + id)
                        .with(httpBasic("an01", "test-analista"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(muestraJson(id, "ENVIADA")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("OPERARIO poniendo VALIDADA → 403 con mensaje claro")
    void operarioValidada403() throws Exception {
        String id = "b0000000-0000-4000-8000-000000000002";
        mockMvc.perform(put("/api/muestras/" + id)
                        .with(httpBasic("op01", "test-operario"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(muestraJson(id, "VALIDADA")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("BORRADOR o ENVIADA")));
    }

    @Test
    @DisplayName("OPERARIO haciendo PUT sobre una muestra ya VALIDADA → 409 y no se modifica")
    void operarioPutSobreValidada409() throws Exception {
        // a...0001 está VALIDADA en los datos iniciales, con cantidad 180
        String id = "a0000000-0000-4000-8000-000000000001";
        mockMvc.perform(put("/api/muestras/" + id)
                        .with(httpBasic("op01", "test-operario"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(muestraJson(id, "ENVIADA")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("La muestra ya fue validada por un supervisor"));

        // Sigue igual que antes
        mockMvc.perform(get("/api/muestras/" + id).with(httpBasic("op01", "test-operario")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("VALIDADA"))
                .andExpect(jsonPath("$.cantidad").value(180));
    }

    @Test
    @DisplayName("PUT sin fotoUrl conserva la fotoUrl que ya tenía la muestra")
    void putSinFotoUrlConservaLaFoto() throws Exception {
        // a...0006 es un BORRADOR de los datos iniciales; se simula que ya tiene foto en el servidor
        String id = "a0000000-0000-4000-8000-000000000006";
        Muestra muestra = muestraRepository.findById(id).orElseThrow();
        muestra.setFotoUrl("/api/muestras/" + id + "/foto");
        muestraRepository.save(muestra);

        // La app sincroniza los datos sin mandar fotoUrl (viene null en el JSON)
        mockMvc.perform(put("/api/muestras/" + id)
                        .with(httpBasic("op01", "test-operario"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(muestraJson(id, "ENVIADA")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ENVIADA"))
                .andExpect(jsonPath("$.fotoUrl").value("/api/muestras/" + id + "/foto"));
    }

    @Test
    @DisplayName("SUPERVISOR observando sin comentario → 400")
    void supervisorObservaSinComentario400() throws Exception {
        mockMvc.perform(patch("/api/muestras/" + ID_ENVIADA + "/estado")
                        .with(httpBasic("su01", "test-supervisor"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"estado": "OBSERVADA", "comentario": "  "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.comentario").exists());
    }

    @Test
    @DisplayName("SUPERVISOR validando → 200 y queda revisadoPor")
    void supervisorValida200() throws Exception {
        mockMvc.perform(patch("/api/muestras/" + ID_ENVIADA + "/estado")
                        .with(httpBasic("su01", "test-supervisor"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"estado": "VALIDADA"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("VALIDADA"))
                .andExpect(jsonPath("$.revisadoPor").value("Supervisor Demo"));
    }
}
