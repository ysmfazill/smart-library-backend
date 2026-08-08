package com.smartlibrary.service.impl;

import com.smartlibrary.exception.ResourceNotFoundException;
import com.smartlibrary.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageServiceImpl(@Value("${file.storage.dir:${FILE_STORAGE_DIR:./uploads}}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("Initialized persistent file storage directory at: {}", this.fileStorageLocation);
        } catch (Exception ex) {
            log.error("Could not create the directory where uploaded files will be stored.", ex);
            throw new RuntimeException("Could not create storage directory.", ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store an empty file.");
        }

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        
        // Prevent path traversal
        if (originalFilename.contains("..")) {
            throw new IllegalArgumentException("Filename contains invalid path sequence: " + originalFilename);
        }

        String fileExtension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex >= 0) {
            fileExtension = originalFilename.substring(dotIndex);
        }

        // Generate unique name
        String storedFileName = UUID.randomUUID().toString() + fileExtension;

        try {
            Path targetDir = this.fileStorageLocation;
            if (StringUtils.hasText(subDir)) {
                targetDir = this.fileStorageLocation.resolve(subDir).normalize();
                Files.createDirectories(targetDir);
            }

            Path targetLocation = targetDir.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("Successfully stored file {} as {}", originalFilename, targetLocation);

            if (StringUtils.hasText(subDir)) {
                return subDir + "/" + storedFileName;
            }
            return storedFileName;
        } catch (IOException ex) {
            log.error("Could not store file {}. Please try again!", originalFilename, ex);
            throw new RuntimeException("Could not store file " + originalFilename, ex);
        }
    }

    @Override
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            
            // Safety check against escaping root directory
            if (!filePath.startsWith(this.fileStorageLocation)) {
                throw new IllegalArgumentException("Invalid file path attempt");
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                log.warn("File not found or not readable: {}", fileName);
                throw new ResourceNotFoundException("File", "fileName", fileName);
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("File", "fileName", fileName);
        }
    }

    @Override
    public void deleteFile(String fileName) {
        if (!StringUtils.hasText(fileName)) return;
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            if (filePath.startsWith(this.fileStorageLocation)) {
                Files.deleteIfExists(filePath);
                log.info("Deleted file: {}", fileName);
            }
        } catch (IOException ex) {
            log.error("Error deleting file {}", fileName, ex);
        }
    }
}
