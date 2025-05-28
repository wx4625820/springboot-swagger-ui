package com.abel.example.service.pdf;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface PdfService {
    String parsePdf(MultipartFile file) throws IOException;
}
