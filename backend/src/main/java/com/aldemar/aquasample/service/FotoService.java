package com.aldemar.aquasample.service;

import com.aldemar.aquasample.exception.FotoInvalidaException;
import com.aldemar.aquasample.exception.FotoNoEncontradaException;
import com.aldemar.aquasample.exception.MuestraNoEncontradaException;
import com.aldemar.aquasample.model.Muestra;
import com.aldemar.aquasample.repository.MuestraRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Guarda y entrega las fotos de las muestras en una carpeta del servidor (app.fotos.dir).
 * En la tabla solo queda la URL para descargarla: /api/muestras/{id}/foto.
 */
@Service
public class FotoService {

    static final long TAMANO_MAXIMO = 5L * 1024 * 1024; // 5 MB

    /** Tipos aceptados y la extensión con que se guarda cada uno. */
    private static final Map<String, String> EXTENSIONES = Map.of(
            MediaType.IMAGE_JPEG_VALUE, "jpg",
            MediaType.IMAGE_PNG_VALUE, "png"
    );

    private final MuestraRepository repository;
    private final Path carpeta;

    public FotoService(MuestraRepository repository, @Value("${app.fotos.dir:./uploads}") String carpeta) {
        this.repository = repository;
        this.carpeta = Path.of(carpeta).toAbsolutePath().normalize();
    }

    /** Guarda la foto como {id}.jpg (o {id}.png) y deja fotoUrl en la muestra. */
    @Transactional
    public String guardar(String id, MultipartFile archivo) {
        Muestra muestra = repository.findById(id).orElseThrow(() -> new MuestraNoEncontradaException(id));

        if (archivo == null || archivo.isEmpty()) {
            throw new FotoInvalidaException("Adjunta una imagen en el campo \"archivo\"");
        }
        if (archivo.getSize() > TAMANO_MAXIMO) {
            throw new FotoInvalidaException("La foto supera el máximo de 5 MB");
        }
        String extension = EXTENSIONES.get(archivo.getContentType());
        if (extension == null) {
            throw new FotoInvalidaException("Solo se aceptan imágenes JPEG o PNG");
        }

        try {
            Files.createDirectories(carpeta);
            // Si antes tenía una foto con la otra extensión, se borra para no dejar dos.
            for (String ext : EXTENSIONES.values()) {
                Files.deleteIfExists(rutaSegura(id, ext));
            }
            try (InputStream entrada = archivo.getInputStream()) {
                Files.copy(entrada, rutaSegura(id, extension), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar la foto", e);
        }

        String url = "/api/muestras/" + id + "/foto";
        muestra.setFotoUrl(url);
        muestra.setActualizadoEn(LocalDateTime.now());
        repository.save(muestra);
        return url;
    }

    /** Foto para descargar: el archivo y su tipo (image/jpeg o image/png). */
    public record FotoArchivo(Resource recurso, MediaType tipo) {
    }

    @Transactional(readOnly = true)
    public FotoArchivo obtener(String id) {
        if (!repository.existsById(id)) {
            throw new MuestraNoEncontradaException(id);
        }
        for (Map.Entry<String, String> tipo : EXTENSIONES.entrySet()) {
            Path ruta = rutaSegura(id, tipo.getValue());
            if (Files.exists(ruta)) {
                return new FotoArchivo(new FileSystemResource(ruta), MediaType.parseMediaType(tipo.getKey()));
            }
        }
        throw new FotoNoEncontradaException(id);
    }

    /** Arma la ruta del archivo y comprueba que quede DENTRO de la carpeta (evita ids como "../../algo"). */
    private Path rutaSegura(String id, String extension) {
        Path ruta = carpeta.resolve(id + "." + extension).normalize();
        if (!ruta.getParent().equals(carpeta)) {
            throw new FotoInvalidaException("Id de muestra no válido");
        }
        return ruta;
    }
}
