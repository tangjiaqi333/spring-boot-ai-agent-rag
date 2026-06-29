package com.itheima.aiagent01.rag;

import com.itheima.aiagent01.common.Result;
import com.itheima.aiagent01.dto.KnowledgeDocumentResponse;
import com.itheima.aiagent01.dto.KnowledgeDocumentSummaryResponse;
import com.itheima.aiagent01.entity.KnowledgeDocument;
import com.itheima.aiagent01.repository.KnowledgeDocumentRepository;
import com.itheima.aiagent01.service.EmbeddingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import com.itheima.aiagent01.dto.KnowledgeSearchResult;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class KnowledgeBaseService {

    private final KnowledgeDocumentRepository knowledgeDocumentRepository;
    private final EmbeddingService embeddingService;

    @Value("${rag.vector-weight:1.0}")
    private double vectorWidth;

    @Value("${rag.vector-weight:1.0}")
    private double keywordWidth;

    public KnowledgeBaseService(
            KnowledgeDocumentRepository knowledgeDocumentRepository,
            EmbeddingService embeddingService
    ) {
        this.knowledgeDocumentRepository = knowledgeDocumentRepository;
        this.embeddingService = embeddingService;
    }

    public void addDocument(String content) {
        if (StringUtils.hasText(content)) {
            KnowledgeDocument document = new KnowledgeDocument(content);
            knowledgeDocumentRepository.save(document);

            System.out.println("知识已保存到 PostgreSQL：" + content);
        }
    }

    public List<String> search(String question) {
        return search(question, 3);
    }

    public List<String> search(String question, int topK) {
        List<String> results = new ArrayList<>();

        if (!StringUtils.hasText(question)) {
            return results;
        }

        List<KnowledgeDocument> documents = knowledgeDocumentRepository.findAll();

        List<ScoredDocument> scoredDocuments = new ArrayList<>();

        for (KnowledgeDocument document : documents) {
            String content = document.getContent();

            int score = calculateScore(content, question);

            if (score > 0) {
                scoredDocuments.add(new ScoredDocument(document, score));
            }
        }

        scoredDocuments.sort((a, b) -> b.getScore() - a.getScore());

        System.out.println("TopK 检索问题：" + question);
        System.out.println("TopK 候选数量：" + scoredDocuments.size());

        for (ScoredDocument scoredDocument : scoredDocuments) {
            System.out.println(
                    "命中 chunkId="
                            + scoredDocument.getDocument().getId()
                            + ", score="
                            + scoredDocument.getScore()
                            + ", chunkIndex="
                            + scoredDocument.getDocument().getChunkIndex()
            );
        }

        int limit = Math.min(topK, scoredDocuments.size());

        for (int i = 0; i < limit; i++) {
            results.add(scoredDocuments.get(i).getDocument().getContent());
        }

        System.out.println("TopK 检索问题：" + question);
        System.out.println("TopK 命中数量：" + results.size());

        return results;
    }

    public List<KnowledgeDocumentResponse> listDocuments() {
        List<KnowledgeDocument> documents = knowledgeDocumentRepository.findAll();

        List<KnowledgeDocumentResponse> responses = new ArrayList<>();

        for (KnowledgeDocument document : documents) {
            responses.add(new KnowledgeDocumentResponse(
                    document.getId(),
                    document.getContent(),
                    document.getCreatedAt(),
                    document.getSourceFileName(),
                    document.getDocumentId(),
                    document.getChunkIndex(),
                    document.getTotalChunks()
            ));
        }

        return responses;
    }

    public boolean deleteDocument(Long id) {
        if (id == null) {
            return false;
        }

        if (!knowledgeDocumentRepository.existsById(id)) {
            return false;
        }

        knowledgeDocumentRepository.deleteById(id);
        return true;
    }

    public boolean updateDocument(Long id, String content) {
        if (id == null || !StringUtils.hasText(content)) {
            return false;
        }

        Optional<KnowledgeDocument> optionalDocument =
                knowledgeDocumentRepository.findById(id);

        if (optionalDocument.isEmpty()) {
            return false;
        }

        KnowledgeDocument document = optionalDocument.get();
        document.setContent(content);
        knowledgeDocumentRepository.save(document);

        return true;
    }

    public void uploadDocument(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null) {
            throw new RuntimeException("文件名不能为空");
        }

        if (!fileName.endsWith(".txt") && !fileName.endsWith(".md")) {
            throw new RuntimeException("目前只支持 txt 和 md 文件");
        }

        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);

            if (!StringUtils.hasText(content)) {
                throw new RuntimeException("文件内容不能为空");
            }

            List<String> chunks = splitText(content, 500, 100);

            String documentId = UUID.randomUUID().toString();

            System.out.println("文件名：" + fileName);
            System.out.println("文件长度：" + content.length());
            System.out.println("切片数量：" + chunks.size());
            System.out.println("documentId：" + documentId);

            ObjectMapper mapper = new ObjectMapper();

            for (int i = 0; i < chunks.size(); i++) {
                String  chunk = chunks.get(i);
                // 1. 生成 embedding（关键)
                double[] vector = embeddingService.embed(chunk);
                // 2. 转 JSON
                String embeddingJson = mapper.writeValueAsString(vector);
                // 3. 构建 document
                KnowledgeDocument document = new KnowledgeDocument(
                        chunk,
                        fileName,
                        documentId,
                        i,
                        chunks.size()
                );
                // 4. 存 embedding
                document.setEmbeddingJson(embeddingJson);
                // 5. 入库
                knowledgeDocumentRepository.save(document);
            }

        } catch (Exception e) {
            throw new RuntimeException("读取文件失败：" + e.getMessage());
        }
    }

    public boolean deleteByDocumentId(String documentId) {
        if (!StringUtils.hasText(documentId)) {
            return false;
        }

        List<KnowledgeDocument> documents =
                knowledgeDocumentRepository.findByDocumentId(documentId);

        if (documents.isEmpty()) {
            return false;
        }

        knowledgeDocumentRepository.deleteAll(documents);

        return true;
    }

    public List<KnowledgeDocumentSummaryResponse> listDocumentSummaries() {
        List<KnowledgeDocument> documents = knowledgeDocumentRepository.findAll();

        Map<String, KnowledgeDocumentSummaryResponse> summaryMap = new LinkedHashMap<>();

        for (KnowledgeDocument document : documents) {
            String documentId = document.getDocumentId();

            if (!StringUtils.hasText(documentId)) {
                continue;
            }

            if (!summaryMap.containsKey(documentId)) {
                summaryMap.put(
                        documentId,
                        new KnowledgeDocumentSummaryResponse(
                                document.getDocumentId(),
                                document.getSourceFileName(),
                                document.getTotalChunks(),
                                document.getCreatedAt()
                        )
                );
            }
        }

        return new ArrayList<>(summaryMap.values());
    }

    private List<String> splitText(String text, int chunkSize, int overlapSize) {
        List<String> chunks = new ArrayList<>();

        if (!StringUtils.hasText(text)) {
            return chunks;
        }

        if (chunkSize <= 0) {
            throw new RuntimeException("分块大小必须大于 0");
        }

        if (overlapSize < 0 || overlapSize >= chunkSize) {
            throw new RuntimeException("重叠大小必须大于等于 0 且小于分块大小");
        }

        int start = 0;

        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());

            String chunk = text.substring(start, end).trim();

            if (StringUtils.hasText(chunk)) {
                chunks.add(chunk);
            }

            if (end == text.length()) {
                break;
            }

            start = end - overlapSize;
        }

        return chunks;
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

    private static class ScoredDocument {
        private final KnowledgeDocument document;
        private final int score;

        public ScoredDocument(KnowledgeDocument document, int score) {
            this.document = document;
            this.score = score;
        }

        public KnowledgeDocument getDocument() {
            return document;
        }

        public int getScore() {
            return score;
        }
    }

    public List<KnowledgeSearchResult> searchWithScore(String question, int topK) {
        List<KnowledgeSearchResult> results = new ArrayList<>();

        if (!StringUtils.hasText(question)) {
            return results;
        }
        List<KnowledgeDocument> documents = knowledgeDocumentRepository.findAll();

        List<ScoredDocument> scoredDocuments = new ArrayList<>();

        for (KnowledgeDocument document : documents) {
            String content = document.getContent();
            int score = calculateScore(content, question);
            if (score > 0) {
                scoredDocuments.add(new ScoredDocument(document, score));
            }
        }

        scoredDocuments.sort((a,b) -> b.getScore() - a.getScore());

        int limit = Math.min(topK, scoredDocuments.size());

        for (int i = 0; i < limit; i++) {
            ScoredDocument scored = scoredDocuments.get(i);
            KnowledgeDocument document = scored.getDocument();

            results.add(new KnowledgeSearchResult(
                    document.getId(),
                    document.getContent(),
                    scored.getScore(),
                    document.getSourceFileName(),
                    document.getDocumentId(),
                    document.getChunkIndex()
            ));
        }

        System.out.println("TopK Source 检索问题：" + question);
        System.out.println("TopK Source 命中数量：" + results.size());

        return results;
    }

    public List<KnowledgeSearchResult> vectorSearchWithScore(String question, int topK) {
        return vectorSearchWithScore(question, topK, 20);
    }

    public List<KnowledgeSearchResult> vectorSearchWithScore(String question, int topK, int minScore) {
        List<KnowledgeSearchResult> results = new ArrayList<>();
        if (!StringUtils.hasText(question)) {
            return results;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();

            double[] questionVector = embeddingService.embed(question);

            List<KnowledgeDocument> documents = knowledgeDocumentRepository.findAll();

            List<HybridScoredDocument> scoredDocuments = new ArrayList<>();

            for (KnowledgeDocument document : documents) {
                if (!StringUtils.hasText(document.getEmbeddingJson())) {
                    continue;
                }

                double[] contentVector =
                        mapper.readValue(document.getEmbeddingJson(), double[].class);

                double similarity =
                        embeddingService.cosineSimilarity(questionVector, contentVector);

                int vectorScore = (int) Math.round(similarity * 100);

                int keywordScore = calculateScore(document.getContent(), question);

                HybridScoredDocument scoredDocument =
                        new HybridScoredDocument(document, vectorScore, keywordScore, vectorWidth, keywordWidth);

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

        } catch (Exception e) {
            throw new RuntimeException("Hybrid 向量检索失败：" + e.getMessage());
        }

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
                    vectorScore * vectorWeight + keywordScore * keywordWeight);
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