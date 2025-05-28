package com.abel.example.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class LLMClient {

    @Value("${ollama.base-url}")
    private String url;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
}
