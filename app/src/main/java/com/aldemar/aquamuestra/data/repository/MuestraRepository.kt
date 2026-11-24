package com.aldemar.aquamuestra.data.repository

import com.aldemar.aquamuestra.data.local.EstadoRevision
import com.aldemar.aquamuestra.data.local.MuestraDao
import com.aldemar.aquamuestra.data.local.MuestraEntity
import com.aldemar.aquamuestra.data.sync.Sincronizador
import kotlinx.coroutines.flow.Flow

/**
 * Punto único de acceso a los datos de muestras para los ViewModels.
 *
 * Recibe el DAO por constructor (sin Hilt ni Koin), lo que además permite
 * reemplazarlo por un mock en las pruebas.
 *
 * @param reloj devuelve la hora actual en milisegundos; se puede cambiar en las pruebas.
 * @param sincronizador se le avisa cada vez que hay un cambio local para que lo envíe al
 *   servidor. Es una interfaz (no WorkManager) para poder probar el Repository con MockK.
 */
class MuestraRepository(
    private val dao: MuestraDao,
    private val reloj: () -> Long = { System.currentTimeMillis() },
    private val sincronizador: Sincronizador = Sincronizador.NINGUNO
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
        sincronizador.sincronizarAhora()
    }

    /** Cambia el estado de revisión y deja la muestra pendiente de sincronizar. */
    suspend fun cambiarEstado(id: String, nuevoEstado: EstadoRevision) {
        dao.cambiarEstado(id, nuevoEstado, reloj())
        sincronizador.sincronizarAhora()
    }

    /** La muestra como Flow: la pantalla se actualiza sola cuando cambia (p. ej. tras validar). */
    fun observarPorId(id: String): Flow<MuestraEntity?> = dao.observarPorId(id)

    /** Muestreos de la misma línea (centro + tren + línea), del más antiguo al más nuevo. */
    fun obtenerHistoricoLinea(centro: String, tren: String, linea: String): Flow<List<MuestraEntity>> =
        dao.obtenerHistoricoLinea(centro, tren, linea)

    /** El supervisor aprueba la muestra. Se borra el comentario de una observación anterior. */
    suspend fun validar(id: String, supervisor: String) {
        dao.registrarRevision(id, EstadoRevision.VALIDADA, comentario = null, revisadoPor = supervisor, actualizadoEn = reloj())
        sincronizador.sincronizarAhora()
    }

    /** El supervisor devuelve la muestra al operador con un comentario de qué corregir. */
    suspend fun observar(id: String, supervisor: String, comentario: String) {
        dao.registrarRevision(id, EstadoRevision.OBSERVADA, comentario.trim(), supervisor, reloj())
        sincronizador.sincronizarAhora()
    }

    suspend fun obtenerPendientesSync(): List<MuestraEntity> = dao.obtenerPendientesSync()

    suspend fun marcarSincronizada(id: String) = dao.marcarSincronizada(id)

    // ---------------------------------------------------------------- Para la sincronización

    /** Cuántas muestras faltan por subir (banner "N pendientes"). */
    fun contarPendientes(): Flow<Int> = dao.contarPendientes()

    /**
     * Marca como sincronizada solo si no cambió mientras se subía.
     * @return true si se marcó; false si el usuario la editó y debe volver a subirse.
     */
    suspend fun marcarSincronizadaSiNoCambio(id: String, actualizadoEn: Long): Boolean =
        dao.marcarSincronizadaSiNoCambio(id, actualizadoEn) > 0

    /**
     * Guarda tal cual una muestra que vino del servidor (ya viene con pendienteSync = false).
     * No avisa al sincronizador: no hay nada nuevo que subir.
     */
    suspend fun guardarDesdeServidor(muestra: MuestraEntity) = dao.upsert(muestra)
}
