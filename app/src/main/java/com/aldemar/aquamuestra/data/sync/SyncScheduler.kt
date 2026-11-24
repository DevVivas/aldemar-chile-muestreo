package com.aldemar.aquamuestra.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/** Estado de la sincronización, para mostrarlo en la UI. */
enum class EstadoSync {
    /** No hay ningún trabajo pendiente. */
    INACTIVO,

    /** Esperando (por ejemplo, a que vuelva la red, o un reintento). */
    EN_COLA,
    SINCRONIZANDO,
    TERMINADO,
    FALLO
}

/**
 * Programa la sincronización con WorkManager. Es el único lugar de la app que conoce WorkManager.
 *
 * - sincronizarAhora(): un trabajo único "sync-muestras" que corre en cuanto haya red.
 * - programarPeriodico(): además, una sincronización cada 15 minutos (trae cambios del servidor).
 */
class SyncScheduler private constructor(context: Context) : Sincronizador {

    private val workManager = WorkManager.getInstance(context.applicationContext)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Solo corre si hay red; si no, WorkManager lo deja esperando hasta que vuelva. */
    private val conRed = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    override fun sincronizarAhora() {
        scope.launch {
            val trabajos = workManager.getWorkInfosForUniqueWorkFlow(TRABAJO_UNICO).first()
            val politica = when {
                // Ya hay uno esperando: ese se llevará todo lo pendiente, no hace falta otro.
                trabajos.any { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.BLOCKED } ->
                    return@launch
                // Uno está corriendo: se encola otro DESPUÉS, para enviar lo que se guardó recién.
                trabajos.any { it.state == WorkInfo.State.RUNNING } -> ExistingWorkPolicy.APPEND_OR_REPLACE
                else -> ExistingWorkPolicy.KEEP
            }
            val trabajo = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(conRed)
                // Si falla (sin conexión, servidor apagado): reintenta a los 10 s, 20 s, 30 s...
                .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
                .build()
            workManager.enqueueUniqueWork(TRABAJO_UNICO, politica, trabajo)
        }
    }

    fun programarPeriodico() {
        val trabajo = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(conRed)
            // La primera vez espera 15 min: justo al programarlo ya corre sincronizarAhora().
            .setInitialDelay(15, TimeUnit.MINUTES)
            .build()
        workManager.enqueueUniquePeriodicWork(TRABAJO_PERIODICO, ExistingPeriodicWorkPolicy.KEEP, trabajo)
    }

    /** Al cerrar sesión: no debe quedar nada sincronizando con las credenciales del usuario anterior. */
    fun cancelar() {
        workManager.cancelUniqueWork(TRABAJO_UNICO)
        workManager.cancelUniqueWork(TRABAJO_PERIODICO)
    }

    /** Estado actual, a partir de los WorkInfo de los dos trabajos. */
    fun estado(): Flow<EstadoSync> = combine(
        workManager.getWorkInfosForUniqueWorkFlow(TRABAJO_UNICO),
        workManager.getWorkInfosForUniqueWorkFlow(TRABAJO_PERIODICO)
    ) { unico, periodico ->
        val estados = unico.map { it.state }
        when {
            (unico + periodico).any { it.state == WorkInfo.State.RUNNING } -> EstadoSync.SINCRONIZANDO
            estados.any { it == WorkInfo.State.ENQUEUED || it == WorkInfo.State.BLOCKED } -> EstadoSync.EN_COLA
            estados.lastOrNull() == WorkInfo.State.SUCCEEDED -> EstadoSync.TERMINADO
            estados.lastOrNull() == WorkInfo.State.FAILED -> EstadoSync.FALLO
            else -> EstadoSync.INACTIVO
        }
    }.distinctUntilChanged()

    companion object {
        const val TRABAJO_UNICO = "sync-muestras"
        const val TRABAJO_PERIODICO = "sync-muestras-periodico"

        @Volatile
        private var INSTANCE: SyncScheduler? = null

        fun getInstance(context: Context): SyncScheduler =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SyncScheduler(context).also { INSTANCE = it }
            }
    }
}
