package com.aldemar.aquamuestra.data.repository

import com.aldemar.aquamuestra.data.local.EstadoRevision
import com.aldemar.aquamuestra.data.local.MuestraDao
import com.aldemar.aquamuestra.data.local.MuestraEntity
import kotlinx.coroutines.flow.Flow

/**
 * Punto único de acceso a los datos de muestras para los ViewModels.
 *
 * Recibe el DAO por constructor (sin Hilt ni Koin), lo que además permite
 * reemplazarlo por un mock en las pruebas.
 *
 * @param reloj devuelve la hora actual en milisegundos; se puede cambiar en las pruebas.
 */
class MuestraRepository(
    private val dao: MuestraDao,
    private val reloj: () -> Long = { System.currentTimeMillis() }
) {

    /** Lista de muestras que se actualiza sola cuando cambia la tabla. */
    fun obtenerTodas(): Flow<List<MuestraEntity>> = dao.obtenerTodas()

    suspend fun obtenerPorId(id: String): MuestraEntity? = dao.obtenerPorId(id)

    /** Guarda (crea o actualiza) la muestra y la deja pendiente de sincronizar. */
    suspend fun guardar(muestra: MuestraEntity) {
        dao.upsert(
            muestra.copy(
                pendienteSync = true,
                actualizadoEn = reloj()
            )
        )
    }

    /** Cambia el estado de revisión y deja la muestra pendiente de sincronizar. */
    suspend fun cambiarEstado(id: String, nuevoEstado: EstadoRevision) {
        dao.cambiarEstado(id, nuevoEstado, reloj())
    }

    suspend fun obtenerPendientesSync(): List<MuestraEntity> = dao.obtenerPendientesSync()

    suspend fun marcarSincronizada(id: String) = dao.marcarSincronizada(id)
}
