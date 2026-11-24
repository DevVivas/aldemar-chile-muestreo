package com.aldemar.aquasample.config;

import com.aldemar.aquasample.model.EstadoRevision;
import com.aldemar.aquasample.model.Muestra;
import com.aldemar.aquasample.repository.MuestraRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Inserta 6 muestras ficticias al arrancar, SOLO si la tabla está vacía.
 * Sirven para probar la API y Swagger sin tener que crear datos a mano.
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

    private final MuestraRepository repository;

    public DatosIniciales(MuestraRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            log.info("Datos iniciales: la tabla ya tiene muestras, no se inserta nada.");
            return;
        }
        LocalDateTime ahora = LocalDateTime.now().withSecond(0).withNano(0);

        List<Muestra> muestras = List.of(
                muestra("a0000000-0000-4000-8000-000000000001", "Calbuco Sur", "103B", "Tren 1 - Módulo A", "Línea 10",
                        100, ahora.minusDays(27), "Operador Demo1", 180, "Primer muestreo del mes",
                        EstadoRevision.VALIDADA, null, "Supervisor Demo"),
                muestra("a0000000-0000-4000-8000-000000000002", "Calbuco Sur", "103B", "Tren 1 - Módulo A", "Línea 10",
                        100, ahora.minusDays(13), "Operador Demo1", 205, "Crecimiento homogéneo",
                        EstadoRevision.VALIDADA, null, "Supervisor Demo"),
                muestra("a0000000-0000-4000-8000-000000000003", "Calbuco Sur", "103B", "Tren 2 - Módulo B", "Línea 15",
                        80, ahora.minusDays(9), "Operador Demo2", 140, "Presencia de epibiontes",
                        EstadoRevision.OBSERVADA, "Revisar el conteo: parece bajo para el tramo.", "Supervisor Demo"),
                muestra("a0000000-0000-4000-8000-000000000004", "Centro Chiloé (Sector Huito)", null, "Tren 3 - Módulo B", "Línea 12",
                        120, ahora.minusDays(6), "Operador Demo2", 260, "",
                        EstadoRevision.ENVIADA, null, null),
                muestra("a0000000-0000-4000-8000-000000000005", "Centro Chiloé (Sector Huito)", null, "Tren 3 - Módulo B", "Línea 18",
                        90, ahora.minusDays(3), "Operador Demo1", 150, "Marea alta durante el muestreo",
                        EstadoRevision.ENVIADA, null, null),
                muestra("a0000000-0000-4000-8000-000000000006", "Centro Chiloé (Sector Huito)", null, "Tren 4 - Módulo C", "Línea 12",
                        100, ahora.minusDays(1), "Operador Demo1", 95, "Falta foto",
                        EstadoRevision.BORRADOR, null, null)
        );
        repository.saveAll(muestras);
        log.info("Datos iniciales: se insertaron {} muestras de ejemplo.", muestras.size());
    }

    private static Muestra muestra(String id, String centro, String concesion, String tren, String linea,
                                   int tramoCm, LocalDateTime fechaHora, String operador, int cantidad,
                                   String observaciones, EstadoRevision estado, String comentario, String revisadoPor) {
        Muestra m = new Muestra();
        m.setId(id);
        m.setCentro(centro);
        m.setConcesion(concesion);
        m.setTren(tren);
        m.setLinea(linea);
        m.setTramoCm(tramoCm);
        m.setFechaHora(fechaHora);
        m.setOperador(operador);
        m.setCantidad(cantidad);
        m.setObservaciones(observaciones);
        m.setEstado(estado);
        m.setComentarioRevision(comentario);
        m.setRevisadoPor(revisadoPor);
        m.setActualizadoEn(fechaHora);
        return m;
    }
}
