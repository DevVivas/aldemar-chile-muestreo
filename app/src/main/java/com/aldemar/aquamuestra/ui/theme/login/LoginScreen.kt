package com.aldemar.aquamuestra.ui.theme.login

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aldemar.aquamuestra.R
import com.aldemar.aquamuestra.data.remote.RedLocal
import com.aldemar.aquamuestra.viewmodel.AppViewModelFactory
import com.aldemar.aquamuestra.viewmodel.ErrorLogin
import com.aldemar.aquamuestra.viewmodel.LoginUiState
import com.aldemar.aquamuestra.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginSuccess: () -> Unit = {},
) {
    val context = LocalContext.current
    val viewModel: LoginViewModel = viewModel(factory = AppViewModelFactory(context))
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Cuando el backend confirmó las credenciales y la sesión quedó guardada → Inicio.
    LaunchedEffect(state.ingresoOk) {
        if (state.ingresoOk) onLoginSuccess()
    }

    // Android 17+: antes de hablar con un servidor en la red local hay que pedir permiso.
    val permisoRedLocal = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) viewModel.iniciarSesion() else viewModel.onPermisoRedDenegado()
    }

    LoginContent(
        state = state,
        onUsuarioChange = viewModel::onUsuarioChange,
        onPasswordChange = viewModel::onPasswordChange,
        onIngresar = {
            when {
                !viewModel.camposCompletos() -> Unit // los errores ya quedaron bajo los campos
                RedLocal.faltaPermiso(context) -> permisoRedLocal.launch(RedLocal.PERMISO)
                else -> viewModel.iniciarSesion()
            }
        },
        modifier = modifier
    )
}

@Composable
private fun LoginContent(
    state: LoginUiState,
    onUsuarioChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onIngresar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focus = LocalFocusManager.current
    var verPassword by rememberSaveable { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp) // en tablet no se estira de lado a lado
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            Encabezado()

            Spacer(Modifier.height(40.dp))

            // ---- Usuario
            OutlinedTextField(
                value = state.usuario,
                onValueChange = onUsuarioChange,
                label = { Text(stringResource(R.string.ingreso_usuario)) },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                isError = state.errorUsuario != null,
                // "Usuario o contraseña incorrectos" se escribe una sola vez, bajo la contraseña;
                // aquí solo se marca el campo en rojo.
                supportingText = state.errorUsuario
                    ?.takeIf { it != ErrorLogin.CREDENCIALES }
                    ?.let { { Text(textoError(it)) } },
                singleLine = true,
                enabled = !state.cargando,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
                modifier = Modifier
                    .fillMaxWidth()
                    // Permite que el gestor de contraseñas del teléfono rellene el campo
                    .semantics { contentType = ContentType.Username }
            )

            Spacer(Modifier.height(8.dp))

            // ---- Contraseña
            OutlinedTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                label = { Text(stringResource(R.string.ingreso_password)) },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { verPassword = !verPassword }) {
                        Icon(
                            imageVector = if (verPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = stringResource(
                                if (verPassword) R.string.ingreso_ocultar_password else R.string.ingreso_mostrar_password
                            )
                        )
                    }
                },
                visualTransformation = if (verPassword) VisualTransformation.None else PasswordVisualTransformation(),
                isError = state.errorPassword != null,
                supportingText = state.errorPassword?.let { { Text(textoError(it)) } },
                singleLine = true,
                enabled = !state.cargando,
                keyboardOptions = KeyboardOptions(
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    focus.clearFocus()
                    onIngresar()
                }),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentType = ContentType.Password }
            )

            // ---- Error que no es de un campo (sin conexión / servidor)
            state.errorGeneral?.let { error ->
                Spacer(Modifier.height(16.dp))
                AvisoError(
                    icono = if (error == ErrorLogin.SIN_CONEXION) Icons.Filled.CloudOff else Icons.Filled.ErrorOutline,
                    texto = textoError(error)
                )
            }

            Spacer(Modifier.height(24.dp))

            // ---- Botón grande, fácil de tocar en terreno
            Button(
                onClick = {
                    focus.clearFocus()
                    onIngresar()
                },
                enabled = !state.cargando,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (state.cargando) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(R.string.ingreso_cargando), style = MaterialTheme.typography.titleMedium)
                } else {
                    Text(stringResource(R.string.ingreso_boton), style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.ingreso_ayuda_terreno),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.ingreso_ayuda_usuario),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** Logo, nombre de la app y para qué sirve. */
@Composable
private fun Encabezado() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        // El logo trae su propio fondo blanco con margen (formato de ícono adaptable):
        // se amplía para que ese fondo llene el recuadro redondeado.
        Image(
            painter = painterResource(R.mipmap.ic_launcher_foreground),
            contentDescription = null, // decorativo: el nombre de la app está justo abajo
            modifier = Modifier
                .size(104.dp)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .graphicsLayer {
                    scaleX = 1.65f
                    scaleY = 1.65f
                }
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.semantics { heading() }
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.ingreso_subtitulo),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Aviso de error general: color + icono + texto (no depende solo del color). TalkBack lo lee al aparecer. */
@Composable
private fun AvisoError(icono: androidx.compose.ui.graphics.vector.ImageVector, texto: String) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            Icon(icono, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text(texto, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun textoError(error: ErrorLogin): String = stringResource(
    when (error) {
        ErrorLogin.USUARIO_VACIO -> R.string.ingreso_error_usuario_vacio
        ErrorLogin.PASSWORD_VACIA -> R.string.ingreso_error_password_vacia
        ErrorLogin.CREDENCIALES -> R.string.ingreso_error_credenciales
        ErrorLogin.SIN_CONEXION -> R.string.ingreso_error_sin_conexion
        ErrorLogin.SIN_PERMISO_RED -> R.string.ingreso_error_permiso_red
        ErrorLogin.SERVIDOR -> R.string.ingreso_error_servidor
    }
)
