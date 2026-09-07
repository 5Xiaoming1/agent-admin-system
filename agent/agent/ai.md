# LangChain4j 动态工具调用方案

## 背景

系统中 Agent 通过 `toolIds`（JSON 数组）关联工具，工具定义（名称、描述、类型、代码、端点、参数）存储在 `tools` 表中。

LangChain4j 的 `@Tool` 注解是编译期静态绑定，而本系统的工具是数据库动态存储的，需要运行时动态构建。

## 整体架构流程

```
用户输入 → Agent（含 toolIds）→ 查DB获取工具定义
                                    ↓
          LangChain4j ChatModel ← 动态构建 ToolSpecification
                                    ↓
          LLM 决定调用哪个工具 → DynamicToolExecutor 执行
                                    ↓
          执行结果返回 LLM → 生成最终回复
```

## 第一步：引入 LangChain4j 依赖

在 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-spring-boot-starter</artifactId>
    <version>1.0.0-beta1</version>
</dependency>
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-open-ai-spring-boot-starter</artifactId>
    <version>1.0.0-beta1</version>
</dependency>
```

## 第二步：核心实现 —— DynamicToolExecutor

将数据库中的工具定义转换为 LangChain4j 可执行的工具：

```java
@Component
@Slf4j
@AllArgsConstructor
public class DynamicToolExecutor {

    private final ToolsMapper toolsMapper;

    /**
     * 根据 Agent 的 toolIds 构建 LangChain4j 工具列表
     * 核心：将 DB 存储的工具 → LangChain4j 可调用的工具
     */
    public List<ToolSpecification> buildToolSpecifications(String toolIdsJson) {
        List<Long> toolIds = parseToolIds(toolIdsJson);
        if (toolIds.isEmpty()) return List.of();

        List<Tools> tools = toolsMapper.selectBatchIds(toolIds);
        return tools.stream()
                .filter(t -> t.getStatus() == 1)
                .map(this::toToolSpecification)
                .collect(Collectors.toList());
    }

    private ToolSpecification toToolSpecification(Tools tool) {
        return ToolSpecification.builder()
                .name(tool.getName())
                .description(tool.getDescription())
                .parameters(toParameters(tool))
                .build();
    }

    private JsonObjectSchema toParameters(Tools tool) {
        // 从 DB 的 parameters 字段（JSON）构建参数 schema
        // 根据实际存储的参数格式解析
        // ...
    }
}
```

## 第三步：三种工具类型的执行方式

### 类型一：`api` —— 调用外部 HTTP 接口

```java
private String executeApiTool(Tools tool, String arguments) {
    RestClient restClient = RestClient.create();
    return restClient.post()
            .uri(tool.getEndpoint())
            .contentType(MediaType.APPLICATION_JSON)
            .body(arguments)
            .retrieve()
            .body(String.class);
}
```

### 类型二：`function` —— 执行代码片段

```java
private String executeFunctionTool(Tools tool, String arguments) {
    try (ScriptEngine engine = new ScriptEngineManager().getEngineByName("js")) {
        engine.put("args", arguments);
        return String.valueOf(engine.eval(tool.getCode()));
    }
}
```

### 类型三：`builtin` —— 系统内置工具

```java
private final Map<String, Function<String, String>> builtinTools = Map.of(
    "天气查询", this::queryWeather,
    "计算器",   this::calculate,
    // ...
);

private String executeBuiltinTool(Tools tool, String arguments) {
    Function<String, String> handler = builtinTools.get(tool.getName());
    if (handler == null) throw new RuntimeException("未知内置工具: " + tool.getName());
    return handler.apply(arguments);
}
```

## 第四步：Agent 对话服务串联

```java
@Service
@AllArgsConstructor
public class AgentChatService {

    private final DynamicToolExecutor toolExecutor;
    private final ToolsService toolsService;
    private final SetupService setupService;

    public String chat(Long agentId, String userMessage) {
        // 1. 加载 Agent 及其关联的工具
        Agent agent = agentMapper.selectById(agentId);
        List<ToolSpecification> toolSpecs = toolExecutor
                .buildToolSpecifications(agent.getToolIds());

        // 2. 获取 Agent 的模型配置
        SetupVO config = setupService.queryById(agent.getSetupId());

        // 3. 构建 ChatModel
        ChatLanguageModel model = OpenAiChatModel.builder()
                .apiKey(config.getApiKey())
                .baseUrl(config.getBaseUrl())
                .build();

        // 4. 构建 Function Calling 对话
        ChatResponse response = model.chat(ChatRequest.builder()
                .messages(userMessage)
                .toolSpecifications(toolSpecs)
                .build());

        // 5. 处理工具调用
        if (response.aiMessage().hasToolExecutionRequests()) {
            for (ToolExecutionRequest request :
                 response.aiMessage().toolExecutionRequests()) {
                String result = toolExecutor.execute(
                    Long.valueOf(request.id()),
                    request.arguments()
                );
                // 将结果返回给 LLM
            }
        }

        return response.aiMessage().text();
    }
}
```

## 第五步：完整对话流程（含工具调用循环）

```
┌─────────────────────────────────────────────────────┐
│  用户: "北京今天天气怎么样？"                         │
│       ↓                                              │
│  LLM 分析: 需要调用"天气查询"工具                    │
│       ↓                                              │
│  执行 tool: GET https://api.weather.com?city=北京    │
│       ↓                                              │
│  结果: {"temp": 25, "weather": "晴"}                 │
│       ↓                                              │
│  LLM 生成: "北京今天晴天，气温25°C"                   │
└─────────────────────────────────────────────────────┘
```

## 总结

| 关键点 | 说明 |
|--------|------|
| 工具定义存储 | 数据库 `tools` 表，Agent 通过 `toolIds` 关联 |
| 动态注册 | 运行时从 DB 读取，构建 `ToolSpecification` |
| 三种执行方式 | `api` → HTTP 调用、`function` → 脚本执行、`builtin` → Java 方法映射 |
| LLM 决策 | LangChain4j 自动将 ToolSpecification 发送给 LLM，LLM 自主决定是否调用 |
| 多轮循环 | 工具执行结果返回 LLM，由 LLM 生成最终自然语言回复 |

核心思想：**工具定义存在数据库，运行时动态构建成 LangChain4j 能识别的 `ToolSpecification`，由 LLM 自主决策调用。系统作为"工具注册中心"，LangChain4j 作为"工具消费者"。**