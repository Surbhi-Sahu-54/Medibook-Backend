package com.medibook.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void storeFileReturnsNullForEmptyInput() {
        FileStorageService service = new FileStorageService(tempDir.toString());

        assertNull(service.storeFile(null, "photos"));
        assertNull(service.storeFile(new MockMultipartFile("file", new byte[0]), "photos"));
    }

    @Test
    void storeFilePersistsAllowedFile() {
        FileStorageService service = new FileStorageService(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile("file", "license.pdf", "application/pdf", "pdf".getBytes());

        String path = service.storeFile(file, "licenses");

        assertTrue(path.startsWith("uploads/licenses/"));
        assertTrue(Files.exists(tempDir.resolve(path.replace("uploads/", ""))));
    }

    @Test
    void storeFileRejectsUnsupportedContentType() {
        FileStorageService service = new FileStorageService(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile("file", "script.exe", "application/x-msdownload", "bad".getBytes());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.storeFile(file, "licenses"));

        assertTrue(exception.getMessage().contains("File type not allowed"));
    }
}
