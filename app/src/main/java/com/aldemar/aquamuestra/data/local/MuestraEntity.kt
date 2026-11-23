package com.aldemar.aquamuestra.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Una fila de la tabla "muestras".
 *
 * El [id] es un UUID generado en el teléfono: así la muestra tiene el mismo ID
 * en el dispositivo y en el servidor, y al sincronizar no se crean duplicados.
 */
@Entity(tableName = "muestras")
data class MuestraEntity(
    @PrimaryKey
    val id: String,
    val centro: String,
    val concesion: String?,
    val tren: String,
    val linea: String,
    val tramoCm: Int,
    /** Fecha y hora de la muestra en milisegundos (epoch millis). */
    val fechaHora: Long,
    val operador: String,
    val cantidad: Int,
    val observaciones: String,
    val estado: EstadoRevision,
    /** Ruta del archivo de la foto en el almacenamiento de la app (null si no tiene). */
    val fotoPath: String?,
    /** true = hay cambios locales que todavía no se envían al servidor. */
    val pendienteSync: Boolean = true,
    /** Última modificación en milisegundos (epoch millis). */
    val actualizadoEn: Long
)
