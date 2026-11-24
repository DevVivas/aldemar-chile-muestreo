package com.aldemar.aquamuestra.data.remote.marine

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API EXTERNA y pública de Open-Meteo Marine (no es nuestro backend).
 * Documentación: https://open-meteo.com/en/docs/marine-weather-api
 */
interface MarineApi {

    @GET("v1/marine")
    suspend fun condicionesActuales(
        @Query("latitude") latitud: Double,
        @Query("longitude") longitud: Double,
        @Query("current") variables: String = VARIABLES,
        @Query("timezone") zonaHoraria: String = "auto"
    ): MarineRespuestaDto

    companion object {
        /** Nombres exactos de Open-Meteo (verificados contra la API: un nombre mal escrito responde error). */
        const val VARIABLES = "wave_height,wave_direction,wave_period,sea_surface_temperature," +
            "ocean_current_velocity,ocean_current_direction"
    }
}
