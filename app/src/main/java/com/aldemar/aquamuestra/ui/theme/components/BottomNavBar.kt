package com.aldemar.aquamuestra.ui.theme.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.aldemar.aquamuestra.R
import com.aldemar.aquamuestra.ui.theme.AppRoutes
import com.aldemar.aquamuestra.ui.theme.SessionManager

/**
 * Navega a una pestaña de la barra inferior.
 *
 * Se vuelve a Inicio (raíz) y se descarta todo lo que esté encima (Historial, Detalle...).
 * NO se usa saveState/restoreState: antes eso restauraba la pila guardada
 * (Historial -> SampleDetail) y por eso "Inicio" terminaba mostrando el detalle.
 */
private fun NavController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(AppRoutes.Inicio.ruta) { inclusive = false }
        launchSingleTop = true
    }
}

@Composable
fun BottomNavBar(navController: NavController) {
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route

    val colores = MaterialTheme.colorScheme
    val lineColor = colores.outlineVariant

    // Indicador con el color secundario de la marca; texto e icono con contraste AA en ambos modos
    val itemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = colores.onSecondaryContainer,
        selectedTextColor = colores.onSurface,
        indicatorColor = colores.secondaryContainer,
        unselectedIconColor = colores.onSurfaceVariant,
        unselectedTextColor = colores.onSurfaceVariant
    )

    NavigationBar(
        containerColor = colores.surface,
        tonalElevation = 0.dp,
        modifier = Modifier.drawBehind {
            drawLine(
                color = lineColor,
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = 1.dp.toPx()
            )
        }
    ) {
        // Opción: INICIO
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_inicio), fontWeight = FontWeight.Medium) },
            selected = currentRoute == AppRoutes.Inicio.ruta,
            onClick = {
                // Si ya estás en Inicio no hace nada; desde cualquier otra pantalla vuelve a InicioScreen
                if (currentRoute != AppRoutes.Inicio.ruta) {
                    navController.navigateToTab(AppRoutes.Inicio.ruta)
                }
            },
            colors = itemColors
        )

        // Opción: HISTORIAL
        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_historial), fontWeight = FontWeight.Medium) },
            // Historial también se marca como activo mientras ves el detalle de una muestra
            selected = currentRoute == AppRoutes.Historial.PATRON ||
                    currentRoute == AppRoutes.SampleDetail.ruta,
            onClick = {
                if (currentRoute != AppRoutes.Historial.PATRON) {
                    navController.navigateToTab(AppRoutes.Historial.ruta)
                }
            },
            colors = itemColors
        )

        // Opción: DASHBOARD (supervisor y analista)
        if (SessionManager.puedeVerDashboard) {
            NavigationBarItem(
                icon = { Icon(Icons.Filled.Dashboard, contentDescription = null) },
                label = { Text(stringResource(R.string.nav_dashboard), fontWeight = FontWeight.Medium) },
                selected = currentRoute == AppRoutes.Dashboard.ruta,
                onClick = {
                    if (currentRoute != AppRoutes.Dashboard.ruta) {
                        navController.navigateToTab(AppRoutes.Dashboard.ruta)
                    }
                },
                colors = itemColors
            )
        }
    }
}
