package com.itheima.aiagent01.controller;


import com.itheima.aiagent01.common.Result;
import com.itheima.aiagent01.dto.*;
import com.itheima.aiagent01.rag.KnowledgeBaseService;
import com.itheima.aiagent01.rag.RerankService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {
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


    public KnowledgeController(KnowledgeBaseService knowledgeBaseService, RerankService rerankService) {
        this.knowledgeBaseService = knowledgeBaseService;
        this.rerankService = rerankService;
    }
    @PostMapping
    public Result<String> addknowledge(@Valid @RequestBody KnowledgeAddRequest request) {
        knowledgeBaseService.addDocument(request.getContent());
        return Result.success("添加成功");
    }

    @GetMapping
    public Result<List<KnowledgeDocumentResponse>> listknowledge() {
        List<KnowledgeDocumentResponse> documents = knowledgeBaseService.listDocuments();
        return Result.success(documents);
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteknowledge(@PathVariable Long id) {
        boolean deleted = knowledgeBaseService.deleteDocument(id);
        if (deleted) {
            return Result.success("知识删除成功");
        }

        return Result.error(404,"删除失败");
    }

    @PutMapping("/{id}")
    public Result<String> updateknowledge(@PathVariable Long id, @Valid @RequestBody KnowledgeAddRequest request) {
        boolean updated = knowledgeBaseService.updateDocument(id, request.getContent());
        if (updated) {
            return Result.success("知识更新成功");
        }

        return Result.error(404,"更新失败");
    }
    @PostMapping("/upload")
    public Result<String> uploadknowledge(@RequestParam("file") MultipartFile file) {
        knowledgeBaseService.uploadDocument(file);
        return Result.success("上传成功");
    }
    @DeleteMapping("/document/{documentId}")
    public Result<String> deleteknowledgeByDocumentId(@PathVariable String documentId) {
        boolean deleted = knowledgeBaseService.deleteByDocumentId(documentId);

        if ( deleted) {
            return Result.success("知识删除成功");
        }
        return Result.error(404,"删除失败");
    }

    @GetMapping("/document")
    public Result<List<KnowledgeDocumentSummaryResponse>> listDocuments() {
        List<KnowledgeDocumentSummaryResponse> documents = knowledgeBaseService.listDocumentSummaries();
        return Result.success(documents);
    }

    @GetMapping("/search")
    public Result<List<KnowledgeSearchResult>> searchKnowledge(
            @RequestParam String question,
            @RequestParam(defaultValue = "3") int topK
    ) {
        List<KnowledgeSearchResult> results =
                knowledgeBaseService.searchWithScore(question, topK);

        return Result.success(results);
    }

    @GetMapping("/vector-search")
    public Result<List<KnowledgeSearchResult>> vectorSearchKnowledge(
            @RequestParam String question,
            @RequestParam(defaultValue = "3") int topK,
            @RequestParam(defaultValue = "20") int minScore
    ) {
        List<KnowledgeSearchResult> results =
                knowledgeBaseService.vectorSearchWithScore(question, topK, minScore);
        return Result.success(results);
    }

    @GetMapping("/rag-debug")
    public Result<RagDebugResponse> ragDebug(
            @RequestParam String question,
            @RequestParam(required = false) Integer candidateK,
            @RequestParam(required = false) Integer topK,
            @RequestParam(required = false) Integer minScore
    ) {
        int finalCandidateK = candidateK != null ? candidateK : ragCandidateK;
        int finalTopK = topK != null ? topK : ragTopK;
        int finalMinScore = minScore != null ? minScore : ragMinScore;

        List<KnowledgeSearchResult> candidates =
                knowledgeBaseService.vectorSearchWithScore(
                        question,
                        finalCandidateK,
                        finalMinScore
                );

        List<KnowledgeSearchResult> reranked;

        if (rerankEnabled && !candidates.isEmpty()) {
            reranked = rerankService.rerank(question, candidates, finalTopK);
        } else {
            int limit = Math.min(finalTopK, candidates.size());
            reranked = candidates.subList(0, limit);
        }

        RagDebugResponse response = new RagDebugResponse(
                question,
                finalCandidateK,
                finalTopK,
                finalMinScore,
                rerankEnabled,
                candidates,
                reranked
        );

        return Result.success(response);
    }

}
