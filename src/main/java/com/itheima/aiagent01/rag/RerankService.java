package com.itheima.aiagent01.rag;

import com.itheima.aiagent01.client.DeepSeekClient;
import com.itheima.aiagent01.dto.KnowledgeSearchResult;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RerankService {

    private final DeepSeekClient deepSeekClient;

    public RerankService(DeepSeekClient deepSeekClient) {
        this.deepSeekClient = deepSeekClient;
    }

    public List<KnowledgeSearchResult> rerank(
            String question,
            List<KnowledgeSearchResult> candidates,
            int topK
    ) {
        if (!StringUtils.hasText(question) || candidates == null || candidates.isEmpty()) {
            return new ArrayList<>();
        }

        if (candidates.size() <= topK) {
            return candidates;
        }

        try {
            List<Map<String, String>> messages = buildRerankMessages(question, candidates);

            String response = deepSeekClient.chat(messages);

            List<Long> rerankIds = parseRerankIds(response);

            if (rerankIds.isEmpty()) {
                return fallbackTopK(candidates, topK);
            }

            List<KnowledgeSearchResult> reranked = new ArrayList<>();

            for (Long id : rerankIds) {
                for (KnowledgeSearchResult candidate : candidates) {
                    if (candidate.getId().equals(id)) {
                        reranked.add(candidate);
                        break;
                    }
                }

                if (reranked.size() >= topK) {
                    break;
                }
            }

            if (reranked.isEmpty()) {
                return fallbackTopK(candidates, topK);
            }

            return reranked;

        } catch (Exception e) {
            System.out.println("Rerank 失败，使用原始 Hybrid TopK：" + e.getMessage());
            return fallbackTopK(candidates, topK);
        }
    }

    private List<Map<String, String>> buildRerankMessages(
            String question,
            List<KnowledgeSearchResult> candidates
    ) {
        StringBuilder candidateText = new StringBuilder();

        for (KnowledgeSearchResult candidate : candidates) {
            candidateText.append("ID: ")
                    .append(candidate.getId())
                    .append("\n");

            candidateText.append("来源文件: ")
                    .append(candidate.getSourceFileName())
                    .append("\n");

            candidateText.append("chunkIndex: ")
                    .append(candidate.getChunkIndex())
                    .append("\n");

            candidateText.append("score: ")
                    .append(candidate.getScore())
                    .append("\n");

            candidateText.append("内容:\n")
                    .append(candidate.getContent())
                    .append("\n\n---\n\n");
        }

        String systemPrompt = """
                你是一个 RAG 检索结果重排序助手。
                你的任务是根据用户问题，从候选 chunk 中选出最相关的内容。
                你只能返回 JSON 数组，不要解释，不要输出 Markdown。
                返回格式示例：[12, 15, 9]
                数组里的数字必须是候选 chunk 的 ID。
                """;

        String userPrompt = """
                用户问题：
                %s
                
                候选 chunk：
                %s
                
                请按相关性从高到低返回最相关的 chunk ID。
                只返回 JSON 数组。
                """.formatted(question, candidateText.toString());

        return List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        );
    }

    private List<Long> parseRerankIds(String response) {
        List<Long> ids = new ArrayList<>();

        if (!StringUtils.hasText(response)) {
            return ids;
        }

        String json = extractJsonArray(response);

        if (!StringUtils.hasText(json)) {
            return ids;
        }

        String content = json
                .replace("[", "")
                .replace("]", "")
                .trim();

        if (!StringUtils.hasText(content)) {
            return ids;
        }

        String[] parts = content.split(",");

        for (String part : parts) {
            String idText = part.trim();

            if (!StringUtils.hasText(idText)) {
                continue;
            }

            try {
                ids.add(Long.parseLong(idText));
            } catch (NumberFormatException e) {
                System.out.println("Rerank ID 解析失败：" + idText);
            }
        }

        return ids;
    }

    private String extractJsonArray(String text) {
        int start = text.indexOf("[");
        int end = text.lastIndexOf("]");

        if (start == -1 || end == -1 || end <= start) {
            return null;
        }

        return text.substring(start, end + 1);
    }

    private List<KnowledgeSearchResult> fallbackTopK(
            List<KnowledgeSearchResult> candidates,
            int topK
    ) {
        List<KnowledgeSearchResult> copied = new ArrayList<>(candidates);

        copied.sort((a, b) -> b.getScore() - a.getScore());

        int limit = Math.min(topK, copied.size());

        return copied.subList(0, limit);
    }
}