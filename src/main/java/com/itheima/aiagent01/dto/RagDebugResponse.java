package com.itheima.aiagent01.dto;

import java.util.ArrayList;
import java.util.List;

public class RagDebugResponse {
    private String question;
    private Integer candidateK;
    private Integer topK;
    private Integer minScore;
    private boolean rerankEnabled;
    private Integer candidateCount;
    private Integer rerankedCount;
    private List<KnowledgeSearchResult> candidates;
    private List<KnowledgeSearchResult> reranked;

    public RagDebugResponse() {
        this.candidates = new ArrayList<>();
        this.reranked = new ArrayList<>();
    }


    public RagDebugResponse(
            String question,
            Integer candidateK,
            Integer topK,
            Integer minScore,
            boolean rerankEnabled,
            List<KnowledgeSearchResult> candidates,
            List<KnowledgeSearchResult> reranked
    ) {
        this.question = question;
        this.candidateK = candidateK;
        this.topK = topK;
        this.minScore = minScore;
        this.rerankEnabled = rerankEnabled;
        this.candidates = candidates;
        this.reranked = reranked;
        this.candidateCount = candidates == null? 0 :candidates.size();
        this.rerankedCount = reranked == null? 0 :reranked.size();
    }

    public String getQuestion() {
        return question;
    }

    public Integer getCandidateK() {
        return candidateK;
    }

    public Integer getTopK() {
        return topK;
    }

    public Integer getMinScore() {
        return minScore;
    }

    public Boolean getRerankEnabled() {
        return rerankEnabled;
    }

    public Integer getCandidateCount() {
        return candidateCount;
    }

    public Integer getRerankedCount() {
        return rerankedCount;
    }

    public List<KnowledgeSearchResult> getCandidates() {
        return candidates;
    }

    public List<KnowledgeSearchResult> getReranked() {
        return reranked;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setCandidateK(Integer candidateK) {
        this.candidateK = candidateK;
    }

    public void setTopK(Integer topK) {
        this.topK = topK;
    }

    public void setMinScore(Integer minScore) {
        this.minScore = minScore;
    }

    public void setRerankEnabled(Boolean rerankEnabled) {
        this.rerankEnabled = rerankEnabled;
    }

    public void setCandidateCount(Integer candidateCount) {
        this.candidateCount = candidateCount;
    }

    public void setRerankedCount(Integer rerankedCount) {
        this.rerankedCount = rerankedCount;
    }

    public void setCandidates(List<KnowledgeSearchResult> candidates) {
        this.candidates = candidates;
    }

    public void setReranked(List<KnowledgeSearchResult> reranked) {
        this.reranked = reranked;
    }

}
