package com.aldemar.aquasample.service;

import com.aldemar.aquasample.dto.MuestraDto;
import com.aldemar.aquasample.dto.MuestraMapper;
import com.aldemar.aquasample.exception.IdNoCoincideException;
import com.aldemar.aquasample.exception.MuestraDuplicadaException;
import com.aldemar.aquasample.exception.MuestraNoEncontradaException;
import com.aldemar.aquasample.model.EstadoRevision;
import com.aldemar.aquasample.model.Muestra;
import com.aldemar.aquasample.repository.MuestraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Lógica de negocio de las muestras. El Controller solo recibe y responde;
 * aquí se decide qué hacer (crear, actualizar, rechazar duplicados...).
 */
@Service
public class MuestraService {

    private final MuestraRepository repository;

    // Spring entrega el Repository por el constructor (inyección de dependencias).
    public MuestraService(MuestraRepository repository) {
        this.repository = repository;
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
    public MuestraDto crear(MuestraDto dto) {
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
    public ResultadoUpsert crearOActualizar(String id, MuestraDto dto) {
        if (!id.equals(dto.id())) {
            throw new IdNoCoincideException(id, dto.id());
        }
        Muestra existente = repository.findById(id).orElse(null);
        boolean creada = existente == null;

        Muestra muestra = creada ? new Muestra() : existente;
        muestra.setId(id);
        return new ResultadoUpsert(guardar(dto, muestra), creada);
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
