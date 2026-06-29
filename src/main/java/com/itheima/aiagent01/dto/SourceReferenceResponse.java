package com.itheima.aiagent01.dto;

public class SourceReferenceResponse {
    private String sourceFileName;
    private String documentId;
    private Integer chunkIndex;
    private Integer score;


    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    public void setSourceFileName(String sourceFileName) {
        this.sourceFileName = sourceFileName;
    }

    public Integer getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(Integer chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }


    public SourceReferenceResponse(String sourceFileName, String documentId, Integer chunkIndex, Integer score) {
        this.sourceFileName = sourceFileName;
        this.documentId = documentId;
        this.chunkIndex = chunkIndex;
        this.score = score;
    }
    public SourceReferenceResponse() {
    }


}
