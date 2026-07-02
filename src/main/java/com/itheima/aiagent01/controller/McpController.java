package com.itheima.aiagent01.controller;

import com.itheima.aiagent01.common.Result;
import com.itheima.aiagent01.dto.McpToolCallRequest;
import com.itheima.aiagent01.dto.McpToolCallResponse;
import com.itheima.aiagent01.dto.McpToolResponse;
import com.itheima.aiagent01.tool.ToolRegistry;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mcp")
public class McpController {

    private final ToolRegistry toolRegistry;

    public McpController(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    @GetMapping("/tools")
    public Result<List<McpToolResponse>> listTools() {
        List<McpToolResponse> tools = toolRegistry.listTools();

        return Result.success(tools);
    }

    @PostMapping("/tools/{toolName}/call")
    public Result<McpToolCallResponse> callTool(
            @PathVariable String toolName,
            @RequestBody(required = false) McpToolCallRequest request
    ) {
        String input = request == null ? null : request.getInput();

        String result = toolRegistry.execute(toolName, input);

        McpToolCallResponse response = new McpToolCallResponse(
                toolName,
                input,
                result
        );

        return Result.success(response);
    }
}