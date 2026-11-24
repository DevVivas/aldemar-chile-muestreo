package com.aldemar.aquamuestra.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aldemar.aquamuestra.data.local.Sesion
import com.aldemar.aquamuestra.data.local.UserRole
import com.aldemar.aquamuestra.data.remote.Resultado
import com.aldemar.aquamuestra.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Por qué falló el login. La pantalla traduce cada uno a un texto. */
enum class ErrorLogin {
    USUARIO_VACIO,
    PASSWORD_VACIA,
    /** 401: usuario o contraseña incorrectos. */
    CREDENCIALES,
    /** No se pudo llegar al servidor. */
    SIN_CONEXION,
    /** El usuario no permitió el acceso a la red local (Android 17+), donde está el servidor. */
    SIN_PERMISO_RED,
    /** El servidor respondió con otro error (o un rol desconocido). */
    SERVIDOR
}

data class LoginUiState(
    val usuario: String = "",
    val password: String = "",
    /** Error que se muestra bajo el campo Usuario. */
    val errorUsuario: ErrorLogin? = null,
    /** Error que se muestra bajo el campo Contraseña. */
    val errorPassword: ErrorLogin? = null,
    /** Error que no es de un campo (sin conexión, servidor). */
    val errorGeneral: ErrorLogin? = null,
    val cargando: Boolean = false,
    /** true cuando el login fue correcto y la sesión ya quedó guardada: la pantalla navega a Inicio. */
    val ingresoOk: Boolean = false
)

/**
 * ViewModel del login.
 * @param guardarSesion guarda la sesión (en la app: SessionManager.iniciarSesion, que usa DataStore).
 */
class LoginViewModel(
    private val auth: AuthRepository,
    private val guardarSesion: suspend (Sesion) -> Unit
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsuarioChange(valor: String) =
        _uiState.update { it.copy(usuario = valor.trim(), errorUsuario = null, errorGeneral = null) }

    fun onPasswordChange(valor: String) =
        _uiState.update {
            it.copy(
                password = valor,
                errorPassword = null,
                errorGeneral = null,
                // Al corregir la contraseña también se quita el "incorrectos" del usuario
                errorUsuario = it.errorUsuario.takeUnless { e -> e == ErrorLogin.CREDENCIALES }
            )
        }

    /**
     * Revisa que los campos no estén vacíos (y marca los errores si lo están).
     * La pantalla la usa antes de pedir el permiso de red local, para no pedirlo por nada.
     */
    fun camposCompletos(): Boolean {
        val s = _uiState.value
        val errorUsuario = if (s.usuario.isBlank()) ErrorLogin.USUARIO_VACIO else null
        val errorPassword = if (s.password.isEmpty()) ErrorLogin.PASSWORD_VACIA else null
        _uiState.update { it.copy(errorUsuario = errorUsuario, errorPassword = errorPassword) }
        return errorUsuario == null && errorPassword == null
    }

    /** El usuario rechazó el permiso de red local: sin él no se puede llegar al servidor. */
    fun onPermisoRedDenegado() =
        _uiState.update { it.copy(errorGeneral = ErrorLogin.SIN_PERMISO_RED) }

    fun iniciarSesion() {
        val s = _uiState.value
        if (s.cargando) return

        // 1. Validación local: no se llama al servidor con campos vacíos.
        if (!camposCompletos()) return

        _uiState.update { it.copy(cargando = true, errorGeneral = null) }
        viewModelScope.launch {
            // 2. Se arma la credencial Basic y se pregunta al backend quién es.
            val credencial = auth.credencialBasic(s.usuario, s.password)
            when (val r = auth.quienSoy(credencial)) {
                is Resultado.Ok -> {
                    val rol = UserRole.entries.firstOrNull { it.name == r.datos.rol }
                    if (rol == null) {
                        _uiState.update { it.copy(cargando = false, errorGeneral = ErrorLogin.SERVIDOR) }
                    } else {
                        // 3. Correcto: se guarda la sesión (DataStore) y se avisa a la pantalla.
                        guardarSesion(Sesion(r.datos.username, r.datos.nombre, rol, credencial))
                        _uiState.update { it.copy(cargando = false, ingresoOk = true) }
                    }
                }
                is Resultado.ErrorHttp ->
                    if (r.codigo == 401) {
                        _uiState.update {
                            it.copy(
                                cargando = false,
                                errorUsuario = ErrorLogin.CREDENCIALES,
                                errorPassword = ErrorLogin.CREDENCIALES
                            )
                        }
                    } else {
                        _uiState.update { it.copy(cargando = false, errorGeneral = ErrorLogin.SERVIDOR) }
                    }
                Resultado.SinConexion ->
                    _uiState.update { it.copy(cargando = false, errorGeneral = ErrorLogin.SIN_CONEXION) }
            }
        }
    }
}
