package com.aldemar.aquamuestra.data.local

/** Un centro de cultivo con su ubicación aproximada. */
data class CentroUbicacion(
    /** Mismo nombre que se usa en el formulario de muestras. */
    val nombre: String,
    /** Nombre corto para los chips. */
    val nombreCorto: String,
    val latitud: Double,
    val longitud: Double
)

/**
 * Catálogo de centros con coordenadas APROXIMADAS (puntos de mar cercanos a cada sector,
 * región de Los Lagos). Son solo referenciales para consultar el pronóstico marino:
 * NO son las ubicaciones reales de los centros de ALDEMAR.
 */
object CentrosCatalogo {

    val centros: List<CentroUbicacion> = listOf(
        CentroUbicacion("Calbuco Sur", "Calbuco Sur", latitud = -41.79, longitud = -73.13),
        CentroUbicacion("Centro Chiloé (Sector Huito)", "Huito", latitud = -41.76, longitud = -73.20),
        CentroUbicacion("Punta Pelluco, módulo 104", "Pelluco", latitud = -41.49, longitud = -72.90),
        CentroUbicacion("Reloncaví Estuario", "Reloncaví", latitud = -41.62, longitud = -72.62)
    )

    fun buscar(nombre: String): CentroUbicacion? = centros.firstOrNull { it.nombre == nombre }
}
