package com.aldemar.aquasample.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.ColumnDefault;

/**
 * Usuario de la API (tabla "usuarios").
 * La contraseña NUNCA se guarda en texto plano: solo su hash BCrypt.
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @Column(length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    private Rol rol;

    /**
     * false = usuario desactivado: no puede iniciar sesión, pero se conserva la fila
     * (sus muestras siguen mostrando quién las registró o revisó).
     * DEFAULT true: al agregar la columna, los usuarios que ya existían quedan activos.
     */
    @ColumnDefault("true")
    @Column(nullable = false)
    private Boolean activo = true;

    public Usuario() {
    }

    public Usuario(String username, String nombre, String passwordHash, Rol rol) {
        this.username = username;
        this.nombre = nombre;
        this.passwordHash = passwordHash;
        this.rol = rol;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public boolean isActivo() { return !Boolean.FALSE.equals(activo); }
    public void setActivo(boolean activo) { this.activo = activo; }
}
