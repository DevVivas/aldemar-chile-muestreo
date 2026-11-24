package com.aldemar.aquamuestra.data.remote.marine

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.io.IOException

/**
 * Dónde se guarda el último dato marino bueno de cada centro.
 * Es una interfaz para que MarineRepository se pueda probar con MockK, sin Android.
 */
interface CacheMarino {
    suspend fun leer(centro: String): CondicionesMarinas?
    suspend fun guardar(centro: String, condiciones: CondicionesMarinas)
}

/** Archivo DataStore propio para el cache marino (separado de la sesión). */
private val Context.cacheMarino: DataStore<Preferences> by preferencesDataStore(name = "cache_marino")

/** Implementación real: guarda cada centro como JSON en DataStore. */
class CacheMarinoDataStore(context: Context) : CacheMarino {

    private val dataStore = context.applicationContext.cacheMarino
    private val gson = Gson()

    override suspend fun leer(centro: String): CondicionesMarinas? {
        val prefs = dataStore.data.catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }.first()
        return prefs[clave(centro)]?.let {
            try {
                gson.fromJson(it, CondicionesMarinas::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun guardar(centro: String, condiciones: CondicionesMarinas) {
        dataStore.edit { it[clave(centro)] = gson.toJson(condiciones) }
    }

    private fun clave(centro: String) = stringPreferencesKey("marino_$centro")
}
