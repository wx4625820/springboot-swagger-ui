package com.abel.example.service.python.impl;

import com.abel.example.model.entity.VideoMetaDataWrapper;
import com.abel.example.service.python.PythonService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PythonServiceImpl implements PythonService {

    @Value("${python.api.url}")
    private String pythonApiUrl;

    @Autowired
    private OkHttpClient httpClient;

    @Autowired
    private ObjectMapper objectMapper;

    public VideoMetaDataWrapper analyzeVideo(String videoUrl) {
        int count = 0;
        try {
            // 构建 JSON 请求体
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            String jsonBody = "{\"video_url\":\"" + videoUrl + "\"}";

            RequestBody body = RequestBody.create(jsonBody, JSON);

            Request request = new Request.Builder()
                    .url(pythonApiUrl + "/api/video/analyze")
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("请求 Python 服务失败: HTTP {}", response.code());
                    throw new RuntimeException("调用 Python 接口失败，状态码：" + response.code());
                }
                String json = response.body() != null ? response.body().string() : "";
                VideoMetaDataWrapper metadata = objectMapper.readValue(json, VideoMetaDataWrapper.class);
                count++;
                System.out.println(count);
                return metadata;
            }
        } catch (Exception e) {
            log.error("调用 Python 接口失败", e);
            throw new RuntimeException("调用 Python 服务异常: " + e.getMessage(), e);
        }
    }
}
