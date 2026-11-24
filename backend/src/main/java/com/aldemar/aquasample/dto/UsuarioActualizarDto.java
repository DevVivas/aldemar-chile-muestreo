package com.aldemar.aquasample.dto;

import com.aldemar.aquasample.model.Rol;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Cuerpo de PUT /api/usuarios/{username}. El username no se cambia (es la clave). */
@Schema(description = "Datos para actualizar un usuario")
public record UsuarioActualizarDto(

        @Schema(example = "Operario Tres")
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre debe tener como máximo 100 caracteres")
        String nombre,

        @Schema(example = "OPERARIO")
        @NotNull(message = "El rol es obligatorio")
        Rol rol,

        @Schema(description = "Opcional. Si no viene, se mantiene como está.", example = "true")
        Boolean activo,

        @Schema(description = "Opcional. Si viene, reemplaza la contraseña (mínimo 8 caracteres).",
                example = "OtraClave456", accessMode = Schema.AccessMode.WRITE_ONLY)
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
        String password
) {
    /** Evita que la contraseña aparezca en un log si alguien imprime el objeto. */
    @Override
    public String toString() {
        return "UsuarioActualizarDto[nombre=" + nombre + ", rol=" + rol + ", activo=" + activo
                + ", password=" + (password == null ? "sin cambio" : "****") + "]";
    }
}
