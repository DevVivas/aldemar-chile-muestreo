package com.aldemar.aquamuestra.ui.theme.historial

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.NoPhotography
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.aldemar.aquamuestra.viewmodel.AppViewModelFactory
import com.aldemar.aquamuestra.viewmodel.FiltrosHistorial
import com.aldemar.aquamuestra.viewmodel.HistorialUiState
import com.aldemar.aquamuestra.viewmodel.HistorialViewModel
import com.aldemar.aquamuestra.viewmodel.OpcionesFiltro
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val LOCALE_CL: Locale = Locale.forLanguageTag("es-CL")

@Composable
fun HistorialScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: HistorialViewModel = viewModel(factory = AppViewModelFactory(context))
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.historial_titulo),
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = { BottomNavBar(navController = navController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        HistorialContenido(
            state = state,
            viewModel = viewModel,
            onAbrirMuestra = { id -> navController.navigate(AppRoutes.SampleDetail.createRoute(id)) },
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
private fun HistorialContenido(
    state: HistorialUiState,
    viewModel: HistorialViewModel,
    onAbrirMuestra: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var mostrarRango by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        // Barra de filtros: fija arriba, la lista se desplaza debajo.
        if (state.total > 0) {
            BarraFiltros(
                filtros = state.filtros,
                opciones = state.opciones,
                onEstado = viewModel::onEstadoChange,
                onCentro = viewModel::onCentroChange,
                onTren = viewModel::onTrenChange,
                onLinea = viewModel::onLineaChange,
                onAbrirFechas = { mostrarRango = true }
            )
            ResumenResultados(
                mostradas = state.muestras.size,
                total = state.total,
                hayFiltros = state.filtros.hayFiltros,
                onLimpiar = viewModel::limpiarFiltros
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }

        when {
            state.cargando -> Cargando()
            state.total == 0 -> EstadoVacio(
                icono = Icons.Filled.Inventory2,
                titulo = stringResource(R.string.hist_vacio_titulo),
                texto = stringResource(R.string.hist_vacio_texto)
            )
            state.muestras.isEmpty() -> EstadoVacio(
                icono = Icons.Filled.FilterAltOff,
                titulo = stringResource(R.string.hist_sin_resultados_titulo),
                texto = stringResource(R.string.hist_sin_resultados_texto),
                accion = stringResource(R.string.hist_limpiar_filtros),
                onAccion = viewModel::limpiarFiltros
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.muestras, key = { it.id }) { muestra ->
                    TarjetaMuestra(
                        muestra = muestra,
                        onClick = { onAbrirMuestra(muestra.id) },
                        // Al filtrar, las tarjetas entran, salen y se reacomodan con una animación suave.
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }

    if (mostrarRango) {
        DialogoRangoFechas(
            desdeUtc = state.filtros.desdeUtc,
            hastaUtc = state.filtros.hastaUtc,
            onAceptar = { desde, hasta ->
                viewModel.onRangoFechasChange(desde, hasta)
                mostrarRango = false
            },
            onQuitar = {
                viewModel.onRangoFechasChange(null, null)
                mostrarRango = false
            },
            onCancelar = { mostrarRango = false }
        )
    }
}

// ===========================================================================================
// Filtros
// ===========================================================================================

@Composable
private fun BarraFiltros(
    filtros: FiltrosHistorial,
    opciones: OpcionesFiltro,
    onEstado: (EstadoRevision?) -> Unit,
    onCentro: (String?) -> Unit,
    onTren: (String?) -> Unit,
    onLinea: (String?) -> Unit,
    onAbrirFechas: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 4.dp)
    ) {
        ChipMenu(
            etiqueta = stringResource(R.string.hist_filtro_estado),
            seleccion = filtros.estado,
            opciones = opciones.estados,
            texto = { textoEstado(it) },
            onSeleccion = onEstado
        )
        ChipMenu(
            etiqueta = stringResource(R.string.hist_filtro_centro),
            seleccion = filtros.centro,
            opciones = opciones.centros,
            texto = { it },
            onSeleccion = onCentro
        )
        ChipMenu(
            etiqueta = stringResource(R.string.hist_filtro_tren),
            seleccion = filtros.tren,
            opciones = opciones.trenes,
            texto = { it },
            onSeleccion = onTren
        )
        ChipMenu(
            etiqueta = stringResource(R.string.hist_filtro_linea),
            seleccion = filtros.linea,
            opciones = opciones.lineas,
            texto = { it },
            onSeleccion = onLinea
        )
        val rangoActivo = filtros.desdeUtc != null
        FilterChip(
            selected = rangoActivo,
            onClick = onAbrirFechas,
            label = {
                Text(
                    if (rangoActivo) {
                        stringResource(
                            R.string.hist_filtro_rango,
                            formatearDiaUtc(filtros.desdeUtc!!),
                            formatearDiaUtc(filtros.hastaUtc ?: filtros.desdeUtc)
                        )
                    } else {
                        stringResource(R.string.hist_filtro_fechas)
                    }
                )
            },
            leadingIcon = { Icon(Icons.Filled.DateRange, contentDescription = null, modifier = Modifier.size(18.dp)) }
        )
    }
}

/** Chip que abre un menú. Muestra el valor elegido; "Todos" quita ese filtro. */
@Composable
private fun <T> ChipMenu(
    etiqueta: String,
    seleccion: T?,
    opciones: List<T>,
    texto: @Composable (T) -> String,
    onSeleccion: (T?) -> Unit
) {
    var abierto by remember { mutableStateOf(false) }

    Box {
        FilterChip(
            selected = seleccion != null,
            onClick = { abierto = true },
            label = {
                Text(
                    text = seleccion?.let { texto(it) } ?: etiqueta,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 180.dp)
                )
            },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null, modifier = Modifier.size(18.dp)) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                selectedTrailingIconColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        )
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.hist_filtro_todos)) },
                onClick = { onSeleccion(null); abierto = false },
                trailingIcon = { if (seleccion == null) Icon(Icons.Filled.Check, contentDescription = null) }
            )
            HorizontalDivider()
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(texto(opcion)) },
                    onClick = { onSeleccion(opcion); abierto = false },
                    trailingIcon = { if (opcion == seleccion) Icon(Icons.Filled.Check, contentDescription = null) }
                )
            }
        }
    }
}

@Composable
private fun ResumenResultados(mostradas: Int, total: Int, hayFiltros: Boolean, onLimpiar: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(start = 20.dp, end = 8.dp)
    ) {
        Text(
            text = if (hayFiltros) {
                pluralStringResource(R.plurals.hist_contador_filtrado, total, mostradas, total)
            } else {
                pluralStringResource(R.plurals.hist_contador_total, total, total)
            },
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            // TalkBack anuncia el nuevo número cada vez que cambian los filtros.
            modifier = Modifier
                .weight(1f)
                .semantics { liveRegion = LiveRegionMode.Polite }
        )
        if (hayFiltros) {
            TextButton(onClick = onLimpiar) { Text(stringResource(R.string.hist_limpiar_filtros)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoRangoFechas(
    desdeUtc: Long?,
    hastaUtc: Long?,
    onAceptar: (Long, Long) -> Unit,
    onQuitar: () -> Unit,
    onCancelar: () -> Unit
) {
    val hoyUtc = remember { hoyMedianocheUtc() }
    val estado = rememberDateRangePickerState(
        initialSelectedStartDateMillis = desdeUtc,
        initialSelectedEndDateMillis = hastaUtc,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= hoyUtc
        }
    )
    DatePickerDialog(
        onDismissRequest = onCancelar,
        confirmButton = {
            TextButton(
                enabled = estado.selectedStartDateMillis != null,
                onClick = {
                    val desde = estado.selectedStartDateMillis ?: return@TextButton
                    // Si solo se eligió un día, el rango es ese mismo día.
                    onAceptar(desde, estado.selectedEndDateMillis ?: desde)
                }
            ) { Text(stringResource(R.string.muestra_aceptar)) }
        },
        dismissButton = {
            Row {
                if (desdeUtc != null) {
                    TextButton(onClick = onQuitar) { Text(stringResource(R.string.hist_limpiar_filtros)) }
                }
                TextButton(onClick = onCancelar) { Text(stringResource(R.string.muestra_cancelar)) }
            }
        }
    ) {
        DateRangePicker(
            state = estado,
            title = {
                Text(
                    stringResource(R.string.hist_rango_titulo),
                    modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp)
                )
            },
            modifier = Modifier.weight(1f)
        )
    }
}

// ===========================================================================================
// Tarjeta de muestra
// ===========================================================================================

@Composable
private fun TarjetaMuestra(muestra: MuestraEntity, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Fila 1: identificación y estado
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = muestra.id.take(8).uppercase(LOCALE_CL),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.weight(1f)
                )
                ChipEstado(muestra.estado)
            }

            // Fila 2: foto + dónde y cuándo
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Miniatura(muestra.fotoPath)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.hist_ubicacion, muestra.centro, muestra.tren, muestra.linea),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatearFechaHora(muestra.fechaHora),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Fila 3: los números del conteo
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.Bottom) {
                Dato(valor = muestra.cantidad.toString(), unidad = stringResource(R.string.hist_individuos))
                Dato(
                    valor = stringResource(R.string.hist_tramo_valor, muestra.tramoCm),
                    unidad = stringResource(R.string.hist_tramo)
                )
            }

            if (muestra.pendienteSync) EtiquetaPendiente()
        }
    }
}

@Composable
private fun Dato(valor: String, unidad: String) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(valor, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.width(6.dp))
        Text(
            unidad,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 2.dp)
        )
    }
}

@Composable
private fun Miniatura(fotoPath: String?) {
    val forma = RoundedCornerShape(12.dp)
    if (fotoPath != null) {
        AsyncImage(
            model = File(fotoPath),
            contentDescription = null, // decorativa: los datos de la tarjeta ya describen la muestra
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(64.dp)
                .clip(forma)
        )
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(64.dp)
                .clip(forma)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Icon(
                Icons.Filled.NoPhotography,
                contentDescription = stringResource(R.string.hist_sin_foto),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private data class EstiloEstado(val fondo: Color, val contenido: Color, val icono: ImageVector)

@Composable
private fun estiloEstado(estado: EstadoRevision): EstiloEstado {
    val c = MaterialTheme.colorScheme
    return when (estado) {
        EstadoRevision.BORRADOR -> EstiloEstado(c.surfaceContainerHighest, c.onSurfaceVariant, Icons.Filled.Edit)
        EstadoRevision.ENVIADA -> EstiloEstado(c.primaryContainer, c.onPrimaryContainer, Icons.AutoMirrored.Filled.Send)
        EstadoRevision.OBSERVADA -> EstiloEstado(c.tertiaryContainer, c.onTertiaryContainer, Icons.Filled.Flag)
        EstadoRevision.VALIDADA -> EstiloEstado(c.secondaryContainer, c.onSecondaryContainer, Icons.Filled.CheckCircle)
    }
}

@Composable
private fun textoEstado(estado: EstadoRevision): String = stringResource(
    when (estado) {
        EstadoRevision.BORRADOR -> R.string.hist_estado_borrador
        EstadoRevision.ENVIADA -> R.string.hist_estado_enviada
        EstadoRevision.OBSERVADA -> R.string.hist_estado_observada
        EstadoRevision.VALIDADA -> R.string.hist_estado_validada
    }
)

/** Chip de estado (no interactivo): color + icono + texto, para no depender solo del color. */
@Composable
private fun ChipEstado(estado: EstadoRevision) {
    val estilo = estiloEstado(estado)
    Surface(color = estilo.fondo, contentColor = estilo.contenido, shape = RoundedCornerShape(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp, end = 10.dp, top = 4.dp, bottom = 4.dp)
        ) {
            Icon(estilo.icono, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(textoEstado(estado), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun EtiquetaPendiente() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Filled.CloudUpload,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            stringResource(R.string.hist_pendiente_sync),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

// ===========================================================================================
// Estados de la pantalla
// ===========================================================================================

@Composable
private fun Cargando() {
    val descripcion = stringResource(R.string.hist_cargando)
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(modifier = Modifier.semantics { this.contentDescription = descripcion })
    }
}

@Composable
private fun EstadoVacio(
    icono: ImageVector,
    titulo: String,
    texto: String,
    accion: String? = null,
    onAccion: () -> Unit = {}
) {
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
            Icon(
                icono,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(44.dp)
            )
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
        if (accion != null) {
            Spacer(Modifier.height(20.dp))
            OutlinedButton(onClick = onAccion, modifier = Modifier.heightIn(min = 48.dp)) { Text(accion) }
        }
    }
}

// ===========================================================================================
// Formato de fechas
// ===========================================================================================

private fun formatearFechaHora(millis: Long): String =
    SimpleDateFormat("d MMM yyyy, HH:mm", LOCALE_CL).format(Date(millis))

/** Las fechas del filtro están en medianoche UTC: se formatean en UTC para no correr el día. */
private fun formatearDiaUtc(millisUtc: Long): String =
    SimpleDateFormat("d MMM", LOCALE_CL).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date(millisUtc))

private fun hoyMedianocheUtc(): Long {
    val local = java.util.Calendar.getInstance()
    return java.util.Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(local.get(java.util.Calendar.YEAR), local.get(java.util.Calendar.MONTH), local.get(java.util.Calendar.DAY_OF_MONTH))
    }.timeInMillis
}
