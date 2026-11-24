package com.aldemar.aquasample.security;

import com.aldemar.aquasample.exception.ErrorRespuesta;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * Los errores 401 y 403 de Spring Security ocurren ANTES de llegar al Controller,
 * así que GlobalExceptionHandler no los ve. Aquí se responden con el mismo formato JSON.
 */
@Component
public class RespuestasSeguridadJson {

    private final JsonMapper jsonMapper;

    public RespuestasSeguridadJson(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    /**
     * 401: no mandó credenciales o son incorrectas.
     * No se envía la cabecera WWW-Authenticate para que el navegador no abra su ventana de login
     * (Swagger tiene su propio botón "Authorize").
     */
    public AuthenticationEntryPoint noAutenticado() {
        return (request, response, ex) -> escribir(request, response, HttpStatus.UNAUTHORIZED,
                "Debes iniciar sesión: envía usuario y contraseña (HTTP Basic)");
    }

    /** 403: está autenticado, pero su rol no tiene permiso para este endpoint. */
    public AccessDeniedHandler sinPermiso() {
        return (request, response, ex) -> escribir(request, response, HttpStatus.FORBIDDEN,
                "Tu rol no tiene permiso para esta acción");
    }

    private void escribir(HttpServletRequest request, HttpServletResponse response, HttpStatus status, String mensaje)
            throws IOException {
        ErrorRespuesta cuerpo = new ErrorRespuesta(LocalDateTime.now(), status.value(), status.getReasonPhrase(),
                mensaje, request.getRequestURI(), null);
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(jsonMapper.writeValueAsString(cuerpo));
    }
}
