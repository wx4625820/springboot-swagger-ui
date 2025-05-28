package com.abel.example.service.weaviate;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface WeaviateService {
    boolean parseAndImport(MultipartFile file) throws IOException;

}
