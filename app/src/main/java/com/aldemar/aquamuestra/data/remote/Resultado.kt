package com.aldemar.aquamuestra.data.remote

import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

/**
 * Resultado de una llamada al backend. La UI decide qué mostrar según el caso,
 * sin tener que conocer excepciones de Retrofit u OkHttp.
 */
sealed interface Resultado<out T> {

    /** Respuesta 2xx con sus datos. */
    data class Ok<T>(val datos: T) : Resultado<T>

    /** El servidor respondió con error (401, 403, 404, 409...). [mensaje] viene del JSON del backend. */
    data class ErrorHttp(val codigo: Int, val mensaje: String?) : Resultado<Nothing>

    /** No se pudo hablar con el servidor: sin red, servidor apagado, tiempo de espera agotado... */
    data object SinConexion : Resultado<Nothing>
}

private val gson = Gson()

/**
 * Ejecuta una llamada de Retrofit y la convierte en [Resultado].
 *  - HttpException → ErrorHttp (con el "mensaje" del JSON de error del backend, si viene)
 *  - IOException   → SinConexion
 */
suspend fun <T> llamarApi(llamada: suspend () -> T): Resultado<T> = try {
    Resultado.Ok(llamada())
} catch (e: CancellationException) {
    throw e
} catch (e: HttpException) {
    val mensaje = try {
        e.response()?.errorBody()?.string()?.let { gson.fromJson(it, ErrorRespuestaDto::class.java)?.mensaje }
    } catch (cuerpoIlegible: Exception) {
        null // el cuerpo no era el JSON esperado: se muestra solo el código
    }
    Resultado.ErrorHttp(e.code(), mensaje)
} catch (e: IOException) {
    Resultado.SinConexion
}
