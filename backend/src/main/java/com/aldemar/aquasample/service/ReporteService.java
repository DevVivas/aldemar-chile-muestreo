package com.aldemar.aquasample.service;

import com.aldemar.aquasample.dto.ResumenDto;
import com.aldemar.aquasample.model.EstadoRevision;
import com.aldemar.aquasample.model.Muestra;
import com.aldemar.aquasample.repository.MuestraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Calcula el resumen recorriendo las muestras del período en Java.
 * Para el volumen de datos de un centro de cultivo es suficiente y más fácil de leer que un SQL con GROUP BY.
 */
@Service
public class ReporteService {

    private final MuestraRepository repository;

    public ReporteService(MuestraRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public ResumenDto resumen(LocalDate desde, LocalDate hasta) {
        LocalDateTime inicio = desde == null ? null : desde.atStartOfDay();
        LocalDateTime finExclusivo = hasta == null ? null : hasta.plusDays(1).atStartOfDay();
        List<Muestra> muestras = repository.buscar(null, null, null, null, inicio, finExclusivo);

        long totalIndividuos = 0;
        double sumaDensidades = 0;
        int conDensidad = 0;

        // Todos los estados parten en 0 para que siempre aparezcan en la respuesta.
        Map<EstadoRevision, Long> porEstado = new EnumMap<>(EstadoRevision.class);
        for (EstadoRevision e : EstadoRevision.values()) {
            porEstado.put(e, 0L);
        }
        // TreeMap: los centros quedan ordenados alfabéticamente. Valor = {muestras, individuos}.
        Map<String, long[]> porCentro = new TreeMap<>();

        for (Muestra m : muestras) {
            totalIndividuos += m.getCantidad();
            porEstado.merge(m.getEstado(), 1L, Long::sum);

            long[] totales = porCentro.computeIfAbsent(m.getCentro(), c -> new long[2]);
            totales[0]++;
            totales[1] += m.getCantidad();

            if (m.getTramoCm() != null && m.getTramoCm() > 0) {
                // Densidad = individuos por metro de cuerda
                sumaDensidades += m.getCantidad() * 100.0 / m.getTramoCm();
                conDensidad++;
            }
        }

        List<ResumenDto.ResumenCentroDto> centros = new ArrayList<>();
        porCentro.forEach((centro, t) -> centros.add(new ResumenDto.ResumenCentroDto(centro, t[0], t[1])));

        Double densidadPromedio = conDensidad == 0 ? null : redondear(sumaDensidades / conDensidad);

        return new ResumenDto(desde, hasta, muestras.size(), totalIndividuos, porEstado, centros, densidadPromedio);
    }

    private static double redondear(double valor) {
        return Math.round(valor * 10.0) / 10.0;
    }
}
