package com.itheima.aiagent01.rag;

import com.itheima.aiagent01.dto.KnowledgeDocumentResponse;
import com.itheima.aiagent01.entity.KnowledgeDocument;
import com.itheima.aiagent01.repository.KnowledgeDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class KnowledgeBaseService {

    private final KnowledgeDocumentRepository knowledgeDocumentRepository;

    public KnowledgeBaseService(KnowledgeDocumentRepository knowledgeDocumentRepository) {
        this.knowledgeDocumentRepository = knowledgeDocumentRepository;
    }

    public void addDocument(String content) {
        if (StringUtils.hasText(content)) {
            KnowledgeDocument document = new KnowledgeDocument(content);
            knowledgeDocumentRepository.save(document);

            System.out.println("知识已保存到 PostgreSQL：" + content);
        }
    }

    public List<String> search(String question) {
        List<String> results = new ArrayList<>();

        if (!StringUtils.hasText(question)) {
            return results;
        }

        List<KnowledgeDocument> documents = knowledgeDocumentRepository.findAll();

        for (KnowledgeDocument document : documents) {
            String content = document.getContent();

            if (isRelevant(content, question)) {
                results.add(content);
            }
        }

        return results;
    }

    private boolean isRelevant(String doc, String question) {
        if (!StringUtils.hasText(doc) || !StringUtils.hasText(question)) {
            return false;
        }

        String q = question.toLowerCase();
        String d = doc.toLowerCase();

        if (q.contains("ai agent") && d.contains("ai agent")) {
            return true;
        }

        if (q.contains("智能体") && d.contains("智能体")) {
            return true;
        }

        if (q.contains("agent") && d.contains("agent")) {
            return true;
        }

        if (q.contains("是什么") && d.contains("ai agent")) {
            return true;
        }

        return false;
    }

    public List<KnowledgeDocumentResponse> listDocuments() {
        List<KnowledgeDocument> documents = knowledgeDocumentRepository.findAll();
        List<KnowledgeDocumentResponse> responses = new ArrayList<>();
        for (KnowledgeDocument document : documents) {
            responses.add(new KnowledgeDocumentResponse(
                    document.getId(),
                    document.getContent(),
                    document.getCreatedAt()
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

        Optional<KnowledgeDocument> optionaldocument = knowledgeDocumentRepository.findById(id);

        if (optionaldocument.isEmpty()) {
            return false;
        }
        KnowledgeDocument document = optionaldocument.get();
        document.setContent(content);
        knowledgeDocumentRepository.save(document);

        return true;
    }

    public void uploadDocument(MultipartFile  file) {
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

            addDocument( content);
        } catch (Exception e) {
            throw new RuntimeException("读取文件失败");
        }
    }

    private List<String> splitText (String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        if (!StringUtils.hasText(text)) {
            return chunks;
        }

        int start = 0;

        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            String chunk = text.substring(start, end).trim();

            if (StringUtils.hasText(chunk)) {
                chunks.add(chunk);
            }

            start = end;
        }

        return chunks;
    }