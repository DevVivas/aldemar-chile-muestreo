package com.aldemar.aquasample.repository;

import com.aldemar.aquasample.model.Rol;
import com.aldemar.aquasample.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a la tabla "usuarios". La clave es el username. */
public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    /** Cuántos usuarios activos tienen este rol (para no quedar sin supervisores). */
    long countByRolAndActivoTrue(Rol rol);
}
