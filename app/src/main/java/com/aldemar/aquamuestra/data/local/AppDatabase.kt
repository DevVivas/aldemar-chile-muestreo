package com.aldemar.aquamuestra.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Base de datos local de la app (archivo "aquamuestra.db").
 * Se crea una sola instancia para toda la app (singleton).
 */
@Database(
    entities = [MuestraEntity::class],
    // v2: se agregan comentarioRevision y revisadoPor a "muestras".
    // v3: se agregan conteoMarcas, ajusteManual y marcasJson (conteo asistido).
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun muestraDao(): MuestraDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aquamuestra.db"
                )
                    // SOLO EN DESARROLLO: si cambia la versión, borra y recrea las tablas
                    // (se pierden los datos locales). Antes de producción hay que escribir Migrations.
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build().also { INSTANCE = it }
            }
    }
}
