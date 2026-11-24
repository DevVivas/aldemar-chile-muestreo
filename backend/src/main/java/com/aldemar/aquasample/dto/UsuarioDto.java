package com.aldemar.aquasample.dto;

import com.aldemar.aquasample.model.Rol;
import io.swagger.v3.oas.annotations.media.Schema;

/** Datos públicos del usuario autenticado (nunca incluye la contraseña). */
@Schema(description = "Usuario autenticado")
public record UsuarioDto(
        @Schema(example = "su01") String username,
        @Schema(example = "Supervisor Demo") String nombre,
        Rol rol
) {
}
