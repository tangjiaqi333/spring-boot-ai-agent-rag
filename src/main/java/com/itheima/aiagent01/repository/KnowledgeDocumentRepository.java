package com.itheima.aiagent01.repository;
import com.itheima.aiagent01.entity.KnowledgeDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, Long> {
    List<KnowledgeDocument> findByDocumentId(String documentId);
}
