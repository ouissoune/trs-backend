package net.kilmerx.trs.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    @Value("${file.upload.dir}")
    private String uploadDir;

    public String storeFile(MultipartFile file) throws IOException {
        File uploadFolder = new File(uploadDir);
        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs();
        }

        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null) {
            int lastDot = originalName.lastIndexOf('.');
            if (lastDot >= 0 && lastDot < originalName.length() - 1) {
                extension = originalName.substring(lastDot);
            }
        }
        String fileName = UUID.randomUUID() + extension;
        Path filePath = Paths.get(uploadDir, fileName);

        Files.write(filePath, file.getBytes());
        log.info("File stored successfully: {}", fileName);

        return fileName;
    }

    public byte[] retrieveFile(String fileName) throws IOException {
        Path filePath = Paths.get(uploadDir, fileName);

        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + fileName);
        }

        return Files.readAllBytes(filePath);
    }

    public void deleteFile(String fileName) throws IOException {
        Path filePath = Paths.get(uploadDir, fileName);

        if (Files.exists(filePath)) {
            Files.delete(filePath);
            log.info("File deleted successfully: {}", fileName);
        } else {
            throw new IOException("File not found: " + fileName);
        }
    }

    public String getFileUrl(String fileName) {
        return "/api/public/files/download/" + fileName;
    }
}
