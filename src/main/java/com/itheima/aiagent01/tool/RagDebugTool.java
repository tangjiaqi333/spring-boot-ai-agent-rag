package com.itheima.aiagent01.tool;

import com.itheima.aiagent01.dto.KnowledgeSearchResult;
import com.itheima.aiagent01.rag.KnowledgeBaseService;
import com.itheima.aiagent01.rag.RerankService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
public class RagDebugTool implements AgentTool{
    private final KnowledgeBaseService knowledgeBaseService;
    private final RerankService rerankService;
    @Value("${rag.top-k:3}")
    private int ragTopK;

    @Value("${rag.candidate-k:10}")
    private int ragCandidateK;

    @Value("${rag.min-score:20}")
    private int ragMinScore;

    @Value("${rag.rerank-enabled:true}")
    private boolean rerankEnabled;

    public RagDebugTool(KnowledgeBaseService knowledgeBaseService, RerankService rerankService) {
        this.knowledgeBaseService = knowledgeBaseService;
        this.rerankService = rerankService;
    }

    @Override
    public String getName() {
        return "rag_debug";
    }

    @Override
    public String getDescription() {
        return "调试 RAG 检索链路。input 传入用户问题，返回 Hybrid 候选、Rerank 结果、candidateCount、rerankedCount。";
    }

    @Override
    public String execute(String input) {
        if (!StringUtils.hasText(input)) {
            return "RAG Debug 问题不能为空";
        }

        List<KnowledgeSearchResult> candidates =
                knowledgeBaseService.vectorSearchWithScore(
                        input,
                        ragCandidateK,
                        ragMinScore
                );

        List<KnowledgeSearchResult> reranked;

        if (rerankEnabled && !candidates.isEmpty()) {
            reranked = rerankService.rerank(input, candidates, ragTopK);
        } else {
            int limit = Math.min(ragTopK, candidates.size());
            reranked = candidates.subList(0, limit);
        }

        StringBuilder builder = new StringBuilder();

        builder.append("RAG Debug 结果：\n\n");
        builder.append("问题：").append(input).append("\n");
        builder.append("candidateK：").append(ragCandidateK).append("\n");
        builder.append("topK：").append(ragTopK).append("\n");
        builder.append("minScore：").append(ragMinScore).append("\n");
        builder.append("rerankEnabled：").append(rerankEnabled).append("\n");
        builder.append("candidateCount：").append(candidates.size()).append("\n");
        builder.append("rerankedCount：").append(reranked.size()).append("\n\n");

        builder.append("Hybrid 候选结果：\n");

        if (candidates.isEmpty()) {
            builder.append("没有命中任何候选 chunk。\n\n");
        } else {
            for (int i = 0; i < candidates.size(); i++) {
                KnowledgeSearchResult result = candidates.get(i);

                builder.append(i + 1)
                        .append(". id=")
                        .append(result.getId())
                        .append(", source=")
                        .append(result.getSourceFileName())
                        .append(", chunkIndex=")
                        .append(result.getChunkIndex())
                        .append(", score=")
                        .append(result.getScore())
                        .append("\n");
            }
            builder.append("\n");
        }

        builder.append("Rerank 后结果：\n");

        if (reranked.isEmpty()) {
            builder.append("Rerank 后没有结果。\n");
        } else {
            for (int i = 0; i < reranked.size(); i++) {
                KnowledgeSearchResult result = reranked.get(i);

                builder.append(i + 1)
                        .append(". id=")
                        .append(result.getId())
                        .append(", source=")
                        .append(result.getSourceFileName())
                        .append(", chunkIndex=")
                        .append(result.getChunkIndex())
                        .append(", score=")
                        .append(result.getScore())
                        .append("\n");
            }
        }

        return builder.toString();
    }
}
