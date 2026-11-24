package com.aldemar.aquasample.dto;

import com.aldemar.aquasample.model.EstadoRevision;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Lo que viaja por la API (JSON). Es distinto de la entidad para no exponer la tabla
 * directamente y para poner aquí las reglas de validación de entrada.
 */
@Schema(description = "Muestra de choritos registrada en terreno")
public record MuestraDto(

        @Schema(description = "UUID generado por la app", example = "3f1c2a9e-7b4d-4c1e-9a2f-1b2c3d4e5f60")
        @NotBlank(message = "El id es obligatorio")
        @Size(max = 36, message = "El id debe tener como máximo 36 caracteres")
        String id,

        @Schema(example = "Calbuco Sur")
        @NotBlank(message = "El centro es obligatorio")
        @Size(max = 100, message = "El centro debe tener como máximo 100 caracteres")
        String centro,

        @Schema(description = "Opcional", example = "103B")
        @Size(max = 50, message = "La concesión debe tener como máximo 50 caracteres")
        String concesion,

        @Schema(example = "Tren 2 - Módulo B")
        @NotBlank(message = "El tren es obligatorio")
        @Size(max = 50, message = "El tren debe tener como máximo 50 caracteres")
        String tren,

        @Schema(example = "Línea 15")
        @NotBlank(message = "La línea es obligatoria")
        @Size(max = 50, message = "La línea debe tener como máximo 50 caracteres")
        String linea,

        @Schema(description = "Largo del tramo contado, en centímetros", example = "100")
        @NotNull(message = "El tramo es obligatorio")
        @Positive(message = "El tramo debe ser mayor a 0 cm")
        Integer tramoCm,

        @Schema(description = "Fecha y hora del muestreo", example = "2026-11-20T10:30:00")
        @NotNull(message = "La fecha y hora es obligatoria")
        LocalDateTime fechaHora,

        @Schema(example = "Operador Demo1")
        @NotBlank(message = "El operador es obligatorio")
        @Size(max = 100, message = "El operador debe tener como máximo 100 caracteres")
        String operador,

        @Schema(description = "Individuos contados", example = "150")
        @NotNull(message = "La cantidad es obligatoria")
        @PositiveOrZero(message = "La cantidad no puede ser negativa")
        Integer cantidad,

        @Schema(example = "Crecimiento homogéneo")
        @Size(max = 300, message = "Las observaciones deben tener como máximo 300 caracteres")
        String observaciones,

        @NotNull(message = "El estado es obligatorio")
        EstadoRevision estado,

        @Schema(description = "URL de la foto (opcional)")
        @Size(max = 500, message = "La URL de la foto debe tener como máximo 500 caracteres")
        String fotoUrl,

        @Schema(description = "Comentario del supervisor al observar (opcional)")
        @Size(max = 300, message = "El comentario debe tener como máximo 300 caracteres")
        String comentarioRevision,

        @Schema(description = "Supervisor que revisó (opcional)")
        @Size(max = 100, message = "El revisor debe tener como máximo 100 caracteres")
        String revisadoPor,

        @Schema(description = "Lo completa el servidor al guardar", accessMode = Schema.AccessMode.READ_ONLY)
        LocalDateTime actualizadoEn
) {
}
