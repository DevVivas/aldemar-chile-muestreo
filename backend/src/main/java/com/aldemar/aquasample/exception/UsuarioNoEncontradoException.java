package com.aldemar.aquasample.exception;

/** El usuario pedido no existe. Se responde con 404. */
public class UsuarioNoEncontradoException extends RuntimeException {

    public UsuarioNoEncontradoException(String username) {
        super("No existe el usuario " + username);
    }
}
