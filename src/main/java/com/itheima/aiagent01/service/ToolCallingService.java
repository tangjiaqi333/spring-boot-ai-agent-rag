package com.itheima.aiagent01.service;

import com.itheima.aiagent01.tool.CalculatorTool;
import com.itheima.aiagent01.tool.CurrentTimeTool;
import org.springframework.stereotype.Service;
import com.itheima.aiagent01.client.DeepSeekClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ToolCallingService {
    private final DeepSeekClient deepSeekClient;
    private final CurrentTimeTool currentTimeTool;
    private final CalculatorTool calculatorTool;


    public ToolCallingService(
            DeepSeekClient deepSeekClient,
            CurrentTimeTool currentTimeTool,
            CalculatorTool calculatorTool) {
        this.deepSeekClient = deepSeekClient;
        this.currentTimeTool = currentTimeTool;
        this.calculatorTool = calculatorTool;
    }

    public String handleToolCallIfNeeded(String message) {
        String toolDecision = decideTool(message);

        if ("current_time".equals(toolDecision)) {
            String currentTime = currentTimeTool.getCurrentTime();

            return summarizeToolResult(
                    message,
                    "current_time",
                    currentTime
            );
        }

        if (toolDecision != null && toolDecision.startsWith("calculator|")) {
            String expression = toolDecision.substring("calculator|".length());
            String result = calculatorTool.calculate(expression);

            return summarizeToolResult(
                    message,
                    "calculator",
                    expression + " = " + result
            );
        }
        return null;

    }

    private String decideTool(String message) {
        List<Map<String, String>> messages = new ArrayList<>();

        messages.add(Map.of(
                "role", "system",
                "content",
                """
                    你是一个工具选择器。你只能从下面三个选项中选择一个：

                    1. current_time
                    用于获取当前真实时间、日期、今天是哪天、现在几点。

                    2. calculator|表达式
                    用于计算简单数学表达式。表达式只允许包含两个数字和一个运算符，例如：
                    calculator|123*456
                    calculator|10+20
                    calculator|100/4

                    3. none
                    不需要工具。

                    规则：
                    - 如果用户问时间，返回 current_time
                    - 如果用户问数学计算，返回 calculator|表达式
                    - 如果不需要工具，返回 none
                    - 不要解释，不要输出其他内容。
                    """
        ));

        messages.add(Map.of(
                "role", "user",
                "content", message
        ));


        return deepSeekClient.chat(messages).trim ();
    }

    private String summarizeToolResult(String message, String toolName, String toolResult) {
        List<Map<String, String>> messages = new ArrayList<>();

        messages.add(Map.of(
                "role", "system",
                "content",
                """
                    你是一个 AI Agent 助手。
                    你会根据工具返回的真实结果，回答用户的问题。
                    
                    要求：
                    - 不要说“根据工具结果”
                    - 不要编造工具没有提供的信息
                    - 回答要自然、简洁
                    """
        ));

        messages.add(Map.of(
                "role", "user",
                "content",
                "用户问题:"+ message + "\n"
                        + "工具名称:" + toolName + "\n"
                        + "工具结果:" + toolResult
        ));
        return deepSeekClient.chat(messages);
    }
}
