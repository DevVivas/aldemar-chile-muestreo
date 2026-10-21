package com.aldemar.aquamuestra.model

// 2. Contiene las lecturas de la sonda del centro (datos de ejemplo).
data class Telemetria(
    val sonda: String = "",
    val temperatura: String = "",
    val salinidad: String = "",
    val oxigeno: String = ""
)
