package com.aldemar.aquamuestra.ui.theme.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
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
import com.aldemar.aquamuestra.ui.theme.*

private fun NavController.navigateToTab(route: String) {
    navigate(route) {
        // Vuelve a Inicio (raíz) y borra todo lo que esté encima (Historial, Detalle...)
        popUpTo(AppRoutes.Inicio.ruta) { inclusive = false }
        launchSingleTop = true
        // SIN saveState / restoreState: así no se restaura SampleDetail
    }
}

@Composable
fun BottomNavBar(navController: NavController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val bg = ThemeManager.backgroundColor
    val border = ThemeManager.borderColor
    val selectedContent = ThemeManager.textColor
    val unselectedContent = ThemeManager.secondaryTextColor
    val indicator = ThemeManager.surfaceColor

    val itemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = selectedContent,
        selectedTextColor = selectedContent,
        indicatorColor = if (ThemeManager.isDarkMode) ColorDarkBorder else ColorFog,
        unselectedIconColor = unselectedContent,
        unselectedTextColor = unselectedContent
    )

    NavigationBar(
        containerColor = bg,
        tonalElevation = 0.dp,
        modifier = Modifier.drawBehind {
            drawLine(
                color = border,
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = 1.dp.toPx()
            )
        }
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_inicio), fontWeight = FontWeight.Medium) },
            selected = currentRoute == AppRoutes.Inicio.ruta,
            onClick = {
                if (currentRoute != AppRoutes.Inicio.ruta) {
                    navController.navigateToTab(AppRoutes.Inicio.ruta)
                }
            },
            colors = itemColors
        )

        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_historial), fontWeight = FontWeight.Medium) },
            // También marcamos Historial cuando estás en el detalle de una muestra
            selected = currentRoute == AppRoutes.Historial.ruta ||
                    currentRoute == AppRoutes.SampleDetail.ruta,
            onClick = {
                if (currentRoute != AppRoutes.Historial.ruta) {
                    navController.navigateToTab(AppRoutes.Historial.ruta)
                }
            },
            colors = itemColors
        )
    }
}