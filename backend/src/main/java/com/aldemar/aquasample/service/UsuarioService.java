package com.aldemar.aquasample.service;

import com.aldemar.aquasample.dto.UsuarioDto;
import com.aldemar.aquasample.model.Usuario;
import com.aldemar.aquasample.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consulta de usuarios (para /api/auth/me y para saber el rol y nombre en las reglas de negocio). */
@Service
public class UsuarioService {

    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Usuario obtener(String username) {
        return repository.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }

    @Transactional(readOnly = true)
    public UsuarioDto obtenerDto(String username) {
        Usuario u = obtener(username);
        return new UsuarioDto(u.getUsername(), u.getNombre(), u.getRol());
    }
}
