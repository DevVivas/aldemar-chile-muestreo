package com.aldemar.aquasample.service;

import com.aldemar.aquasample.dto.CambioEstadoDto;
import com.aldemar.aquasample.dto.MuestraDto;
import com.aldemar.aquasample.dto.MuestraMapper;
import com.aldemar.aquasample.exception.AccionNoPermitidaException;
import com.aldemar.aquasample.exception.IdNoCoincideException;
import com.aldemar.aquasample.exception.MuestraDuplicadaException;
import com.aldemar.aquasample.exception.MuestraNoEncontradaException;
import com.aldemar.aquasample.exception.MuestraYaValidadaException;
import com.aldemar.aquasample.exception.ValidacionCampoException;
import com.aldemar.aquasample.model.EstadoRevision;
import com.aldemar.aquasample.model.Muestra;
import com.aldemar.aquasample.model.Rol;
import com.aldemar.aquasample.repository.MuestraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Lógica de negocio de las muestras. El Controller solo recibe y responde;
 * aquí se decide qué hacer (crear, actualizar, rechazar duplicados...).
 */
@Service
public class MuestraService {

    /** Estados que un OPERARIO puede dejar en una muestra al crearla o sincronizarla. */
    private static final Set<EstadoRevision> ESTADOS_OPERARIO = Set.of(EstadoRevision.BORRADOR, EstadoRevision.ENVIADA);

    private final MuestraRepository repository;
    private final UsuarioService usuarioService;

    // Spring entrega las dependencias por el constructor (inyección de dependencias).
    public MuestraService(MuestraRepository repository, UsuarioService usuarioService) {
        this.repository = repository;
        this.usuarioService = usuarioService;
    }

    /** Resultado de un upsert: la muestra guardada y si se creó (true) o se actualizó (false). */
    public record ResultadoUpsert(MuestraDto muestra, boolean creada) {
    }

    @Transactional(readOnly = true)
    public List<MuestraDto> listar(String centro, String tren, String linea, EstadoRevision estado,
                                   LocalDate desde, LocalDate hasta) {
        // "desde" y "hasta" son días completos: desde las 00:00 de "desde" hasta antes de las 00:00 del día siguiente a "hasta".
        LocalDateTime inicio = desde == null ? null : desde.atStartOfDay();
        LocalDateTime finExclusivo = hasta == null ? null : hasta.plusDays(1).atStartOfDay();

        return repository.buscar(vacioANull(centro), vacioANull(tren), vacioANull(linea), estado, inicio, finExclusivo)
                .stream()
                .map(MuestraMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public MuestraDto obtener(String id) {
        return repository.findById(id)
                .map(MuestraMapper::toDto)
                .orElseThrow(() -> new MuestraNoEncontradaException(id));
    }

    /** Crea una muestra nueva. Si el id ya existe, responde 409 (no la pisa). */
    @Transactional
    public MuestraDto crear(MuestraDto dto, String username) {
        verificarEstadoPermitido(usuarioService.obtener(username).getRol(), dto.estado());
        if (repository.existsById(dto.id())) {
            throw new MuestraDuplicadaException(dto.id());
        }
        Muestra nueva = new Muestra();
        nueva.setId(dto.id());
        return guardar(dto, nueva);
    }

    /**
     * Crea o actualiza (upsert) por id. Es lo que usa la sincronización:
     * enviar la misma muestra dos veces la actualiza, nunca la duplica.
     */
    @Transactional
    public ResultadoUpsert crearOActualizar(String id, MuestraDto dto, String username) {
        if (!id.equals(dto.id())) {
            throw new IdNoCoincideException(id, dto.id());
        }
        Rol rol = usuarioService.obtener(username).getRol();
        verificarEstadoPermitido(rol, dto.estado());
        Muestra existente = repository.findById(id).orElse(null);

        // Una muestra que el supervisor ya validó queda cerrada para el operario:
        // si su teléfono sincroniza una versión vieja, no debe pisar la validación.
        if (existente != null && existente.getEstado() == EstadoRevision.VALIDADA && rol == Rol.OPERARIO) {
            throw new MuestraYaValidadaException();
        }
        boolean creada = existente == null;

        Muestra muestra = creada ? new Muestra() : existente;
        muestra.setId(id);
        return new ResultadoUpsert(guardar(dto, muestra), creada);
    }

    /**
     * Cambio de estado hecho por un supervisor (la URL ya está restringida a SUPERVISOR en SecurityConfig).
     * OBSERVADA exige comentario. Guarda quién revisó y cuándo.
     */
    @Transactional
    public MuestraDto cambiarEstado(String id, CambioEstadoDto cambio, String username) {
        Muestra muestra = repository.findById(id).orElseThrow(() -> new MuestraNoEncontradaException(id));

        String comentario = cambio.comentario() == null ? "" : cambio.comentario().trim();
        if (cambio.estado() == EstadoRevision.OBSERVADA && comentario.isEmpty()) {
            throw new ValidacionCampoException("comentario",
                    "Para observar una muestra escribe qué debe corregir el operador");
        }

        muestra.setEstado(cambio.estado());
        // Al observar se guarda el comentario; en cualquier otro estado se limpia la observación anterior.
        muestra.setComentarioRevision(cambio.estado() == EstadoRevision.OBSERVADA ? comentario : null);
        muestra.setRevisadoPor(usuarioService.obtener(username).getNombre());
        muestra.setActualizadoEn(LocalDateTime.now());
        return MuestraMapper.toDto(repository.save(muestra));
    }

    /**
     * Regla de negocio: un OPERARIO solo puede dejar la muestra en BORRADOR o ENVIADA.
     * Observar o validar es trabajo del SUPERVISOR. Si no se cumple → 403 con mensaje claro.
     */
    static void verificarEstadoPermitido(Rol rol, EstadoRevision estado) {
        if (rol == Rol.OPERARIO && !ESTADOS_OPERARIO.contains(estado)) {
            throw new AccionNoPermitidaException(
                    "Un operario solo puede guardar muestras en BORRADOR o ENVIADA. "
                            + "Solo un supervisor puede dejarlas en " + estado + ".");
        }
    }

    @Transactional
    public void eliminar(String id) {
        if (!repository.existsById(id)) {
            throw new MuestraNoEncontradaException(id);
        }
        repository.deleteById(id);
    }

    private MuestraDto guardar(MuestraDto dto, Muestra muestra) {
        MuestraMapper.copiarEnEntidad(dto, muestra);
        muestra.setActualizadoEn(LocalDateTime.now());
        return MuestraMapper.toDto(repository.save(muestra));
    }

    private static String vacioANull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor;
    }
}
