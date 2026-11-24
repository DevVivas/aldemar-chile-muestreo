package com.aldemar.aquasample.dto;

import com.aldemar.aquasample.model.EstadoRevision;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Respuesta de GET /api/reportes/resumen. */
@Schema(description = "Resumen de muestras para el dashboard y los reportes")
public record ResumenDto(
        @Schema(description = "Inicio del período (null = sin límite)") LocalDate desde,
        @Schema(description = "Fin del período (null = sin límite)") LocalDate hasta,
        @Schema(example = "6") long totalMuestras,
        @Schema(description = "Suma de individuos contados", example = "1030") long totalIndividuos,
        @Schema(description = "Cantidad de muestras en cada estado (incluye los que tienen 0)")
        Map<EstadoRevision, Long> porEstado,
        @Schema(description = "Muestras e individuos por centro, ordenado por nombre")
        List<ResumenCentroDto> porCentro,
        @Schema(description = "Promedio de individuos por metro (cantidad / tramoCm * 100). null si no hay muestras",
                example = "172.5")
        Double densidadPromedio
) {

    @Schema(description = "Totales de un centro")
    public record ResumenCentroDto(
            @Schema(example = "Calbuco Sur") String centro,
            @Schema(example = "3") long muestras,
            @Schema(example = "525") long individuos
    ) {
    }
}
