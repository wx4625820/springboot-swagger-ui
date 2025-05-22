package com.abel.example.service.file;

import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;


public interface FileService {
    String uploadFile(MultipartFile file);

    String getDownloadUrl(String fileName);

    CompletableFuture<String> asyncUploadFileWithProgressWrapper(MultipartFile file, String uniqueFileName);

    double getProgress(String taskId);

    String getFile();

    boolean deleteFile(String fileName);
}
