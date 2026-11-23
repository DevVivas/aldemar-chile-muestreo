package com.aldemar.aquamuestra.data.local

/**
 * Estados por los que pasa una muestra.
 * BORRADOR -> ENVIADA -> OBSERVADA / VALIDADA (decide el supervisor).
 */
enum class EstadoRevision {
    BORRADOR,
    ENVIADA,
    OBSERVADA,
    VALIDADA
}
