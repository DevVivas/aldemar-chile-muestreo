package com.aldemar.aquamuestra.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/** Consultas SQL sobre la tabla "muestras". Room genera el código a partir de esta interfaz. */
@Dao
interface MuestraDao {

    /** Inserta la muestra o, si ya existe una con el mismo id, la reemplaza (upsert). */
    @Upsert
    suspend fun upsert(muestra: MuestraEntity)

    /** Todas las muestras, de la más reciente a la más antigua. El Flow avisa cada vez que la tabla cambia. */
    @Query("SELECT * FROM muestras ORDER BY fechaHora DESC")
    fun obtenerTodas(): Flow<List<MuestraEntity>>

    @Query("SELECT * FROM muestras WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: String): MuestraEntity?

    /** Muestras con cambios locales que aún no llegan al servidor. */
    @Query("SELECT * FROM muestras WHERE pendienteSync = 1")
    suspend fun obtenerPendientesSync(): List<MuestraEntity>

    @Query("UPDATE muestras SET pendienteSync = 0 WHERE id = :id")
    suspend fun marcarSincronizada(id: String)

    /** Cambia el estado y deja la muestra pendiente de sincronizar. */
    @Query(
        "UPDATE muestras SET estado = :estado, pendienteSync = 1, actualizadoEn = :actualizadoEn " +
            "WHERE id = :id"
    )
    suspend fun cambiarEstado(id: String, estado: EstadoRevision, actualizadoEn: Long)
}
