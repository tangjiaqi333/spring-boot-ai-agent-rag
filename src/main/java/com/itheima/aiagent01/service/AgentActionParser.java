package com.itheima.aiagent01.service;

import com.itheima.aiagent01.dto.AgentAction;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;
//把模型返回的 JSON 解析成 AgentAction 对象
@Service
public class AgentActionParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AgentAction parse(String text) {

        if (!StringUtils.hasText(text)){
            return new AgentAction("final", null, null, "模型没有返回有效内容");
        }
        try {
            String json = extractJsonObject(text);
            if (!StringUtils.hasText(json)){
                return new AgentAction("final", null, null, text);
            }

            return objectMapper.readValue(json, AgentAction.class);
        } catch (Exception e) {
            return new AgentAction("final", null, null, text);
        }
    }

    private String extractJsonObject(String text) {
        int start = text.indexOf("{");
        int end = text.lastIndexOf("}");
        if (start >= 0 && end >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return null;
    }
}
