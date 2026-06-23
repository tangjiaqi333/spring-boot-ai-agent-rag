package com.itheima.aiagent01.rag;

import com.itheima.aiagent01.client.DeepSeekClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RagService {

    private final KnowledgeBaseService knowledgeBaseService;
    private final DeepSeekClient deepSeekClient;

    public RagService( KnowledgeBaseService knowledgeBaseService , DeepSeekClient deepSeekClient) {
        this.knowledgeBaseService = knowledgeBaseService;
        this.deepSeekClient = deepSeekClient;
    }
    public String answerIfKnowledgeMatched(String useMassage) {
        List<String> knowledgeList = knowledgeBaseService.search(useMassage);

        System.out.println("RAG 命中知识数量：" + knowledgeList.size());
        System.out.println("RAG 命中内容：" + knowledgeList);



        if (knowledgeList.isEmpty()) {
            return null;
        }

        List<Map<String, String>> messages = buildRagMessages(useMassage,knowledgeList);

        return deepSeekClient.chat(messages);
    }

    public List<Map<String, String>> buildRagMessagesIfKnowledgeMatched(String useMassage) {
        List<String> knowledgeList = knowledgeBaseService.search(useMassage);

        System.out.println("RAG 命中知识数量：" + knowledgeList.size());
        System.out.println("RAG 命中内容：" + knowledgeList);



        if (knowledgeList.isEmpty()) {
            return null;
        }

        return buildRagMessages(useMassage,knowledgeList);
    }

    private List<Map<String, String>> buildRagMessages(String useMessage, List<String> knowledgeList) {
        String knowledge = String.join("\n\n", knowledgeList);

        List<Map<String, String>> messages = new ArrayList<>();

        messages.add(Map.of(
                "role", "system",
                "content",
                """
                你是一个严格的知识库问答助手。

                你只能根据我提供的知识库内容回答用户问题。
                不要扩展知识库以外的信息。
                不要输出代码。
                不要写太长。
                不要编造。
                如果知识库内容足够回答，请用 2-4 句话回答。
                """
        ));

        messages.add(Map.of(
                "role", "user",
                "content",
                "知识库内容：\n"
                        + useMessage
                        + "\n\n用户问题："
                        + knowledgeList
        ));

        return messages;
    }
}
