package com.aldemar.aquamuestra.data.sync

import android.util.Log
import com.aldemar.aquamuestra.data.local.MuestraEntity
import com.aldemar.aquamuestra.data.remote.AquaSampleApi
import com.aldemar.aquamuestra.data.remote.MuestraMapper
import com.aldemar.aquamuestra.data.remote.Resultado
import com.aldemar.aquamuestra.data.remote.llamarApi
import com.aldemar.aquamuestra.data.repository.MuestraRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/** Cómo terminó una sincronización. */
enum class ResultadoSync {
    /** Todo lo que se podía enviar y bajar, se hizo. */
    COMPLETO,

    /** Sin conexión, servidor apagado o error 5xx: WorkManager debe reintentar más tarde. */
    REINTENTAR
}

/**
 * Lógica de la sincronización (la usa SyncWorker). No depende de WorkManager ni de Android
 * más que para el log, así que se puede probar con MockK.
 *
 *  1. SUBIR: cada muestra pendiente se envía con PUT (idempotente: mismo id = no duplica) y luego su foto.
 *  2. BAJAR: se traen todas las muestras del servidor y se guardan en Room, salvo las que
 *     siguen pendientes en el teléfono (lo local pendiente gana).
 *
 * @param comprimirFoto achica la foto antes de subirla (devuelve un archivo temporal o null).
 */
class SincronizadorMuestras(
    private val api: AquaSampleApi,
    private val repository: MuestraRepository,
    private val comprimirFoto: (File) -> File?,
    private val log: (String) -> Unit = { Log.w(TAG, it) }
) {

    /**
     * Nunca corren dos sincronizaciones a la vez (por ejemplo, la inmediata y la periódica):
     * si no, ambas podrían enviar la misma muestra nueva al mismo tiempo. La segunda espera a la primera.
     */
    suspend fun ejecutar(): ResultadoSync = candado.withLock {
        if (subir() == ResultadoSync.REINTENTAR) return@withLock ResultadoSync.REINTENTAR
        bajar()
    }

    // ------------------------------------------------------------------------------------- SUBIR

    private suspend fun subir(): ResultadoSync {
        // De la más antigua a la más nueva
        for (muestra in repository.obtenerPendientesSync()) {
            when (val r = llamarApi { api.guardarMuestra(muestra.id, MuestraMapper.aDto(muestra)) }) {
                is Resultado.Ok -> {
                    when (subirFoto(muestra)) {
                        PasoFoto.REINTENTAR -> return ResultadoSync.REINTENTAR
                        PasoFoto.ERROR -> Unit // queda pendiente; se intentará en la próxima sincronización
                        PasoFoto.OK -> {
                            // Solo si no la editaron mientras se subía (si no, se enviará de nuevo)
                            repository.marcarSincronizadaSiNoCambio(muestra.id, muestra.actualizadoEn)
                        }
                    }
                }

                is Resultado.ErrorHttp -> when {
                    // 409: en el servidor ya está VALIDADA. Gana el servidor: se trae su versión.
                    r.codigo == 409 -> if (traerDelServidor(muestra) == ResultadoSync.REINTENTAR) {
                        return ResultadoSync.REINTENTAR
                    }
                    // 5xx: problema del servidor → reintentar todo más tarde
                    r.codigo >= 500 -> return ResultadoSync.REINTENTAR
                    // 400 / 401 / 403...: no se arregla reintentando. Queda pendiente y se sigue con la próxima.
                    else -> log("Muestra ${muestra.id}: el servidor respondió ${r.codigo} (${r.mensaje}). Queda pendiente.")
                }

                Resultado.SinConexion -> return ResultadoSync.REINTENTAR
            }
        }
        return ResultadoSync.COMPLETO
    }

    private enum class PasoFoto { OK, ERROR, REINTENTAR }

    /** Sube la foto local (comprimida). Si la muestra no tiene foto en el teléfono, no hay nada que hacer. */
    private suspend fun subirFoto(muestra: MuestraEntity): PasoFoto {
        val original = muestra.fotoPath?.let(::File)?.takeIf { it.exists() } ?: return PasoFoto.OK
        val temporal = comprimirFoto(original) ?: run {
            log("Muestra ${muestra.id}: no se pudo leer la foto ${original.name}.")
            return PasoFoto.ERROR
        }
        try {
            val parte = MultipartBody.Part.createFormData(
                "archivo", "${muestra.id}.jpg", temporal.asRequestBody("image/jpeg".toMediaType())
            )
            return when (val r = llamarApi { api.subirFoto(muestra.id, parte) }) {
                is Resultado.Ok -> PasoFoto.OK
                Resultado.SinConexion -> PasoFoto.REINTENTAR
                is Resultado.ErrorHttp -> if (r.codigo >= 500) {
                    PasoFoto.REINTENTAR
                } else {
                    log("Foto de ${muestra.id}: el servidor respondió ${r.codigo} (${r.mensaje}).")
                    PasoFoto.ERROR
                }
            }
        } finally {
            temporal.delete() // el temporal siempre se borra
        }
    }

    /** Reemplaza la muestra local por la del servidor (conservando la foto local) y la deja sincronizada. */
    private suspend fun traerDelServidor(local: MuestraEntity): ResultadoSync =
        when (val r = llamarApi { api.obtenerMuestra(local.id) }) {
            is Resultado.Ok -> {
                repository.guardarDesdeServidor(
                    MuestraMapper.aEntidad(r.datos, fotoPathLocal = local.fotoPath, marcasJsonLocal = local.marcasJson)
                )
                log("Muestra ${local.id}: ya estaba validada en el servidor; se reemplazó por la versión del servidor.")
                ResultadoSync.COMPLETO
            }
            Resultado.SinConexion -> ResultadoSync.REINTENTAR
            is Resultado.ErrorHttp -> if (r.codigo >= 500) ResultadoSync.REINTENTAR else ResultadoSync.COMPLETO
        }

    // ------------------------------------------------------------------------------------- BAJAR

    private suspend fun bajar(): ResultadoSync =
        when (val r = llamarApi { api.listarMuestras() }) {
            is Resultado.Ok -> {
                for (dto in r.datos) {
                    val local = repository.obtenerPorId(dto.id)
                    if (local?.pendienteSync == true) continue // lo local pendiente gana: se sube en la próxima
                    repository.guardarDesdeServidor(
                        MuestraMapper.aEntidad(dto, fotoPathLocal = local?.fotoPath, marcasJsonLocal = local?.marcasJson)
                    )
                }
                ResultadoSync.COMPLETO
            }
            Resultado.SinConexion -> ResultadoSync.REINTENTAR
            is Resultado.ErrorHttp -> if (r.codigo >= 500) {
                ResultadoSync.REINTENTAR
            } else {
                log("No se pudieron bajar las muestras: ${r.codigo} (${r.mensaje}).")
                ResultadoSync.COMPLETO
            }
        }

    companion object {
        const val TAG = "SyncMuestras"

        /** Un solo candado para toda la app (los workers corren en el mismo proceso). */
        private val candado = Mutex()
    }
}
