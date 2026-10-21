package com.aldemar.aquamuestra.model

// 1. Resume la última muestra registrada que se muestra en el dashboard.
data class UltimaMuestra(
    val linea: String = "",
    val hace: String = "",
    val profundidad: String = "",
    val densidad: String = "",
    val calibre: String = ""
)
