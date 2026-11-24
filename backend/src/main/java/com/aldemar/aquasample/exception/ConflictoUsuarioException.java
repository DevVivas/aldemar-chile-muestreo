package com.aldemar.aquasample.exception;

/**
 * La petición es válida, pero choca con el estado actual de los usuarios. Se responde con 409.
 * Ejemplos: el username ya existe, un supervisor intenta desactivarse a sí mismo
 * o la acción dejaría el sistema sin supervisores activos.
 */
public class ConflictoUsuarioException extends RuntimeException {

    public ConflictoUsuarioException(String mensaje) {
        super(mensaje);
    }
}
