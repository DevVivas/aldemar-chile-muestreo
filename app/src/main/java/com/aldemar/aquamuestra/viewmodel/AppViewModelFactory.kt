package com.aldemar.aquamuestra.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aldemar.aquamuestra.data.local.AppDatabase
import com.aldemar.aquamuestra.data.remote.RetrofitClient
import com.aldemar.aquamuestra.data.repository.AuthRepository
import com.aldemar.aquamuestra.data.repository.MuestraRepository
import com.aldemar.aquamuestra.ui.theme.SessionManager

/**
 * Crea los ViewModels de la app "a mano" (sin Hilt ni Koin):
 * BD -> DAO -> Repository -> ViewModel.
 *
 * @param muestraId ID de la muestra para el detalle o para editar en el formulario (opcional).
 */
class AppViewModelFactory(
    context: Context,
    private val muestraId: String? = null
) : ViewModelProvider.Factory {

    private val appContext = context.applicationContext

    private val muestraRepository: MuestraRepository by lazy {
        MuestraRepository(AppDatabase.getInstance(appContext).muestraDao())
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
                guardarSesion = SessionManager::iniciarSesion
            ) as T

        else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
