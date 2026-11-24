package com.aldemar.aquamuestra.data.remote

import com.aldemar.aquamuestra.data.local.EstadoRevision

/*
 * DTOs: lo que viaja como JSON entre la app y el backend.
 * Tienen los mismos nombres de campo que los records del backend (Gson los asocia por nombre).
 */

/** Respuesta de GET /api/auth/me. */
data class UsuarioDto(
    val username: String,
    val nombre: String,
    /** "OPERARIO", "SUPERVISOR" o "ANALISTA". */
    val rol: String
)

/** Muestra tal como la entiende el backend. Las fechas van como texto ISO (2026-11-20T10:30:00). */
data class MuestraDto(
    val id: String,
    val centro: String,
    val concesion: String?,
    val tren: String,
    val linea: String,
    val tramoCm: Int,
    val fechaHora: String,
    val operador: String,
    val cantidad: Int,
    val observaciones: String?,
    val estado: EstadoRevision,
    val fotoUrl: String?,
    val comentarioRevision: String?,
    val revisadoPor: String?,
    /** Lo completa el servidor; al enviar puede ir null. */
    val actualizadoEn: String?,
    /** Individuos marcados sobre la foto. null en muestras antiguas del servidor. */
    val conteoMarcas: Int? = null,
    /** Ajuste manual del conteo (puede ser negativo). */
    val ajusteManual: Int? = null
    // marcasJson NO se envía: las posiciones de las marcas solo se guardan en el teléfono.
)

/** Cuerpo de PATCH /api/muestras/{id}/estado. */
data class CambioEstadoDto(
    val estado: EstadoRevision,
    val comentario: String?
)

/** Respuesta de POST /api/muestras/{id}/foto. */
data class FotoSubidaDto(val fotoUrl: String)

/** Formato de los errores del backend (ErrorRespuesta). */
data class ErrorRespuestaDto(
    val status: Int?,
    val mensaje: String?,
    val errores: Map<String, String>?
)
