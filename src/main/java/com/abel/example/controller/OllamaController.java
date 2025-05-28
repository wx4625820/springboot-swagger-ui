package com.abel.example.controller;

import com.abel.example.model.dto.RequestDTO.AnalyzeResumeRequest;
import com.abel.example.model.response.ResponseMessage;
import com.abel.example.service.ollama.OllamaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/ollama")
@Tag(name = "ollama", description = "ollama相关接口")
@Slf4j
public class OllamaController {
    @Autowired
    private OllamaService ollamaService;

    @PostMapping("/analyze")
    public ResponseMessage analyzeResume(@Valid @RequestBody AnalyzeResumeRequest request) {
        String text = request.getText();
        log.info("OllamaController#analyzeResume, request:{}", text);
        return ResponseMessage.success(ollamaService.analyzeResume(text));
    }
}
