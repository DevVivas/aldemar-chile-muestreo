package com.aldemar.aquasample.exception;

/**
 * El código corto (8 caracteres) coincide con más de una muestra: no se puede saber cuál es.
 * Se responde con 409 y se pide usar el UUID completo.
 */
public class CodigoAmbiguoException extends RuntimeException {

    public CodigoAmbiguoException(String codigo, int cantidad) {
        super("Hay " + cantidad + " muestras cuyo ID empieza con " + codigo
                + ". Usa el ID completo con GET /api/muestras/{id}.");
    }
}
