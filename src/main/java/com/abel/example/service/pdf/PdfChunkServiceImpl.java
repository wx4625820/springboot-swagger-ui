package com.abel.example.service.pdf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PdfChunkServiceImpl implements PdfChunkService {

    @Autowired
    private PdfService pdfService;

    public List<String> parsePdfToChunks(MultipartFile file) throws IOException {
        String fullText = pdfService.parsePdf(file);
        return Arrays.stream(fullText.split("\\r?\\n\\r?\\n"))
                .filter(s -> !s.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.toList());
    }
}
