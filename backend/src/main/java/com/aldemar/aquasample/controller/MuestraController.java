package com.aldemar.aquasample.controller;

import com.aldemar.aquasample.dto.CambioEstadoDto;
import com.aldemar.aquasample.dto.MuestraDto;
import com.aldemar.aquasample.exception.ErrorRespuesta;
import com.aldemar.aquasample.model.EstadoRevision;
import com.aldemar.aquasample.service.FotoService;
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
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Endpoints REST de muestras. Solo recibe la petición, valida el DTO (@Valid)
 * y delega en el Service. Los permisos por rol están en SecurityConfig y
 * los errores los convierte GlobalExceptionHandler.
 */
@RestController
@RequestMapping("/api/muestras")
@Tag(name = "Muestras", description = "Registro, consulta, revisión y sincronización de muestras de choritos")
@ApiResponses({
        // Respuestas comunes a todos los endpoints de este controller
        @ApiResponse(responseCode = "401", description = "Sin credenciales o credenciales incorrectas",
                content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
        @ApiResponse(responseCode = "403", description = "Tu rol no tiene permiso para esta acción",
                content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
})
public class MuestraController {

    private final MuestraService service;
    private final FotoService fotoService;

    public MuestraController(MuestraService service, FotoService fotoService) {
        this.service = service;
        this.fotoService = fotoService;
    }

    @GetMapping
    @Operation(
            summary = "Listar muestras",
            description = "Devuelve las muestras de la más reciente a la más antigua. Todos los filtros son "
                    + "opcionales y se combinan entre sí (deben cumplirse todos). Cualquier rol."
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
    @Operation(summary = "Obtener una muestra por id", description = "Cualquier rol.")
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
            description = "Crea una muestra nueva con el id (UUID) que viene de la app. Si el id ya existe responde "
                    + "409; para actualizar usa PUT. OPERARIO o SUPERVISOR; un OPERARIO solo puede usar los "
                    + "estados BORRADOR o ENVIADA."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Muestra creada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (incluye errores por campo)",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "409", description = "Ya existe una muestra con ese id",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public ResponseEntity<MuestraDto> crear(@Valid @RequestBody MuestraDto dto, Authentication auth) {
        MuestraDto creada = service.crear(dto, auth.getName());
        return ResponseEntity.created(URI.create("/api/muestras/" + creada.id())).body(creada);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Crear o actualizar una muestra (upsert)",
            description = "Si el id no existe la crea (201); si existe la actualiza (200). Es el endpoint que usa "
                    + "la sincronización de la app: enviar la misma muestra varias veces nunca la duplica. "
                    + "OPERARIO o SUPERVISOR; un OPERARIO solo puede dejarla en BORRADOR o ENVIADA (si no, 403)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Muestra actualizada"),
            @ApiResponse(responseCode = "201", description = "Muestra creada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o id distinto al de la URL",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "409", description = "Un OPERARIO intentó modificar una muestra ya VALIDADA",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public ResponseEntity<MuestraDto> crearOActualizar(
            @Parameter(description = "UUID de la muestra") @PathVariable String id,
            @Valid @RequestBody MuestraDto dto,
            Authentication auth
    ) {
        MuestraService.ResultadoUpsert resultado = service.crearOActualizar(id, dto, auth.getName());
        HttpStatus status = resultado.creada() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(resultado.muestra());
    }

    @PatchMapping("/{id}/estado")
    @Operation(
            summary = "Cambiar el estado de una muestra (revisión)",
            description = "Solo SUPERVISOR. OBSERVADA exige comentario. Guarda en revisadoPor el nombre del "
                    + "supervisor y actualiza actualizadoEn. En cualquier estado distinto de OBSERVADA se limpia "
                    + "el comentario anterior."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "400", description = "Estado faltante u OBSERVADA sin comentario",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "404", description = "No existe una muestra con ese id",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public MuestraDto cambiarEstado(
            @Parameter(description = "UUID de la muestra") @PathVariable String id,
            @Valid @RequestBody CambioEstadoDto cambio,
            Authentication auth
    ) {
        return service.cambiarEstado(id, cambio, auth.getName());
    }

    @PostMapping(value = "/{id}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Subir la foto de una muestra",
            description = "Formulario multipart con el campo \"archivo\". Solo JPEG o PNG, máximo 5 MB. "
                    + "Reemplaza la foto anterior y deja fotoUrl = /api/muestras/{id}/foto. OPERARIO o SUPERVISOR."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Foto guardada; devuelve {\"fotoUrl\": \"...\"}"),
            @ApiResponse(responseCode = "400", description = "Falta el archivo o no es JPEG/PNG",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "404", description = "No existe una muestra con ese id",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "413", description = "La foto supera 5 MB",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public Map<String, String> subirFoto(
            @Parameter(description = "UUID de la muestra") @PathVariable String id,
            @Parameter(description = "Imagen JPEG o PNG (máx. 5 MB)") @RequestPart("archivo") MultipartFile archivo
    ) {
        return Map.of("fotoUrl", fotoService.guardar(id, archivo));
    }

    @GetMapping("/{id}/foto")
    @Operation(summary = "Descargar la foto de una muestra", description = "Devuelve la imagen (JPEG o PNG). Cualquier rol.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "La imagen",
                    content = {@Content(mediaType = "image/jpeg"), @Content(mediaType = "image/png")}),
            @ApiResponse(responseCode = "404", description = "La muestra no existe o todavía no tiene foto",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public ResponseEntity<Resource> descargarFoto(@Parameter(description = "UUID de la muestra") @PathVariable String id) {
        FotoService.FotoArchivo foto = fotoService.obtener(id);
        return ResponseEntity.ok().contentType(foto.tipo()).body(foto.recurso());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una muestra", description = "Solo SUPERVISOR.")
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
