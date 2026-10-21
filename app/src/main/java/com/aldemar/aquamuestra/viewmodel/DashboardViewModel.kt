package com.aldemar.aquamuestra.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aldemar.aquamuestra.model.DashboardUiState
import com.aldemar.aquamuestra.repository.DashboardRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    // 6. El ViewModel solicita los datos al Repository.
    private val repository = DashboardRepository()

    // 7. Estado interno modificable solamente por el ViewModel.
    private val _uiState = MutableStateFlow(repository.obtenerDatos())

    // 8. La pantalla puede observar el estado, pero no modificarlo.
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // 9. Guarda el resultado de la consulta de conexión hecha por la pantalla.
    fun mostrarInternet(conectado: Boolean) {
        _uiState.update {
            it.copy(
                internet = if (conectado) "Con conexión"
                else "Sin conexión"
            )
        }
    }

    // 10. Guarda la fecha obtenida desde el dispositivo.
    fun mostrarFecha(valor: String) {
        _uiState.update { it.copy(fecha = valor) }
    }

    // 11. Simula la sincronización de los registros guardados en el dispositivo.
    fun sincronizar() {

        // Obtenemos los datos actuales.
        val actual = _uiState.value

        // Si ya se está enviando, no hacemos nada.
        if (actual.estadoSync == "Enviando") return

        // Sin conexión no se puede sincronizar.
        if (actual.internet != "Con conexión") {
            _uiState.update {
                it.copy(mensaje = "Sin conexión: los datos se guardan en el dispositivo")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(estadoSync = "Enviando") }
            delay(1200)
            _uiState.update {
                it.copy(
                    estadoSync = "Completado",
                    mensaje = "Sincronización completada"
                )
            }
            delay(2500)
            _uiState.update { it.copy(estadoSync = "Inactivo") }
        }
    }

    // 12. Borra el mensaje después de mostrarlo.
    fun limpiarMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }
}
