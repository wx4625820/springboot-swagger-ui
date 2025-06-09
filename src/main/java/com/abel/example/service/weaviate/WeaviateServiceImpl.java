package com.abel.example.service.weaviate;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WeaviateServiceImpl implements WeaviateService {

    private static final int BATCH_SIZE = 10;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${weaviate.graphql-url}")
    private String graphqlUrl;

    @Value("${weaviate.batch-url}")
    private String batchUrl;

    @Override
    public boolean importChunks(List<String> chunks) {
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

                Map<String, Object> batchPayload = new HashMap<>();
                batchPayload.put("objects", objectsBatch);

                String jsonPayload = objectMapper.writeValueAsString(batchPayload);
                System.out.println("Payload: " + jsonPayload);

                HttpPost post = new HttpPost(batchUrl);
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
    public List<String> search(String query) {
        JSONObject graphqlPayload = buildGraphQLPayload(query);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(graphqlUrl);
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(graphqlPayload.toString(), StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = httpClient.execute(post)) {
                String jsonResponse = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                logDebugInfo(graphqlPayload.toString(), jsonResponse);

                return parseSearchResults(jsonResponse);
            }
        } catch (Exception e) {
            return handleSearchError(e);
        }
    }

    private JSONObject buildGraphQLPayload(String query) {
        JSONObject payload = new JSONObject();
        String sanitizedQuery = sanitizeQuery(query);
        String graphqlQuery = String.format(
                "{ Get { Knowledge( bm25: { query: \"%s\" }, limit: 3 ) { content } } }",
                sanitizedQuery
        );
        payload.put("query", graphqlQuery);
        return payload;
    }

    private String sanitizeQuery(String query) {
        return query.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", " ")
                .trim();
    }

    private List<String> parseSearchResults(String jsonResponse) throws Exception {
        Map<String, Object> responseMap = objectMapper.readValue(
                jsonResponse,
                new TypeReference<Map<String, Object>>() {}
        );

        if (responseMap.containsKey("errors")) {
            return Collections.singletonList("查询错误: " + responseMap.get("errors"));
        }

        return Optional.ofNullable(responseMap)
                .map(r -> (Map<String, Object>) r.get("data"))
                .map(d -> (Map<String, Object>) d.get("Get"))
                .map(g -> (List<Map<String, Object>>) g.get("Knowledge"))
                .orElse(Collections.emptyList())
                .stream()
                .map(item -> (String) item.get("content"))
                .collect(Collectors.toList());
    }

    private List<String> handleSearchError(Exception e) {
        String errorMsg = "检索失败: " + e.getMessage();
        System.err.println(errorMsg);
        return Collections.singletonList(errorMsg);
    }

    private void logDebugInfo(String request, String response) {
        System.out.println("===== 调试信息 =====");
        System.out.println("GraphQL 请求:\n" + request);
        System.out.println("Weaviate 响应:\n" + response);
        System.out.println("===================");
    }
}
