package com.aldemar.aquasample.security;

import com.aldemar.aquasample.model.Usuario;
import com.aldemar.aquasample.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Le dice a Spring Security cómo buscar un usuario: lo lee de la tabla "usuarios".
 * Spring compara después la contraseña recibida con el hash BCrypt guardado.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository repository;

    public UsuarioDetailsService(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = repository.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // roles("SUPERVISOR") crea la autoridad "ROLE_SUPERVISOR", que es la que revisa hasRole(...).
        return User.withUsername(usuario.getUsername())
                .password(usuario.getPasswordHash())
                .roles(usuario.getRol().name())
                // Usuario desactivado: Spring Security rechaza el login (DisabledException → 401).
                .disabled(!usuario.isActivo())
                .build();
    }
}
