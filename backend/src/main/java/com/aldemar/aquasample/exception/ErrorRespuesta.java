package com.aldemar.aquasample.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

/** Formato JSON de todos los errores de la API. */
@Schema(description = "Respuesta de error")
public record ErrorRespuesta(
        @Schema(example = "2026-11-24T10:15:30") LocalDateTime timestamp,
        @Schema(example = "400") int status,
        @Schema(example = "Bad Request") String error,
        @Schema(example = "Hay campos con errores") String mensaje,
        @Schema(example = "/api/muestras") String ruta,
        @Schema(description = "Errores por campo (solo en validaciones)", example = "{\"centro\": \"El centro es obligatorio\"}")
        Map<String, String> errores
) {
}
