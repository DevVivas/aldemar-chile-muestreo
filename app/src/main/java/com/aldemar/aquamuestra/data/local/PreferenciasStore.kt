package com.aldemar.aquamuestra.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Sesión guardada en el teléfono.
 * [credencialBasic] es el valor completo de la cabecera "Authorization: Basic ..." que se
 * envía al backend en cada petición.
 */
data class Sesion(
    val username: String,
    val nombre: String,
    val rol: UserRole,
    val credencialBasic: String
)

/**
 * Un solo archivo DataStore por app. La primera vez, copia el modo oscuro que estaba
 * en SharedPreferences ("aquamuestra_prefs") para no perder la preferencia del usuario.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "aquasample",
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, "aquamuestra_prefs", setOf("dark_mode")))
    }
)

/**
 * Lee y guarda las preferencias de la app con DataStore: la sesión y el modo oscuro.
 * Todo se expone como Flow, así la UI se actualiza sola cuando algo cambia.
 */
class PreferenciasStore private constructor(context: Context) {

    private val dataStore = context.applicationContext.dataStore

    private object Claves {
        val USERNAME = stringPreferencesKey("sesion_username")
        val NOMBRE = stringPreferencesKey("sesion_nombre")
        val ROL = stringPreferencesKey("sesion_rol")
        val CREDENCIAL = stringPreferencesKey("sesion_credencial")
        val MODO_OSCURO = booleanPreferencesKey("dark_mode")
    }

    /** Si el archivo no se puede leer, se sigue con valores vacíos en vez de cerrar la app. */
    private val datos: Flow<Preferences> = dataStore.data.catch { e ->
        if (e is IOException) emit(emptyPreferences()) else throw e
    }

    /** Sesión actual, o null si no hay nadie con sesión iniciada. */
    val sesion: Flow<Sesion?> = datos.map { p ->
        val username = p[Claves.USERNAME]
        val credencial = p[Claves.CREDENCIAL]
        val rol = p[Claves.ROL]?.let { valor -> UserRole.entries.firstOrNull { it.name == valor } }
        if (username == null || credencial == null || rol == null) {
            null
        } else {
            Sesion(username, p[Claves.NOMBRE] ?: username, rol, credencial)
        }
    }

    val modoOscuro: Flow<Boolean> = datos.map { it[Claves.MODO_OSCURO] ?: false }

    suspend fun guardarSesion(sesion: Sesion) {
        dataStore.edit { p ->
            p[Claves.USERNAME] = sesion.username
            p[Claves.NOMBRE] = sesion.nombre
            p[Claves.ROL] = sesion.rol.name
            p[Claves.CREDENCIAL] = sesion.credencialBasic
        }
    }

    /** Cerrar sesión: borra los datos de la sesión (el modo oscuro se mantiene). */
    suspend fun borrarSesion() {
        dataStore.edit { p ->
            p.remove(Claves.USERNAME)
            p.remove(Claves.NOMBRE)
            p.remove(Claves.ROL)
            p.remove(Claves.CREDENCIAL)
        }
    }

    suspend fun guardarModoOscuro(activo: Boolean) {
        dataStore.edit { it[Claves.MODO_OSCURO] = activo }
    }

    companion object {
        @Volatile
        private var INSTANCE: PreferenciasStore? = null

        fun getInstance(context: Context): PreferenciasStore =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferenciasStore(context).also { INSTANCE = it }
            }
    }
}
