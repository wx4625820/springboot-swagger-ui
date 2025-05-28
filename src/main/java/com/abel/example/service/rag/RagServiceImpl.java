package com.abel.example.service.rag;

import com.abel.example.service.ollama.OllamaService;
import com.abel.example.service.weaviate.WeaviateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RagServiceImpl implements RagService {
    @Autowired
    private WeaviateService weaviateService;

    @Autowired
    private OllamaService ollamaService;


    @Override
    public String answerQuestion(String question) {
        List<String> relevantChunks = weaviateService.search(question);
        String context = String.join("\n", relevantChunks);

        String prompt = buildPrompt(question, context);

        return ollamaService.generateAnswer(prompt);
    }

    private String buildPrompt(String question, String context) {
        return "以下是与问题相关的知识片段，请根据这些内容用中文回答问题：\n\n"
                + context + "\n\n问题：" + question;
    }

}
