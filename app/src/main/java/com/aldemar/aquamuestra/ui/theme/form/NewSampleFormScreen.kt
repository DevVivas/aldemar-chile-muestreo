package com.aldemar.aquamuestra.ui.theme.form

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.aldemar.aquamuestra.R
import com.aldemar.aquamuestra.data.local.EstadoRevision
import com.aldemar.aquamuestra.ui.theme.SessionManager
import com.aldemar.aquamuestra.ui.theme.components.AppTopBar
import com.aldemar.aquamuestra.viewmodel.AppViewModelFactory
import com.aldemar.aquamuestra.viewmodel.ErrorCampo
import com.aldemar.aquamuestra.viewmodel.MuestraFormValidator
import com.aldemar.aquamuestra.viewmodel.NewSampleEvento
import com.aldemar.aquamuestra.viewmodel.NewSampleUiState
import com.aldemar.aquamuestra.viewmodel.NewSampleViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// Opciones de ejemplo (más adelante vendrán de Room o del backend).
private val CENTROS = listOf("Centro Chiloé (Sector Huito)", "Punta Pelluco, módulo 104", "Calbuco Sur", "Reloncaví Estuario")
private val TRENES = listOf("Tren 1 - Módulo A", "Tren 2 - Módulo B", "Tren 3 - Módulo B", "Tren 4 - Módulo C")
private val LINEAS = listOf("Línea 10", "Línea 12", "Línea 15", "Línea 18")

/** Tiempo que se ve la confirmación antes de volver atrás. */
private const val PAUSA_CONFIRMACION_MS = 1_200L

private val LOCALE_CL: Locale = Locale.forLanguageTag("es-CL")

@Composable
fun NewSampleFormScreen(
    modifier: Modifier = Modifier,
    /** null = muestra nueva; con valor = editar/corregir esa muestra (mismo ID). */
    muestraId: String? = null,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: NewSampleViewModel = viewModel(factory = AppViewModelFactory(context, muestraId))
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // El operador parte con el usuario de la sesión (se puede editar).
    LaunchedEffect(Unit) { viewModel.precargarOperador(SessionManager.currentUser?.name) }

    NewSampleFormContent(
        state = state,
        viewModel = viewModel,
        onBackClick = onBackClick,
        modifier = modifier
    )
}

@Composable
private fun NewSampleFormContent(
    state: NewSampleUiState,
    viewModel: NewSampleViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val msgBorrador = stringResource(R.string.muestra_ok_borrador)
    val msgEnviada = stringResource(R.string.muestra_ok_enviada)
    val msgErrores = stringResource(R.string.muestra_aviso_errores)
    val msgErrorGuardar = stringResource(R.string.muestra_aviso_error_guardar)
    val msgPermiso = stringResource(R.string.muestra_aviso_permiso_camara)

    // ---- Eventos de un solo uso que manda el ViewModel
    LaunchedEffect(state.evento) {
        when (val evento = state.evento) {
            is NewSampleEvento.GuardadoOk -> {
                val mensaje = if (evento.estado == EstadoRevision.ENVIADA) msgEnviada else msgBorrador
                scope.launch { snackbarHostState.showSnackbar(mensaje) }
                delay(PAUSA_CONFIRMACION_MS)
                onBackClick()
            }
            NewSampleEvento.HayErrores -> {
                viewModel.onEventoMostrado()
                scope.launch { snackbarHostState.showSnackbar(msgErrores) }
            }
            NewSampleEvento.ErrorAlGuardar -> {
                viewModel.onEventoMostrado()
                scope.launch { snackbarHostState.showSnackbar(msgErrorGuardar) }
            }
            null -> Unit
        }
    }

    // ---- Cámara (se mantiene TakePicture; CameraX llegará en otra tarea)
    var rutaFotoPendiente by rememberSaveable { mutableStateOf<String?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val ruta = rutaFotoPendiente
        if (ok && ruta != null) viewModel.onFotoCapturada(ruta) else ruta?.let { File(it).delete() }
        rutaFotoPendiente = null
    }

    fun abrirCamara() {
        val archivo = crearArchivoFoto(context)
        rutaFotoPendiente = archivo.absolutePath
        takePictureLauncher.launch(uriDeArchivo(context, archivo))
    }

    val permisoCamaraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) abrirCamara() else scope.launch { snackbarHostState.showSnackbar(msgPermiso) }
    }

    val onTomarFoto = {
        val tienePermiso = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (tienePermiso) abrirCamara() else permisoCamaraLauncher.launch(Manifest.permission.CAMERA)
    }

    var mostrarFecha by rememberSaveable { mutableStateOf(false) }
    var mostrarHora by rememberSaveable { mutableStateOf(false) }

    // Qué botón se tocó, para mostrar el indicador de carga en ese y no en el otro.
    var accionEnCurso by remember { mutableStateOf<EstadoRevision?>(null) }

    val guardado = state.evento as? NewSampleEvento.GuardadoOk
    val bloqueado = state.isSaving || guardado != null || state.cargando
    val errores = state.errores

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            val titulo = when (state.estadoOriginal) {
                null -> R.string.muestra_titulo
                EstadoRevision.BORRADOR -> R.string.muestra_titulo_editar
                else -> R.string.muestra_titulo_corregir
            }
            AppTopBar(title = stringResource(titulo), onBackClick = onBackClick)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BarraAcciones(
                guardandoComo = if (state.isSaving) accionEnCurso else null,
                guardadoComo = guardado?.estado,
                habilitado = !bloqueado,
                onGuardarBorrador = {
                    accionEnCurso = EstadoRevision.BORRADOR
                    viewModel.guardarBorrador()
                },
                onEnviar = {
                    accionEnCurso = EstadoRevision.ENVIADA
                    viewModel.enviar()
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            if (state.cargando) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            FichaId(id = state.id)

            // ---------------------------------------------------------------- Ubicación
            Seccion(titulo = stringResource(R.string.muestra_seccion_ubicacion)) {
                CampoSeleccion(
                    label = stringResource(R.string.muestra_centro),
                    valor = state.centro,
                    opciones = CENTROS,
                    onSeleccion = viewModel::onCentroChange,
                    error = errores.centro?.let { stringResource(R.string.muestra_error_centro) },
                    habilitado = !bloqueado
                )
                OutlinedTextField(
                    value = state.concesion,
                    onValueChange = viewModel::onConcesionChange,
                    label = { Text(stringResource(R.string.muestra_concesion)) },
                    supportingText = { Text(stringResource(R.string.muestra_concesion_ayuda)) },
                    singleLine = true,
                    enabled = !bloqueado,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                CampoSeleccion(
                    label = stringResource(R.string.muestra_tren),
                    valor = state.tren,
                    opciones = TRENES,
                    onSeleccion = viewModel::onTrenChange,
                    error = errores.tren?.let { stringResource(R.string.muestra_error_tren) },
                    habilitado = !bloqueado
                )
                CampoSeleccion(
                    label = stringResource(R.string.muestra_linea),
                    valor = state.linea,
                    opciones = LINEAS,
                    onSeleccion = viewModel::onLineaChange,
                    error = errores.linea?.let { stringResource(R.string.muestra_error_linea) },
                    habilitado = !bloqueado
                )
            }

            // ---------------------------------------------------------------- Fecha y operador
            Seccion(titulo = stringResource(R.string.muestra_seccion_registro)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CampoToque(
                        label = stringResource(R.string.muestra_fecha),
                        valor = formatearFecha(state.fechaHora),
                        icono = Icons.Filled.CalendarMonth,
                        descripcionAccion = stringResource(R.string.muestra_elegir_fecha),
                        habilitado = !bloqueado,
                        onClick = { mostrarFecha = true },
                        modifier = Modifier.weight(1.4f)
                    )
                    CampoToque(
                        label = stringResource(R.string.muestra_hora),
                        valor = formatearHora(state.fechaHora),
                        icono = Icons.Filled.Schedule,
                        descripcionAccion = stringResource(R.string.muestra_elegir_hora),
                        habilitado = !bloqueado,
                        onClick = { mostrarHora = true },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = state.operador,
                    onValueChange = viewModel::onOperadorChange,
                    label = { Text(stringResource(R.string.muestra_operador)) },
                    isError = errores.operador != null,
                    supportingText = errores.operador?.let { { Text(stringResource(R.string.muestra_error_operador)) } },
                    singleLine = true,
                    enabled = !bloqueado,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ---------------------------------------------------------------- Conteo
            Seccion(titulo = stringResource(R.string.muestra_seccion_conteo)) {
                OutlinedTextField(
                    value = state.tramoCm,
                    onValueChange = viewModel::onTramoCmChange,
                    label = { Text(stringResource(R.string.muestra_tramo)) },
                    suffix = { Text(stringResource(R.string.muestra_tramo_sufijo)) },
                    isError = errores.tramoCm != null,
                    supportingText = {
                        Text(
                            when (errores.tramoCm) {
                                ErrorCampo.OBLIGATORIO -> stringResource(R.string.muestra_error_tramo_vacio)
                                null -> stringResource(R.string.muestra_tramo_ayuda)
                                else -> stringResource(R.string.muestra_error_tramo_cero)
                            }
                        )
                    },
                    singleLine = true,
                    enabled = !bloqueado,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )
                ContadorIndividuos(
                    cantidad = state.cantidad,
                    error = when (errores.cantidad) {
                        ErrorCampo.OBLIGATORIO -> stringResource(R.string.muestra_error_cantidad_vacia)
                        null -> null
                        else -> stringResource(R.string.muestra_error_cantidad_negativa)
                    },
                    habilitado = !bloqueado,
                    onCantidadChange = viewModel::onCantidadChange,
                    onAjustar = viewModel::onCantidadAjustar
                )
            }

            // ---------------------------------------------------------------- Foto
            Seccion(titulo = stringResource(R.string.muestra_seccion_foto)) {
                FotoMuestra(
                    fotoPath = state.fotoPath,
                    error = errores.foto?.let { stringResource(R.string.muestra_error_foto) },
                    habilitado = !bloqueado,
                    onTomarFoto = onTomarFoto,
                    onQuitarFoto = viewModel::onFotoQuitada
                )
            }

            // ---------------------------------------------------------------- Observaciones
            Seccion(titulo = stringResource(R.string.muestra_seccion_observaciones)) {
                val max = MuestraFormValidator.MAX_OBSERVACIONES
                OutlinedTextField(
                    value = state.observaciones,
                    onValueChange = viewModel::onObservacionesChange,
                    placeholder = { Text(stringResource(R.string.muestra_observaciones_hint)) },
                    isError = errores.observaciones != null,
                    supportingText = {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            if (errores.observaciones != null) {
                                Text(stringResource(R.string.muestra_error_observaciones, max))
                            }
                            Spacer(Modifier.weight(1f))
                            Text(stringResource(R.string.muestra_contador_caracteres, state.observaciones.length, max))
                        }
                    },
                    minLines = 3,
                    enabled = !bloqueado,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }

    if (mostrarFecha) {
        DialogoFecha(
            fechaHora = state.fechaHora,
            onAceptar = { viewModel.onFechaChange(it); mostrarFecha = false },
            onCancelar = { mostrarFecha = false }
        )
    }
    if (mostrarHora) {
        DialogoHora(
            fechaHora = state.fechaHora,
            onAceptar = { h, m -> viewModel.onHoraChange(h, m); mostrarHora = false },
            onCancelar = { mostrarHora = false }
        )
    }
}

// ===========================================================================================
// Componentes de la pantalla
// ===========================================================================================

/** Ficha con el ID de la muestra: el único bloque destacado de la pantalla. */
@Composable
private fun FichaId(id: String) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            Text(stringResource(R.string.muestra_id_label), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            SelectionContainer {
                Column {
                    Text(
                        text = id.take(8).uppercase(LOCALE_CL),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 3.sp
                    )
                    Text(
                        text = id,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.muestra_id_ayuda), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun Seccion(titulo: String, contenido: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.semantics { heading() }
        )
        contenido()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CampoSeleccion(
    label: String,
    valor: String,
    opciones: List<String>,
    onSeleccion: (String) -> Unit,
    error: String?,
    habilitado: Boolean
) {
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandido && habilitado,
        onExpandedChange = { if (habilitado) expandido = it }
    ) {
        OutlinedTextField(
            value = valor,
            onValueChange = {},
            readOnly = true,
            enabled = habilitado,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, habilitado)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion, style = MaterialTheme.typography.bodyLarge) },
                    onClick = {
                        onSeleccion(opcion)
                        expandido = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

/** Campo de solo lectura que abre un selector (fecha u hora) al tocarlo. */
@Composable
private fun CampoToque(
    label: String,
    valor: String,
    icono: ImageVector,
    descripcionAccion: String,
    habilitado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaccion = remember { MutableInteractionSource() }
    val presionado by interaccion.collectIsPressedAsState()
    LaunchedEffect(presionado) { if (presionado && habilitado) onClick() }

    OutlinedTextField(
        value = valor,
        onValueChange = {},
        readOnly = true,
        enabled = habilitado,
        label = { Text(label) },
        trailingIcon = {
            IconButton(onClick = onClick, enabled = habilitado) {
                Icon(icono, contentDescription = descripcionAccion)
            }
        },
        singleLine = true,
        interactionSource = interaccion,
        modifier = modifier
    )
}

@Composable
private fun ContadorIndividuos(
    cantidad: String,
    error: String?,
    habilitado: Boolean,
    onCantidadChange: (String) -> Unit,
    onAjustar: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = cantidad,
            onValueChange = onCantidadChange,
            label = { Text(stringResource(R.string.muestra_cantidad)) },
            textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
            isError = error != null,
            supportingText = { Text(error ?: stringResource(R.string.muestra_cantidad_ayuda)) },
            singleLine = true,
            enabled = habilitado,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(-5, -1, 1, 5).forEach { delta ->
                val descripcion = if (delta < 0) {
                    stringResource(R.string.muestra_restar, -delta)
                } else {
                    stringResource(R.string.muestra_sumar, delta)
                }
                FilledTonalButton(
                    onClick = { onAjustar(delta) },
                    enabled = habilitado,
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .semantics { contentDescription = descripcion }
                ) {
                    Text(
                        text = if (delta < 0) "−${-delta}" else "+$delta",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun FotoMuestra(
    fotoPath: String?,
    error: String?,
    habilitado: Boolean,
    onTomarFoto: () -> Unit,
    onQuitarFoto: () -> Unit
) {
    val forma = RoundedCornerShape(16.dp)

    if (fotoPath == null) {
        Surface(
            onClick = onTomarFoto,
            enabled = habilitado,
            shape = forma,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(
                width = if (error != null) 2.dp else 1.dp,
                color = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .semantics { if (error != null) error(error) }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Filled.PhotoCamera,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.muestra_foto_tomar),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    stringResource(R.string.muestra_foto_ayuda),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    } else {
        AsyncImage(
            model = File(fotoPath),
            contentDescription = stringResource(R.string.muestra_foto_descripcion),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .clip(forma)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onTomarFoto,
                enabled = habilitado,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.muestra_foto_otra))
            }
            TextButton(
                onClick = onQuitarFoto,
                enabled = habilitado,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.muestra_foto_quitar))
            }
        }
    }
}

/** Botones fijos abajo: siempre a mano, grandes para usar con guantes o en movimiento. */
@Composable
private fun BarraAcciones(
    guardandoComo: EstadoRevision?,
    guardadoComo: EstadoRevision?,
    habilitado: Boolean,
    onGuardarBorrador: () -> Unit,
    onEnviar: () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // El botón que acaba de guardar se mantiene con su color (solo muestra ✓, no hace nada)
                // para que la confirmación se lea bien; el otro queda deshabilitado.
                OutlinedButton(
                    onClick = { if (habilitado) onGuardarBorrador() },
                    enabled = habilitado || guardadoComo == EstadoRevision.BORRADOR,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    ContenidoBoton(
                        texto = stringResource(R.string.muestra_guardar_borrador),
                        guardado = guardadoComo == EstadoRevision.BORRADOR,
                        guardando = guardandoComo == EstadoRevision.BORRADOR
                    )
                }
                Button(
                    onClick = { if (habilitado) onEnviar() },
                    enabled = habilitado || guardadoComo == EstadoRevision.ENVIADA,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    ContenidoBoton(
                        texto = stringResource(R.string.muestra_enviar),
                        guardado = guardadoComo == EstadoRevision.ENVIADA,
                        guardando = guardandoComo == EstadoRevision.ENVIADA
                    )
                }
            }
        }
    }
}

@Composable
private fun ContenidoBoton(texto: String, guardado: Boolean, guardando: Boolean) {
    when {
        guardado -> {
            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.muestra_guardada), style = MaterialTheme.typography.titleSmall)
        }
        guardando -> CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
        else -> Text(texto, style = MaterialTheme.typography.titleSmall, maxLines = 1)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoFecha(
    fechaHora: Long,
    onAceptar: (Long) -> Unit,
    onCancelar: () -> Unit
) {
    // No se permiten fechas futuras: una muestra siempre se registra hoy o antes.
    val hoyUtc = remember { medianocheUtc(System.currentTimeMillis()) }
    val estado = rememberDatePickerState(
        initialSelectedDateMillis = medianocheUtc(fechaHora),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= hoyUtc
        }
    )
    DatePickerDialog(
        onDismissRequest = onCancelar,
        confirmButton = {
            TextButton(onClick = { estado.selectedDateMillis?.let(onAceptar) ?: onCancelar() }) {
                Text(stringResource(R.string.muestra_aceptar))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text(stringResource(R.string.muestra_cancelar)) }
        }
    ) {
        DatePicker(state = estado)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoHora(
    fechaHora: Long,
    onAceptar: (Int, Int) -> Unit,
    onCancelar: () -> Unit
) {
    val cal = remember(fechaHora) { Calendar.getInstance().apply { timeInMillis = fechaHora } }
    val estado = rememberTimePickerState(
        initialHour = cal.get(Calendar.HOUR_OF_DAY),
        initialMinute = cal.get(Calendar.MINUTE),
        is24Hour = true
    )
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(stringResource(R.string.muestra_hora_titulo)) },
        text = { TimePicker(state = estado) },
        confirmButton = {
            TextButton(onClick = { onAceptar(estado.hour, estado.minute) }) {
                Text(stringResource(R.string.muestra_aceptar))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text(stringResource(R.string.muestra_cancelar)) }
        }
    )
}

// ===========================================================================================
// Ayudantes
// ===========================================================================================

private fun formatearFecha(millis: Long): String =
    SimpleDateFormat("d MMM yyyy", LOCALE_CL).format(Date(millis))

private fun formatearHora(millis: Long): String =
    SimpleDateFormat("HH:mm", LOCALE_CL).format(Date(millis))

/** El DatePicker trabaja con la medianoche en UTC del día elegido. */
private fun medianocheUtc(millisLocal: Long): Long {
    val local = Calendar.getInstance().apply { timeInMillis = millisLocal }
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

/** Archivo vacío donde la cámara del sistema escribirá la foto. */
private fun crearArchivoFoto(context: Context): File {
    val carpeta = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
    return File.createTempFile("muestra_${System.currentTimeMillis()}_", ".jpg", carpeta)
}

private fun uriDeArchivo(context: Context, archivo: File): Uri =
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
