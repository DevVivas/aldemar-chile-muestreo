package com.aldemar.aquamuestra.ui.theme.inicio

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.aldemar.aquamuestra.R
import com.aldemar.aquamuestra.data.local.EstadoRevision
import com.aldemar.aquamuestra.data.local.MuestraEntity
import com.aldemar.aquamuestra.data.local.UserRole
import com.aldemar.aquamuestra.data.sync.SyncScheduler
import com.aldemar.aquamuestra.ui.theme.AppRoutes
import com.aldemar.aquamuestra.ui.theme.SessionManager
import com.aldemar.aquamuestra.ui.theme.ThemeManager
import com.aldemar.aquamuestra.ui.theme.components.AppTopBar
import com.aldemar.aquamuestra.ui.theme.components.BannerSincronizacion
import com.aldemar.aquamuestra.ui.theme.components.BottomNavBar
import com.aldemar.aquamuestra.ui.theme.components.ChipEstado
import com.aldemar.aquamuestra.ui.theme.components.TarjetaCondicionesMarinas
import com.aldemar.aquamuestra.viewmodel.AppViewModelFactory
import com.aldemar.aquamuestra.viewmodel.AvisoCierre
import com.aldemar.aquamuestra.viewmodel.CierreSesion
import com.aldemar.aquamuestra.viewmodel.InicioViewModel
import com.aldemar.aquamuestra.viewmodel.MarineViewModel
import com.aldemar.aquamuestra.viewmodel.SyncViewModel
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val LOCALE_CL: Locale = Locale.forLanguageTag("es-CL")

@Composable
fun InicioScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: InicioViewModel = viewModel(factory = AppViewModelFactory(context))
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val syncViewModel: SyncViewModel = viewModel(factory = AppViewModelFactory(context))
    val sync by syncViewModel.uiState.collectAsStateWithLifecycle()
    val marineViewModel: MarineViewModel = viewModel(factory = AppViewModelFactory(context))
    val marino by marineViewModel.uiState.collectAsStateWithLifecycle()

    val usuario = SessionManager.currentUser

    // Cierre de sesión confirmado: nada debe seguir sincronizando con las credenciales de este usuario.
    LaunchedEffect(state.cierre) {
        if (state.cierre == CierreSesion.Salir) {
            SyncScheduler.getInstance(context).cancelar()
            SessionManager.logout()
            viewModel.onSalidaHecha()
            // Vuelve al Login y borra la pila: "atrás" no puede regresar a la app.
            navController.navigate(AppRoutes.Login.ruta) {
                popUpTo(AppRoutes.Inicio.ruta) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.inicio_titulo),
                showShadow = false,
                actions = {
                    MenuOpciones(
                        onSincronizar = syncViewModel::sincronizarAhora,
                        onCerrarSesion = viewModel::solicitarCierre
                    )
                }
            )
        },
        bottomBar = { BottomNavBar(navController = navController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Saludo(nombre = usuario?.name.orEmpty(), rol = usuario?.role, modifier = Modifier.entrada(0))

            BannerSincronizacion(estado = sync.banner, onSincronizar = syncViewModel::sincronizarAhora)

            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.entrada(1)) {
                TituloSeccion(stringResource(R.string.inicio_acciones))
                AccionesPorRol(rol = usuario?.role, porRevisar = state.porRevisar, navController = navController)
            }

            UltimasMuestras(
                muestras = state.ultimas,
                cargando = state.cargando,
                onVerTodas = { navController.navigate(AppRoutes.Historial.ruta) },
                onAbrir = { id -> navController.navigate(AppRoutes.SampleDetail.createRoute(id)) },
                modifier = Modifier.entrada(2)
            )

            // Condiciones marinas (API externa Open-Meteo)
            TarjetaCondicionesMarinas(
                state = marino,
                onCentro = marineViewModel::recargar,
                onRecargar = { marineViewModel.recargar() },
                modifier = Modifier.entrada(3)
            )
        }
    }

    (state.cierre as? CierreSesion.Confirmar)?.let { confirmar ->
        DialogoCierre(
            estado = confirmar,
            onSincronizarYSalir = viewModel::sincronizarYSalir,
            onSalirIgual = viewModel::salirIgual,
            onCancelar = viewModel::cancelarCierre
        )
    }
}

// =============================================================================================
// Barra superior: menú con modo oscuro, sincronizar y cerrar sesión
// =============================================================================================

@Composable
private fun MenuOpciones(onSincronizar: () -> Unit, onCerrarSesion: () -> Unit) {
    var abierto by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { abierto = true }) {
            Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.cd_opciones))
        }
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            // Modo oscuro: el interruptor muestra el estado; tocar la fila lo cambia (se guarda en DataStore)
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_modo_oscuro)) },
                leadingIcon = { Icon(Icons.Filled.DarkMode, contentDescription = null) },
                trailingIcon = {
                    Switch(checked = ThemeManager.isDarkMode, onCheckedChange = { ThemeManager.isDarkMode = it })
                },
                onClick = { ThemeManager.isDarkMode = !ThemeManager.isDarkMode }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_sincronizar)) },
                leadingIcon = { Icon(Icons.Filled.Sync, contentDescription = null) },
                onClick = {
                    abierto = false
                    onSincronizar()
                }
            )
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_cerrar_sesion), color = MaterialTheme.colorScheme.error) },
                leadingIcon = {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                },
                onClick = {
                    abierto = false
                    onCerrarSesion()
                }
            )
        }
    }
}

// =============================================================================================
// Saludo y acciones
// =============================================================================================

@Composable
private fun Saludo(nombre: String, rol: UserRole?, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(R.string.inicio_hola, nombre),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics { heading() }
        )
        if (rol != null) ChipRol(rol)
    }
}

@Composable
private fun ChipRol(rol: UserRole) {
    val texto = stringResource(
        when (rol) {
            UserRole.OPERARIO -> R.string.inicio_rol_operario
            UserRole.SUPERVISOR -> R.string.inicio_rol_supervisor
            UserRole.ANALISTA -> R.string.inicio_rol_analista
        }
    )
    val descripcion = stringResource(R.string.inicio_cd_rol, texto)
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = descripcion }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 6.dp)
        ) {
            Icon(Icons.Filled.Badge, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(texto, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun AccionesPorRol(rol: UserRole?, porRevisar: Int, navController: NavController) {
    val irDashboard = { navController.navigate(AppRoutes.Dashboard.ruta) }
    val irHistorial = { navController.navigate(AppRoutes.Historial.ruta) }
    when (rol) {
        UserRole.OPERARIO -> {
            TarjetaAccion(
                icono = Icons.Filled.Add,
                titulo = stringResource(R.string.inicio_nueva),
                subtitulo = stringResource(R.string.inicio_nueva_sub),
                destacada = true,
                onClick = { navController.navigate(AppRoutes.NewSample.ruta) }
            )
            TarjetaAccion(
                icono = Icons.AutoMirrored.Filled.List,
                titulo = stringResource(R.string.inicio_mis_muestras),
                subtitulo = stringResource(R.string.inicio_mis_muestras_sub),
                onClick = irHistorial
            )
        }
        UserRole.SUPERVISOR -> {
            TarjetaAccion(
                icono = Icons.Filled.FactCheck,
                titulo = stringResource(R.string.inicio_por_revisar, porRevisar),
                subtitulo = if (porRevisar == 0) {
                    stringResource(R.string.inicio_por_revisar_cero)
                } else {
                    pluralStringResource(R.plurals.inicio_por_revisar_sub, porRevisar, porRevisar)
                },
                // Se destaca solo si hay trabajo pendiente
                destacada = porRevisar > 0,
                onClick = { navController.navigate(AppRoutes.Historial.filtrado(EstadoRevision.ENVIADA)) }
            )
            TarjetaAccion(
                icono = Icons.Filled.Dashboard,
                titulo = stringResource(R.string.inicio_dashboard),
                subtitulo = stringResource(R.string.inicio_dashboard_sub),
                onClick = irDashboard
            )
        }
        UserRole.ANALISTA -> {
            TarjetaAccion(
                icono = Icons.Filled.Dashboard,
                titulo = stringResource(R.string.inicio_dashboard),
                subtitulo = stringResource(R.string.inicio_dashboard_sub),
                destacada = true,
                onClick = irDashboard
            )
            TarjetaAccion(
                icono = Icons.AutoMirrored.Filled.List,
                titulo = stringResource(R.string.inicio_historial),
                subtitulo = stringResource(R.string.inicio_historial_sub),
                onClick = irHistorial
            )
            TarjetaAccion(
                icono = Icons.Filled.PictureAsPdf,
                titulo = stringResource(R.string.inicio_exportar),
                subtitulo = stringResource(R.string.inicio_exportar_sub),
                onClick = irDashboard
            )
        }
        null -> Unit
    }
}

/** Tarjeta de acción grande (toda la tarjeta es el botón, mínimo 72 dp de alto). */
@Composable
private fun TarjetaAccion(
    icono: ImageVector,
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit,
    destacada: Boolean = false
) {
    val c = MaterialTheme.colorScheme
    val fondo = if (destacada) c.primary else c.surfaceContainerLow
    val tinta = if (destacada) c.onPrimary else c.onSurface
    val tintaSuave = if (destacada) c.onPrimary.copy(alpha = 0.85f) else c.onSurfaceVariant
    val fondoIcono = if (destacada) c.onPrimary.copy(alpha = 0.15f) else c.secondaryContainer
    val tintaIcono = if (destacada) c.onPrimary else c.onSecondaryContainer

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = fondo, contentColor = tinta),
        modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = fondoIcono, shape = CircleShape, modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icono, contentDescription = null, tint = tintaIcono, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitulo, style = MaterialTheme.typography.bodyMedium, color = tintaSuave)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = tintaSuave)
        }
    }
}

// =============================================================================================
// Últimas muestras
// =============================================================================================

@Composable
private fun UltimasMuestras(
    muestras: List<MuestraEntity>,
    cargando: Boolean,
    onVerTodas: () -> Unit,
    onAbrir: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            TituloSeccion(stringResource(R.string.inicio_ultimas), Modifier.weight(1f))
            if (muestras.isNotEmpty()) {
                TextButton(onClick = onVerTodas, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.inicio_ver_todas))
                }
            }
        }
        when {
            cargando -> Unit
            muestras.isEmpty() -> Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Icon(Icons.Filled.Inventory2, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(12.dp))
                Text(
                    stringResource(R.string.inicio_sin_muestras),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            else -> muestras.forEach { m -> MiniTarjeta(m, onClick = { onAbrir(m.id) }) }
        }
    }
}

@Composable
private fun MiniTarjeta(m: MuestraEntity, onClick: () -> Unit) {
    OutlinedCard(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp)
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    m.id.take(8).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                if (m.pendienteSync) {
                    Icon(
                        Icons.Filled.CloudUpload,
                        contentDescription = stringResource(R.string.hist_pendiente_sync),
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                }
                ChipEstado(m.estado)
            }
            Text(
                "${m.centro} · ${m.tren} · ${m.linea}",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "${fecha(m.fechaHora)}  ·  " + stringResource(R.string.inicio_individuos, entero(m.cantidad)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TituloSeccion(texto: String, modifier: Modifier = Modifier) {
    Text(
        texto,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier.semantics { heading() }
    )
}

// =============================================================================================
// Diálogo: cerrar sesión con muestras sin sincronizar
// =============================================================================================

@Composable
private fun DialogoCierre(
    estado: CierreSesion.Confirmar,
    onSincronizarYSalir: () -> Unit,
    onSalirIgual: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        // Mientras sincroniza no se cierra tocando afuera (hay que esperar el resultado)
        onDismissRequest = { if (!estado.sincronizando) onCancelar() },
        icon = { Icon(Icons.Filled.CloudUpload, contentDescription = null) },
        title = { Text(pluralStringResource(R.plurals.cierre_titulo, estado.pendientes, estado.pendientes)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.cierre_texto))
                when (estado.aviso) {
                    AvisoCierre.SIN_CONEXION -> Aviso(stringResource(R.string.cierre_sin_conexion))
                    AvisoCierre.QUEDAN_PENDIENTES ->
                        Aviso(pluralStringResource(R.plurals.cierre_quedan, estado.pendientes, estado.pendientes))
                    null -> Unit
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSincronizarYSalir,
                enabled = !estado.sincronizando,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                if (estado.sincronizando) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.cierre_sincronizando))
                } else {
                    Text(stringResource(R.string.cierre_sincronizar_salir))
                }
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onCancelar, enabled = !estado.sincronizando, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.cierre_cancelar))
                }
                TextButton(onClick = onSalirIgual, enabled = !estado.sincronizando, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.cierre_salir_igual), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    )
}

@Composable
private fun Aviso(texto: String) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(texto, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(12.dp))
    }
}

// =============================================================================================
// Animación de entrada y formato
// =============================================================================================

/** Entrada discreta: cada bloque aparece con un fundido y sube 16 dp, escalonado 60 ms. */
@Composable
private fun Modifier.entrada(orden: Int): Modifier {
    val progreso = remember { Animatable(0f) }
    val desplazamiento = with(LocalDensity.current) { 16.dp.toPx() }
    LaunchedEffect(Unit) {
        delay(orden * 60L)
        progreso.animateTo(1f, tween(durationMillis = 280, easing = FastOutSlowInEasing))
    }
    return graphicsLayer {
        alpha = progreso.value
        translationY = (1f - progreso.value) * desplazamiento
    }
}

private fun entero(n: Int): String = NumberFormat.getIntegerInstance(LOCALE_CL).format(n)

private fun fecha(millis: Long): String = SimpleDateFormat("d MMM, HH:mm", LOCALE_CL).format(Date(millis))

