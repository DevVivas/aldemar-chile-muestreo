package com.aldemar.aquamuestra.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aldemar.aquamuestra.data.local.AppDatabase
import com.aldemar.aquamuestra.data.repository.MuestraRepository

/**
 * Crea los ViewModels de la app "a mano" (sin Hilt ni Koin):
 * BD -> DAO -> Repository -> ViewModel.
 */
class AppViewModelFactory(context: Context) : ViewModelProvider.Factory {

    private val appContext = context.applicationContext

    private val muestraRepository: MuestraRepository by lazy {
        MuestraRepository(AppDatabase.getInstance(appContext).muestraDao())
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(NewSampleViewModel::class.java) ->
            NewSampleViewModel(muestraRepository) as T

        modelClass.isAssignableFrom(HistorialViewModel::class.java) ->
            HistorialViewModel(muestraRepository) as T

        else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
