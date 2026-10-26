package com.aldemar.aquamuestra.ui.theme

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aldemar.aquamuestra.ui.theme.detalle.SampleDetailScreen
import com.aldemar.aquamuestra.ui.theme.form.NewSampleFormScreen
import com.aldemar.aquamuestra.ui.theme.historial.HistorialScreen
import com.aldemar.aquamuestra.ui.theme.inicio.InicioScreen
import com.aldemar.aquamuestra.ui.theme.login.LoginScreen

sealed class AppRoutes(val ruta: String) {
    object Login : AppRoutes("login")
    object NewSample : AppRoutes("new_sample")
    object Inicio : AppRoutes("inicio_screen")
    object Historial : AppRoutes("historial_screen")
    object SampleDetail : AppRoutes("sample_detail_screen/{sampleId}") {
        fun createRoute(sampleId: String) = "sample_detail_screen/$sampleId"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // El tema envuelve TODA la navegación: así el modo oscuro llega a todas las pantallas
    // (y a diálogos, menús, campos de texto, etc.). Si MainActivity ya lo envuelve, no pasa nada.
    AquaMuestraTheme {
        // Fondo base para que no se vea blanco durante las transiciones entre pantallas
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NavHost(
                navController = navController,
                startDestination = AppRoutes.Inicio.ruta
            ) {

                // 1. Pantalla de Login
                composable(AppRoutes.Login.ruta) {
                    LoginScreen { _, _, _ ->
                        navController.navigate(AppRoutes.Inicio.ruta) {
                            popUpTo(AppRoutes.Login.ruta) { inclusive = true }
                        }
                    }
                }

                // 2. Pantalla de Inicio
                composable(AppRoutes.Inicio.ruta) {
                    InicioScreen(navController = navController)
                }

                // 3. Pantalla de Nueva Muestra
                composable(AppRoutes.NewSample.ruta) {
                    NewSampleFormScreen(
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }

                // 4. Pantalla de Historial
                composable(AppRoutes.Historial.ruta) {
                    HistorialScreen(navController = navController)
                }

                // 5. Pantalla de Detalle de Muestra
                composable(
                    route = AppRoutes.SampleDetail.ruta,
                    arguments = listOf(
                        navArgument("sampleId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val sampleId = backStackEntry.arguments?.getString("sampleId") ?: ""
                    SampleDetailScreen(
                        sampleId = sampleId,
                        navController = navController
                    )
                }
            }
        }
    }
}
