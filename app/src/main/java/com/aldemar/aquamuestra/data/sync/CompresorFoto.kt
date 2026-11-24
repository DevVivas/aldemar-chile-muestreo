package com.aldemar.aquamuestra.data.sync

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Achica la foto antes de subirla: en terreno la señal es mala y una foto de cámara pesa varios MB.
 * Resultado: JPEG con el lado mayor de máximo [LADO_MAXIMO] px y calidad [CALIDAD_JPEG].
 * La foto original del teléfono NO se modifica: se crea un archivo temporal que hay que borrar.
 */
object CompresorFoto {

    const val LADO_MAXIMO = 1600
    const val CALIDAD_JPEG = 80

    /** @return archivo temporal comprimido (dentro de [carpetaTemporal]), o null si la imagen no se pudo leer. */
    fun comprimir(original: File, carpetaTemporal: File): File? {
        // 1. Leer solo el tamaño (sin cargar la imagen en memoria)
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(original.absolutePath, limites)
        if (limites.outWidth <= 0 || limites.outHeight <= 0) return null

        // 2. Cargarla ya reducida a una potencia de 2 (ahorra memoria con fotos grandes)
        var muestreo = 1
        while (max(limites.outWidth, limites.outHeight) / (muestreo * 2) >= LADO_MAXIMO) muestreo *= 2
        val opciones = BitmapFactory.Options().apply { inSampleSize = muestreo }
        var bitmap = BitmapFactory.decodeFile(original.absolutePath, opciones) ?: return null

        // 3. Ajuste fino al tamaño máximo
        val ladoMayor = max(bitmap.width, bitmap.height)
        if (ladoMayor > LADO_MAXIMO) {
            val escala = LADO_MAXIMO.toFloat() / ladoMayor
            bitmap = Bitmap.createScaledBitmap(
                bitmap, (bitmap.width * escala).roundToInt(), (bitmap.height * escala).roundToInt(), true
            )
        }

        // 4. Girarla según la orientación EXIF (si no, una foto vertical llegaría acostada)
        val grados = when (ExifInterface(original.absolutePath)
            .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        if (grados != 0f) {
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply { postRotate(grados) }, true)
        }

        // 5. Guardar como JPEG en un archivo temporal
        val temporal = File.createTempFile("subida_", ".jpg", carpetaTemporal)
        temporal.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPEG, it) }
        bitmap.recycle()
        return temporal
    }
}
