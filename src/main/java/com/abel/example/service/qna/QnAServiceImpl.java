package com.abel.example.service.qna;

import com.abel.example.client.LLMClient;
import com.abel.example.client.WeaviateClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QnAServiceImpl implements QnAService {
    @Autowired
    private WeaviateClient weaviateClient;
    @Autowired
    private LLMClient llmClient;


    public String answerQuestion(String question) {
        List<String> relevantChunks = weaviateClient.search(question);
        String context = String.join("\n", relevantChunks);

        String prompt = "以下是与问题相关的知识片段，请根据这些内容用中文回答问题：\n\n"
                + context + "\n\n问题：" + question;

        return llmClient.generateAnswer(prompt);
    }
}
