package com.aldemar.aquamuestra.ui.screens

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Waves
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aldemar.aquamuestra.ui.components.dashboard.BarraInferiorDashboard
import com.aldemar.aquamuestra.ui.components.dashboard.DashboardTopBar
import com.aldemar.aquamuestra.ui.components.dashboard.HistorialCard
import com.aldemar.aquamuestra.ui.components.dashboard.MuestrasHoyCard
import com.aldemar.aquamuestra.ui.components.dashboard.NuevaMuestraBanner
import com.aldemar.aquamuestra.ui.components.dashboard.RevisionesCard
import com.aldemar.aquamuestra.ui.components.dashboard.SyncCard
import com.aldemar.aquamuestra.ui.components.dashboard.TelemetriaCard
import com.aldemar.aquamuestra.ui.components.dashboard.TituloSeccion
import com.aldemar.aquamuestra.ui.components.dashboard.UltimaMuestraCard
import com.aldemar.aquamuestra.ui.components.dashboard.UserCard
import com.aldemar.aquamuestra.ui.theme.AquaBackground
import com.aldemar.aquamuestra.ui.theme.AquaMuestraTheme
import com.aldemar.aquamuestra.ui.theme.AquaSurface
import com.aldemar.aquamuestra.ui.theme.AquaTeal
import com.aldemar.aquamuestra.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    nombre: String = "Operador Demo",
    rol: String = "Operador de Muestreo",
    puedeCrear: Boolean = true,
    onNuevaMuestra: () -> Unit = {},
    onHistorial: () -> Unit = {},
    onCerrarSesion: () -> Unit = {},
    vm: DashboardViewModel = viewModel()
) {

    // 24. Compose observa permanentemente nuestro StateFlow.
    val state by vm.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // 25. SnackbarHostState permite mostrar mensajes en pantalla.
    val snackbar = remember { SnackbarHostState() }

    // 26. Al abrir la pantalla consultamos la conexión y la fecha del dispositivo.
    LaunchedEffect(Unit) {
        vm.mostrarInternet(hayInternet(context))
        vm.mostrarFecha(fechaActual())
    }

    // 27. Cuando el ViewModel entrega un mensaje, lo mostramos y luego lo limpiamos.
    LaunchedEffect(state.mensaje) {
        state.mensaje?.let { mensaje ->
            snackbar.showSnackbar(mensaje)
            vm.limpiarMensaje()
        }
    }

    // 28. Scaffold organiza la estructura de la pantalla.
    Scaffold(
        containerColor = AquaBackground,
        topBar = {
            DashboardTopBar(
                centro = state.centro,
                internet = state.internet,
                onInternet = { vm.mostrarInternet(hayInternet(context)) },
                onPerfil = onCerrarSesion
            )
        },
        bottomBar = {
            BarraInferiorDashboard(onHistorial = onHistorial)
        },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            if (puedeCrear) {
                FloatingActionButton(
                    onClick = onNuevaMuestra,
                    containerColor = AquaTeal,
                    contentColor = AquaSurface,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Nueva muestra"
                    )
                }
            }
        }
    ) { padding ->

        // 29. Column organiza las secciones y permite desplazarse.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            UserCard(
                nombre = nombre,
                detalle = "$rol • ${state.turno}",
                registrosOffline = state.registrosOffline
            )

            TituloSeccion(
                icono = Icons.Outlined.Waves,
                titulo = "Resumen Operativo",
                extra = "Hoy, ${state.fecha}"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MuestrasHoyCard(
                    hoy = state.muestrasHoy,
                    variacion = state.variacion,
                    ayer = state.muestrasAyer,
                    modifier = Modifier.weight(1f)
                )
                RevisionesCard(
                    validadas = state.validadas,
                    pendientes = state.pendientes,
                    modifier = Modifier.weight(1f)
                )
            }

            if (puedeCrear) {
                NuevaMuestraBanner(onClick = onNuevaMuestra)
            }

            UltimaMuestraCard(muestra = state.ultimaMuestra)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HistorialCard(
                    total = state.totalHistorial,
                    onClick = onHistorial,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                SyncCard(
                    estadoSync = state.estadoSync,
                    onSync = {
                        vm.mostrarInternet(hayInternet(context))
                        vm.sincronizar()
                    },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }

            TelemetriaCard(telemetria = state.telemetria)

            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

// Consulta si el dispositivo tiene conexión a Internet.
private fun hayInternet(context: Context): Boolean {

    val gestor = context.getSystemService(
        Context.CONNECTIVITY_SERVICE
    ) as ConnectivityManager

    val red = gestor.activeNetwork

    val capacidades = gestor.getNetworkCapabilities(red)

    return capacidades?.hasCapability(
        NetworkCapabilities.NET_CAPABILITY_VALIDATED
    ) == true
}

// Obtiene la fecha actual del dispositivo.
private fun fechaActual(): String {
    val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formato.format(Date())
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenPreview() {
    AquaMuestraTheme { HomeScreen() }
}
