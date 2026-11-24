package com.aldemar.aquasample.repository;

import com.aldemar.aquasample.model.EstadoRevision;
import com.aldemar.aquasample.model.Muestra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Acceso a la tabla "muestras". JpaRepository ya trae findById, findAll, save,
 * existsById, deleteById, count... sin escribir SQL.
 */
public interface MuestraRepository extends JpaRepository<Muestra, String> {

    /**
     * Búsqueda con filtros opcionales: si un parámetro llega null, esa condición se ignora.
     * "hasta" es exclusivo (fechaHora < hasta).
     */
    @Query("""
            SELECT m FROM Muestra m
            WHERE (:centro IS NULL OR m.centro = :centro)
              AND (:tren   IS NULL OR m.tren   = :tren)
              AND (:linea  IS NULL OR m.linea  = :linea)
              AND (:estado IS NULL OR m.estado = :estado)
              AND (:desde  IS NULL OR m.fechaHora >= :desde)
              AND (:hasta  IS NULL OR m.fechaHora <  :hasta)
            ORDER BY m.fechaHora DESC
            """)
    List<Muestra> buscar(
            @Param("centro") String centro,
            @Param("tren") String tren,
            @Param("linea") String linea,
            @Param("estado") EstadoRevision estado,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta
    );
}
