package com.aldemar.aquasample.controller;

import com.aldemar.aquasample.dto.UsuarioActualizarDto;
import com.aldemar.aquasample.dto.UsuarioCrearDto;
import com.aldemar.aquasample.dto.UsuarioDto;
import com.aldemar.aquasample.exception.ErrorRespuesta;
import com.aldemar.aquasample.model.Rol;
import com.aldemar.aquasample.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * Administración de usuarios. Todo el controller es solo para SUPERVISOR (ver SecurityConfig).
 * Las respuestas usan UsuarioDto: nunca incluyen la contraseña ni su hash.
 */
@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Crear, consultar, editar y desactivar usuarios (solo SUPERVISOR)")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Sin credenciales, credenciales incorrectas o usuario desactivado",
                content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
        @ApiResponse(responseCode = "403", description = "Tu rol no es SUPERVISOR",
                content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
})
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Ordenados por username. Los filtros son opcionales y se combinan.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de usuarios (puede venir vacía)",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UsuarioDto.class)))),
            @ApiResponse(responseCode = "400", description = "Algún filtro tiene un valor no válido",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public List<UsuarioDto> listar(
            @Parameter(description = "Solo este rol") @RequestParam(required = false) Rol rol,
            @Parameter(description = "true = solo activos; false = solo desactivados") @RequestParam(required = false) Boolean activo
    ) {
        return service.listar(rol, activo);
    }

    @GetMapping("/{username}")
    @Operation(summary = "Obtener un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "El usuario no existe",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public UsuarioDto obtener(@Parameter(description = "Username", example = "op01") @PathVariable String username) {
        return service.buscar(username);
    }

    @PostMapping
    @Operation(summary = "Crear un usuario",
            description = "Username de 3 a 20 letras o números; contraseña de mínimo 8 caracteres (se guarda con BCrypt). "
                    + "El usuario queda activo.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado (sin contraseña en la respuesta)"),
            @ApiResponse(responseCode = "400", description = "Algún campo no cumple las validaciones",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "409", description = "Ya existe un usuario con ese username",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public ResponseEntity<UsuarioDto> crear(@Valid @RequestBody UsuarioCrearDto dto) {
        UsuarioDto creado = service.crear(dto);
        return ResponseEntity.created(URI.create("/api/usuarios/" + creado.username())).body(creado);
    }

    @PutMapping("/{username}")
    @Operation(summary = "Actualizar un usuario",
            description = "Cambia nombre y rol; activo y password son opcionales (si viene password, se vuelve a "
                    + "hashear). Un supervisor no puede desactivarse ni quitarse el rol a sí mismo, y siempre debe "
                    + "quedar al menos un supervisor activo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado"),
            @ApiResponse(responseCode = "400", description = "Algún campo no cumple las validaciones",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "404", description = "El usuario no existe",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "409", description = "Se desactivaría o quitaría el rol a sí mismo, "
                    + "o no quedaría ningún supervisor activo",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public UsuarioDto actualizar(@Parameter(description = "Username", example = "op03") @PathVariable String username,
                                 @Valid @RequestBody UsuarioActualizarDto dto,
                                 Authentication authentication) {
        return service.actualizar(username, dto, authentication.getName());
    }

    @DeleteMapping("/{username}")
    @Operation(summary = "Desactivar un usuario",
            description = "No borra la fila: deja activo=false para conservar la trazabilidad de sus muestras. "
                    + "El usuario ya no puede iniciar sesión (401). Se puede reactivar con PUT y activo=true.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario desactivado"),
            @ApiResponse(responseCode = "404", description = "El usuario no existe",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "409", description = "Intentó desactivarse a sí mismo o no quedaría ningún "
                    + "supervisor activo",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public ResponseEntity<Void> desactivar(@Parameter(description = "Username", example = "op03") @PathVariable String username,
                                           Authentication authentication) {
        service.desactivar(username, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
