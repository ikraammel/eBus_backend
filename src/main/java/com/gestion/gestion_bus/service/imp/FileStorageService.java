package com.gestion.gestion_bus.service.imp;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads");


    public FileStorageService() throws IOException {
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
    }

    public String save(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadDir.resolve(filename);

            Files.copy(file.getInputStream(), filePath);

            // IMPORTANT : retourner une URL web et non un chemin disque
            return "/uploads/" + filename;

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'enregistrement du fichier", e);
        }
    }

}
