package com.aldemar.aquamuestra.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aldemar.aquamuestra.data.local.AppDatabase
import com.aldemar.aquamuestra.data.remote.RetrofitClient
import com.aldemar.aquamuestra.data.repository.MuestraRepository
import com.aldemar.aquamuestra.ui.theme.SessionManager

/**
 * Trabajo en segundo plano que sincroniza las muestras. Lo ejecuta WorkManager cuando hay red
 * (aunque la app esté cerrada) y lo reintenta solo si algo falla por conexión.
 */
class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Sin sesión no hay credenciales: no hay nada que hacer (no es un error).
        SessionManager.cargarSesion(applicationContext) ?: return Result.success()

        // Sincronizador.NINGUNO: lo que el worker guarda en Room no debe programar otra sincronización.
        val repository = MuestraRepository(AppDatabase.getInstance(applicationContext).muestraDao())
        val sincronizador = SincronizadorMuestras(
            api = RetrofitClient.api,
            repository = repository,
            comprimirFoto = { original -> CompresorFoto.comprimir(original, applicationContext.cacheDir) }
        )

        return when (sincronizador.ejecutar()) {
            ResultadoSync.COMPLETO -> Result.success()
            ResultadoSync.REINTENTAR -> Result.retry() // WorkManager espera (backoff) y vuelve a intentar
        }
    }
}
