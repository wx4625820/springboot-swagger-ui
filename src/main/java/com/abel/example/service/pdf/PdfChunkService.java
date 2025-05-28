package com.abel.example.service.pdf;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface PdfChunkService {
    List<String> parsePdfToChunks(MultipartFile file) throws IOException;
}
