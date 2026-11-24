package com.aldemar.aquamuestra.data.local

/** Rol del usuario en la app (mismos nombres que en el backend). */
enum class UserRole {
    /** Registra muestras en terreno y las envía a revisión. */
    OPERARIO,

    /** Revisa las muestras: las observa o las valida. */
    SUPERVISOR,

    /** Consulta historial y dashboard. Solo lectura. */
    ANALISTA
}
