package com.aldemar.aquasample.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Convierte las excepciones en respuestas JSON claras (siempre con el formato {@link ErrorRespuesta}).
 * Así el Controller no tiene try/catch.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 400: falló @Valid en el cuerpo. Devuelve un error por cada campo. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> validacion(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errores.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }
        return respuesta(HttpStatus.BAD_REQUEST, "Hay campos con errores", req, errores);
    }

    /** 400: JSON mal formado o un valor con tipo incorrecto (por ejemplo, un estado que no existe). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuesta> jsonInvalido(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST,
                "El cuerpo no es un JSON válido o tiene un valor con formato incorrecto", req, null);
    }

    /** 400: parámetro de la URL con formato incorrecto (por ejemplo ?estado=XYZ o ?desde=ayer). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorRespuesta> parametroInvalido(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST, "Parámetro con formato incorrecto", req,
                Map.of(ex.getName(), "Valor no válido: " + ex.getValue()));
    }

    @ExceptionHandler(IdNoCoincideException.class)
    public ResponseEntity<ErrorRespuesta> idNoCoincide(IdNoCoincideException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST, ex.getMessage(), req, Map.of("id", ex.getMessage()));
    }

    /** 404: la muestra no existe. */
    @ExceptionHandler(MuestraNoEncontradaException.class)
    public ResponseEntity<ErrorRespuesta> noEncontrada(MuestraNoEncontradaException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage(), req, null);
    }

    /** 400: validación que depende de otros datos (p. ej. comentario obligatorio al observar). */
    @ExceptionHandler(ValidacionCampoException.class)
    public ResponseEntity<ErrorRespuesta> validacionCampo(ValidacionCampoException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST, "Hay campos con errores", req, Map.of(ex.getCampo(), ex.getMessage()));
    }

    /** 400: la foto no es JPEG/PNG, viene vacía o pesa demasiado. */
    @ExceptionHandler(FotoInvalidaException.class)
    public ResponseEntity<ErrorRespuesta> fotoInvalida(FotoInvalidaException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST, ex.getMessage(), req, Map.of("archivo", ex.getMessage()));
    }

    /** 400: el formulario multipart no trae el campo "archivo". */
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorRespuesta> faltaArchivo(MissingServletRequestPartException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST, "Falta el archivo", req,
                Map.of(ex.getRequestPartName(), "Adjunta una imagen JPEG o PNG"));
    }

    /** 413: la subida supera el límite de spring.servlet.multipart.max-file-size. */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorRespuesta> archivoGrande(MaxUploadSizeExceededException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.CONTENT_TOO_LARGE, "La foto supera el máximo de 5 MB", req,
                Map.of("archivo", "La foto supera el máximo de 5 MB"));
    }

    /** 403: regla de negocio (por ejemplo, un operario intentando validar). */
    @ExceptionHandler(AccionNoPermitidaException.class)
    public ResponseEntity<ErrorRespuesta> accionNoPermitida(AccionNoPermitidaException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.FORBIDDEN, ex.getMessage(), req, null);
    }

    /** 404: la muestra existe pero no tiene foto. */
    @ExceptionHandler(FotoNoEncontradaException.class)
    public ResponseEntity<ErrorRespuesta> fotoNoEncontrada(FotoNoEncontradaException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage(), req, null);
    }

    /** 409: un operario intentó modificar una muestra ya validada. */
    @ExceptionHandler(MuestraYaValidadaException.class)
    public ResponseEntity<ErrorRespuesta> yaValidada(MuestraYaValidadaException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.CONFLICT, ex.getMessage(), req, null);
    }

    /** 409: se intentó crear (POST) un id que ya existe. */
    @ExceptionHandler(MuestraDuplicadaException.class)
    public ResponseEntity<ErrorRespuesta> duplicada(MuestraDuplicadaException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.CONFLICT, ex.getMessage(), req, null);
    }

    /** 404: el usuario no existe. */
    @ExceptionHandler(UsuarioNoEncontradoException.class)
    public ResponseEntity<ErrorRespuesta> usuarioNoEncontrado(UsuarioNoEncontradoException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage(), req, null);
    }

    /** 409: username repetido o regla de supervisores (desactivarse a sí mismo, quedar sin supervisores). */
    @ExceptionHandler(ConflictoUsuarioException.class)
    public ResponseEntity<ErrorRespuesta> conflictoUsuario(ConflictoUsuarioException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.CONFLICT, ex.getMessage(), req, null);
    }

    private ResponseEntity<ErrorRespuesta> respuesta(HttpStatus status, String mensaje, HttpServletRequest req,
                                                     Map<String, String> errores) {
        ErrorRespuesta cuerpo = new ErrorRespuesta(
                LocalDateTime.now(), status.value(), status.getReasonPhrase(), mensaje, req.getRequestURI(), errores);
        return ResponseEntity.status(status).body(cuerpo);
    }
}
