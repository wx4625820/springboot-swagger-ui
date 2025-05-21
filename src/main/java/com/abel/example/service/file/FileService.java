package com.abel.example.service.file;

import org.springframework.web.multipart.MultipartFile;


public interface FileService {
    String uploadFile(MultipartFile file);

    String getDownloadUrl(String fileName);
}
