package com.aldemar.aquamuestra.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.aldemar.aquamuestra.data.local.PreferenciasStore
import com.aldemar.aquamuestra.data.local.Sesion
import com.aldemar.aquamuestra.data.local.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Usuario con sesión iniciada (lo que necesita la UI; nunca incluye la contraseña). */
data class AppUser(
    val username: String,
    val name: String,
    val role: UserRole
)

/**
 * Sesión de la app. Ya no hay usuarios escritos en el código: el login lo valida el backend
 * (GET /api/auth/me) y la sesión se guarda en DataStore (PreferenciasStore).
 *
 * Expone:
 *  - [sesion]: la sesión como Flow (fuente de verdad, desde DataStore).
 *  - [currentUser]: copia en un estado de Compose, para que la UI la lea directo y se redibuje sola.
 *  - [credencial]: la cabecera "Basic ..." que usa AuthInterceptor en cada petición.
 */
object SessionManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var store: PreferenciasStore? = null

    lateinit var sesion: Flow<Sesion?>
        private set

    private var _currentUser by mutableStateOf<AppUser?>(null)

    /** true cuando ya se leyó DataStore al menos una vez (antes de eso no se sabe si hay sesión). */
    var cargado by mutableStateOf(false)
        private set

    /** Cabecera Authorization de la sesión actual. La lee OkHttp desde otro hilo, por eso @Volatile. */
    @Volatile
    var credencial: String? = null
        private set

    val currentUser: AppUser? get() = _currentUser
    val isLoggedIn: Boolean get() = currentUser != null
    val rol: UserRole? get() = currentUser?.role

    // ---- Permisos de navegación por rol
    /** Nueva muestra: solo OPERARIO. */
    val puedeCrearMuestras: Boolean get() = rol == UserRole.OPERARIO

    /** Dashboard: SUPERVISOR y ANALISTA. */
    val puedeVerDashboard: Boolean get() = rol == UserRole.SUPERVISOR || rol == UserRole.ANALISTA

    /** Exportar el reporte PDF de validadas: SUPERVISOR y ANALISTA (el operario no). */
    val puedeExportarReportes: Boolean get() = rol == UserRole.SUPERVISOR || rol == UserRole.ANALISTA

    /** Empieza a escuchar DataStore. Se puede llamar varias veces; solo actúa la primera. */
    fun init(context: Context) {
        if (store != null) return
        val s = PreferenciasStore.getInstance(context)
        store = s
        sesion = s.sesion
        scope.launch {
            s.sesion.collect { guardada ->
                _currentUser = guardada?.let { AppUser(it.username, it.nombre, it.rol) }
                credencial = guardada?.credencialBasic
                cargado = true
            }
        }
    }

    /**
     * Lee la sesión guardada y la deja lista para AuthInterceptor, esperando a DataStore.
     * La usa SyncWorker, que puede correr con la app cerrada (sin que MainActivity la haya cargado).
     * @return la sesión, o null si nadie inició sesión.
     */
    suspend fun cargarSesion(context: Context): Sesion? {
        init(context)
        val guardada = sesion.first()
        credencial = guardada?.credencialBasic
        return guardada
    }

    /** Guarda la sesión tras un login correcto. El estado se actualiza al tiro para poder navegar. */
    suspend fun iniciarSesion(nueva: Sesion) {
        _currentUser = AppUser(nueva.username, nueva.nombre, nueva.rol)
        credencial = nueva.credencialBasic
        store?.guardarSesion(nueva)
    }

    /** Cerrar sesión: borra la sesión de DataStore. */
    fun logout() {
        _currentUser = null
        credencial = null
        scope.launch { store?.borrarSesion() }
    }
}
