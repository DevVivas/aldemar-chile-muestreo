package com.aldemar.aquamuestra.repository

import com.aldemar.aquamuestra.model.DashboardUiState
import com.aldemar.aquamuestra.model.Telemetria
import com.aldemar.aquamuestra.model.UltimaMuestra

class DashboardRepository {

    // 4. Por ahora simulamos los datos del dashboard con valores ficticios; luego vendrán de Room.
    fun obtenerDatos(): DashboardUiState {

        // 5. El Repository entrega los datos al resto de la aplicación.
        return DashboardUiState(
            centro = "Centro Demo Norte",
            turno = "Turno A",
            registrosOffline = 12,
            muestrasHoy = 12,
            muestrasAyer = 10,
            validadas = 8,
            pendientes = 4,
            ultimaMuestra = UltimaMuestra(
                linea = "Línea A-04",
                hace = "Hace 25 min",
                profundidad = "6.5 m",
                densidad = "150 ind / 0.5 m",
                calibre = "42 mm"
            ),
            totalHistorial = 124,
            telemetria = Telemetria(
                sonda = "Sonda Buoy-02",
                temperatura = "11.4°C",
                salinidad = "32.8 PSU",
                oxigeno = "8.6 mg/L"
            )
        )
    }
}
