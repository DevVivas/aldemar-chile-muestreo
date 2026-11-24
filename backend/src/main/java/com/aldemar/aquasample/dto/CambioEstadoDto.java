package com.aldemar.aquasample.dto;

import com.aldemar.aquasample.model.EstadoRevision;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Cuerpo de PATCH /api/muestras/{id}/estado. */
@Schema(description = "Cambio de estado hecho por un supervisor")
public record CambioEstadoDto(

        @Schema(example = "OBSERVADA")
        @NotNull(message = "El estado es obligatorio")
        EstadoRevision estado,

        @Schema(description = "Obligatorio si el estado es OBSERVADA", example = "El tramo parece corto, vuelve a medir.")
        @Size(max = 300, message = "El comentario debe tener como máximo 300 caracteres")
        String comentario
) {
}
