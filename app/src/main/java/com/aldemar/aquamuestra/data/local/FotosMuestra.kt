package com.aldemar.aquamuestra.data.local

import android.content.Context
import java.io.File

/**
 * Dónde se guardan las fotos: en el almacenamiento PRIVADO de la app (filesDir/fotos).
 * Ninguna otra app puede leerlas, no aparecen en la galería y se borran al desinstalar.
 *
 * Cada muestra tiene una sola foto: fotos/{idMuestra}.jpg. Mientras el operador decide
 * "Usar foto" o "Repetir", la captura queda en un archivo temporal aparte.
 */
object FotosMuestra {

    private fun carpeta(context: Context): File =
        File(context.filesDir, "fotos").apply { mkdirs() }

    /** Archivo temporal donde CameraX guarda la captura antes de confirmarla. */
    fun archivoCaptura(context: Context, idMuestra: String): File =
        File(carpeta(context), "${idMuestra}_captura.jpg")

    /** Foto definitiva de la muestra. */
    fun archivoFinal(context: Context, idMuestra: String): File =
        File(carpeta(context), "$idMuestra.jpg")

    /**
     * "Usar foto": la captura pasa a ser la foto de la muestra (si ya había una, se reemplaza).
     * @return la ruta de la foto definitiva.
     */
    fun confirmar(context: Context, idMuestra: String): String {
        val captura = archivoCaptura(context, idMuestra)
        val destino = archivoFinal(context, idMuestra)
        if (destino.exists()) destino.delete()
        if (!captura.renameTo(destino)) {
            captura.copyTo(destino, overwrite = true)
            captura.delete()
        }
        return destino.absolutePath
    }

    /** "Repetir" o salir sin usar la foto: se borra la captura temporal. */
    fun descartarCaptura(context: Context, idMuestra: String) {
        archivoCaptura(context, idMuestra).delete()
    }
}
