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

    /** Igual que [obtenerPorId], pero avisa cada vez que esa muestra cambia. */
    @Query("SELECT * FROM muestras WHERE id = :id LIMIT 1")
    fun observarPorId(id: String): Flow<MuestraEntity?>

    /** Todos los muestreos de una misma línea (centro + tren + línea), del más antiguo al más nuevo. */
    @Query(
        "SELECT * FROM muestras WHERE centro = :centro AND tren = :tren AND linea = :linea " +
            "ORDER BY fechaHora ASC"
    )
    fun obtenerHistoricoLinea(centro: String, tren: String, linea: String): Flow<List<MuestraEntity>>

    /** Registra la revisión del supervisor y deja la muestra pendiente de sincronizar. */
    @Query(
        "UPDATE muestras SET estado = :estado, comentarioRevision = :comentario, revisadoPor = :revisadoPor, " +
            "pendienteSync = 1, actualizadoEn = :actualizadoEn WHERE id = :id"
    )
    suspend fun registrarRevision(
        id: String,
        estado: EstadoRevision,
        comentario: String?,
        revisadoPor: String,
        actualizadoEn: Long
    )

    /** Muestras con cambios locales que aún no llegan al servidor. */
    @Query("SELECT * FROM muestras WHERE pendienteSync = 1 ORDER BY fechaHora ASC")
    suspend fun obtenerPendientesSync(): List<MuestraEntity>

    /** Cuántas muestras faltan por sincronizar (para el banner). Se actualiza solo. */
    @Query("SELECT COUNT(*) FROM muestras WHERE pendienteSync = 1")
    fun contarPendientes(): Flow<Int>

    /**
     * Marca la muestra como sincronizada SOLO si no cambió desde que se envió
     * (mismo actualizadoEn). Si el usuario la editó mientras se subía, sigue pendiente
     * y se enviará de nuevo en la próxima sincronización.
     */
    @Query("UPDATE muestras SET pendienteSync = 0 WHERE id = :id AND actualizadoEn = :actualizadoEn")
    suspend fun marcarSincronizadaSiNoCambio(id: String, actualizadoEn: Long): Int

    @Query("UPDATE muestras SET pendienteSync = 0 WHERE id = :id")
    suspend fun marcarSincronizada(id: String)

    /** Cambia el estado y deja la muestra pendiente de sincronizar. */
    @Query(
        "UPDATE muestras SET estado = :estado, pendienteSync = 1, actualizadoEn = :actualizadoEn " +
            "WHERE id = :id"
    )
    suspend fun cambiarEstado(id: String, estado: EstadoRevision, actualizadoEn: Long)
}
