package com.itheima.aiagent01.tool;

import com.itheima.aiagent01.dto.KnowledgeSearchResult;
import com.itheima.aiagent01.rag.KnowledgeBaseService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
public class KnowledgeSearchTool implements AgentTool{
    private final KnowledgeBaseService knowledgeBaseService;
    public KnowledgeSearchTool(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    @Override
    public String getName() {
        return "knowledge_search";
    }
    @Override
    public String getDescription() {
        return "从知识库中检索和用户问题相关的内容。input 传入用户要查询的问题，例如：RAG 为什么需要切片？";
    }

    @Override
    public String execute(String input) {
        if (!StringUtils.hasText(input)) {
            return "知识库检索问题不能为空";
        }

        List<KnowledgeSearchResult> results =
                knowledgeBaseService.vectorSearchWithScore(input, 3, 20);

        if (results.isEmpty()) {
            return "知识库中没有检索到相关内容";
        }

        StringBuilder builder = new StringBuilder();

        builder.append("知识库检索结果：\n\n");

        for (int i = 0; i < results.size(); i++) {
            KnowledgeSearchResult result = results.get(i);

            builder.append("结果 ")
                    .append(i + 1)
                    .append("：\n");

            builder.append("来源文件：")
                    .append(result.getSourceFileName())
                    .append("\n");

            builder.append("chunkIndex：")
                    .append(result.getChunkIndex())
                    .append("\n");

            builder.append("score：")
                    .append(result.getScore())
                    .append("\n");

            builder.append("内容：\n")
                    .append(result.getContent())
                    .append("\n\n---\n\n");
        }

        return builder.toString();
    }
}