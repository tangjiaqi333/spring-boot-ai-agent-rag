# AI Agent Backend Demo

基于 Java Spring Boot 构建的 AI Agent 后端学习项目，支持多轮会话、SSE 流式输出、Tool Calling、RAG 知识库、文档上传切片、Hybrid Search、Rerank、引用来源和 RAG Debug 调试接口。

本项目的目标不是简单调用大模型 API，而是学习一个 AI Agent 后端系统如何接收用户输入、编排工具调用、检索知识库、管理会话记忆，并稳定返回结果。

---

## 技术栈

* Java
* Spring Boot
* Spring Web
* Spring Data JPA
* PostgreSQL
* DeepSeek API
* SSE / SseEmitter
* RAG
* Tool Calling
* Hybrid Search
* Rerank

---

## 项目核心能力

### 1. 普通聊天

支持用户通过接口发送问题，由后端调用大模型生成回答。

```http
POST /api/chat
```

请求示例：

```json
{
  "message": "RAG 为什么需要切片？",
  "conversationId": "可选"
}
```

返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": "RAG 需要切片是因为...",
    "conversationId": "xxx",
    "ragUsed": true,
    "sources": []
  }
}
```

---

### 2. SSE 流式输出

支持模型回答像真实聊天产品一样逐步返回。

```http
GET /api/chat/stream?message=RAG为什么需要切片
```

SSE 事件包括：

```text
conversationId
message
ragUsed
sources
done
error
```

---

### 3. 会话记忆 Conversation Memory

系统会为每轮对话保存 `conversationId`，并将用户与助手的消息持久化到 PostgreSQL。

支持：

```http
GET /api/chat/conversations
GET /api/chat/{conversationId}/messages
DELETE /api/chat/{conversationId}
```

---

### 4. Tool Calling

系统支持让大模型判断是否需要调用工具。

当前工具包括：

* 当前时间工具
* 计算器工具

核心思想：

```text
LLM 负责判断是否需要工具
Java 负责执行工具
Tool 返回真实结果
LLM 再组织语言回答用户
```

---

### 5. RAG 知识库

支持添加知识、查询知识、修改知识、删除知识、上传文档。

```http
POST /api/knowledge
GET /api/knowledge
PUT /api/knowledge/{id}
DELETE /api/knowledge/{id}
POST /api/knowledge/upload
```

上传文件支持：

* `.txt`
* `.md`

---

## RAG 流程

完整 RAG 流程：

```text
用户问题
↓
Hybrid Search 粗召回
↓
Rerank 重排序
↓
取 TopK chunk
↓
构造 RAG Prompt
↓
调用 DeepSeek
↓
返回回答 + sources
```

---

## 文档切片 Chunking

上传文档后，系统会将长文档切成多个 chunk。

每个 chunk 会保存：

* content
* sourceFileName
* documentId
* chunkIndex
* totalChunks
* embeddingJson

其中：

```text
sourceFileName：来自哪个文件
documentId：属于哪个文档
chunkIndex：当前是第几个 chunk
totalChunks：该文档一共有几个 chunk
embeddingJson：该 chunk 的向量表示
```

支持按照 `documentId` 删除整个文档：

```http
DELETE /api/knowledge/document/{documentId}
```

删除一个文件，本质是删除该 `documentId` 下的所有 chunk。

---

## Embedding 与向量检索

当前项目使用 Mock Embedding 学习向量检索原理。

流程：

```text
文本
↓
tokenize
↓
hash
↓
double[] vector
↓
cosine similarity
```

上传文档时，系统会提前为每个 chunk 生成 embedding，并存入数据库的 `embedding_json` 字段。

查询时：

```text
用户问题 → 生成 questionVector
chunk → 从数据库读取 embeddingJson
计算 cosine similarity
得到 vectorScore
```

---

## Hybrid Search

系统不仅使用向量相似度，还结合关键词得分。

```text
finalScore = vectorScore * vectorWeight + keywordScore * keywordWeight
```

配置项：

```yaml
rag:
  top-k: 3
  min-score: 20
  candidate-k: 10
  rerank-enabled: true
  vector-weight: 1.0
  keyword-weight: 1.0
```

---

## Rerank

Hybrid Search 会先召回候选 chunk，然后通过 Rerank 再次判断哪些 chunk 最适合回答用户问题。

流程：

```text
Hybrid Search → candidateK
↓
Rerank
↓
TopK
↓
RAG Prompt
```

如果 Rerank 失败，系统会自动 fallback 到原始 Hybrid TopK。

---

## RAG Debug 接口

为了观察 RAG 检索链路，项目提供 RAG Debug 接口。

```http
GET /api/knowledge/rag-debug?question=RAG为什么需要切片
```

返回内容包括：

* question
* candidateK
* topK
* minScore
* rerankEnabled
* candidates
* reranked
* candidateCount
* rerankedCount

用于判断：

```text
Hybrid 是否命中
Rerank 是否正常
最终给模型的 chunk 是否合理
```

---

## 项目结构

```text
com.itheima.aiagent01
├── client
│   └── DeepSeekClient
├── common
│   └── Result
├── controller
│   ├── ChatController
│   └── KnowledgeController
├── dto
│   ├── ChatRequest
│   ├── ChatResponse
│   ├── KnowledgeSearchResult
│   ├── RagAnswerResult
│   ├── RagDebugResponse
│   ├── RagMessageResult
│   └── SourceReferenceResponse
├── entity
│   ├── ChatMessage
│   └── KnowledgeDocument
├── exception
│   └── GlobalExceptionHandler
├── rag
│   ├── KnowledgeBaseService
│   ├── RagService
│   └── RerankService
├── repository
│   ├── ChatMessageRepository
│   └── KnowledgeDocumentRepository
├── service
│   ├── AiChatService
│   ├── ConversationService
│   ├── EmbeddingService
│   └── ToolCallingService
└── tool
    ├── CalculatorTool
    └── CurrentTimeTool
```

---

## 核心调用链路

### 普通聊天调用链路

```text
ChatController
↓
AiChatService
↓
ToolCallingService
↓
RagService
↓
KnowledgeBaseService
↓
RerankService
↓
DeepSeekClient
↓
ConversationService
↓
ChatResponse
```
## 系统架构图

### 整体架构

```text
前端 / Apifox
    |
    | HTTP / SSE
    v
Controller 层
    |
    | 接收请求，解析参数，返回统一 Result
    v
AiChatService
    |
    | 负责 AI Agent 主流程编排
    |
    |-------------------------------
    |               |              |
    v               v              v
ToolCallingService  RagService     ConversationService
    |               |              |
    |               |              |
    v               v              v
Tool 工具系统        RAG 检索系统    会话记忆系统
    |               |              |
    |               |              |
CalculatorTool      KnowledgeBaseService   ChatMessageRepository
CurrentTimeTool     RerankService          PostgreSQL
                    EmbeddingService
                    KnowledgeDocumentRepository
                    PostgreSQL
    |
    v
DeepSeekClient
    |
    | 调用 DeepSeek API
    v
LLM 回答
    |
    v
ChatResponse / SSE Event
```

---

## POST /api/chat 调用链路

```text
用户发送问题
    |
    v
ChatController
    |
    v
AiChatService.chat()
    |
    | 1. 判断 conversationId
    |    如果前端传入 conversationId，则继续该会话
    |    如果没有传入，则生成新的 UUID
    |
    v
ConversationService.loadHistoryFromDb()
    |
    | 从 PostgreSQL 中读取历史对话
    |
    v
ToolCallingService.handleToolCallIfNeeded()
    |
    | 判断是否需要工具
    |
    |-------------------------
    |                         |
    | 需要工具                 | 不需要工具
    v                         v
调用 CalculatorTool /          进入 RAG 流程
CurrentTimeTool
    |
    v
DeepSeek 总结工具结果
    |
    v
保存 user / assistant 消息
    |
    v
返回 ChatResponse
```

---

## RAG 命中链路

```text
用户问题
    |
    v
RagService.answerIfKnowledgeMatched()
    |
    v
KnowledgeBaseService.vectorSearchWithScore()
    |
    | 1. 用户问题生成 questionVector
    | 2. 从 knowledge_documents 读取 chunk
    | 3. 读取每个 chunk 的 embeddingJson
    | 4. 计算 cosine similarity
    | 5. 计算 keywordScore
    | 6. 得到 finalScore
    | 7. 根据 minScore 过滤
    | 8. 返回 candidateK 个候选 chunk
    |
 
RerankService.rerank()
    |
    | 让 LLM 从候选 chunk 中重新排序
    |
    v
取 TopK chunk
    |
    v
RagService 构造 RAG Prompt
    |
    v
DeepSeekClient.chat()
    |
    v
返回 RagAnswerResult
    |
    | answer + sources
    |
    v
AiChatService 保存聊天记录
    |
    v
返回 ChatResponse
```

---

## RAG 未命中链路

```text
用户问题
    |
    v
Hybrid Search
    |
    v
candidateCount = 0
    |
    v
RagService 返回 null
    |
    v
AiChatService 走普通 DeepSeek
    |
    v
DeepSeekClient.chat()
    |
    v
保存聊天记录
    |
    v
返回 ChatResponse
```

---

## SSE 流式调用链路

```text
前端请求：
GET /api/chat/stream?message=xxx
    |
    v
ChatController
    |
    v
AiChatService.streamChat()
    |
    v
创建 SseEmitter 长连接
    |
    v
new Thread() 开启异步任务
    |
    v
判断 Tool / RAG / 普通 LLM
    |
    v
DeepSeekClient.streamChat()
    |
    v
后端不断 emitter.send()
    |
    | event: conversationId
    | event: message
    | event: ragUsed
    | event: sources
    | event: done
    |
    v
前端逐步接收模型输出
```

---

## RAG Debug 调用链路

```text
GET /api/knowledge/rag-debug?question=xxx
    |
    v
KnowledgeController.ragDebug()
    |
    v
KnowledgeBaseService.vectorSearchWithScore()
    |
    | 返回 Hybrid Search candidates
    |
    v
RerankService.rerank()
    |
    | 返回 reranked results
    |
    v
RagDebugResponse
    |
    | question
    | candidateK
    | topK
    | minScore
    | rerankEnabled
    | candidates
    | reranked
    | candidateCount
    | rerankedCount
    |
    v
用于调试 RAG 检索链路
```

---

## 核心模块职责

```text
ChatController
负责聊天接口，包括普通聊天和 SSE 流式聊天。

KnowledgeController
负责知识库接口，包括文档上传、知识查询、删除文档、向量检索和 RAG Debug。

AiChatService
负责 AI Agent 主流程编排，决定走 Tool Calling、RAG，还是普通 DeepSeek。

ConversationService
负责会话记忆，包括保存聊天记录、查询历史消息、查询会话列表和删除会话。

ToolCallingService
负责判断是否需要调用工具，并执行工具调用流程。

RagService
负责 RAG 主逻辑，包括知识库命中判断、RAG Prompt 构造、sources 构造和 RAG 返回结果。

KnowledgeBaseService
负责知识库核心能力，包括文档上传、chunk 切片、embedding 入库、Hybrid Search 和 TopK 检索。

RerankService
负责对 Hybrid Search 的候选 chunk 进行重排序。

EmbeddingService
负责将文本转换成 mock embedding 向量，并计算 cosine similarity。

DeepSeekClient
负责调用 DeepSeek 普通接口和流式接口。

Repository
负责 PostgreSQL 数据库读写。
```

---

### RAG 命中流程

```text
用户问题
↓
Hybrid Search 命中知识库
↓
Rerank 选出最终 chunk
↓
构造 RAG Prompt
↓
DeepSeek 回答
↓
返回 ragUsed=true + sources
```

---

### RAG 未命中流程

```text
用户问题
↓
Hybrid Search 命中数量为 0
↓
RagService 返回 null
↓
AiChatService 走普通 DeepSeek
↓
返回 ragUsed=false
```

---

## 启动配置示例

```yaml
ai:
  base-url: https://api.deepseek.com
  api-key: ${DEEPSEEK_API_KEY}
  model: deepseek-v4-flash
  system-prompt: 你是一个 Java 和 AI Agent 学习助手，请用简单清楚的方式回答问题。

rag:
  top-k: 3
  min-score: 20
  candidate-k: 10
  rerank-enabled: true
  vector-weight: 1.0
  keyword-weight: 1.0
```

---

## 当前项目定位

这是一个 AI Agent 后端学习项目，重点不是 UI，而是后端核心能力：

```text
LLM 接入
Tool Calling
RAG
Memory
Streaming
Hybrid Search
Rerank
Debug
工程化分层
```

它可以作为后续继续学习真实 Embedding、pgvector、Agent Loop、多工具系统、前端页面和部署上线的基础项目。
