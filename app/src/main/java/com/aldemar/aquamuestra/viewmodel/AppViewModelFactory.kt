package com.aldemar.aquamuestra.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aldemar.aquamuestra.data.local.AppDatabase
import com.aldemar.aquamuestra.data.remote.RetrofitClient
import com.aldemar.aquamuestra.data.remote.marine.MarineClient
import com.aldemar.aquamuestra.data.remote.marine.MarineRepository
import com.aldemar.aquamuestra.data.repository.AuthRepository
import com.aldemar.aquamuestra.data.repository.MuestraRepository
import com.aldemar.aquamuestra.data.sync.ConectividadObserver
import com.aldemar.aquamuestra.data.sync.SyncScheduler
import com.aldemar.aquamuestra.ui.theme.SessionManager

/**
 * Crea los ViewModels de la app "a mano" (sin Hilt ni Koin):
 * BD -> DAO -> Repository (+ SyncScheduler) -> ViewModel.
 *
 * @param muestraId ID de la muestra para el detalle o para editar en el formulario (opcional).
 */
class AppViewModelFactory(
    context: Context,
    private val muestraId: String? = null
) : ViewModelProvider.Factory {

    private val appContext = context.applicationContext

    private val syncScheduler: SyncScheduler by lazy { SyncScheduler.getInstance(appContext) }

    /** Cada cambio que guarda el Repository le pide al SyncScheduler que lo envíe al servidor. */
    private val muestraRepository: MuestraRepository by lazy {
        MuestraRepository(
            dao = AppDatabase.getInstance(appContext).muestraDao(),
            sincronizador = syncScheduler
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(NewSampleViewModel::class.java) ->
            NewSampleViewModel(muestraRepository, muestraIdEditar = muestraId) as T

        modelClass.isAssignableFrom(HistorialViewModel::class.java) ->
            HistorialViewModel(muestraRepository) as T

        modelClass.isAssignableFrom(DetalleMuestraViewModel::class.java) ->
            DetalleMuestraViewModel(
                muestraId = requireNotNull(muestraId) { "El detalle necesita el ID de la muestra" },
                repository = muestraRepository,
                rol = SessionManager.currentUser?.role,
                nombreUsuario = SessionManager.currentUser?.name
            ) as T

        modelClass.isAssignableFrom(LoginViewModel::class.java) ->
            LoginViewModel(
                auth = AuthRepository(RetrofitClient.api),
                guardarSesion = { sesion ->
                    SessionManager.iniciarSesion(sesion)
                    // Recién iniciada la sesión: subir lo pendiente y traer lo del servidor
                    syncScheduler.sincronizarAhora()
                    syncScheduler.programarPeriodico()
                }
            ) as T

        modelClass.isAssignableFrom(SyncViewModel::class.java) ->
            SyncViewModel(
                repository = muestraRepository,
                hayInternet = ConectividadObserver(appContext).hayInternet,
                estadoSync = syncScheduler.estado(),
                sincronizador = syncScheduler
            ) as T

        modelClass.isAssignableFrom(ConteoViewModel::class.java) -> ConteoViewModel() as T

        // API externa: usa MarineClient (Retrofit separado, sin credenciales del backend)
        modelClass.isAssignableFrom(MarineViewModel::class.java) ->
            MarineViewModel(MarineRepository(MarineClient.api, appContext)) as T

        else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
