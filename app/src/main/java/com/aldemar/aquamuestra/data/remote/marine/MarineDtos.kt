package com.aldemar.aquamuestra.data.remote.marine

import com.google.gson.annotations.SerializedName

/*
 * Respuesta de Open-Meteo Marine (GET v1/marine). Ejemplo real:
 * {
 *   "latitude": -41.79, "longitude": -73.12, "timezone": "America/Santiago",
 *   "current": { "time": "2026-11-24T01:45", "wave_height": 0.22, "wave_direction": 251,
 *                "wave_period": 2.65, "sea_surface_temperature": 15.8,
 *                "ocean_current_velocity": 0.4, "ocean_current_direction": 270 }
 * }
 * La API usa snake_case; @SerializedName lo une con los nombres de Kotlin.
 * Todo es nullable: en algunos puntos el modelo no tiene dato de alguna variable.
 */
data class MarineRespuestaDto(
    val latitude: Double?,
    val longitude: Double?,
    val timezone: String?,
    val current: MarineActualDto?
)

data class MarineActualDto(
    /** Hora local del dato (timezone=auto), formato "2026-11-24T01:45". */
    val time: String?,
    /** Altura significativa de ola, en metros. */
    @SerializedName("wave_height") val alturaOla: Double?,
    /** Dirección DESDE la que vienen las olas, en grados (0 = norte). */
    @SerializedName("wave_direction") val direccionOla: Double?,
    /** Período de la ola, en segundos. */
    @SerializedName("wave_period") val periodoOla: Double?,
    /** Temperatura superficial del mar, en °C. */
    @SerializedName("sea_surface_temperature") val temperaturaMar: Double?,
    /** Velocidad de la corriente, en km/h. */
    @SerializedName("ocean_current_velocity") val velocidadCorriente: Double?,
    /** Dirección HACIA la que fluye la corriente, en grados. */
    @SerializedName("ocean_current_direction") val direccionCorriente: Double?
)
