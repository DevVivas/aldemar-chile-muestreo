package com.aldemar.aquamuestra.data.sync

/**
 * Lo único que el Repository necesita saber de la sincronización: "pide sincronizar ahora".
 *
 * Es una interfaz para que MuestraRepository NO dependa de WorkManager: en la app la implementa
 * SyncScheduler y en los tests se reemplaza con un mock (MockK) o con [NINGUNO].
 */
fun interface Sincronizador {

    fun sincronizarAhora()

    companion object {
        /** No hace nada (valor por defecto y para tests). */
        val NINGUNO = Sincronizador { }
    }
}
