package com.itheima.aiagent01.rag;

import com.itheima.aiagent01.dto.KnowledgeDocumentResponse;
import com.itheima.aiagent01.dto.KnowledgeDocumentSummaryResponse;
import com.itheima.aiagent01.entity.KnowledgeDocument;
import com.itheima.aiagent01.exception.BusinessException;
import com.itheima.aiagent01.repository.KnowledgeDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.*;
//创建知识文档服务类
@Service
public class KnowledgeDocumentService {

    private final KnowledgeDocumentRepository knowledgeDocumentRepository;
    private final DocumentChunkService documentChunkService;
    private final KnowledgeEmbeddingService knowledgeEmbeddingService;

    public KnowledgeDocumentService(
            KnowledgeDocumentRepository knowledgeDocumentRepository,
            DocumentChunkService documentChunkService,
            KnowledgeEmbeddingService knowledgeEmbeddingService
    ) {
        this.knowledgeDocumentRepository = knowledgeDocumentRepository;
        this.documentChunkService = documentChunkService;
        this.knowledgeEmbeddingService = knowledgeEmbeddingService;
    }

    public void addDocument(String content) {
        if (StringUtils.hasText(content)) {
            KnowledgeDocument document = new KnowledgeDocument(content);

            String embeddingJson = knowledgeEmbeddingService.toEmbeddingJson(content);
            document.setEmbeddingJson(embeddingJson);

            knowledgeDocumentRepository.save(document);

            System.out.println("知识已保存到 PostgreSQL：" + content);
        }
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

        String embeddingJson = knowledgeEmbeddingService.toEmbeddingJson(content);
        document.setEmbeddingJson(embeddingJson);

        knowledgeDocumentRepository.save(document);

        return true;
    }

    public void uploadDocument(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null) {
            throw new BusinessException("文件名不能为空");
        }

        if (!fileName.endsWith(".txt") && !fileName.endsWith(".md")) {
            throw new BusinessException("目前只支持 txt 和 md 文件");
        }

        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);

            if (!StringUtils.hasText(content)) {
                throw new BusinessException("文件内容不能为空");
            }

            List<String> chunks = documentChunkService.splitText(content, 500, 100);

            String documentId = UUID.randomUUID().toString();

            System.out.println("文件名：" + fileName);
            System.out.println("文件长度：" + content.length());
            System.out.println("切片数量：" + chunks.size());
            System.out.println("documentId：" + documentId);

            for (int i = 0; i < chunks.size(); i++) {
                String chunk = chunks.get(i);

                String embeddingJson =
                        knowledgeEmbeddingService.toEmbeddingJson(chunk);

                KnowledgeDocument document = new KnowledgeDocument(
                        chunk,
                        fileName,
                        documentId,
                        i,
                        chunks.size()
                );

                document.setEmbeddingJson(embeddingJson);

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
}