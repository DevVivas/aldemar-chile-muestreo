package com.aldemar.aquasample.exception;

/** El archivo subido no es una imagen JPEG/PNG válida o supera el tamaño permitido. Se responde con 400. */
public class FotoInvalidaException extends RuntimeException {

    public FotoInvalidaException(String mensaje) {
        super(mensaje);
    }
}
