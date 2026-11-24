package com.aldemar.aquasample.exception;

/**
 * Error de validación que depende de otros datos (no se puede expresar con una anotación).
 * Ejemplo: el comentario es obligatorio SOLO si el estado es OBSERVADA. Se responde con 400.
 */
public class ValidacionCampoException extends RuntimeException {

    private final String campo;

    public ValidacionCampoException(String campo, String mensaje) {
        super(mensaje);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
