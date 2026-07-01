package com.itheima.aiagent01.service;

import com.itheima.aiagent01.client.DeepSeekClient;
import com.itheima.aiagent01.dto.AgentAction;
import com.itheima.aiagent01.dto.AgentLoopResponse;
import com.itheima.aiagent01.dto.AgentStepResponse;
import com.itheima.aiagent01.tool.CalculatorTool;
import com.itheima.aiagent01.tool.CurrentTimeTool;
import com.itheima.aiagent01.tool.ToolRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AgentLoopService {

    private static final int MAX_STEPS = 3;

    @Value("${ai.system-prompt}")
    private String systemPrompt;

    private final ToolCallingService toolCallingService;
    private final DeepSeekClient deepSeekClient;
    private final ConversationService conversationService;
    private final ToolRegistry toolRegistry;
    private final AgentActionParser agentActionParser;

    public AgentLoopService(
            ToolCallingService toolCallingService,
            DeepSeekClient deepSeekClient,
            ConversationService conversationService,
            ToolRegistry toolRegistry,
            AgentActionParser agentActionParser
    ) {
        this.toolCallingService = toolCallingService;
        this.deepSeekClient = deepSeekClient;
        this.conversationService = conversationService;
        this.toolRegistry = toolRegistry;
        this.agentActionParser = agentActionParser;
    }

    public AgentLoopResponse run(String conversationId, String message) {
        String finalConversationId = StringUtils.hasText(conversationId)
                ? conversationId
                : UUID.randomUUID().toString();

        List<AgentStepResponse> steps = new ArrayList<>();

        String finalAnswer = null;

        for (int step = 1; step <= MAX_STEPS; step++) {
            String toolReply = toolCallingService.handleToolCallIfNeeded(message);

            if (StringUtils.hasText(toolReply)) {
                steps.add(new AgentStepResponse(
                        step,
                        "TOOL_OBSERVATION",
                        message,
                        null,
                        "legacy_tool_calling",
                        message,
                        toolReply,
                        null
                ));

                String finalPrompt = """
                        用户原始问题：
                        %s
                        
                        工具观察结果：
                        %s
                        
                        请基于工具观察结果，给用户一个简洁、准确的最终回答。
                        """.formatted(message, toolReply);

                List<Map<String, String>> messages = new ArrayList<>();

                messages.add(Map.of("role", "system", "content", systemPrompt));
                messages.add(Map.of("role", "user", "content", finalPrompt));

                finalAnswer = deepSeekClient.chat(messages);

                steps.add(new AgentStepResponse(
                        step + 1,
                        "FINAL_ANSWER",
                        message,
                        null,
                        null,
                        null,
                        null,
                        finalAnswer
                ));

                break;
            }

            steps.add(new AgentStepResponse(
                    step,
                    "NO_TOOL",
                    message,
                    null,
                    null,
                    null,
                    "没有需要调用的工具",
                    null
            ));

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.addAll(conversationService.loadHistoryFromDb(finalConversationId));
            messages.add(Map.of("role", "user", "content", message));

            finalAnswer = deepSeekClient.chat(messages);

            steps.add(new AgentStepResponse(
                    step + 1,
                    "FINAL_ANSWER",
                    message,
                    null,
                    null,
                    null,
                    null,
                    finalAnswer
            ));

            break;
        }

        if (!StringUtils.hasText(finalAnswer)) {
            finalAnswer = "任务未完成";
        }

        conversationService.saveMessage(finalConversationId, "user", message);
        conversationService.saveMessage(finalConversationId, "assistant", finalAnswer);

        return new AgentLoopResponse(finalConversationId, finalAnswer, steps);
    }

    private String executeTool(String toolName, String toolInput) {
        return toolRegistry.execute(toolName, toolInput);
    }

    public AgentLoopResponse runV2(String conversationId, String message) {
        String finalConversationId = StringUtils.hasText(conversationId)
                ? conversationId
                : UUID.randomUUID().toString();

        List<AgentStepResponse> steps = new ArrayList<>();
        //Agent 的临时工作记忆
        StringBuilder scratchpad = new StringBuilder();

        String finalAnswer = null;

        for (int step = 1; step <= MAX_STEPS; step++) {
            String toolDescriptions = toolRegistry.getToolDescriptions();

            String decisionPrompt = """
                    你是一个可以调用工具的 Agent。
            
                    你必须只返回 JSON，不要输出 Markdown，不要解释。
            
                    可用工具：
                    %s
            
                    返回格式只能是以下两种之一：
            
                    如果需要调用工具：
                    {
                      "action": "tool",
                      "toolName": "工具名称",
                      "toolInput": "工具输入",
                      "answer": null
                    }
            
                    如果可以最终回答：
                    {
                      "action": "final",
                      "toolName": null,
                      "toolInput": null,
                      "answer": "最终回答内容"
                    }
            
                    工具选择规则：
                    - 如果用户问当前时间，使用 current_time
                    - 如果用户问数学计算，使用 calculator
                    - 如果用户要求查询知识库、根据资料回答、查找文档内容，使用 knowledge_search
                    - 如果用户问知识库里有哪些文档、上传了哪些文件、文档列表，使用 document_list
                    - 如果用户问 RAG 检索是否命中、为什么没命中、命中了哪些 chunk、检索调试结果，使用 rag_debug
                    - 如果不需要工具，直接 final
            
                    用户问题：
                    %s
            
                    已有观察记录：
                    %s
                    """.formatted(toolDescriptions, message, scratchpad.toString());

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.add(Map.of("role", "user", "content", decisionPrompt));

            String modelDecision = deepSeekClient.chat(messages);

            AgentAction action = agentActionParser.parse(modelDecision);

            steps.add(new AgentStepResponse(
                    step,
                    "THINK",
                    message,
                    modelDecision,
                    null,
                    null,
                    null,
                    null
            ));

            if ("final".equalsIgnoreCase(action.getAction())) {
                finalAnswer = action.getAnswer();

                steps.add(new AgentStepResponse(
                        step + 1,
                        "FINAL_ANSWER",
                        message,
                        null,
                        null,
                        null,
                        null,
                        finalAnswer
                ));

                break;
            }

            if ("tool".equalsIgnoreCase(action.getAction())) {
                String observation = executeTool(
                        action.getToolName(),
                        action.getToolInput()
                );

                scratchpad.append("Step ")
                        .append(step)
                        .append(" 调用工具：")
                        .append(action.getToolName())
                        .append("\n输入：")
                        .append(action.getToolInput())
                        .append("\n观察结果：")
                        .append(observation)
                        .append("\n\n");

                steps.add(new AgentStepResponse(
                        step,
                        "TOOL_OBSERVATION",
                        message,
                        null,
                        action.getToolName(),
                        action.getToolInput(),
                        observation,
                        null
                ));

                continue;
            }

            finalAnswer = "Agent 无法识别下一步动作";
            break;
        }

        if (!StringUtils.hasText(finalAnswer)) {
            finalAnswer = "Agent 达到最大执行步数，任务未完成";
        }

        conversationService.saveMessage(finalConversationId, "user", message);
        conversationService.saveMessage(finalConversationId, "assistant", finalAnswer);

        return new AgentLoopResponse(finalConversationId, finalAnswer, steps);
    }
}