package com.aldemar.aquasample.controller;

import com.aldemar.aquasample.dto.UsuarioDto;
import com.aldemar.aquasample.exception.ErrorRespuesta;
import com.aldemar.aquasample.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint que usa la app para el login: confirma las credenciales y devuelve el rol. */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Datos del usuario que inició sesión")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/me")
    @Operation(
            summary = "Quién soy",
            description = "Devuelve username, nombre y rol del usuario autenticado. La app lo llama al iniciar "
                    + "sesión: si responde 200, las credenciales son correctas."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario autenticado"),
            @ApiResponse(responseCode = "401", description = "Sin credenciales o credenciales incorrectas",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public UsuarioDto me(Authentication authentication) {
        // "authentication" lo llena Spring Security con el usuario que pasó el login HTTP Basic.
        return usuarioService.obtenerDto(authentication.getName());
    }
}
