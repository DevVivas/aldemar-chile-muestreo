package com.aldemar.aquasample.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Reglas de seguridad de la API.
 *
 * - HTTP Basic: cada petición trae usuario y contraseña en la cabecera Authorization.
 * - STATELESS: no hay sesión ni cookies; por eso CSRF se desactiva (CSRF ataca sesiones con cookies).
 * - Las reglas se revisan de arriba hacia abajo y gana la PRIMERA que coincide.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, RespuestasSeguridadJson respuestas) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(basic -> basic.authenticationEntryPoint(respuestas.noAutenticado()))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(respuestas.noAutenticado())
                        .accessDeniedHandler(respuestas.sinPermiso()))
                .authorizeHttpRequests(auth -> auth
                        // Swagger y la página de error: públicos
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/error").permitAll()

                        // Quién soy: cualquier usuario autenticado
                        .requestMatchers(HttpMethod.GET, "/api/auth/me").authenticated()

                        // Muestras
                        .requestMatchers(HttpMethod.GET, "/api/muestras/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/muestras/*/estado").hasRole("SUPERVISOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/muestras/**").hasRole("SUPERVISOR")
                        .requestMatchers(HttpMethod.POST, "/api/muestras/**").hasAnyRole("OPERARIO", "SUPERVISOR")
                        .requestMatchers(HttpMethod.PUT, "/api/muestras/**").hasAnyRole("OPERARIO", "SUPERVISOR")

                        // Usuarios (listar, crear, editar, desactivar): solo SUPERVISOR
                        .requestMatchers("/api/usuarios", "/api/usuarios/**").hasRole("SUPERVISOR")

                        // Reportes
                        .requestMatchers("/api/reportes/**").hasAnyRole("SUPERVISOR", "ANALISTA")

                        // Cualquier otra ruta: cerrada
                        .anyRequest().denyAll()
                );
        return http.build();
    }

    /** BCrypt: guarda un hash con "sal" aleatoria; nunca se puede recuperar la contraseña original. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
