package com.aldemar.aquamuestra.data.remote.marine

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aldemar.aquamuestra.data.local.CentroUbicacion
import com.aldemar.aquamuestra.data.remote.Resultado
import com.aldemar.aquamuestra.data.remote.llamarApi
import com.google.gson.Gson
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.io.IOException

/** Condiciones marinas ya listas para mostrar (modelo de la app, no el JSON de Open-Meteo). */
data class CondicionesMarinas(
    val alturaOlaM: Double?,
    val direccionOlaGrados: Double?,
    val periodoOlaS: Double?,
    val temperaturaMarC: Double?,
    val corrienteKmh: Double?,
    val direccionCorrienteGrados: Double?,
    /** Hora local del dato según Open-Meteo ("2026-11-24T01:45"). */
    val horaDato: String?,
    /** Cuándo lo consultó la app (epoch millis). */
    val consultadoEn: Long
)

/** Archivo DataStore propio para el cache marino (separado de la sesión). */
private val Context.cacheMarino: DataStore<Preferences> by preferencesDataStore(name = "cache_marino")

/**
 * Consulta Open-Meteo Marine y guarda el último resultado exitoso de cada centro,
 * para poder mostrarlo sin internet.
 */
class MarineRepository(
    private val api: MarineApi,
    context: Context,
    private val reloj: () -> Long = { System.currentTimeMillis() }
) {
    private val dataStore = context.applicationContext.cacheMarino
    private val gson = Gson()

    /** Pide las condiciones actuales del centro. Si sale bien, las deja en cache. */
    suspend fun consultar(centro: CentroUbicacion): Resultado<CondicionesMarinas> {
        val r = llamarApi { api.condicionesActuales(centro.latitud, centro.longitud) }
        return when (r) {
            is Resultado.Ok -> {
                val actual = r.datos.current
                if (actual == null) {
                    Resultado.ErrorHttp(200, "Open-Meteo no entregó datos para este punto")
                } else {
                    val condiciones = CondicionesMarinas(
                        alturaOlaM = actual.alturaOla,
                        direccionOlaGrados = actual.direccionOla,
                        periodoOlaS = actual.periodoOla,
                        temperaturaMarC = actual.temperaturaMar,
                        corrienteKmh = actual.velocidadCorriente,
                        direccionCorrienteGrados = actual.direccionCorriente,
                        horaDato = actual.time,
                        consultadoEn = reloj()
                    )
                    guardarCache(centro, condiciones)
                    Resultado.Ok(condiciones)
                }
            }
            is Resultado.ErrorHttp -> Resultado.ErrorHttp(r.codigo, r.mensaje)
            Resultado.SinConexion -> Resultado.SinConexion
        }
    }

    /** Último dato bueno guardado para el centro, o null si nunca se consultó. */
    suspend fun leerCache(centro: CentroUbicacion): CondicionesMarinas? {
        val prefs = dataStore.data.catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }.first()
        return prefs[clave(centro)]?.let {
            try {
                gson.fromJson(it, CondicionesMarinas::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }

    private suspend fun guardarCache(centro: CentroUbicacion, condiciones: CondicionesMarinas) {
        dataStore.edit { it[clave(centro)] = gson.toJson(condiciones) }
    }

    private fun clave(centro: CentroUbicacion) = stringPreferencesKey("marino_${centro.nombre}")
}
