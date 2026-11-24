package com.aldemar.aquasample.repository;

import com.aldemar.aquasample.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a la tabla "usuarios". La clave es el username. */
public interface UsuarioRepository extends JpaRepository<Usuario, String> {
}
