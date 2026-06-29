package com.itheima.aiagent01.dto;

import java.util.ArrayList;
import java.util.List;

public class RagAnswerResult {

    private String answer;
    private List<SourceReferenceResponse> sources;

    public RagAnswerResult() {
        this.sources = new ArrayList<>();
    }

    public RagAnswerResult(String answer, List<SourceReferenceResponse> sources) {
        this.answer = answer;
        this.sources = sources;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<SourceReferenceResponse> getSources() {
        return sources;
    }

    public void setSources(List<SourceReferenceResponse> sources) {
        this.sources = sources;
    }
}
