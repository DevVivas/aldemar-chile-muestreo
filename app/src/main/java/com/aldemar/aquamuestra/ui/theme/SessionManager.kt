package com.aldemar.aquamuestra.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf

enum class UserRole { SUPERVISOR, OPERARIO }

data class AppUser(
    val email: String,
    val password: String,
    val name: String,
    val role: UserRole
)

/**
 * Sesión de la app (SOLO PARA DEMO): los usuarios están escritos en el código.
 * Para una app real las credenciales deben validarse contra un servidor.
 */
object SessionManager {
    private const val PREFS_NAME = "aquamuestra_prefs"
    private const val KEY_SESSION_EMAIL = "session_email"

    // Usuarios ficticios
    val demoUsers = listOf(
        AppUser(
            email = "su01",
            password = "123",
            name = "Supervisor Demo",
            role = UserRole.SUPERVISOR
        ),
        AppUser(
            email = "op01",
            password = "123",
            name = "Operador Demo1",
            role = UserRole.OPERARIO
        ),
        AppUser(
            email = "op02",
            password = "123",
            name = "Operador Demo2",
            role = UserRole.OPERARIO
        )
    )

    private var prefs: SharedPreferences? = null
    private val _currentUser = mutableStateOf<AppUser?>(null)

    /** Usuario con sesión iniciada (null si no hay). Es un estado: la UI se actualiza sola. */
    val currentUser: AppUser? get() = _currentUser.value

    val isLoggedIn: Boolean get() = currentUser != null

    val isSupervisor: Boolean get() = currentUser?.role == UserRole.SUPERVISOR

    /** Carga la sesión guardada (si el usuario marcó "Mantener sesión activa"). Se puede llamar varias veces. */
    fun init(context: Context) {
        if (prefs != null) return
        val p = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs = p
        val savedEmail = p.getString(KEY_SESSION_EMAIL, null)
        _currentUser.value = demoUsers.firstOrNull { it.email == savedEmail }
    }

    /** Devuelve true si el correo y la contraseña son correctos. */
    fun login(email: String, password: String, keepSession: Boolean): Boolean {
        val user = demoUsers.firstOrNull {
            it.email.equals(email.trim(), ignoreCase = true) && it.password == password
        } ?: return false

        _currentUser.value = user
        prefs?.edit()?.apply {
            if (keepSession) putString(KEY_SESSION_EMAIL, user.email) else remove(KEY_SESSION_EMAIL)
        }?.apply()
        return true
    }

    fun logout() {
        _currentUser.value = null
        prefs?.edit()?.remove(KEY_SESSION_EMAIL)?.apply()
    }
}
