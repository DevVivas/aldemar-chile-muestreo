package com.aldemar.aquasample.exception;

/** Se lanza al crear (POST) una muestra con un id que ya existe. Se responde con 409. */
public class MuestraDuplicadaException extends RuntimeException {

    public MuestraDuplicadaException(String id) {
        super("Ya existe una muestra con id " + id + ". Usa PUT /api/muestras/" + id + " para actualizarla.");
    }
}
