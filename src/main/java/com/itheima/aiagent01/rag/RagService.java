package com.itheima.aiagent01.rag;

import com.itheima.aiagent01.client.DeepSeekClient;
import com.itheima.aiagent01.dto.KnowledgeSearchResult;
import com.itheima.aiagent01.dto.RagAnswerResult;
import com.itheima.aiagent01.dto.RagMessageResult;
import com.itheima.aiagent01.dto.SourceReferenceResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RagService {

    private final KnowledgeBaseService knowledgeBaseService;
    private final DeepSeekClient deepSeekClient;
    private final RerankService rerankService;

    @Value("${rag.top-k:3}")
    private int ragTopK = 3;

    @Value("${rag.min-score:20}")
    private int ragMinScore = 20;

    @Value("${rag.candidate-k:10}")
    private int ragCandidateK;

    @Value("${rag.rerank-enabled:true}")
    private boolean rerankEnabled;

    public RagService(
            KnowledgeBaseService knowledgeBaseService,
            DeepSeekClient deepSeekClient,
            RerankService rerankService
    ) {
        this.knowledgeBaseService = knowledgeBaseService;
        this.deepSeekClient = deepSeekClient;
        this.rerankService = rerankService;
    }

    private List<SourceReferenceResponse> buildSourceReferences(
            List<KnowledgeSearchResult> searchResults
    ) {
        List<SourceReferenceResponse> sources = new ArrayList<>();

        for (KnowledgeSearchResult result : searchResults) {
            sources.add(new SourceReferenceResponse(
                    result.getSourceFileName(),
                    result.getDocumentId(),
                    result.getChunkIndex(),
                    result.getScore()
            ));
        }

        return sources;
    }

    public RagAnswerResult answerIfKnowledgeMatched(String userMessage) {
        List<KnowledgeSearchResult> candidates =
                knowledgeBaseService.vectorSearchWithScore(userMessage, ragCandidateK, ragMinScore);

        System.out.println("RAG 候选数量：" + candidates.size());

        if (candidates.isEmpty()) {
            return null;
        }

        List<KnowledgeSearchResult> searchResults = rerankEnabled
                ? rerankService.rerank(userMessage, candidates, ragTopK)
                : candidates.stream().limit(ragTopK).toList();

        System.out.println("Rerank 后命中数量：" + searchResults.size());

        List<Map<String, String>> messages = buildRagMessages(userMessage, searchResults);

        String answer = deepSeekClient.chat(messages);

        List<SourceReferenceResponse> sources = buildSourceReferences(searchResults);

        return new RagAnswerResult(answer, sources);
    }

    public RagMessageResult buildRagMessagesIfKnowledgeMatched(String userMessage) {
        List<KnowledgeSearchResult> candidates =
                knowledgeBaseService.vectorSearchWithScore(userMessage, ragCandidateK, ragMinScore);

        System.out.println("Stream RAG 候选数量：" + candidates.size());

        if (candidates.isEmpty()) {
            return null;
        }

        List<KnowledgeSearchResult> searchResults = rerankEnabled
                ? rerankService.rerank(userMessage, candidates, ragTopK)
                : candidates.stream().limit(ragTopK).toList();

        System.out.println("Stream Rerank 后命中数量：" + searchResults.size());

        List<Map<String, String>> messages = buildRagMessages(userMessage, searchResults);

        List<SourceReferenceResponse> sources = buildSourceReferences(searchResults);

        return new RagMessageResult(messages, sources);
    }

    private List<Map<String, String>> buildRagMessages(
            String userMessage,
            List<KnowledgeSearchResult> searchResults
    ) {
        StringBuilder knowledgeText = new StringBuilder();

        for (KnowledgeSearchResult result : searchResults) {
            knowledgeText.append("来源文件：")
                    .append(result.getSourceFileName())
                    .append("，chunkIndex：")
                    .append(result.getChunkIndex())
                    .append("，score：")
                    .append(result.getScore())
                    .append("\n")
                    .append(result.getContent())
                    .append("\n\n");
        }

        List<Map<String, String>> messages = new ArrayList<>();

        messages.add(Map.of(
                "role", "system",
                "content",
                """
                你是一个严格的知识库问答助手。

                你只能根据【知识库内容】回答用户问题。
                严禁使用知识库以外的信息。
                如果知识库内容没有提到，就回答：知识库中没有相关信息。
                不要输出代码。
                不要写太长。
                请用 2-4 句话回答。
                """
        ));

        messages.add(Map.of(
                "role", "user",
                "content",
                "知识库内容：\n"
                        + knowledgeText
                        + "\n\n用户问题："
                        + userMessage
        ));

        return messages;
    }

    private String buildSourcesText(List<KnowledgeSearchResult> searchResults) {
        StringBuilder builder = new StringBuilder();

        builder.append("\n\n参考来源：");

        for (KnowledgeSearchResult result : searchResults) {
            builder.append("\n- ")
                    .append(result.getSourceFileName())
                    .append(" / chunk ")
                    .append(result.getChunkIndex())
                    .append(" / score ")
                    .append(result.getScore());
        }

        return builder.toString();
    }
}