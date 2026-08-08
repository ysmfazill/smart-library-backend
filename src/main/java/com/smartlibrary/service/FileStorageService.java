package com.smartlibrary.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    /**
     * Stores a file on persistent storage and returns the generated access URL or file path key.
     *
     * @param file Uploaded file.
     * @param subDir Optional subdirectory (e.g., "books", "covers").
     * @return Generated file storage key or relative URL.
     */
    String storeFile(MultipartFile file, String subDir);

    /**
     * Loads a file resource by filename or path.
     *
     * @param fileName Path or filename.
     * @return Loaded Spring Resource.
     */
    Resource loadFileAsResource(String fileName);

    /**
     * Deletes a file by filename or path.
     *
     * @param fileName Path or filename.
     */
    void deleteFile(String fileName);
}
