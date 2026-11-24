package com.aldemar.aquamuestra.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aldemar.aquamuestra.ui.theme.camara.CamaraScreen
import com.aldemar.aquamuestra.ui.theme.conteo.ConteoScreen
import com.aldemar.aquamuestra.ui.theme.dashboard.DashboardScreen
import com.aldemar.aquamuestra.viewmodel.ResultadoConteo
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
    object Camara : AppRoutes("camara/{muestraId}") {
        fun crear(muestraId: String) = "camara/$muestraId"
    }
    /** Los datos de entrada llegan por el SavedStateHandle del formulario (ver Claves). */
    object Conteo : AppRoutes("conteo")
}

/**
 * Claves para pasar datos entre pantallas con el SavedStateHandle de la pantalla anterior
 * (la forma recomendada de "devolver un resultado" con Navigation Compose).
 */
private object Claves {
    const val FOTO_RESULTADO = "foto_resultado"
    const val CONTEO_FOTO = "conteo_foto"
    const val CONTEO_MARCAS = "conteo_marcas_json"
    const val CONTEO_AJUSTE = "conteo_ajuste"
    const val CONTEO_R_MARCADOS = "conteo_r_marcados"
    const val CONTEO_R_AJUSTE = "conteo_r_ajuste"
    const val CONTEO_R_JSON = "conteo_r_json"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Se calcula UNA sola vez. MainActivity ya leyó la sesión de DataStore: si hay una guardada,
    // entra directo a Inicio aunque no haya internet (modo terreno); si no, al Login.
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
            // Solo el OPERARIO crea o corrige muestras. Otro rol que llegue aquí vuelve a Inicio.
            if (!SessionManager.puedeCrearMuestras) {
                VolverAInicio(navController)
                return@composable
            }
            val handle = backStackEntry.savedStateHandle
            val fotoNueva by handle.getStateFlow<String?>(Claves.FOTO_RESULTADO, null).collectAsStateWithLifecycle()
            val conteoJson by handle.getStateFlow<String?>(Claves.CONTEO_R_JSON, null).collectAsStateWithLifecycle()
            val conteoNuevo = conteoJson?.let {
                ResultadoConteo(
                    conteoMarcas = handle[Claves.CONTEO_R_MARCADOS] ?: 0,
                    ajusteManual = handle[Claves.CONTEO_R_AJUSTE] ?: 0,
                    marcasJson = it
                )
            }
            NewSampleFormScreen(
                muestraId = backStackEntry.arguments?.getString("muestraId"),
                fotoNueva = fotoNueva,
                conteoNuevo = conteoNuevo,
                onResultadosUsados = {
                    handle.remove<String>(Claves.FOTO_RESULTADO)
                    handle.remove<String>(Claves.CONTEO_R_JSON)
                },
                onAbrirCamara = { id -> navController.navigate(AppRoutes.Camara.crear(id)) },
                onAbrirConteo = { foto, marcas, ajuste ->
                    handle[Claves.CONTEO_FOTO] = foto
                    handle[Claves.CONTEO_MARCAS] = marcas
                    handle[Claves.CONTEO_AJUSTE] = ajuste
                    navController.navigate(AppRoutes.Conteo.ruta)
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Cámara (CameraX): devuelve la ruta de la foto al formulario
        composable(
            route = AppRoutes.Camara.ruta,
            arguments = listOf(navArgument("muestraId") { type = NavType.StringType })
        ) { backStackEntry ->
            if (!SessionManager.puedeCrearMuestras) {
                VolverAInicio(navController)
                return@composable
            }
            CamaraScreen(
                muestraId = backStackEntry.arguments?.getString("muestraId").orEmpty(),
                onFotoLista = { ruta ->
                    navController.previousBackStackEntry?.savedStateHandle?.set(Claves.FOTO_RESULTADO, ruta)
                    navController.popBackStack()
                },
                onCerrar = { navController.popBackStack() }
            )
        }

        // Conteo asistido: lee la foto y el conteo actual del formulario y le devuelve el resultado
        composable(AppRoutes.Conteo.ruta) {
            if (!SessionManager.puedeCrearMuestras) {
                VolverAInicio(navController)
                return@composable
            }
            val formulario = remember { navController.previousBackStackEntry?.savedStateHandle }
            ConteoScreen(
                fotoPath = formulario?.get<String>(Claves.CONTEO_FOTO),
                marcasJson = formulario?.get<String>(Claves.CONTEO_MARCAS),
                ajusteManual = formulario?.get<Int>(Claves.CONTEO_AJUSTE) ?: 0,
                onGuardar = { r ->
                    formulario?.set(Claves.CONTEO_R_MARCADOS, r.conteoMarcas)
                    formulario?.set(Claves.CONTEO_R_AJUSTE, r.ajusteManual)
                    formulario?.set(Claves.CONTEO_R_JSON, r.marcasJson) // se escribe al final: es la señal
                    navController.popBackStack()
                },
                onCancelar = { navController.popBackStack() }
            )
        }

        // 4. Pantalla de Historial
        composable(AppRoutes.Historial.ruta) {
            HistorialScreen(navController = navController)
        }

        // 5. Dashboard (SUPERVISOR y ANALISTA). Otro rol que llegue aquí vuelve a Inicio.
        composable(AppRoutes.Dashboard.ruta) {
            if (SessionManager.puedeVerDashboard) {
                DashboardScreen(navController = navController)
            } else {
                VolverAInicio(navController)
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

/** Redirige a Inicio (para rutas que el rol actual no puede ver). */
@Composable
private fun VolverAInicio(navController: NavHostController) {
    LaunchedEffect(Unit) {
        navController.navigate(AppRoutes.Inicio.ruta) {
            popUpTo(AppRoutes.Inicio.ruta) { inclusive = false }
            launchSingleTop = true
        }
    }
}
