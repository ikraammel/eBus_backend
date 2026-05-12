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

    private final Path privateDir = Paths.get(System.getProperty("user.dir"), "uploads", "private");
    private final Path publicDir  = Paths.get(System.getProperty("user.dir"), "uploads", "public");

    public FileStorageService() throws IOException {
        Files.createDirectories(privateDir);
        Files.createDirectories(publicDir);
    }

    /** Fichiers sensibles (CIN, carte scolaire) — accès admin uniquement */
    public String savePrivate(MultipartFile file) {
        return doSave(file, privateDir, "/uploads/private/");
    }

    /** Fichiers publics (objets perdus, avatar) — accès libre */
    public String savePublic(MultipartFile file) {
        return doSave(file, publicDir, "/uploads/public/");
    }

    /** Conservé pour rétro-compatibilité (avatars existants) */
    public String save(MultipartFile file) {
        return savePublic(file);
    }

    private String doSave(MultipartFile file, Path dir, String urlPrefix) {
        if (file == null || file.isEmpty()) return null;
        try {
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), dir.resolve(filename));
            return urlPrefix + filename;
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'enregistrement du fichier", e);
        }
    }
}