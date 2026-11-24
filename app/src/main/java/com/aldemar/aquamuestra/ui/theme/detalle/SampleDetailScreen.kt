package com.aldemar.aquamuestra.ui.theme.detalle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.NoPhotography
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.aldemar.aquamuestra.R
import com.aldemar.aquamuestra.data.local.EstadoRevision
import com.aldemar.aquamuestra.data.local.MuestraEntity
import com.aldemar.aquamuestra.ui.theme.AppRoutes
import com.aldemar.aquamuestra.ui.theme.components.AppTopBar
import com.aldemar.aquamuestra.ui.theme.components.BottomNavBar
import com.aldemar.aquamuestra.ui.theme.components.ChipEstado
import com.aldemar.aquamuestra.ui.theme.components.EtiquetaPendiente
import com.aldemar.aquamuestra.viewmodel.AccionRevision
import com.aldemar.aquamuestra.viewmodel.AccionesRevision
import com.aldemar.aquamuestra.viewmodel.AppViewModelFactory
import com.aldemar.aquamuestra.viewmodel.DetalleEvento
import com.aldemar.aquamuestra.viewmodel.DetalleMuestraViewModel
import com.aldemar.aquamuestra.viewmodel.DetalleUiState
import com.aldemar.aquamuestra.viewmodel.PuntoEvolucion
import kotlinx.coroutines.launch
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

private val LOCALE_CL: Locale = Locale.forLanguageTag("es-CL")

@Composable
fun SampleDetailScreen(
    sampleId: String,
    navController: NavController,
    onBackClick: () -> Unit = { navController.popBackStack() }
) {
    val context = LocalContext.current
    val viewModel: DetalleMuestraViewModel = viewModel(factory = AppViewModelFactory(context, sampleId))
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var mostrarObservar by rememberSaveable { mutableStateOf(false) }

    val msgValidada = stringResource(R.string.det_ok_validada)
    val msgObservada = stringResource(R.string.det_ok_observada)
    val msgError = stringResource(R.string.det_error_guardar)

    // Eventos de un solo uso (Snackbar de confirmación o error)
    LaunchedEffect(state.evento) {
        val mensaje = when (val evento = state.evento) {
            is DetalleEvento.AccionOk ->
                if (evento.nuevoEstado == EstadoRevision.VALIDADA) msgValidada else msgObservada
            DetalleEvento.ErrorAlGuardar -> msgError
            DetalleEvento.ComentarioVacio, null -> null
        }
        if (state.evento != null) viewModel.onEventoMostrado()
        if (mensaje != null) scope.launch { snackbarHostState.showSnackbar(mensaje) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppTopBar(title = stringResource(R.string.detail_title), onBackClick = onBackClick) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column {
                val muestra = state.muestra
                if (muestra != null && state.acciones.isNotEmpty()) {
                    BarraAcciones(
                        acciones = state.acciones,
                        estado = muestra.estado,
                        habilitado = !state.procesando,
                        onValidar = viewModel::validar,
                        onObservar = { mostrarObservar = true },
                        onEditar = { navController.navigate(AppRoutes.NewSample.editar(muestra.id)) }
                    )
                }
                BottomNavBar(navController = navController)
            }
        }
    ) { padding ->
        val muestra = state.muestra
        Box(modifier = Modifier.padding(padding)) {
            when {
                state.cargando -> Cargando()
                state.error -> MensajeCentro(
                    icono = Icons.Filled.ErrorOutline,
                    titulo = stringResource(R.string.det_error_titulo),
                    texto = stringResource(R.string.det_error_texto),
                    onVolver = onBackClick
                )
                state.noEncontrada || muestra == null -> MensajeCentro(
                    icono = Icons.Filled.SearchOff,
                    titulo = stringResource(R.string.det_no_encontrada_titulo),
                    texto = stringResource(R.string.det_no_encontrada_texto),
                    onVolver = onBackClick
                )
                else -> ContenidoDetalle(muestra = muestra, state = state)
            }
        }
    }

    if (mostrarObservar) {
        DialogoObservar(
            onConfirmar = { comentario ->
                viewModel.observar(comentario)
                mostrarObservar = false
            },
            onCancelar = { mostrarObservar = false }
        )
    }
}

@Composable
private fun ContenidoDetalle(muestra: MuestraEntity, state: DetalleUiState) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        FotoGrande(muestra)
        Identificacion(muestra)

        if (muestra.comentarioRevision != null) {
            ObservacionSupervisor(muestra.comentarioRevision, muestra.revisadoPor)
        } else if (muestra.estado == EstadoRevision.VALIDADA && muestra.revisadoPor != null) {
            LineaValidada(muestra.revisadoPor)
        }

        Cifras(cantidad = muestra.cantidad, densidad = state.densidad, tramoCm = muestra.tramoCm)
        DatosMuestreo(muestra)
        Observaciones(muestra.observaciones)
        Evolucion(state.evolucion)
        Spacer(Modifier.height(8.dp))
    }
}

// ===========================================================================================
// Bloques del detalle
// ===========================================================================================

@Composable
private fun FotoGrande(muestra: MuestraEntity) {
    val forma = RoundedCornerShape(20.dp)
    val modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(4f / 3f)
        .clip(forma)
    if (muestra.fotoPath != null) {
        AsyncImage(
            model = File(muestra.fotoPath),
            contentDescription = stringResource(R.string.det_foto_descripcion, muestra.id.take(8).uppercase(LOCALE_CL)),
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Icon(
                Icons.Filled.NoPhotography,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.det_sin_foto),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun Identificacion(muestra: MuestraEntity) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = muestra.id.take(8).uppercase(LOCALE_CL),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp,
                modifier = Modifier.weight(1f)
            )
            ChipEstado(muestra.estado)
        }
        SelectionContainer {
            Text(
                text = muestra.id,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (muestra.pendienteSync) EtiquetaPendiente()
    }
}

/** Comentario del supervisor: el bloque más visible cuando la muestra está observada. */
@Composable
private fun ObservacionSupervisor(comentario: String, autor: String?) {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Icon(Icons.Filled.Flag, contentDescription = null, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    stringResource(R.string.det_observacion_titulo),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.semantics { heading() }
                )
                Text(comentario, style = MaterialTheme.typography.bodyLarge)
                if (autor != null) {
                    Text(stringResource(R.string.det_observacion_por, autor), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun LineaValidada(autor: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            stringResource(R.string.det_validada_por, autor),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

/** Las tres cifras del conteo, grandes y alineadas. */
@Composable
private fun Cifras(cantidad: Int, densidad: Double?, tramoCm: Int) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(vertical = 16.dp)) {
            Cifra(cantidad.toString(), stringResource(R.string.det_cantidad), Modifier.weight(1f))
            Cifra(densidad?.let { formatearDecimal(it) } ?: "—", stringResource(R.string.det_densidad), Modifier.weight(1f))
            Cifra(tramoCm.toString(), stringResource(R.string.det_tramo), Modifier.weight(1f))
        }
    }
}

@Composable
private fun Cifra(valor: String, unidad: String, modifier: Modifier = Modifier) {
    // Se lee "212 individuos" como una sola frase con TalkBack.
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clearAndSetSemantics { contentDescription = "$valor $unidad" }
    ) {
        Text(valor, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        Text(
            unidad,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DatosMuestreo(muestra: MuestraEntity) {
    val sinDato = stringResource(R.string.det_sin_dato)
    Seccion(titulo = stringResource(R.string.det_seccion_datos)) {
        Column {
            FilaDato(stringResource(R.string.det_centro), muestra.centro)
            FilaDato(stringResource(R.string.det_concesion), muestra.concesion ?: sinDato)
            FilaDato(stringResource(R.string.det_tren), muestra.tren)
            FilaDato(stringResource(R.string.det_linea), muestra.linea)
            FilaDato(stringResource(R.string.det_fecha_hora), formatearFechaHora(muestra.fechaHora))
            FilaDato(stringResource(R.string.det_responsable), muestra.operador, ultima = true)
        }
    }
}

@Composable
private fun FilaDato(etiqueta: String, valor: String, ultima: Boolean = false) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .semantics(mergeDescendants = true) {}
    ) {
        Text(
            etiqueta,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(128.dp)
        )
        Text(valor, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
    }
    if (!ultima) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun Observaciones(texto: String) {
    Seccion(titulo = stringResource(R.string.det_seccion_observaciones), icono = Icons.AutoMirrored.Filled.Assignment) {
        Text(
            text = texto.ifBlank { stringResource(R.string.det_sin_observaciones) },
            style = MaterialTheme.typography.bodyLarge,
            color = if (texto.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
        )
    }
}

/** Barras simples de densidad por muestreo, dibujadas con Compose (sin librerías). */
@Composable
private fun Evolucion(puntos: List<PuntoEvolucion>) {
    Seccion(titulo = stringResource(R.string.det_seccion_evolucion), icono = Icons.AutoMirrored.Filled.ShowChart) {
        if (puntos.size <= 1) {
            Text(
                stringResource(R.string.det_evolucion_primero),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return@Seccion
        }
        Text(
            stringResource(R.string.det_evolucion_ayuda),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        val maximo = puntos.mapNotNull { it.densidad }.maxOrNull()?.takeIf { it > 0.0 } ?: 1.0
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            puntos.forEach { FilaEvolucion(it, maximo) }
        }
    }
}

@Composable
private fun FilaEvolucion(punto: PuntoEvolucion, maximo: Double) {
    val colores = MaterialTheme.colorScheme
    val fondo = if (punto.esActual) colores.primaryContainer else colores.surface
    val colorBarra = if (punto.esActual) colores.primary else colores.outline
    val fraccion = ((punto.densidad ?: 0.0) / maximo).toFloat().coerceIn(0.02f, 1f)

    Surface(color = fondo, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .semantics(mergeDescendants = true) {}
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    formatearFechaCorta(punto.fechaHora),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (punto.esActual) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier.width(76.dp)
                )
                // Barra proporcional a la densidad (la más alta ocupa todo el ancho)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraccion)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(7.dp))
                            .background(colorBarra)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = punto.densidad?.let { "${formatearDecimal(it)} ${stringResource(R.string.det_densidad)}" } ?: "—",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.det_cantidad_corta, punto.cantidad),
                    style = MaterialTheme.typography.bodySmall,
                    color = colores.onSurfaceVariant,
                    modifier = Modifier.width(76.dp)
                )
                punto.variacionPct?.let { Variacion(it) }
                Spacer(Modifier.weight(1f))
                if (punto.esActual) {
                    Text(
                        stringResource(R.string.det_esta_muestra),
                        style = MaterialTheme.typography.labelMedium,
                        color = colores.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun Variacion(pct: Double) {
    val sube = pct >= 0
    val texto = "${if (sube) "+" else "−"}${formatearDecimal(abs(pct))} %"
    val descripcion = stringResource(
        if (sube) R.string.det_variacion_sube else R.string.det_variacion_baja,
        "${formatearDecimal(abs(pct))} %"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clearAndSetSemantics { contentDescription = descripcion }
    ) {
        Icon(
            if (sube) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(2.dp))
        Text(texto, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Seccion(titulo: String, icono: ImageVector? = null, contenido: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icono != null) {
                Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(
                titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.semantics { heading() }
            )
        }
        contenido()
    }
}

// ===========================================================================================
// Acciones
// ===========================================================================================

@Composable
private fun BarraAcciones(
    acciones: List<AccionRevision>,
    estado: EstadoRevision,
    habilitado: Boolean,
    onValidar: () -> Unit,
    onObservar: () -> Unit,
    onEditar: () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                if (AccionRevision.OBSERVAR in acciones) {
                    OutlinedButton(
                        onClick = onObservar,
                        enabled = habilitado,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                    ) {
                        Icon(Icons.Filled.Flag, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.det_observar), style = MaterialTheme.typography.titleSmall)
                    }
                }
                if (AccionRevision.VALIDAR in acciones) {
                    Button(
                        onClick = onValidar,
                        enabled = habilitado,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.det_validar), style = MaterialTheme.typography.titleSmall)
                    }
                }
                if (AccionRevision.EDITAR in acciones) {
                    Button(
                        onClick = onEditar,
                        enabled = habilitado,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(
                                if (estado == EstadoRevision.OBSERVADA) R.string.det_corregir else R.string.det_editar_borrador
                            ),
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }
        }
    }
}

/** Diálogo para observar: el comentario es obligatorio y el error se explica bajo el campo. */
@Composable
private fun DialogoObservar(onConfirmar: (String) -> Unit, onCancelar: () -> Unit) {
    var comentario by rememberSaveable { mutableStateOf("") }
    var intento by rememberSaveable { mutableStateOf(false) }
    val max = AccionesRevision.MAX_COMENTARIO
    val invalido = intento && !AccionesRevision.comentarioValido(comentario)

    AlertDialog(
        onDismissRequest = onCancelar,
        icon = { Icon(Icons.Filled.Flag, contentDescription = null) },
        title = { Text(stringResource(R.string.det_obs_dialogo_titulo)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.det_obs_dialogo_texto), style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = comentario,
                    onValueChange = { if (it.length <= max) comentario = it },
                    label = { Text(stringResource(R.string.det_obs_comentario)) },
                    isError = invalido,
                    supportingText = {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            // El error ocupa el espacio libre y el contador queda separado a la derecha.
                            Box(modifier = Modifier.weight(1f)) {
                                if (invalido) Text(stringResource(R.string.det_obs_error_vacio))
                            }
                            Text(
                                stringResource(R.string.det_obs_contador, comentario.length, max),
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                    },
                    minLines = 3,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    intento = true
                    if (AccionesRevision.comentarioValido(comentario)) onConfirmar(comentario)
                },
                modifier = Modifier.heightIn(min = 48.dp)
            ) { Text(stringResource(R.string.det_obs_confirmar)) }
        },
        dismissButton = {
            TextButton(onClick = onCancelar, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(R.string.muestra_cancelar))
            }
        }
    )
}

// ===========================================================================================
// Estados de la pantalla
// ===========================================================================================

@Composable
private fun Cargando() {
    val descripcion = stringResource(R.string.det_cargando)
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(modifier = Modifier.semantics { contentDescription = descripcion })
    }
}

@Composable
private fun MensajeCentro(icono: ImageVector, titulo: String, texto: String, onVolver: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(44.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text(titulo, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            texto,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        OutlinedButton(onClick = onVolver, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(stringResource(R.string.det_volver))
        }
    }
}

// ===========================================================================================
// Formato
// ===========================================================================================

private fun formatearFechaHora(millis: Long): String =
    SimpleDateFormat("d MMM yyyy, HH:mm", LOCALE_CL).format(Date(millis))

private fun formatearFechaCorta(millis: Long): String =
    SimpleDateFormat("d MMM yy", LOCALE_CL).format(Date(millis))

private fun formatearDecimal(valor: Double): String =
    NumberFormat.getNumberInstance(LOCALE_CL).apply {
        maximumFractionDigits = 1
        minimumFractionDigits = 0
    }.format(valor)
