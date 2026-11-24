package com.aldemar.aquasample.dto;

import com.aldemar.aquasample.model.Muestra;

/** Convierte entre la entidad (tabla) y el DTO (JSON). Sin librerías: copia campo a campo. */
public final class MuestraMapper {

    private MuestraMapper() {
    }

    public static MuestraDto toDto(Muestra m) {
        return new MuestraDto(
                m.getId(),
                m.getCentro(),
                m.getConcesion(),
                m.getTren(),
                m.getLinea(),
                m.getTramoCm(),
                m.getFechaHora(),
                m.getOperador(),
                m.getCantidad(),
                m.getObservaciones(),
                m.getEstado(),
                m.getFotoUrl(),
                m.getComentarioRevision(),
                m.getRevisadoPor(),
                m.getActualizadoEn()
        );
    }

    /**
     * Copia los datos del DTO sobre una entidad (nueva o existente).
     * No toca id ni actualizadoEn: eso lo decide el Service.
     */
    public static void copiarEnEntidad(MuestraDto dto, Muestra m) {
        m.setCentro(dto.centro().trim());
        m.setConcesion(vacioANull(dto.concesion()));
        m.setTren(dto.tren().trim());
        m.setLinea(dto.linea().trim());
        m.setTramoCm(dto.tramoCm());
        m.setFechaHora(dto.fechaHora());
        m.setOperador(dto.operador().trim());
        m.setCantidad(dto.cantidad());
        m.setObservaciones(dto.observaciones() == null ? "" : dto.observaciones().trim());
        m.setEstado(dto.estado());
        m.setFotoUrl(vacioANull(dto.fotoUrl()));
        m.setComentarioRevision(vacioANull(dto.comentarioRevision()));
        m.setRevisadoPor(vacioANull(dto.revisadoPor()));
    }

    private static String vacioANull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
