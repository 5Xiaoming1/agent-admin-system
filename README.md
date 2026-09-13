# Agent 后端管理系统

[TOC]

## 项目介绍

Agent 后端管理系统是一个基于 **Spring Boot 3.2** + **LangChain4j** 的智能体（AI Agent）管理平台，提供智能体管理、工具调用、知识库（RAG）、AI 对话、用户管理、数据看板等完整功能。系统集成了阿里云通义千问（Qwen）大模型，支持流式/非流式对话、会话记忆、系统业务工具动态调用和知识库检索增强生成。

### 技术栈

| 技术         | 版本        | 说明                     |
| ------------ | ----------- | ------------------------ |
| Spring Boot  | 3.2.5       | 基础框架                 |
| Java         | 21          | 运行环境                 |
| MyBatis-Plus | 3.5.9       | ORM 框架                 |
| MySQL        | 8.x         | 关系型数据库             |
| Redis        | 7.x         | 缓存 / 会话记忆 / 验证码 |
| Knife4j      | 4.5.0       | API 文档（Swagger 增强） |
| JWT (jjwt)   | 0.12.7      | 身份认证                 |
| LangChain4j  | 1.0.1-beta6 | AI 编排框架              |
| Qwen3.7      | -           | 大语言模型（通义千问）   |
| Lombok       | -           | 代码简化                 |

### 核心特性

- **智能体管理**：支持创建和管理多种类型的 AI Agent（聊天型/任务型/工作流型），关联配置、知识库和工具，跟踪 Token 用量
- **AI 对话**：支持流式（SSE）和非流式对话，具备会话记忆（Redis 持久化）、知识库检索增强（RAG）和系统业务工具动态调用能力
- **工具管理**：支持三种工具类型——外部 API 调用（api）、代码片段执行（function）、系统内置工具（builtin），AI 可自主决策调用
- **知识库管理**：支持文档上传/下载/管理，按 document/qa/web 三种类型分目录存储，文档内容经向量化后用于 RAG 检索
- **配置管理**：集中管理大模型 API 密钥和接口地址，支持多配置切换，密钥 AES 加密存储
- **用户管理**：完整的用户 CRUD、登录认证（JWT + 验证码）、头像上传、密码修改
- **数据看板**：仪表盘概览、Token 用量趋势、Agent 用量排行、Agent 类型分布统计
- **操作日志**：自动记录登录日志和业务操作日志，支持查询和清理
- **五表关联**：Agent 通过 `setupId` 关联配置、通过 `agent_knowledge` 中间表关联知识库、通过 `toolIds` 关联工具，形成完整的智能体生态

## 软件架构

```
agent/
── src/main/java/com/tianji/agent/
│   ├── AgentApplication.java          # 应用入口
│   ├── config/                        # 配置层
│   │   ├── ai/                        # AI 配置（向量库、检索器、会话记忆）
│   │   ├── captcha/                   # 验证码生成工具
│   │   ├── common/                    # 统一响应 Result
│   │   ├── crypto/                    # AES 加解密
│   │   ├── filter/                    # JWT 过滤器
│   │   ├── jackson/                   # JSON 序列化配置
│   │   ├── jwt/                       # JWT 工具（生成/解析）
│   │   ├── knife4j/                   # API 文档配置
│   │   ├── mybatisplus/               # MyBatis-Plus 配置
│   │   ├── redis/                     # Redis 工具类
│   │   ├── tool/                      # 系统业务工具（AI 可调用）
│   │   ├── upload/                    # 文件上传工具
│   │   └── web/                       # Web MVC 配置（静态资源）
│   ├── controller/                    # 控制器层
│   │   ├── system/                    # 系统管理（用户、日志）
│   │   ├── AIChatController.java      # AI 对话接口
│   │   ├── AgentController.java       # 智能体管理接口
│   │   ├── DataController.java        # 数据看板接口
│   │   ├── KnowledgeController.java   # 知识库管理接口
│   │   ├── LoginController.java       # 登录认证接口
│   │   ├── SetupController.java       # 配置管理接口
│   │   └── ToolsController.java       # 工具管理接口
│   ├── service/                       # 服务层
│   │   ├── AssistantService.java      # AI 助手接口（LangChain4j @AiService）
│   │   └── impl/                      # 各业务服务实现
│   ├── mapper/                        # MyBatis-Plus 数据访问层
│   ├── model/                         # 数据模型
│   │   ├── entity/                    # 数据库实体
│   │   ├── dto/                       # 请求参数对象
│   │   └── vo/                        # 响应视图对象
│   ── resources/
│       ├── application.yaml           # 应用配置
│       └── prompts/
│           └── system-prompt.txt      # AI 系统提示词
├── data/
│   ├── knowledgebase/                 # 知识库文件存储
│   │   ├── ai-embedding-store.json    # AI 助手知识库向量库
│   │   ── embedding-store.json       # 管理系统知识库向量库
│   └── tools/                         # 工具代码文件存储
├── static/uploads/avatars/            # 头像文件存储
└── pom.xml                            # Maven 依赖配置
```

### 分层架构

```
┌─────────────────────────────────────────┐
│              Controller 层               │
│       RESTful API / SSE 流式响应         │
─────────────────────────────────────────┤
│              Service 层                  │
│    业务逻辑 / @AiService 声明式 AI      │
─────────────────────────────────────────┤
│              Mapper 层                   │
│        MyBatis-Plus 数据访问             │
├─────────────────────────────────────────┤
│           基础设施层                      │
│  MySQL / Redis / LangChain4j / Qwen     │
└─────────────────────────────────────────┘
```

### AI 对话流程

```
用户输入 → JWT 认证 → AIChatController
                          ↓
        AssistantService (@AiService)
         ├── 系统提示词（system-prompt.txt）
         ├── 会话记忆（Redis ChatMemoryStore）
         ├── 知识库检索（EmbeddingStoreContentRetriever）
         └── 系统工具调用（SystemBusinessTool 60+ 工具）
                          ↓
        Qwen 大模型（阿里云）
                          ↓
        流式/非流式响应 → 持久化对话历史 → 返回前端
```

## 安装教程

### 环境要求

- **JDK 21** 或更高版本
- **Maven 3.8** 或更高版本
- **MySQL 8.0** 或更高版本
- **Redis 7.0** 或更高版本

### 安装步骤

1.  **克隆项目**

```bash
git clone <仓库地址>
cd agent
```

2.  **创建数据库**

在 MySQL 中创建数据库：

```sql
CREATE DATABASE agent_01 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

导入项目提供的 SQL 初始化脚本（如有）。

3.  **配置 Redis**

确保 Redis 服务已启动，默认连接 `localhost:6379`，密码 `123456`，使用 `database 10`。

4.  **修改配置文件**

编辑 `src/main/resources/application.yaml`，根据实际环境修改：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/agent_01?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: 你的数据库密码
  data:
    redis:
      host: localhost
      port: 6379
      password: 你的Redis密码
      database: 10
```

如需更换大模型，修改 `langchain4j.open-ai` 相关配置。

5.  **编译并启动**

```bash
mvn clean package -DskipTests
java -jar target/agent-0.0.1-SNAPSHOT.jar
```

或者直接使用 Maven 插件运行：

```bash
mvn spring-boot:run
```

启动成功后控制台输出：

```
========================================
  Agent 应用启动成功！
  Knife4j 文档: http://localhost:8081/doc.html
  Swagger UI:  http://localhost:8081/swagger-ui/index.html
========================================
```

## 使用说明

### API 接口概览

模块

基础路径

说明

登录认证

`/api/login`

验证码获取、用户登录、JWT 令牌签发

用户管理

`/api/system/user`

用户 CRUD、密码修改、头像上传

智能体管理

`/api/agents`

Agent CRUD、状态切换、启用列表

工具管理

`/api/tools`

工具 CRUD、状态切换（api/function/builtin）

知识库管理

`/api/knowledgebase`

知识库 CRUD、文件上传/下载/删除

配置管理

`/api/setup`

模型配置 CRUD、状态切换、下拉选项

AI 对话

`/api/ai`

流式/非流式对话、会话历史、记忆清除

数据看板

`/api/data`

仪表盘概览、Token 趋势、用量排行、分布

登录日志

`/api/system/login-log`

登录日志查询、删除、清空

操作日志

`/api/system/operation-log`

操作日志查询、删除、清空

### 接口文档

启动项目后，访问以下地址查看完整的 API 文档并进行在线测试：

- **Knife4j 文档**（推荐）：[http://localhost:8081/doc.html](http://localhost:8081/doc.html)
- **Swagger UI**：[http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)
- **OpenAPI JSON**：[http://localhost:8081/v3/api-docs](http://localhost:8081/v3/api-docs)

### 典型使用流程

1.  **系统初始化**：配置管理 → 新增大模型配置（API Key + Base URL）
2.  **知识库搭建**：知识库管理 → 创建知识库 → 上传文档
3.  **工具注册**：工具管理 → 新增工具（API 接口/函数/内置工具）
4.  **智能体创建**：智能体管理 → 新建 Agent → 关联配置、知识库和工具
5.  **AI 对话**：AI 对话 → 发送消息 → AI 基于知识库和工具自主回答
6.  **数据监控**：数据看板 → 查看 Token 消耗、Agent 用量、类型分布

## 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request

## 许可证

© 2026 xya Agent. All rights reserved.