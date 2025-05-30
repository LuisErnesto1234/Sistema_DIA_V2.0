package com.spring.udemy.controlagua.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class ImagenService {

    public String guardarImagen(MultipartFile file, String imagenActual) throws IOException {
        String rutaBase = System.getProperty("user.dir");
        String rutaSubida = rutaBase + File.separator + "uploads" + File.separator + "perfil";
        File carpetaSubida = new File(rutaSubida);

        // Crear la carpeta si no existe
        if (!carpetaSubida.exists()) {
            if (!carpetaSubida.mkdirs()) {
                throw new RuntimeException("No se pudo crear la carpeta de subida");
            }
        }

        // Eliminar la imagen actual si existe
        if (imagenActual != null && !imagenActual.isEmpty()) {
            String rutaImagenActual = rutaBase + imagenActual.replace("/", File.separator);
            File archivoImagenActual = new File(rutaImagenActual);
            if (archivoImagenActual.exists()) {
                if (!archivoImagenActual.delete()) {
                    throw new RuntimeException("No se pudo eliminar la imagen anterior");
                }
            }
        }

        // Guardar la nueva imagen
        String nombreImagen = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path rutaImagen = Paths.get(rutaSubida + File.separator + nombreImagen);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, rutaImagen, StandardCopyOption.REPLACE_EXISTING);
        }

        return "/uploads/perfil/" + nombreImagen;
    }

}
