package com.aldemar.aquasample.controller;

import com.aldemar.aquasample.dto.ResumenDto;
import com.aldemar.aquasample.exception.ErrorRespuesta;
import com.aldemar.aquasample.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reportes")
@Tag(name = "Reportes", description = "Indicadores para supervisores y analistas")
public class ReporteController {

    private final ReporteService service;

    public ReporteController(ReporteService service) {
        this.service = service;
    }

    @GetMapping("/resumen")
    @Operation(
            summary = "Resumen de muestras",
            description = "Total de muestras e individuos, cantidad por estado, totales por centro y densidad "
                    + "promedio (individuos por metro = cantidad / tramoCm * 100). Solo SUPERVISOR o ANALISTA."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumen calculado"),
            @ApiResponse(responseCode = "400", description = "Fecha con formato incorrecto",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "401", description = "Sin credenciales o credenciales incorrectas",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "403", description = "Tu rol no puede ver reportes (solo SUPERVISOR o ANALISTA)",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public ResumenDto resumen(
            @Parameter(description = "Desde este día, inclusive (yyyy-MM-dd)", example = "2026-11-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @Parameter(description = "Hasta este día, inclusive (yyyy-MM-dd)", example = "2026-11-30")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta
    ) {
        return service.resumen(desde, hasta);
    }
}
