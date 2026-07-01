package com.itheima.aiagent01.tool;

import com.itheima.aiagent01.dto.KnowledgeDocumentSummaryResponse;
import com.itheima.aiagent01.rag.KnowledgeBaseService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DocumentListTool implements AgentTool{
    private final KnowledgeBaseService knowledgeBaseService;
    public DocumentListTool(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }
    @Override
    public String getName() {
        return "document_list";
    }
    @Override
    public String getDescription() {
        return "查询当前知识库中已经上传的文档列表。无需参数。";
    }
    @Override
    public String execute(String input) {
        List<KnowledgeDocumentSummaryResponse> documents = knowledgeBaseService.listDocumentSummaries();

        if (documents == null || documents.isEmpty()){
            return "当前知识库中没有上传的文档。";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("当前知识库中已经上传的文档列表：\n\n");
        for (int i = 0; i < documents.size(); i++){
            KnowledgeDocumentSummaryResponse document = documents.get(i);

            builder.append(i + 1)
                    .append(". 文件名：")
                    .append(document.getSourceFileName())
                    .append("\n");

            builder.append("   documentId：")
                    .append(document.getDocumentId())
                    .append("\n");

            builder.append("   chunk 数量：")
                    .append(document.getTotalChunks())
                    .append("\n");

            builder.append("   创建时间：")
                    .append(document.getCreatedAt())
                    .append("\n\n");
        }

        return builder.toString();
    }
}
