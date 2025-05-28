package com.abel.example.service.weaviate;


import java.util.List;

public interface WeaviateService {
    boolean importChunks(List<String> chunks);
    List<String> search(String query);

}
