package com.abel.example.service.weaviate;

import com.abel.example.service.pdf.PdfService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class WeaviateServiceImpl implements WeaviateService {

    private static final int BATCH_SIZE = 10;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private PdfService pdfService;

    @Value("${weaviate.graphql-url}")
    private String url;

    private boolean importTextChunksToWeaviate(List<String> chunks) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            int total = chunks.size();
            for (int i = 0; i < total; i += BATCH_SIZE) {
                int end = Math.min(i + BATCH_SIZE, total);
                List<Map<String, Object>> objectsBatch = new ArrayList<>();

                for (int j = i; j < end; j++) {
                    Map<String, Object> obj = new HashMap<>();
                    obj.put("class", "Knowledge");
                    obj.put("id", UUID.randomUUID().toString());

                    Map<String, Object> props = new HashMap<>();
                    props.put("content", chunks.get(j));
                    obj.put("properties", props);

                    objectsBatch.add(obj);
                }

                // 修正点：将 objectsBatch 包装成符合 Weaviate 批量导入的格式
                Map<String, Object> batchPayload = new HashMap<>();
                batchPayload.put("objects", objectsBatch); // 关键：将数组放在 "objects" 字段下

                String jsonPayload = objectMapper.writeValueAsString(batchPayload);
                System.out.println("Payload: " + jsonPayload);

                HttpPost post = new HttpPost(url);
                post.setHeader("Content-Type", "application/json");
                post.setEntity(new StringEntity(jsonPayload, StandardCharsets.UTF_8));

                try (CloseableHttpResponse response = client.execute(post)) {
                    int status = response.getStatusLine().getStatusCode();
                    String respBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    if (status < 200 || status >= 300) {
                        System.err.println("批量导入失败，状态码：" + status + "，响应：" + respBody);
                        return false;
                    }
                    System.out.println("成功导入 " + (end - i) + " 条数据");
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean parseAndImport(MultipartFile file) throws IOException {
        String fullText = pdfService.parsePdf(file);
        // 简单按段落分块
        String[] chunks = fullText.split("\\r?\\n\\r?\\n");

        List<String> validChunks = new ArrayList<>();
        for (String chunk : chunks) {
            if (!chunk.trim().isEmpty()) {
                validChunks.add(chunk.trim());
            }
        }
        return importTextChunksToWeaviate(validChunks);
    }
}
