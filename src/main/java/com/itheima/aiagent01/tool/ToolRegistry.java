package com.itheima.aiagent01.tool;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
//管理所有工具
@Component
public class ToolRegistry {

    private final Map<String, AgentTool> toolMap = new HashMap<>();

    public ToolRegistry(List<AgentTool> tools) {
        for (AgentTool tool : tools) {
            toolMap.put(tool.getName(), tool);
        }
    }

    public String execute(String toolName, String toolInput) {
        if (!StringUtils.hasText(toolName)) {
            return "工具名不能为空";
        }

        AgentTool tool = toolMap.get(toolName);

        if (tool == null) {
            return "未知工具：" + toolName;
        }

        return tool.execute(toolInput);
    }

    public String getToolDescriptions() {
        StringBuilder builder = new StringBuilder();

        for (AgentTool tool : toolMap.values()) {
            builder.append("- ")
                    .append(tool.getName())
                    .append("：")
                    .append(tool.getDescription())
                    .append("\n");
        }

        return builder.toString();
    }
}