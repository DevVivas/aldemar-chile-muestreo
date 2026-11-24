package com.aldemar.aquasample.config;

import com.aldemar.aquasample.model.EstadoRevision;
import com.aldemar.aquasample.model.Muestra;
import com.aldemar.aquasample.model.Rol;
import com.aldemar.aquasample.model.Usuario;
import com.aldemar.aquasample.repository.MuestraRepository;
import com.aldemar.aquasample.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Al arrancar:
 * 1. Si la columna muestras.estado quedó como ENUM de MySQL (versión anterior), la pasa a varchar(20).
 * 2. Si no hay usuarios, crea op01, su01 y an01 con contraseñas de variables de entorno.
 * 3. Si no hay muestras, inserta 6 muestras ficticias.
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

    private final MuestraRepository muestraRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbc;

    // Contraseñas de los usuarios demo: vienen del archivo .env (nunca escritas en el código).
    private final String passwordOperario;
    private final String passwordSupervisor;
    private final String passwordAnalista;

    public DatosIniciales(MuestraRepository muestraRepository, UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder, JdbcTemplate jdbc,
                          @Value("${DEMO_PASSWORD_OPERARIO:}") String passwordOperario,
                          @Value("${DEMO_PASSWORD_SUPERVISOR:}") String passwordSupervisor,
                          @Value("${DEMO_PASSWORD_ANALISTA:}") String passwordAnalista) {
        this.muestraRepository = muestraRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbc = jdbc;
        this.passwordOperario = passwordOperario;
        this.passwordSupervisor = passwordSupervisor;
        this.passwordAnalista = passwordAnalista;
    }

    @Override
    public void run(String... args) {
        convertirEstadoAVarchar();
        crearUsuariosDemo();
        crearMuestrasDemo();
    }

    /**
     * Hibernate con ddl-auto=update NO cambia el tipo de una columna que ya existe.
     * La primera versión creó "estado" como ENUM de MySQL; esto la deja como varchar(20).
     * En una base nueva (o en H2 para los tests) no encuentra nada y no hace nada.
     */
    private void convertirEstadoAVarchar() {
        try {
            List<String> tipos = jdbc.queryForList(
                    "SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS "
                            + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'muestras' AND COLUMN_NAME = 'estado'",
                    String.class);
            if (!tipos.isEmpty() && "enum".equalsIgnoreCase(tipos.get(0))) {
                jdbc.execute("ALTER TABLE muestras MODIFY estado VARCHAR(20) NOT NULL");
                log.info("Migración: muestras.estado pasó de ENUM a VARCHAR(20).");
            }
        } catch (Exception e) {
            log.debug("No se revisó el tipo de muestras.estado: {}", e.getMessage());
        }
    }

    private void crearUsuariosDemo() {
        if (usuarioRepository.count() > 0) {
            return;
        }
        crearUsuario("op01", "Operario Demo", passwordOperario, Rol.OPERARIO, "DEMO_PASSWORD_OPERARIO");
        crearUsuario("su01", "Supervisor Demo", passwordSupervisor, Rol.SUPERVISOR, "DEMO_PASSWORD_SUPERVISOR");
        crearUsuario("an01", "Analista Demo", passwordAnalista, Rol.ANALISTA, "DEMO_PASSWORD_ANALISTA");
    }

    private void crearUsuario(String username, String nombre, String password, Rol rol, String variable) {
        if (password == null || password.isBlank()) {
            log.warn("No se creó el usuario {}: falta la variable de entorno {}.", username, variable);
            return;
        }
        // Se guarda solo el hash BCrypt, nunca la contraseña.
        usuarioRepository.save(new Usuario(username, nombre, passwordEncoder.encode(password), rol));
        log.info("Usuario demo creado: {} ({})", username, rol);
    }

    private void crearMuestrasDemo() {
        if (muestraRepository.count() > 0) {
            log.info("Datos iniciales: la tabla ya tiene muestras, no se inserta nada.");
            return;
        }
        LocalDateTime ahora = LocalDateTime.now().withSecond(0).withNano(0);

        List<Muestra> muestras = List.of(
                muestra("Calbuco Sur", "103B", "Tren 1 - Módulo A", "Línea 10",
                        100, ahora.minusDays(27), "Operario Demo", 180, "Primer muestreo del mes",
                        EstadoRevision.VALIDADA, null, "Supervisor Demo"),
                muestra("Calbuco Sur", "103B", "Tren 1 - Módulo A", "Línea 10",
                        100, ahora.minusDays(13), "Operario Demo", 205, "Crecimiento homogéneo",
                        EstadoRevision.VALIDADA, null, "Supervisor Demo"),
                muestra("Calbuco Sur", "103B", "Tren 2 - Módulo B", "Línea 15",
                        80, ahora.minusDays(9), "Operario Demo", 140, "Presencia de epibiontes",
                        EstadoRevision.OBSERVADA, "Revisar el conteo: parece bajo para el tramo.", "Supervisor Demo"),
                muestra("Centro Chiloé (Sector Huito)", null, "Tren 3 - Módulo B", "Línea 12",
                        120, ahora.minusDays(6), "Operario Demo", 260, "",
                        EstadoRevision.ENVIADA, null, null),
                muestra("Centro Chiloé (Sector Huito)", null, "Tren 3 - Módulo B", "Línea 18",
                        90, ahora.minusDays(3), "Operario Demo", 150, "Marea alta durante el muestreo",
                        EstadoRevision.ENVIADA, null, null),
                muestra("Centro Chiloé (Sector Huito)", null, "Tren 4 - Módulo C", "Línea 12",
                        100, ahora.minusDays(1), "Operario Demo", 95, "Falta foto",
                        EstadoRevision.BORRADOR, null, null)
        );
        muestraRepository.saveAll(muestras);
        log.info("Datos iniciales: se insertaron {} muestras de ejemplo.", muestras.size());
    }

    private static Muestra muestra(String centro, String concesion, String tren, String linea,
                                   int tramoCm, LocalDateTime fechaHora, String operador, int cantidad,
                                   String observaciones, EstadoRevision estado, String comentario, String revisadoPor) {
        Muestra m = new Muestra();
        // UUID aleatorio, igual que los que genera la app (no IDs fijos tipo a0000000-...).
        m.setId(UUID.randomUUID().toString());
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
