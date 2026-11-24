package com.aldemar.aquasample.dto;

import com.aldemar.aquasample.model.Rol;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Cuerpo de POST /api/usuarios. */
@Schema(description = "Datos para crear un usuario")
public record UsuarioCrearDto(

        @Schema(description = "De 3 a 20 letras o números, sin espacios", example = "op03")
        @NotBlank(message = "El usuario es obligatorio")
        @Pattern(regexp = "^[A-Za-z0-9]{3,20}$", message = "El usuario debe tener de 3 a 20 letras o números, sin espacios")
        String username,

        @Schema(example = "Operario Tres")
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre debe tener como máximo 100 caracteres")
        String nombre,

        @Schema(description = "Mínimo 8 caracteres. Se guarda solo su hash BCrypt.", example = "ClaveSegura123",
                accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
        String password,

        @Schema(example = "OPERARIO")
        @NotNull(message = "El rol es obligatorio")
        Rol rol
) {
    /** Evita que la contraseña aparezca en un log si alguien imprime el objeto. */
    @Override
    public String toString() {
        return "UsuarioCrearDto[username=" + username + ", nombre=" + nombre + ", rol=" + rol + "]";
    }
}
