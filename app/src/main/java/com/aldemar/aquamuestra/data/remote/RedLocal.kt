package com.aldemar.aquamuestra.data.remote

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import com.aldemar.aquamuestra.BuildConfig

/**
 * Permiso de red local (Android 17 / API 37 en adelante).
 *
 * Desde Android 17, una app con targetSdk 37 NO puede conectarse a direcciones de la red local
 * (10.x, 172.16-31.x, 192.168.x) sin el permiso ACCESS_LOCAL_NETWORK, que el usuario acepta en un
 * diálogo. El backend de desarrollo está justamente en una de esas direcciones
 * (10.0.2.2 en el emulador, o la IP del PC en el Wi-Fi), así que hay que pedirlo antes del login.
 * Si el backend estuviera en internet (por ejemplo https://api.aldemar.cl), no se pide nada.
 */
object RedLocal {

    const val PERMISO = "android.permission.ACCESS_LOCAL_NETWORK"

    /** Android 17 = API 37. */
    private const val API_ANDROID_17 = 37

    /** true si antes de llamar al backend hay que pedir el permiso de red local. */
    fun faltaPermiso(context: Context): Boolean =
        Build.VERSION.SDK_INT >= API_ANDROID_17 &&
            esDireccionLocal(BuildConfig.API_BASE_URL) &&
            ContextCompat.checkSelfPermission(context, PERMISO) != PackageManager.PERMISSION_GRANTED

    /** ¿La URL apunta a una IP privada de red local? */
    fun esDireccionLocal(url: String): Boolean {
        val host = Uri.parse(url).host ?: return false
        val partes = host.split(".").mapNotNull { it.toIntOrNull() }
        if (partes.size != 4) return false
        val (a, b) = partes
        return a == 10 ||
            (a == 172 && b in 16..31) ||
            (a == 192 && b == 168)
    }
}
