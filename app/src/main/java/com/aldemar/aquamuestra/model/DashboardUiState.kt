package com.aldemar.aquamuestra.model

// 3. Contiene todos los datos visibles del dashboard.
data class DashboardUiState(
    val centro: String = "",
    val turno: String = "",
    val registrosOffline: Int = 0,
    val muestrasHoy: Int = 0,
    val muestrasAyer: Int = 0,
    val validadas: Int = 0,
    val pendientes: Int = 0,
    val ultimaMuestra: UltimaMuestra = UltimaMuestra(),
    val totalHistorial: Int = 0,
    val telemetria: Telemetria = Telemetria(),
    val internet: String = "Sin consultar",
    val fecha: String = "--/--/----",
    val estadoSync: String = "Inactivo",
    val mensaje: String? = null
) {

    // El cambio porcentual se calcula aquí para que los componentes solo muestren datos.
    val variacion: Int
        get() = if (muestrasAyer > 0) (muestrasHoy - muestrasAyer) * 100 / muestrasAyer else 0
}
