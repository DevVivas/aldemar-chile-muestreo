package com.aldemar.aquamuestra.data.repository

import com.aldemar.aquamuestra.data.remote.AquaSampleApi
import com.aldemar.aquamuestra.data.remote.Resultado
import com.aldemar.aquamuestra.data.remote.UsuarioDto
import com.aldemar.aquamuestra.data.remote.llamarApi
import okhttp3.Credentials

/** Login contra el backend. */
class AuthRepository(private val api: AquaSampleApi) {

    /** Arma la cabecera "Basic <usuario:contraseña en Base64>" que espera Spring Security. */
    fun credencialBasic(usuario: String, password: String): String =
        Credentials.basic(usuario, password, Charsets.UTF_8)

    /**
     * Pregunta al backend quién es el dueño de [credencial] (GET /api/auth/me).
     * 200 = credenciales correctas (trae nombre y rol); 401 = incorrectas.
     */
    suspend fun quienSoy(credencial: String): Resultado<UsuarioDto> = llamarApi { api.me(credencial) }
}
