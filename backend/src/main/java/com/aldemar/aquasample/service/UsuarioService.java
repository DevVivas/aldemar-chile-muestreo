package com.aldemar.aquasample.service;

import com.aldemar.aquasample.dto.UsuarioActualizarDto;
import com.aldemar.aquasample.dto.UsuarioCrearDto;
import com.aldemar.aquasample.dto.UsuarioDto;
import com.aldemar.aquasample.exception.ConflictoUsuarioException;
import com.aldemar.aquasample.exception.UsuarioNoEncontradoException;
import com.aldemar.aquasample.model.Rol;
import com.aldemar.aquasample.model.Usuario;
import com.aldemar.aquasample.repository.UsuarioRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Usuarios: consulta (para /api/auth/me y las reglas de muestras) y CRUD del supervisor.
 *
 * Reglas del CRUD:
 * - La contraseña se guarda solo como hash BCrypt y nunca sale en las respuestas (se devuelve UsuarioDto).
 * - "Eliminar" = desactivar (activo=false): la fila se conserva para no perder quién hizo cada muestra.
 * - Un supervisor no puede desactivarse ni quitarse el rol a sí mismo.
 * - Siempre debe quedar al menos un supervisor activo.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    // ------------------------------------------------------------------ Consulta (login y muestras)

    @Transactional(readOnly = true)
    public Usuario obtener(String username) {
        return repository.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }

    @Transactional(readOnly = true)
    public UsuarioDto obtenerDto(String username) {
        return UsuarioDto.desde(obtener(username));
    }

    // ------------------------------------------------------------------ CRUD (solo SUPERVISOR)

    /** Lista ordenada por username. Los filtros son opcionales y se combinan. */
    @Transactional(readOnly = true)
    public List<UsuarioDto> listar(Rol rol, Boolean activo) {
        return repository.findAll(Sort.by("username")).stream()
                .filter(u -> rol == null || u.getRol() == rol)
                .filter(u -> activo == null || u.isActivo() == activo)
                .map(UsuarioDto::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioDto buscar(String username) {
        return UsuarioDto.desde(buscarEntidad(username));
    }

    @Transactional
    public UsuarioDto crear(UsuarioCrearDto dto) {
        if (repository.existsById(dto.username())) {
            throw new ConflictoUsuarioException("Ya existe el usuario " + dto.username());
        }
        Usuario nuevo = new Usuario(dto.username(), dto.nombre().trim(), passwordEncoder.encode(dto.password()), dto.rol());
        return UsuarioDto.desde(repository.save(nuevo));
    }

    /**
     * Actualiza nombre, rol y (si vienen) activo y contraseña.
     * @param actor username del supervisor que hace el cambio.
     */
    @Transactional
    public UsuarioDto actualizar(String username, UsuarioActualizarDto dto, String actor) {
        Usuario u = buscarEntidad(username);
        boolean activoNuevo = dto.activo() == null ? u.isActivo() : dto.activo();
        verificarReglasSupervisor(u, dto.rol(), activoNuevo, actor);

        u.setNombre(dto.nombre().trim());
        u.setRol(dto.rol());
        u.setActivo(activoNuevo);
        if (dto.password() != null) {
            u.setPasswordHash(passwordEncoder.encode(dto.password())); // se re-hashea; la anterior deja de servir
        }
        return UsuarioDto.desde(repository.save(u));
    }

    /** "Eliminar": deja el usuario inactivo (no borra la fila). Si ya estaba inactivo, no hace nada. */
    @Transactional
    public void desactivar(String username, String actor) {
        Usuario u = buscarEntidad(username);
        verificarReglasSupervisor(u, u.getRol(), false, actor);
        u.setActivo(false);
        repository.save(u);
    }

    private Usuario buscarEntidad(String username) {
        return repository.findById(username).orElseThrow(() -> new UsuarioNoEncontradoException(username));
    }

    /**
     * 409 si el cambio deja al supervisor que lo hace sin acceso a esta pantalla,
     * o si deja el sistema sin ningún supervisor activo.
     */
    private void verificarReglasSupervisor(Usuario u, Rol rolNuevo, boolean activoNuevo, String actor) {
        boolean pierdeSupervision = u.getRol() == Rol.SUPERVISOR && u.isActivo()
                && (rolNuevo != Rol.SUPERVISOR || !activoNuevo);
        if (!pierdeSupervision) {
            return;
        }
        if (u.getUsername().equalsIgnoreCase(actor)) {
            throw new ConflictoUsuarioException(
                    "No puedes desactivarte ni quitarte el rol de supervisor a ti mismo. Pídeselo a otro supervisor.");
        }
        if (repository.countByRolAndActivoTrue(Rol.SUPERVISOR) <= 1) {
            throw new ConflictoUsuarioException("Debe quedar al menos un supervisor activo.");
        }
    }
}
