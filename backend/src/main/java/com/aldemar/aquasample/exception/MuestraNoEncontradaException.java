package com.aldemar.aquasample.exception;

/** Se lanza cuando se pide una muestra que no existe. Se responde con 404. */
public class MuestraNoEncontradaException extends RuntimeException {

    public MuestraNoEncontradaException(String id) {
        super("No existe una muestra con id " + id);
    }

    private MuestraNoEncontradaException(String mensaje, boolean mensajeCompleto) {
        super(mensaje);
    }

    /** Para la búsqueda por ID corto (8 caracteres). */
    public static MuestraNoEncontradaException porCodigo(String codigo) {
        return new MuestraNoEncontradaException("No existe una muestra cuyo ID empiece con " + codigo, true);
    }
}
