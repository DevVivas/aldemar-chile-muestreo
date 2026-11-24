package com.aldemar.aquasample.controller;

import com.aldemar.aquasample.dto.MuestraDto;
import com.aldemar.aquasample.exception.ErrorRespuesta;
import com.aldemar.aquasample.model.EstadoRevision;
import com.aldemar.aquasample.service.MuestraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

/**
 * Endpoints REST de muestras. Solo recibe la petición, valida el DTO (@Valid)
 * y delega en el Service. Los errores los convierte GlobalExceptionHandler.
 */
@RestController
@RequestMapping("/api/muestras")
@Tag(name = "Muestras", description = "Registro, consulta y sincronización de muestras de choritos")
public class MuestraController {

    private final MuestraService service;

    public MuestraController(MuestraService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(
            summary = "Listar muestras",
            description = "Devuelve las muestras de la más reciente a la más antigua. Todos los filtros son "
                    + "opcionales y se combinan entre sí (deben cumplirse todos)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de muestras (puede venir vacía)",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MuestraDto.class)))),
            @ApiResponse(responseCode = "400", description = "Algún filtro tiene formato incorrecto",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public List<MuestraDto> listar(
            @Parameter(description = "Centro de cultivo exacto", example = "Calbuco Sur")
            @RequestParam(required = false) String centro,
            @Parameter(description = "Tren exacto", example = "Tren 2 - Módulo B")
            @RequestParam(required = false) String tren,
            @Parameter(description = "Línea exacta", example = "Línea 15")
            @RequestParam(required = false) String linea,
            @Parameter(description = "Estado de revisión")
            @RequestParam(required = false) EstadoRevision estado,
            @Parameter(description = "Desde este día, inclusive (yyyy-MM-dd)", example = "2026-11-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @Parameter(description = "Hasta este día, inclusive (yyyy-MM-dd)", example = "2026-11-30")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta
    ) {
        return service.listar(centro, tren, linea, estado, desde, hasta);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una muestra por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Muestra encontrada"),
            @ApiResponse(responseCode = "404", description = "No existe una muestra con ese id",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public MuestraDto obtener(@Parameter(description = "UUID de la muestra") @PathVariable String id) {
        return service.obtener(id);
    }

    @PostMapping
    @Operation(
            summary = "Crear una muestra",
            description = "Crea una muestra nueva con el id (UUID) que viene de la app. "
                    + "Si el id ya existe responde 409; para actualizar usa PUT."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Muestra creada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (incluye errores por campo)",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "409", description = "Ya existe una muestra con ese id",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public ResponseEntity<MuestraDto> crear(@Valid @RequestBody MuestraDto dto) {
        MuestraDto creada = service.crear(dto);
        return ResponseEntity.created(URI.create("/api/muestras/" + creada.id())).body(creada);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Crear o actualizar una muestra (upsert)",
            description = "Si el id no existe la crea (201); si existe la actualiza (200). "
                    + "Es el endpoint que usa la sincronización de la app: enviar la misma muestra "
                    + "varias veces nunca la duplica. El id de la URL debe coincidir con el del cuerpo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Muestra actualizada"),
            @ApiResponse(responseCode = "201", description = "Muestra creada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o id distinto al de la URL",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public ResponseEntity<MuestraDto> crearOActualizar(
            @Parameter(description = "UUID de la muestra") @PathVariable String id,
            @Valid @RequestBody MuestraDto dto
    ) {
        MuestraService.ResultadoUpsert resultado = service.crearOActualizar(id, dto);
        HttpStatus status = resultado.creada() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(resultado.muestra());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una muestra")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Muestra eliminada"),
            @ApiResponse(responseCode = "404", description = "No existe una muestra con ese id",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public ResponseEntity<Void> eliminar(@Parameter(description = "UUID de la muestra") @PathVariable String id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
