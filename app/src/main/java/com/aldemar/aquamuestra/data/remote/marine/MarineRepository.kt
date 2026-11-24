package com.aldemar.aquamuestra.data.remote.marine

import com.aldemar.aquamuestra.data.local.CentroUbicacion
import com.aldemar.aquamuestra.data.remote.Resultado
import com.aldemar.aquamuestra.data.remote.llamarApi

/** Condiciones marinas ya listas para mostrar (modelo de la app, no el JSON de Open-Meteo). */
data class CondicionesMarinas(
    val alturaOlaM: Double?,
    val direccionOlaGrados: Double?,
    val periodoOlaS: Double?,
    val temperaturaMarC: Double?,
    val corrienteKmh: Double?,
    val direccionCorrienteGrados: Double?,
    /** Hora local del dato según Open-Meteo ("2026-11-24T01:45"). */
    val horaDato: String?,
    /** Cuándo lo consultó la app (epoch millis). */
    val consultadoEn: Long
)

/**
 * Consulta Open-Meteo Marine y guarda el último resultado exitoso de cada centro,
 * para poder mostrarlo sin internet.
 *
 * @param cache dónde se guarda el último dato (en la app: DataStore; en los tests: un mock).
 */
class MarineRepository(
    private val api: MarineApi,
    private val cache: CacheMarino,
    private val reloj: () -> Long = { System.currentTimeMillis() }
) {

    /** Pide las condiciones actuales del centro. Si sale bien, las deja en cache. */
    suspend fun consultar(centro: CentroUbicacion): Resultado<CondicionesMarinas> {
        val r = llamarApi { api.condicionesActuales(centro.latitud, centro.longitud) }
        return when (r) {
            is Resultado.Ok -> {
                val actual = r.datos.current
                if (actual == null) {
                    Resultado.ErrorHttp(200, "Open-Meteo no entregó datos para este punto")
                } else {
                    val condiciones = CondicionesMarinas(
                        alturaOlaM = actual.alturaOla,
                        direccionOlaGrados = actual.direccionOla,
                        periodoOlaS = actual.periodoOla,
                        temperaturaMarC = actual.temperaturaMar,
                        corrienteKmh = actual.velocidadCorriente,
                        direccionCorrienteGrados = actual.direccionCorriente,
                        horaDato = actual.time,
                        consultadoEn = reloj()
                    )
                    cache.guardar(centro.nombre, condiciones)
                    Resultado.Ok(condiciones)
                }
            }
            is Resultado.ErrorHttp -> Resultado.ErrorHttp(r.codigo, r.mensaje)
            Resultado.SinConexion -> Resultado.SinConexion
        }
    }

    /** Último dato bueno guardado para el centro, o null si nunca se consultó. */
    suspend fun leerCache(centro: CentroUbicacion): CondicionesMarinas? = cache.leer(centro.nombre)
}
