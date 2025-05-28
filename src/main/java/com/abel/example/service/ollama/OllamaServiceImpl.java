package com.abel.example.service.ollama;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class OllamaServiceImpl implements OllamaService {


    @Value("${ollama.base-url}")
    private String url;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> analyzeResume(String resumeText) {
        Map<String, Object> result = new HashMap<>();
        try {
            String prompt = buildPrompt(resumeText);
            String requestBody = buildRequestBody(prompt);
            String response = sendRequestToOllama(requestBody);
            Map<String, Object> parsed = parseOllamaResponse(response);

            result.put("success", true);
            result.put("data", parsed);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @Override
    public String generateAnswer(String prompt) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);
            post.setHeader("Content-Type", "application/json");

            // 构造请求体
            Map<String, Object> payload = new HashMap<>();
            payload.put("model", "qwen3:0.6b");
            payload.put("prompt", prompt);
            payload.put("stream", false);

            String jsonPayload = objectMapper.writeValueAsString(payload);
            post.setEntity(new StringEntity(jsonPayload, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = httpClient.execute(post)) {
                String jsonResponse = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                // 解析返回的 JSON
                Map<String, Object> map = objectMapper.readValue(jsonResponse, Map.class);

                return map.get("response").toString();
            }
        } catch (Exception e) {
            return "调用生成接口失败：" + e.getMessage();
        }
    }

    private String buildPrompt(String resumeText) {
        return String.format("""
                你是一个专业的中文简历顾问，请对以下简历进行分析，包括：
                1. 总体评分（满分100）；
                2. 技能匹配度评分；
                3. 项目经验丰富度评分；
                4. 语言表达清晰度评分；
                5. 提出3条优化建议；

                请用中文输出分析内容，并严格输出如下结构的 JSON（不要照抄示例，仅用于格式参考）：

                {
                  "totalScore": 数字,
                  "skillScore": 数字,
                  "projectScore": 数字,
                  "languageScore": 数字,
                  "suggestions": ["建议1", "建议2", "建议3"]
                }

                简历内容如下：
                %s
                """, resumeText);
    }


    private String buildRequestBody(String prompt) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("model", "qwen3:0.6b");
        body.put("prompt", prompt);
        body.put("stream", false);
        return objectMapper.writeValueAsString(body);
    }

    private String sendRequestToOllama(String jsonBody) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = client.execute(post)) {
                return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            }
        }
    }

    private Map<String, Object> parseOllamaResponse(String rawResponse) throws Exception {
        Map<String, String> topLevel = objectMapper.readValue(rawResponse, Map.class);
        String content = topLevel.get("response");

        try {
            return objectMapper.readValue(content, Map.class);
        } catch (Exception e) {
            // fallback：模型输出了非 JSON 内容
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("raw", content);
            fallback.put("parseError", "模型未按 JSON 格式返回");
            return fallback;
        }
    }
}
