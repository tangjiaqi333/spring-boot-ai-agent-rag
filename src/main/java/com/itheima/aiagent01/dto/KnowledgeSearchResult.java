package com.itheima.aiagent01.dto;

public class KnowledgeSearchResult {
    private Long id;
    private String content;
    private int score;
    private String sourceFileName;
    private String documentId;
    private Integer chunkIndex;

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    public void setSourceFileName(String sourceFileName) {
        this.sourceFileName = sourceFileName;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public Integer getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(Integer chunkIndex) {
        this.chunkIndex = chunkIndex;
    }



    public KnowledgeSearchResult(Long id, String content, int score, String sourceFileName, String documentId, Integer chunkIndex) {
        this.id = id;
        this.content = content;
        this.score = score;
        this.sourceFileName = sourceFileName;
        this.documentId = documentId;
        this.chunkIndex = chunkIndex;
    }

    public KnowledgeSearchResult() {
    }





}
