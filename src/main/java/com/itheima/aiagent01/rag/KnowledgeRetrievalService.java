package com.itheima.aiagent01.rag;

import com.itheima.aiagent01.dto.KnowledgeSearchResult;
import com.itheima.aiagent01.entity.KnowledgeDocument;
import com.itheima.aiagent01.repository.KnowledgeDocumentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
//检索系统
@Service
public class KnowledgeRetrievalService {

    private final KnowledgeDocumentRepository knowledgeDocumentRepository;
    private final KnowledgeEmbeddingService knowledgeEmbeddingService;

    @Value("${rag.vector-weight:1.0}")
    private double vectorWeight = 1.0;

    @Value("${rag.keyword-weight:1.0}")
    private double keywordWeight = 1.0;

    public KnowledgeRetrievalService(
            KnowledgeDocumentRepository knowledgeDocumentRepository,
            KnowledgeEmbeddingService knowledgeEmbeddingService
    ) {
        this.knowledgeDocumentRepository = knowledgeDocumentRepository;
        this.knowledgeEmbeddingService = knowledgeEmbeddingService;
    }

    public List<KnowledgeSearchResult> vectorSearchWithScore(
            String question,
            int topK,
            int minScore
    ) {
        List<KnowledgeSearchResult> results = new ArrayList<>();

        if (!StringUtils.hasText(question)) {
            return results;
        }

        double[] questionVector = knowledgeEmbeddingService.embedText(question);

        List<KnowledgeDocument> documents = knowledgeDocumentRepository.findAll();

        List<HybridScoredDocument> scoredDocuments = new ArrayList<>();

        for (KnowledgeDocument document : documents) {
            if (!StringUtils.hasText(document.getEmbeddingJson())) {
                continue;
            }

            double[] contentVector =
                    knowledgeEmbeddingService.fromEmbeddingJson(document.getEmbeddingJson());

            double similarity =
                    knowledgeEmbeddingService.cosineSimilarity(questionVector, contentVector);

            int vectorScore = (int) Math.round(similarity * 100);

            int keywordScore = calculateScore(document.getContent(), question);

            HybridScoredDocument scoredDocument =
                    new HybridScoredDocument(
                            document,
                            vectorScore,
                            keywordScore,
                            vectorWeight,
                            keywordWeight
                    );

            if (scoredDocument.getFinalScore() >= minScore) {
                scoredDocuments.add(scoredDocument);
            }
        }

        scoredDocuments.sort((a, b) -> b.getFinalScore() - a.getFinalScore());

        int limit = Math.min(topK, scoredDocuments.size());

        for (int i = 0; i < limit; i++) {
            HybridScoredDocument scored = scoredDocuments.get(i);
            KnowledgeDocument document = scored.getDocument();

            results.add(new KnowledgeSearchResult(
                    document.getId(),
                    document.getContent(),
                    scored.getFinalScore(),
                    document.getSourceFileName(),
                    document.getDocumentId(),
                    document.getChunkIndex()
            ));

            System.out.println(
                    "Hybrid 命中 chunkId=" + document.getId()
                            + ", vectorScore=" + scored.getVectorScore()
                            + ", keywordScore=" + scored.getKeywordScore()
                            + ", finalScore=" + scored.getFinalScore()
                            + ", chunkIndex=" + document.getChunkIndex()
            );
        }

        System.out.println("Hybrid 检索问题：" + question);
        System.out.println("Hybrid 最低分：" + minScore);
        System.out.println("Hybrid 命中数量：" + results.size());

        return results;
    }

    private int calculateScore(String content, String question) {
        if (!StringUtils.hasText(content) || !StringUtils.hasText(question)) {
            return 0;
        }

        String lowerContent = content.toLowerCase();
        String lowerQuestion = question.toLowerCase();

        int score = 0;

        String[] keywords = lowerQuestion
                .replace("？", " ")
                .replace("?", " ")
                .replace("，", " ")
                .replace(",", " ")
                .replace("。", " ")
                .replace(".", " ")
                .split("\\s+");

        for (String keyword : keywords) {
            if (!StringUtils.hasText(keyword)) {
                continue;
            }

            if (lowerContent.contains(keyword)) {
                score += 1;
            }
        }

        if (lowerQuestion.contains("ai agent") && lowerContent.contains("ai agent")) {
            score += 5;
        }

        if (lowerQuestion.contains("rag") && lowerContent.contains("rag")) {
            score += 5;
        }

        if (lowerQuestion.contains("切片") && lowerContent.contains("切片")) {
            score += 5;
        }

        if (lowerQuestion.contains("chunk") && lowerContent.contains("chunk")) {
            score += 5;
        }

        if (lowerQuestion.contains("工具") && lowerContent.contains("工具")) {
            score += 3;
        }

        if (lowerQuestion.contains("tool") && lowerContent.contains("tool")) {
            score += 3;
        }

        return score;
    }

    private static class HybridScoredDocument {
        private final KnowledgeDocument document;
        private final int vectorScore;
        private final int keywordScore;
        private final int finalScore;

        public HybridScoredDocument(
                KnowledgeDocument document,
                int vectorScore,
                int keywordScore,
                double vectorWeight,
                double keywordWeight
        ) {
            this.document = document;
            this.vectorScore = vectorScore;
            this.keywordScore = keywordScore;
            this.finalScore = (int) Math.round(
                    vectorScore * vectorWeight + keywordScore * keywordWeight
            );
        }

        public KnowledgeDocument getDocument() {
            return document;
        }

        public int getVectorScore() {
            return vectorScore;
        }

        public int getKeywordScore() {
            return keywordScore;
        }

        public int getFinalScore() {
            return finalScore;
        }
    }
}
