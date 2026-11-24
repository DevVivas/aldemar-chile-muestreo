package com.aldemar.aquasample.dto;

import com.aldemar.aquasample.model.Rol;
import com.aldemar.aquasample.model.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Datos públicos de un usuario. NUNCA incluye la contraseña ni su hash:
 * es la única forma en que un usuario sale de la API.
 */
@Schema(description = "Usuario (sin contraseña)")
public record UsuarioDto(
        @Schema(example = "op03") String username,
        @Schema(example = "Operario Tres") String nombre,
        Rol rol,
        @Schema(description = "false = desactivado (no puede iniciar sesión)", example = "true") boolean activo
) {
    public static UsuarioDto desde(Usuario u) {
        return new UsuarioDto(u.getUsername(), u.getNombre(), u.getRol(), u.isActivo());
    }
}
