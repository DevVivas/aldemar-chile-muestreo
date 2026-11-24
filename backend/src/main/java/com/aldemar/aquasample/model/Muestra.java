package com.aldemar.aquasample.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Una fila de la tabla "muestras" en MySQL.
 *
 * El id es el UUID que genera la app en el teléfono: NO se autogenera aquí.
 * Así la misma muestra tiene el mismo id en el teléfono y en el servidor,
 * y al sincronizar dos veces no se duplica.
 */
@Entity
@Table(
        name = "muestras",
        indexes = @Index(name = "idx_muestras_linea", columnList = "centro, tren, linea")
)
public class Muestra {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 100)
    private String centro;

    @Column(length = 50)
    private String concesion;

    @Column(nullable = false, length = 50)
    private String tren;

    @Column(nullable = false, length = 50)
    private String linea;

    @Column(name = "tramo_cm", nullable = false)
    private Integer tramoCm;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(nullable = false, length = 100)
    private String operador;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(length = 300)
    private String observaciones;

    // varchar y no ENUM de MySQL: así agregar un estado nuevo no obliga a cambiar la columna.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    private EstadoRevision estado;

    @Column(name = "foto_url", length = 500)
    private String fotoUrl;

    @Column(name = "comentario_revision", length = 300)
    private String comentarioRevision;

    @Column(name = "revisado_por", length = 100)
    private String revisadoPor;

    /** Individuos marcados sobre la foto en la app (conteo asistido). null en muestras antiguas. */
    @Column(name = "conteo_marcas")
    private Integer conteoMarcas;

    /** Ajuste manual del operador sobre el conteo (puede ser negativo). cantidad = conteoMarcas + ajusteManual. */
    @Column(name = "ajuste_manual")
    private Integer ajusteManual;

    /** Cuándo recibió el servidor el último cambio. */
    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    // JPA necesita un constructor vacío.
    public Muestra() {
    }

    // ---------------------------------------------------------------- Getters y setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCentro() { return centro; }
    public void setCentro(String centro) { this.centro = centro; }

    public String getConcesion() { return concesion; }
    public void setConcesion(String concesion) { this.concesion = concesion; }

    public String getTren() { return tren; }
    public void setTren(String tren) { this.tren = tren; }

    public String getLinea() { return linea; }
    public void setLinea(String linea) { this.linea = linea; }

    public Integer getTramoCm() { return tramoCm; }
    public void setTramoCm(Integer tramoCm) { this.tramoCm = tramoCm; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public String getOperador() { return operador; }
    public void setOperador(String operador) { this.operador = operador; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public EstadoRevision getEstado() { return estado; }
    public void setEstado(EstadoRevision estado) { this.estado = estado; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public String getComentarioRevision() { return comentarioRevision; }
    public void setComentarioRevision(String comentarioRevision) { this.comentarioRevision = comentarioRevision; }

    public String getRevisadoPor() { return revisadoPor; }
    public void setRevisadoPor(String revisadoPor) { this.revisadoPor = revisadoPor; }

    public Integer getConteoMarcas() { return conteoMarcas; }
    public void setConteoMarcas(Integer conteoMarcas) { this.conteoMarcas = conteoMarcas; }

    public Integer getAjusteManual() { return ajusteManual; }
    public void setAjusteManual(Integer ajusteManual) { this.ajusteManual = ajusteManual; }

    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }
}
