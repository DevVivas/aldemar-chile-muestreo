package com.aldemar.aquamuestra.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Se ejecuta antes de CADA petición y le agrega la cabecera
 *   Authorization: Basic <usuario:contraseña en Base64>
 * con las credenciales de la sesión guardada.
 *
 * Si la petición ya trae Authorization (el login, que prueba credenciales nuevas), no la toca.
 *
 * @param credencialActual devuelve el valor "Basic ..." de la sesión, o null si no hay sesión.
 */
class AuthInterceptor(private val credencialActual: () -> String?) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val peticion = chain.request()
        val credencial = credencialActual()

        if (peticion.header("Authorization") != null || credencial == null) {
            return chain.proceed(peticion)
        }
        return chain.proceed(
            peticion.newBuilder()
                .header("Authorization", credencial)
                .build()
        )
    }
}
