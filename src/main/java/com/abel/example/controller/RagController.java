package com.abel.example.controller;


import com.abel.example.model.dto.RequestDTO.AskQuestionRequest;
import com.abel.example.model.response.ResponseMessage;
import com.abel.example.service.pdf.PdfChunkService;
import com.abel.example.service.rag.RagService;
import com.abel.example.service.weaviate.WeaviateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping(value = "/rag")
@Tag(name = "rag", description = "RAG问答系统")
public class RagController {

    @Autowired
    private RagService ragService;


    @Autowired
    private WeaviateService weaviateService;

    @Autowired
    private PdfChunkService pdfChunkService;


    @PostMapping("/ask")
    @Operation(summary = "RAG 问答接口", description = "根据 Java、测试、产品岗等知识库进行问答")
    public ResponseMessage askQuestion(@Valid @RequestBody AskQuestionRequest request) {
        String question = request.getQuestion();
        String answer = ragService.answerQuestion(question);
        return ResponseMessage.success(answer);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, value = "/parse-and-import")
    @Operation(summary = "RAG 上传知识", description = "上传RAG到Weaviate")
    public ResponseMessage parseAndImport(@RequestParam("file") MultipartFile file) {
        try {
            List<String> chunks = pdfChunkService.parsePdfToChunks(file);
            boolean success = weaviateService.importChunks(chunks);
            return ResponseMessage.success(success ? "导入成功" : "导入失败");
        } catch (IOException e) {
            return ResponseMessage.error("文件解析失败: " + e.getMessage());
        } catch (Exception e) {
            return ResponseMessage.error("导入异常: " + e.getMessage());
        }
    }

}
