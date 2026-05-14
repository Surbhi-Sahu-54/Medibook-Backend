package com.medibook.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadDir;
    private static final List<String> ALLOWED_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "application/pdf"
    );
    private static final long MAX_SIZE = 10 * 1024 * 1024; // 10MB

    public FileStorageService(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + e.getMessage());
        }
    }

    public String storeFile(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) return null;

        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new RuntimeException("File type not allowed. Allowed: jpg, jpeg, png, pdf");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new RuntimeException("File size exceeds 10MB limit");
        }

        String original = file.getOriginalFilename();
        String ext = (original != null && original.contains("."))
                ? original.substring(original.lastIndexOf('.'))
                : "";
        String filename = UUID.randomUUID() + ext;

        try {
            Path targetDir = this.uploadDir.resolve(subDir);
            Files.createDirectories(targetDir);
            Path target = targetDir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return "uploads/" + subDir + "/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + e.getMessage());
        }
    }
}
