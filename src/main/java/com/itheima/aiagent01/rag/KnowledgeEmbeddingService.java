package com.itheima.aiagent01.rag;

import com.itheima.aiagent01.service.EmbeddingService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;
//embeddingJson 处理
@Service
public class KnowledgeEmbeddingService {
    private final EmbeddingService embeddingService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public KnowledgeEmbeddingService(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    public double[] embedText(String text) {
        return embeddingService.embed(text);
    }

    public String toEmbeddingJson(String text){
        try {
            double[] vector = embedText(text);
            return objectMapper.writeValueAsString(vector);
        } catch (Exception e) {
            throw new RuntimeException("生成 embeddingJson 失败：" + e.getMessage());
        }
    }

    public double[] fromEmbeddingJson(String embeddingJson){
        if (!StringUtils.hasText(embeddingJson)){
            return new double[0];
        }
        try {
            return objectMapper.readValue(embeddingJson, double[].class);
        } catch (Exception e) {
            throw new RuntimeException("解析 embeddingJson 失败：" + e.getMessage());
        }
    }

    public double cosineSimilarity(double[] a, double[] b){
        return embeddingService.cosineSimilarity(a, b);
    }



}
