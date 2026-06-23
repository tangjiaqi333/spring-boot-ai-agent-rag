package com.itheima.aiagent01.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class DeepSeekClient {

    @Value("${ai.base-url}")
    private String baseUrl;

    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String chat(List<Map<String, String>> messages) {
        String url = baseUrl + "/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", messages
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        Map response = restTemplate.postForObject(url, entity, Map.class);

        if (response == null) {
            return "DeepSeek API 没有返回结果";
        }

        List choices = (List) response.get("choices");

        if (choices == null || choices.isEmpty()) {
            return "DeepSeek API 返回结果为空";
        }

        Map firstChoice = (Map) choices.get(0);
        Map messageMap = (Map) firstChoice.get("message");

        if (messageMap == null) {
            return "DeepSeek API 返回格式异常";
        }

        return (String) messageMap.get("content");
    }

    public void streamChat(
            List<Map<String, String>> messages,
            Consumer<String> onContent
    ) {
        String url = baseUrl + "/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.TEXT_EVENT_STREAM));
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", messages,
                "stream", true
        );

        restTemplate.execute(
                url,
                HttpMethod.POST,
                request -> {
                    request.getHeaders().putAll(headers);
                    objectMapper.writeValue(request.getBody(), requestBody);
                },
                response -> {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(response.getBody(), StandardCharsets.UTF_8)
                    )) {
                        String line;

                        while ((line = reader.readLine()) != null) {
                            if (!line.startsWith("data:")) {
                                continue;
                            }

                            String data = line.substring(5).trim();

                            if ("[DONE]".equals(data)) {
                                break;
                            }

                            JsonNode root = objectMapper.readTree(data);

                            JsonNode contentNode = root.path("choices")
                                    .path(0)
                                    .path("delta")
                                    .path("content");

                            if (!contentNode.isMissingNode() && !contentNode.isNull()) {
                                String content = contentNode.asText();

                                if (!content.isEmpty()) {
                                    onContent.accept(content);
                                }
                            }
                        }
                    }

                    return null;
                }
        );
    }
}