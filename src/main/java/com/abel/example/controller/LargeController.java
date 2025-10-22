package com.abel.example.controller;


import com.abel.example.model.dto.RequestDTO.AskQuestionRequest;
import com.abel.example.model.response.ResponseMessage;
import com.abel.example.service.pdf.PdfChunkService;
import com.abel.example.service.rag.RagService;
import com.abel.example.service.weaviate.WeaviateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping(value = "/rag")
@Tag(name = "rag", description = "RAG问答系统")
@Slf4j
public class LargeController {

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

    @PostMapping("/analyze-resume")
    public void analyzeResume(@RequestBody String content, HttpServletResponse response) {
        response.setContentType("text/plain;charset=UTF-8");
        ObjectMapper objectMapper = new ObjectMapper();

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost("http://192.168.31.74:5000/api/resume/analyze");
            post.setHeader("Content-Type", "application/json");

            // 构造 JSON 数据
            Map<String, String> data = new HashMap<>();
            data.put("resume_content", content);
            String json = objectMapper.writeValueAsString(data); // 自动处理转义问题
            post.setEntity(new StringEntity(json, StandardCharsets.UTF_8));

            try (CloseableHttpResponse upstreamResponse = client.execute(post);
                 var in = new BufferedReader(new InputStreamReader(upstreamResponse.getEntity().getContent()));
                 var out = response.getWriter()) {

                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println(line);
                    out.write(line + "\n");
                    out.flush();
                }
            }
        } catch (IOException e) {
            log.error("LargeController#analyzeResume error: {}", e.getMessage(), e);
        }
    }

    @PostMapping("/analyze-video")
    public void analyzeVideo(@RequestBody Map<String, String> body, HttpServletResponse response) {
        String url = body.get("url");
        response.setContentType("text/plain;charset=UTF-8");
        ObjectMapper objectMapper = new ObjectMapper();

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost("http://192.168.31.74:5000/api/video/analyze_interview");
            post.setHeader("Content-Type", "application/json");

            // 构造 JSON 数据
            Map<String, String> data = new HashMap<>();
            data.put("video_url", url);
            String json = objectMapper.writeValueAsString(data); // 自动处理转义问题
            post.setEntity(new StringEntity(json, StandardCharsets.UTF_8));

            try (CloseableHttpResponse upstreamResponse = client.execute(post);
                 var in = new BufferedReader(new InputStreamReader(upstreamResponse.getEntity().getContent()));
                 var out = response.getWriter()) {

                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println(line);
                    out.write(line + "\n");
                    out.flush();
                }
            }
        } catch (IOException e) {
            log.error("LargeController#analyzeVideo error: {}", e.getMessage(), e);
        }
    }
}
