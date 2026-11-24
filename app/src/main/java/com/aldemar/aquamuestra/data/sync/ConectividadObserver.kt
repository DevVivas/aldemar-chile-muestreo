package com.aldemar.aquamuestra.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Avisa si el teléfono tiene internet, como un Flow<Boolean> que cambia solo
 * (por ejemplo al activar o quitar el modo avión).
 */
class ConectividadObserver(context: Context) {

    private val connectivity = context.applicationContext.getSystemService(ConnectivityManager::class.java)

    val hayInternet: Flow<Boolean> = callbackFlow {
        fun tieneInternet(capacidades: NetworkCapabilities?): Boolean =
            capacidades != null &&
                capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

        fun estadoActual() = tieneInternet(connectivity.getNetworkCapabilities(connectivity.activeNetwork))

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(estadoActual())
            }

            override fun onCapabilitiesChanged(network: Network, capacidades: NetworkCapabilities) {
                trySend(tieneInternet(capacidades))
            }

            override fun onLost(network: Network) {
                trySend(false)
            }
        }

        trySend(estadoActual()) // valor inicial
        connectivity.registerDefaultNetworkCallback(callback)
        awaitClose { connectivity.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()
}
