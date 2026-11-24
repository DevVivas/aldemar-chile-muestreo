package com.aldemar.aquamuestra.ui.theme.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.aldemar.aquamuestra.R
import com.aldemar.aquamuestra.data.local.CentrosCatalogo
import com.aldemar.aquamuestra.data.local.EstadoRevision
import com.aldemar.aquamuestra.ui.theme.SessionManager
import com.aldemar.aquamuestra.ui.theme.components.AppTopBar
import com.aldemar.aquamuestra.ui.theme.components.BotonExportarPdf
import com.aldemar.aquamuestra.ui.theme.components.BottomNavBar
import com.aldemar.aquamuestra.ui.theme.components.TarjetaCondicionesMarinas
import com.aldemar.aquamuestra.ui.theme.components.estiloEstado
import com.aldemar.aquamuestra.ui.theme.components.rememberExportadorPdf
import com.aldemar.aquamuestra.ui.theme.components.textoEstado
import com.aldemar.aquamuestra.ui.theme.components.textoGeneradoPor
import com.aldemar.aquamuestra.viewmodel.AppViewModelFactory
import com.aldemar.aquamuestra.viewmodel.DashboardUiState
import com.aldemar.aquamuestra.viewmodel.DashboardViewModel
import com.aldemar.aquamuestra.viewmodel.Indicadores
import com.aldemar.aquamuestra.viewmodel.LineaCultivo
import com.aldemar.aquamuestra.viewmodel.MarineViewModel
import com.aldemar.aquamuestra.viewmodel.PeriodoDashboard
import com.aldemar.aquamuestra.viewmodel.PuntoLinea
import com.aldemar.aquamuestra.viewmodel.ResumenCentro
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnModel
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val LOCALE_CL: Locale = Locale.forLanguageTag("es-CL")

/** Etiquetas del eje X de los gráficos (nombres de centro o fechas). */
private val EtiquetasX = ExtraStore.Key<List<String>>()

private enum class MetricaCentro { MUESTRAS, INDIVIDUOS }
private enum class MetricaLinea { CANTIDAD, DENSIDAD }

@Composable
fun DashboardScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: DashboardViewModel = viewModel(factory = AppViewModelFactory(context))
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val marineViewModel: MarineViewModel = viewModel(factory = AppViewModelFactory(context))
    val marino by marineViewModel.uiState.collectAsStateWithLifecycle()

    val snackbar = remember { SnackbarHostState() }
    val exportador = rememberExportadorPdf(snackbar)
    val generadoPor = textoGeneradoPor()

    Scaffold(
        topBar = {
            AppTopBar(title = stringResource(R.string.dashboard_titulo), onBackClick = { navController.popBackStack() })
        },
        bottomBar = { BottomNavBar(navController = navController) },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Encabezado(
                nombre = SessionManager.currentUser?.name.orEmpty(),
                puedeExportar = SessionManager.puedeExportarReportes,
                generandoPdf = exportador.generando,
                onExportar = { exportador.exportar(viewModel.prepararReporte(generadoPor)) }
            )

            Filtros(
                state = state,
                onPeriodo = viewModel::onPeriodoChange,
                onCentro = viewModel::onCentroChange
            )

            // Al cambiar los filtros el contenido cambia con un fundido corto (sin saltos bruscos).
            AnimatedContent(
                targetState = when {
                    state.cargando -> Contenido.CARGANDO
                    state.vacio -> Contenido.VACIO
                    else -> Contenido.DATOS
                },
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(120)) },
                label = "contenido_dashboard"
            ) { contenido ->
                when (contenido) {
                    Contenido.CARGANDO -> Cargando()
                    Contenido.VACIO -> Vacio()
                    Contenido.DATOS -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        TarjetasKpi(state.indicadores)
                        GraficoPorCentro(state.indicadores.porCentro)
                        GraficoEstados(state.indicadores)
                        GraficoEvolucion(state.indicadores, onLinea = viewModel::onLineaChange)
                    }
                }
            }

            // Se mantiene la tarjeta de condiciones marinas (Open-Meteo).
            TarjetaCondicionesMarinas(
                state = marino,
                onCentro = marineViewModel::recargar,
                onRecargar = { marineViewModel.recargar() }
            )
        }
    }
}

private enum class Contenido { CARGANDO, VACIO, DATOS }

// =============================================================================================
// Encabezado y filtros
// =============================================================================================

@Composable
private fun Encabezado(nombre: String, puedeExportar: Boolean, generandoPdf: Boolean, onExportar: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.dash_hola, nombre),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(end = 8.dp).semantics { heading() }
        )
        if (puedeExportar) BotonExportarPdf(generando = generandoPdf, onClick = onExportar)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Filtros(
    state: DashboardUiState,
    onPeriodo: (PeriodoDashboard) -> Unit,
    onCentro: (String?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val periodos = listOf(
            PeriodoDashboard.SIETE_DIAS to stringResource(R.string.dash_periodo_7),
            PeriodoDashboard.TREINTA_DIAS to stringResource(R.string.dash_periodo_30),
            PeriodoDashboard.TODO to stringResource(R.string.dash_periodo_todo)
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            periodos.forEachIndexed { i, (periodo, texto) ->
                SegmentedButton(
                    selected = state.filtros.periodo == periodo,
                    onClick = { onPeriodo(periodo) },
                    shape = SegmentedButtonDefaults.itemShape(i, periodos.size)
                ) { Text(texto) }
            }
        }

        var abierto by remember { mutableStateOf(false) }
        val todos = stringResource(R.string.dash_todos_centros)
        Box {
            FilterChip(
                selected = state.filtros.centro != null,
                onClick = { abierto = true },
                label = {
                    Text(
                        stringResource(R.string.dash_filtro_centro, state.filtros.centro ?: todos),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = { Icon(Icons.Filled.Place, contentDescription = null, modifier = Modifier.size(18.dp)) },
                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) }
            )
            DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
                DropdownMenuItem(text = { Text(todos) }, onClick = { onCentro(null); abierto = false })
                state.centros.forEach { centro ->
                    DropdownMenuItem(text = { Text(centro) }, onClick = { onCentro(centro); abierto = false })
                }
            }
        }
    }
}

// =============================================================================================
// KPI
// =============================================================================================

@Composable
private fun TarjetasKpi(ind: Indicadores) {
    val sinDato = stringResource(R.string.dash_sin_dato)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Kpi(Icons.Filled.Tag, stringResource(R.string.dash_kpi_muestras), entero(ind.totalMuestras), null, Modifier.weight(1f))
            Kpi(Icons.Filled.Pets, stringResource(R.string.dash_kpi_individuos), entero(ind.totalIndividuos), stringResource(R.string.dash_kpi_individuos_ayuda), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Kpi(
                Icons.Filled.Straighten,
                stringResource(R.string.dash_kpi_densidad),
                ind.densidadPromedio?.let(::decimal) ?: sinDato,
                stringResource(R.string.dash_kpi_densidad_unidad),
                Modifier.weight(1f)
            )
            Kpi(
                Icons.Filled.PendingActions,
                stringResource(R.string.dash_kpi_pendientes),
                entero(ind.pendientesRevision),
                stringResource(R.string.dash_kpi_pendientes_ayuda),
                Modifier.weight(1f),
                destacado = ind.pendientesRevision > 0
            )
        }
        if (ind.pendientesSync > 0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.CloudUpload, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(R.string.dash_pendientes_sync, ind.pendientesSync),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun Kpi(
    icono: ImageVector,
    titulo: String,
    valor: String,
    detalle: String?,
    modifier: Modifier = Modifier,
    destacado: Boolean = false
) {
    val c = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.semantics(mergeDescendants = true) {},
        shape = RoundedCornerShape(16.dp),
        color = if (destacado) c.tertiaryContainer else c.surfaceContainerLow,
        contentColor = if (destacado) c.onTertiaryContainer else c.onSurface
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icono, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(titulo, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(6.dp))
            // El número cambia con un fundido corto cuando cambian los filtros
            AnimatedContent(targetState = valor, label = "kpi_$titulo") { v ->
                Text(v, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
            }
            if (detalle != null) {
                Text(detalle, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

// =============================================================================================
// Gráficos
// =============================================================================================

@Composable
private fun TarjetaGrafico(icono: ImageVector, titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    OutlinedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp).animateContentSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    titulo,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.semantics { heading() }
                )
            }
            contenido()
        }
    }
}

/** Selector de 2 opciones (una sola métrica por gráfico: nunca dos ejes Y). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> SelectorMetrica(opciones: List<Pair<T, String>>, elegida: T, onElegir: (T) -> Unit) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        opciones.forEachIndexed { i, (valor, texto) ->
            SegmentedButton(
                selected = valor == elegida,
                onClick = { onElegir(valor) },
                shape = SegmentedButtonDefaults.itemShape(i, opciones.size),
                icon = {}
            ) { Text(texto, maxLines = 1) }
        }
    }
}

@Composable
private fun GraficoPorCentro(porCentro: List<ResumenCentro>) {
    var metrica by rememberSaveable { mutableStateOf(MetricaCentro.MUESTRAS) }
    val nombreMetrica = stringResource(
        if (metrica == MetricaCentro.MUESTRAS) R.string.dash_metrica_muestras else R.string.dash_metrica_individuos
    )
    val valores = porCentro.map { if (metrica == MetricaCentro.MUESTRAS) it.muestras else it.individuos }
    val nombres = porCentro.map { CentrosCatalogo.buscar(it.centro)?.nombreCorto ?: it.centro.take(12) }

    TarjetaGrafico(Icons.Filled.BarChart, stringResource(R.string.dash_por_centro)) {
        SelectorMetrica(
            opciones = listOf(
                MetricaCentro.MUESTRAS to stringResource(R.string.dash_metrica_muestras),
                MetricaCentro.INDIVIDUOS to stringResource(R.string.dash_metrica_individuos)
            ),
            elegida = metrica,
            onElegir = { metrica = it }
        )

        val producer = remember { CartesianChartModelProducer() }
        LaunchedEffect(valores, nombres) {
            producer.runTransaction {
                columnModel { series(valores) }
                extras { it[EtiquetasX] = nombres }
            }
        }
        val color = MaterialTheme.colorScheme.primary
        val tinta = MaterialTheme.colorScheme.onSurface
        val tintaSuave = MaterialTheme.colorScheme.onSurfaceVariant
        val etiquetaX = remember { CartesianValueFormatter { ctx, x, _ -> ctx.model.extraStore[EtiquetasX].getOrElse(x.toInt()) { "" } } }
        val resumen = porCentro.zip(valores).joinToString { (c, v) -> "${c.centro}: $v" }
        // Eje Y en 4 pasos ENTEROS (no hay "media muestra") con aire sobre la barra más alta
        val paso = kotlin.math.ceil((valores.maxOrNull() ?: 0) * 1.25 / 4).coerceAtLeast(1.0)
        val maximo = paso * 4

        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(
                    columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                        rememberLineComponent(
                            fill = Fill(color),
                            thickness = 28.dp,
                            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                        )
                    ),
                    // Valor escrito sobre cada barra: no hay que adivinarlo con el eje
                    dataLabel = rememberTextComponent(style = TextStyle(color = tinta, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)),
                    dataLabelValueFormatter = remember { CartesianValueFormatter.decimal(decimalCount = 0, thousandsSeparator = ".") },
                    // 25 % de aire sobre la barra más alta para que su número quede afuera, legible
                    rangeProvider = remember(maximo) { CartesianLayerRangeProvider.fixed(minY = 0.0, maxY = maximo) }
                ),
                startAxis = VerticalAxis.rememberStart(
                    label = rememberTextComponent(style = TextStyle(color = tintaSuave, fontSize = 11.sp), margins = Insets(4.dp)),
                    valueFormatter = remember { CartesianValueFormatter.decimal(decimalCount = 0, thousandsSeparator = ".") },
                    itemPlacer = remember(paso) { VerticalAxis.ItemPlacer.step(step = { paso }) },
                    line = null,
                    tick = null
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    label = rememberTextComponent(style = TextStyle(color = tinta, fontSize = 12.sp), margins = Insets(4.dp)),
                    valueFormatter = etiquetaX,
                    guideline = null
                )
            ),
            modelProducer = producer,
            scrollState = rememberVicoScrollState(scrollEnabled = false),
            zoomState = rememberVicoZoomState(zoomEnabled = false, initialZoom = Zoom.Content),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clearAndSetSemantics {
                    contentDescription = "Gráfico de barras: $nombreMetrica por centro. $resumen"
                }
        )
    }
}

@Composable
private fun GraficoEstados(ind: Indicadores) {
    TarjetaGrafico(Icons.Filled.Inventory2, stringResource(R.string.dash_estados)) {
        val maximo = (ind.porEstado.values.maxOrNull() ?: 0).coerceAtLeast(1)
        EstadoRevision.entries.forEach { estado ->
            FilaEstado(estado, ind.porEstado[estado] ?: 0, maximo, ind.totalMuestras)
        }
    }
}

/** Una barra por estado: icono + nombre + número (+ %), con los mismos colores que el chip de estado. */
@Composable
private fun FilaEstado(estado: EstadoRevision, cantidad: Int, maximo: Int, total: Int) {
    val estilo = estiloEstado(estado)
    val fraccion by animateFloatAsState(cantidad.toFloat() / maximo, tween(400), label = "barra_${estado.name}")
    val porcentaje = if (total == 0) 0 else Math.round(cantidad * 100f / total)
    val nombre = textoEstado(estado)

    Column(Modifier.semantics(mergeDescendants = true) {}) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(28.dp).background(estilo.fondo, CircleShape),
                contentAlignment = Alignment.Center
            ) { Icon(estilo.icono, contentDescription = null, tint = estilo.contenido, modifier = Modifier.size(16.dp)) }
            Spacer(Modifier.width(10.dp))
            Text(nombre, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text("$cantidad", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "  ($porcentaje %)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier
                .padding(start = 38.dp)
                .fillMaxWidth()
                .height(10.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(4.dp))
        ) {
            if (cantidad > 0) {
                Box(
                    Modifier
                        .fillMaxWidth(fraccion)
                        .height(10.dp)
                        .background(estilo.fondo, RoundedCornerShape(4.dp))
                        .border(1.dp, estilo.contenido.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

@Composable
private fun GraficoEvolucion(ind: Indicadores, onLinea: (LineaCultivo) -> Unit) {
    val activa = ind.lineaActiva ?: return
    var metrica by rememberSaveable { mutableStateOf(MetricaLinea.CANTIDAD) }

    TarjetaGrafico(Icons.AutoMirrored.Filled.TrendingUp, stringResource(R.string.dash_evolucion)) {
        SelectorLinea(ind.lineas, activa, onLinea)
        SelectorMetrica(
            opciones = listOf(
                MetricaLinea.CANTIDAD to stringResource(R.string.dash_metrica_cantidad),
                MetricaLinea.DENSIDAD to stringResource(R.string.dash_metrica_densidad)
            ),
            elegida = metrica,
            onElegir = { metrica = it }
        )

        val puntos = ind.evolucion
        val valores = puntos.map { if (metrica == MetricaLinea.CANTIDAD) it.cantidad.toDouble() else it.densidad ?: 0.0 }
        val fechas = puntos.map { fechaCorta(it.fechaHora) }
        val formato: (Double) -> String = { if (metrica == MetricaLinea.CANTIDAD) entero(it.toInt()) else decimal(it) }

        if (puntos.size >= 2) {
            LineaVico(valores, fechas, decimales = if (metrica == MetricaLinea.CANTIDAD) 0 else 1, descripcion =
                "Gráfico de líneas de ${if (metrica == MetricaLinea.CANTIDAD) "cantidad" else "densidad"} en ${activa.etiqueta}. " +
                    puntos.indices.joinToString { "${fechas[it]}: ${formato(valores[it])}" })
            Text(
                stringResource(
                    R.string.dash_evolucion_resumen,
                    puntos.size,
                    formato(valores.last()),
                    fechaCorta(puntos.last().fechaHora),
                    formato(valores.min()),
                    formato(valores.max())
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            // Con un solo punto no hay "evolución": se dice el dato en texto.
            Text(
                stringResource(R.string.dash_evolucion_uno, formato(valores.first()), fechaCorta(puntos.first().fechaHora)),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun SelectorLinea(lineas: List<LineaCultivo>, activa: LineaCultivo, onLinea: (LineaCultivo) -> Unit) {
    var abierto by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { abierto = true }, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.dash_elegir_linea),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    activa.etiqueta,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            lineas.forEach { l ->
                DropdownMenuItem(text = { Text(l.etiqueta) }, onClick = { onLinea(l); abierto = false })
            }
        }
    }
}

@Composable
private fun LineaVico(valores: List<Double>, fechas: List<String>, decimales: Int, descripcion: String) {
    val producer = remember { CartesianChartModelProducer() }
    LaunchedEffect(valores, fechas) {
        producer.runTransaction {
            lineModel { series(valores) }
            extras { it[EtiquetasX] = fechas }
        }
    }
    val color = MaterialTheme.colorScheme.primary
    val tinta = MaterialTheme.colorScheme.onSurface
    val tintaSuave = MaterialTheme.colorScheme.onSurfaceVariant
    val etiquetaX = remember { CartesianValueFormatter { ctx, x, _ -> ctx.model.extraStore[EtiquetasX].getOrElse(x.toInt()) { "" } } }
    val formatoY = remember(decimales) { CartesianValueFormatter.decimal(decimalCount = decimales, decimalSeparator = ",", thousandsSeparator = ".") }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        fill = remember(color) { LineCartesianLayer.LineFill.single(Fill(color)) },
                        stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 2.dp),
                        pointProvider = LineCartesianLayer.PointProvider.single(
                            LineCartesianLayer.Point(rememberShapeComponent(Fill(color), CircleShape), 8.dp)
                        )
                    )
                )
            ),
            startAxis = VerticalAxis.rememberStart(
                label = rememberTextComponent(style = TextStyle(color = tintaSuave, fontSize = 11.sp), margins = Insets(4.dp)),
                valueFormatter = formatoY,
                line = null,
                tick = null
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                label = rememberTextComponent(style = TextStyle(color = tintaSuave, fontSize = 11.sp), margins = Insets(4.dp)),
                valueFormatter = etiquetaX,
                guideline = null,
                // Con muchas fechas se muestra una de cada dos para que no se encimen
                itemPlacer = remember(fechas.size) { HorizontalAxis.ItemPlacer.aligned(spacing = { if (fechas.size > 6) 2 else 1 }) }
            ),
            // Al tocar un punto se muestra su valor
            marker = rememberDefaultCartesianMarker(
                label = rememberTextComponent(
                    style = TextStyle(color = MaterialTheme.colorScheme.inverseOnSurface, fontSize = 12.sp),
                    padding = Insets(6.dp),
                    background = rememberShapeComponent(Fill(MaterialTheme.colorScheme.inverseSurface), RoundedCornerShape(6.dp))
                )
            )
        ),
        modelProducer = producer,
        scrollState = rememberVicoScrollState(scrollEnabled = false),
        zoomState = rememberVicoZoomState(zoomEnabled = false, initialZoom = Zoom.Content),
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clearAndSetSemantics { contentDescription = descripcion }
    )
}

// =============================================================================================
// Estados de la pantalla
// =============================================================================================

@Composable
private fun Cargando() {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CircularProgressIndicator()
        Text(stringResource(R.string.dash_cargando), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Vacio() {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            Icons.Filled.FilterAltOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )
        Text(
            stringResource(R.string.dash_vacio_titulo),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            stringResource(R.string.dash_vacio_texto),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// =============================================================================================
// Formato
// =============================================================================================

private fun entero(n: Int): String = NumberFormat.getIntegerInstance(LOCALE_CL).format(n)

private fun decimal(d: Double): String =
    NumberFormat.getNumberInstance(LOCALE_CL).apply { maximumFractionDigits = 1; minimumFractionDigits = 1 }.format(d)

private fun fechaCorta(millis: Long): String = SimpleDateFormat("dd/MM", LOCALE_CL).format(Date(millis))

