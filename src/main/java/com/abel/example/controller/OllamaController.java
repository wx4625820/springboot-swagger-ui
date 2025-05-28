package com.abel.example.controller;

import com.abel.example.model.response.ResponseMessage;
import com.abel.example.service.ollama.OllamaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(value = "ollama")
@Tag(name = "ollama", description = "ollama相关接口")
public class OllamaController {
    @Autowired
    private OllamaService ollamaService;

    @PostMapping("/analyze")
    public ResponseMessage analyzeResume(@RequestBody Map<String, String> request) {
        String resumeText = request.get("text");
        return ResponseMessage.success(ollamaService.analyzeResume(resumeText));
    }
}
