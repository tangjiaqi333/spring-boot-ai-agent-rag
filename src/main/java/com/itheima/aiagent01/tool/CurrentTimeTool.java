package com.itheima.aiagent01.tool;


import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class CurrentTimeTool implements AgentTool {

    public String getCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    public String getName() {
        return "current_time";
    }

    @Override
    public String getDescription() {
        return "获取当前系统时间。无需参数。";
    }

    @Override
    public String execute(String input) {
        return getCurrentTime();
    }
}
