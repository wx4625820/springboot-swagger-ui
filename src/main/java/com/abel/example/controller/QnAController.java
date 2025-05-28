package com.abel.example.controller;


import com.abel.example.model.response.ResponseMessage;
import com.abel.example.service.qna.QnAService;
import com.abel.example.service.weaviate.WeaviateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;


@RestController
@RequestMapping(value = "rag")
@Tag(name = "rag", description = "RAG问答系统")
public class QnAController {

    @Autowired
    private QnAService qnaService;


    @Autowired
    private WeaviateService weaviateService;


    @PostMapping("/ask")
    @Operation(summary = "RAG 问答接口", description = "根据 Java、测试、产品岗等知识库进行问答")
    public ResponseMessage askQuestion(@RequestBody Map<String, String> requestBody) {
        String question = requestBody.get("question");
        String answer = qnaService.answerQuestion(question);
        return ResponseMessage.success(answer);
    }

    @Operation(summary = "RAG 上传知识", description = "上传RAG到Weaviate")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, value = "parse-and-import")
    public ResponseMessage parseAndImport(@RequestParam("file") MultipartFile file) {
        Boolean res = null;
        try {
            res = weaviateService.parseAndImport(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseMessage.success(res);
    }
}
