package com.abel.example.service.ollama;

import java.util.Map;

public interface OllamaService {
    Map<String, Object> analyzeResume(String resumeText);


    String generateAnswer(String prompt);
}
