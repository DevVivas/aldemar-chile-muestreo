package com.aldemar.aquamuestra.data.remote

import com.aldemar.aquamuestra.data.local.MuestraEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Convierte entre la fila de Room (MuestraEntity) y el JSON del backend (MuestraDto).
 *
 * Diferencias que resuelve:
 *  - Fechas: Room guarda milisegundos (Long); el backend usa texto ISO "2026-11-20T10:30:00".
 *  - Foto: Room guarda la RUTA del archivo en el teléfono (fotoPath); el backend guarda la URL de
 *    descarga (fotoUrl). No son intercambiables, por eso se manejan aparte.
 */
object MuestraMapper {

    /** Formato ISO sin zona: el backend lo interpreta como hora local (America/Santiago). */
    private const val FORMATO_ISO = "yyyy-MM-dd'T'HH:mm:ss"

    private fun formato() = SimpleDateFormat(FORMATO_ISO, Locale.US)

    fun aIso(millis: Long): String = formato().format(Date(millis))

    /** Acepta también fracciones de segundo ("...:04.677807"): se ignoran. */
    fun desdeIso(texto: String): Long = formato().parse(texto.take(19))?.time ?: 0L

    /**
     * Para enviar al backend.
     * @param fotoUrl URL de la foto en el servidor, si ya se subió (la ruta local no le sirve al backend).
     */
    fun aDto(entidad: MuestraEntity, fotoUrl: String? = null): MuestraDto = MuestraDto(
        id = entidad.id,
        centro = entidad.centro,
        concesion = entidad.concesion,
        tren = entidad.tren,
        linea = entidad.linea,
        tramoCm = entidad.tramoCm,
        fechaHora = aIso(entidad.fechaHora),
        operador = entidad.operador,
        cantidad = entidad.cantidad,
        observaciones = entidad.observaciones,
        estado = entidad.estado,
        fotoUrl = fotoUrl,
        comentarioRevision = entidad.comentarioRevision,
        revisadoPor = entidad.revisadoPor,
        actualizadoEn = null // lo pone el servidor
    )

    /**
     * Para guardar en Room algo que vino del backend. Queda como ya sincronizado (pendienteSync = false).
     * @param fotoPathLocal ruta de la foto en este teléfono, si existe (se conserva la que ya había).
     */
    fun aEntidad(dto: MuestraDto, fotoPathLocal: String? = null): MuestraEntity = MuestraEntity(
        id = dto.id,
        centro = dto.centro,
        concesion = dto.concesion,
        tren = dto.tren,
        linea = dto.linea,
        tramoCm = dto.tramoCm,
        fechaHora = desdeIso(dto.fechaHora),
        operador = dto.operador,
        cantidad = dto.cantidad,
        observaciones = dto.observaciones.orEmpty(),
        estado = dto.estado,
        fotoPath = fotoPathLocal,
        pendienteSync = false,
        actualizadoEn = dto.actualizadoEn?.let { desdeIso(it) } ?: System.currentTimeMillis(),
        comentarioRevision = dto.comentarioRevision,
        revisadoPor = dto.revisadoPor
    )
}
