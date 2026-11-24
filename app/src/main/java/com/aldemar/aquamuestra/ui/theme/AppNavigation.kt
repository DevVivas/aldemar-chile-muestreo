package com.aldemar.aquamuestra.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aldemar.aquamuestra.ui.theme.dashboard.DashboardScreen
import com.aldemar.aquamuestra.ui.theme.detalle.SampleDetailScreen
import com.aldemar.aquamuestra.ui.theme.form.NewSampleFormScreen
import com.aldemar.aquamuestra.ui.theme.historial.HistorialScreen
import com.aldemar.aquamuestra.ui.theme.inicio.InicioScreen
import com.aldemar.aquamuestra.ui.theme.login.LoginScreen

sealed class AppRoutes(val ruta: String) {
    object Login : AppRoutes("login")
    object NewSample : AppRoutes("new_sample") {
        /** Patrón con argumento opcional: "new_sample" abre una muestra nueva. */
        const val PATRON = "new_sample?muestraId={muestraId}"
        fun editar(muestraId: String) = "new_sample?muestraId=$muestraId"
    }
    object Inicio : AppRoutes("inicio_screen")
    object Historial : AppRoutes("historial_screen")
    object Dashboard : AppRoutes("dashboard_screen") // Solo supervisor
    object SampleDetail : AppRoutes("sample_detail_screen/{sampleId}") {
        fun createRoute(sampleId: String) = "sample_detail_screen/$sampleId"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Se calcula UNA sola vez: si hay una sesión guardada entra directo a Inicio, si no, al Login.
    val startDestination = remember {
        SessionManager.init(context)
        if (SessionManager.isLoggedIn) AppRoutes.Inicio.ruta else AppRoutes.Login.ruta
    }

    // El tema (AquaMuestraTheme) y el fondo base los pone MainActivity, una sola vez.
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // 1. Pantalla de Login
        composable(AppRoutes.Login.ruta) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(AppRoutes.Inicio.ruta) {
                        popUpTo(AppRoutes.Login.ruta) { inclusive = true }
                    }
                }
            )
        }

        // 2. Pantalla de Inicio
        composable(AppRoutes.Inicio.ruta) {
            InicioScreen(navController = navController)
        }

        // 3. Pantalla de Nueva Muestra
        composable(
            route = AppRoutes.NewSample.PATRON,
            arguments = listOf(
                navArgument("muestraId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            NewSampleFormScreen(
                muestraId = backStackEntry.arguments?.getString("muestraId"),
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // 4. Pantalla de Historial
        composable(AppRoutes.Historial.ruta) {
            HistorialScreen(navController = navController)
        }

        // 5. Dashboard (solo supervisor). Si un operario llegara aquí, se le devuelve a Inicio.
        composable(AppRoutes.Dashboard.ruta) {
            if (SessionManager.isSupervisor) {
                DashboardScreen(navController = navController)
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(AppRoutes.Inicio.ruta) {
                        popUpTo(AppRoutes.Inicio.ruta) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            }
        }

        // 6. Pantalla de Detalle de Muestra
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
