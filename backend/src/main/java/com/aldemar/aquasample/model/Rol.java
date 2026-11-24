package com.aldemar.aquasample.model;

/** Roles de los usuarios de AquaSample. */
public enum Rol {
    /** Registra muestras en terreno y las envía a revisión. */
    OPERARIO,
    /** Revisa las muestras: las observa o las valida. */
    SUPERVISOR,
    /** Consulta datos y reportes (solo lectura). */
    ANALISTA
}
