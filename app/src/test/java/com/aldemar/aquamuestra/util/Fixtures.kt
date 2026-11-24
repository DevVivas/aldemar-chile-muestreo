package com.aldemar.aquamuestra.util

import com.aldemar.aquamuestra.data.local.EstadoRevision
import com.aldemar.aquamuestra.data.local.MuestraEntity
import com.aldemar.aquamuestra.data.remote.MuestraDto
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

/** Datos de ejemplo reutilizables en las pruebas. */
object Fixtures {

    fun muestra(
        id: String = "m-1",
        centro: String = "Calbuco Sur",
        tren: String = "Tren 1 - Módulo A",
        linea: String = "Línea 10",
        tramoCm: Int = 100,
        fechaHora: Long = 1_000_000L,
        cantidad: Int = 150,
        estado: EstadoRevision = EstadoRevision.ENVIADA,
        fotoPath: String? = null,
        pendienteSync: Boolean = true,
        actualizadoEn: Long = 5_000L,
        comentarioRevision: String? = null,
        revisadoPor: String? = null,
        conteoMarcas: Int = 0,
        ajusteManual: Int = cantidad,
        marcasJson: String? = null,
        operador: String = "Operario Demo",
        observaciones: String = ""
    ) = MuestraEntity(
        id = id,
        centro = centro,
        concesion = null,
        tren = tren,
        linea = linea,
        tramoCm = tramoCm,
        fechaHora = fechaHora,
        operador = operador,
        cantidad = cantidad,
        observaciones = observaciones,
        estado = estado,
        fotoPath = fotoPath,
        pendienteSync = pendienteSync,
        actualizadoEn = actualizadoEn,
        comentarioRevision = comentarioRevision,
        revisadoPor = revisadoPor,
        conteoMarcas = conteoMarcas,
        ajusteManual = ajusteManual,
        marcasJson = marcasJson
    )

    fun dto(
        id: String = "m-1",
        estado: EstadoRevision = EstadoRevision.ENVIADA,
        cantidad: Int = 150,
        conteoMarcas: Int? = 140,
        ajusteManual: Int? = 10,
        revisadoPor: String? = null,
        comentarioRevision: String? = null
    ) = MuestraDto(
        id = id,
        centro = "Calbuco Sur",
        concesion = null,
        tren = "Tren 1 - Módulo A",
        linea = "Línea 10",
        tramoCm = 100,
        fechaHora = "2026-11-20T10:30:00",
        operador = "Operario Demo",
        cantidad = cantidad,
        observaciones = null,
        estado = estado,
        fotoUrl = null,
        comentarioRevision = comentarioRevision,
        revisadoPor = revisadoPor,
        actualizadoEn = "2026-11-20T10:31:05.123456",
        conteoMarcas = conteoMarcas,
        ajusteManual = ajusteManual
    )

    /** Excepción de Retrofit para un código HTTP, con el cuerpo JSON de error del backend. */
    fun httpError(codigo: Int, cuerpo: String = """{"status":$codigo,"mensaje":"Mensaje del servidor"}"""): HttpException =
        HttpException(Response.error<Any>(codigo, cuerpo.toResponseBody("application/json".toMediaType())))
}
