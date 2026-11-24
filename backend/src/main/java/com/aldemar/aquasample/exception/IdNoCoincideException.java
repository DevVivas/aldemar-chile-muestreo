package com.aldemar.aquasample.exception;

/** Se lanza en PUT cuando el id de la URL y el id del cuerpo son distintos. Se responde con 400. */
public class IdNoCoincideException extends RuntimeException {

    public IdNoCoincideException(String idUrl, String idCuerpo) {
        super("El id de la URL (" + idUrl + ") no coincide con el id del cuerpo (" + idCuerpo + ")");
    }
}
