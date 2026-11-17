# 代码生成器 MCP Server 集成设计方案

## 概述

给 yanggu-code-generator 后端新增一层 **MCP（Model Context Protocol）Server**，把"元数据发现 + 代码生成"能力以标准 MCP 工具的形式暴露给 AI Agent（首个对接目标：Qoder）。让 Agent 能自主完成 `发现项目/表 → 查看字段配置 → 预览文件树 → 生成结构化代码 → 写入自身工作区` 的闭环，把这个生成器从"人点网页按钮"升级为"可被 Agent 调用的确定性脚手架"。

本方案**不改动现有网页端生成链路**（zip 下载 / 服务器本地写盘保持原样），MCP 只是给已有的"预览产物"新增一个消费端。

## 1. 背景与目标

- **动机**：模板生成相对 LLM 的核心优势是确定性、可复现、零幻觉、已固化的团队架构约定。把这些能力做成 Agent 可编程调用的工具，是本项目在 AI 时代的主要价值延续方向。
- **目标**：
  1. 后端内嵌一个 MCP Server 端点（HTTP + SSE），Agent 通过远程 HTTP 连接。
  2. 暴露**高层 + 发现型**工具集：Agent 能先枚举项目/表/模板组拿到 ID，再触发预览与生成。
  3. 生成结果以**结构化文件树**（路径 + 内容列表）作为工具返回，由 Agent 落盘到自己的工作区，**服务器不落盘**。
- **非目标（本期不做）**：
  - 不做通过 MCP 写回元数据（新增/修改项目、表字段配置、模板）。本期工具**全部只读或纯生成**。
  - 不做 MCP resources / prompts / completions 能力，只做 tools。
  - **升级 Spring Boot 3.3.1 → 3.5.x** 以采用 Spring AI 官方 MCP Server starter（见第 2 节结论）；但**不跨代升级到 Boot 4.0 / Spring AI 2.0**（跨度大、迁移成本高）。
  - 不做多用户 RBAC（现有 `AuthController` 本就是写死的假 token，见第 7 节）。

## 2. 兼容性核实结论（关键决策）

已获授权升级 Spring Boot，故重新核对版本矩阵（context7 官方文档 + 联网）：

| 候选 | 版本 | 对 Spring Boot 的要求 | 结论 |
|------|------|----------------------|------|
| **Spring AI MCP Server WebMVC starter** | **1.0.3**（2025-10-01 发布） | 支持 **Boot 3.4.x / 3.5.x**（官方 EPIC #3379 明确同分支支持二者） | **采用** |
| Spring AI MCP Server | 2.0+ | 要求 **Boot 4.0 / Framework 7** | 排除（跨代大迁移） |
| MCP Java SDK 核心 `io.modelcontextprotocol.sdk:mcp` | 稳定版 | 仅需 Servlet 6.0（框架无关） | 退居备胎（需手写传输与工具装配） |

**最终选型：升级 Spring Boot 至最新 3.5.x + Spring AI 1.0.3 MCP Server WebMVC starter。**
- 为何升 Boot 而非用轻量 SDK：升级后传输、SSE 端点、工具注册（`@Tool` 注解）、结果序列化全部由框架托管，我们要手写的薄壳代码显著减少，且与 Spring 生态鉴权/配置一致。
- 为何选 3.5.x 而非 3.4.x：3.5.x 是 3.x 最后一支、仍在维护，Spring AI 1.0.3 完全支持，且 knife4j/springdoc 对其适配版本更成熟；3.4.x 已偏旧。
- ⚠️ **首要迁移风险**：Boot 3.3.1 → 3.5.x 属跨 2 个 minor 版本，需重点验证 **knife4j 4.5.0 / springdoc-openapi** 与 Boot 3.5 的兼容（通常需同步升级 springdoc 到对应版本）。列为切片 1 第一验证项；若个别依赖不兼容，退路为降一档到 Boot 3.4.x（同样满足 Spring AI 1.0.3）。

## 3. 整体架构

```
AI Agent (Qoder)
      │  MCP over HTTP+SSE（Bearer API-Key 过滤器）
      ▼
Spring AI MCP Server WebMVC 自动装配的端点（/mcp/sse 事件流 + /mcp/message 请求）
      │  McpServerAutoConfiguration 依据容器里的 ToolCallbackProvider bean 自动暴露 tools
      ▼
CodeGeneratorMcpService（新增：@Tool 注解方法，返回值由框架自动序列化为 MCP 结果）
      │  注册 bean：MethodToolCallbackProvider.builder().toolObjects(...)
      │  直接注入调用，零网络跳数
      ├── GeneratorService.preview（复用已有产物 → List<PreviewTemplateVO> 树）
      ├── ProjectService / TableService / TableFieldService / DatasourceService / TemplateGroupService / EnumService（发现型只读）
      ▼
框架封装为 MCP tool 结果（结构化 JSON / 异常 → isError）
```

分层原则：新增的 `mcp` 包只做"协议适配 + 参数校验 + 结果序列化"，**不含任何生成逻辑**；生成逻辑 100% 复用 `service` 层。MCP 层是薄壳。

## 4. 依赖与传输层

### 4.1 pom.xml 改动

```xml
<properties>
    <spring-boot.version>3.5.x</spring-boot.version>   <!-- 升到最新 3.5.x；不兼容则退 3.4.x -->
    <spring-ai.version>1.0.3</spring-ai.version>
</properties>

<!-- dependencyManagement：在已有 spring-boot-dependencies 之后追加 Spring AI BOM -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-bom</artifactId>
    <version>${spring-ai.version}</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>

<!-- dependencies：MCP Server WebMVC starter（自带 mcp-spring-webmvc + spring-boot-starter-web） -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
</dependency>
```

- Boot 版本升级 + Spring AI BOM 的**版本联动**是切片 1 首要验证项：确认 knife4j 4.5.0 / springdoc-openapi 有对应 Boot 3.5 的适配版本（否则同步升级 springdoc 或降 Boot 到 3.4.x）。
- starter 自带 SSE 传输与自动配置，**不再手写 `HttpServletSseServerTransportProvider` 与 `ServletRegistrationBean`**。

### 4.2 application.yaml 传输配置（自动装配，无需 Java 配置类）

```yaml
spring:
  ai:
    mcp:
      server:
        enabled: true                    # 总开关，生产默认 false，对接期显式开启
        name: yanggu-code-generator
        version: 1.0.0
        type: SYNC                       # 同步工具，实现最简
        instructions: "代码生成器的元数据发现与代码生成工具"
        sse-endpoint: /mcp/sse           # 客户端建立事件流的地址
        sse-message-endpoint: /mcp/message  # 客户端发请求的地址
        capabilities:
          tool: true
```

- 端点由框架在 DispatcherServlet 下自动注册（非独立 Servlet），路径与第 10 节 Qoder 接入地址一致。
- 工具注册见第 5 节：只需把带 `@Tool` 方法的 bean 交给 `MethodToolCallbackProvider`，自动配置即合并暴露为 MCP tools。

```java
// config/McpToolConfig.java（唯一需要新增的装配类，只做工具聚合注册）
@Bean
public ToolCallbackProvider codegenTools(CodeGeneratorMcpService svc) {
    return MethodToolCallbackProvider.builder().toolObjects(svc).build();
}
```

## 5. MCP 工具面设计

工具分两类，均写在 `CodeGeneratorMcpService` 中，每个公开方法用 Spring AI 的 `@Tool(description=...)` 注解声明；入参用 `@ToolParam` 描述，JSON `inputSchema` 由框架依据方法签名自动生成，无需手写 schema。所有工具**无副作用**。

### 5.1 发现型只读工具（让 Agent 拿到 ID、理解现状）

| Tool name | 入参 | 返回（JSON） | 复用的 Service 方法 |
|-----------|------|-------------|--------------------|
| `list_projects` | 可选 `{keyword, page, size}` | 项目摘要列表（id/name/datasourceId/模板组ID/generatorType） | `ProjectService.page/list` |
| `get_project` | `{projectId}` | 项目详情 + 关联的表模板组/枚举模板组/前后端路径 | `ProjectService.getById` |
| `list_tables` | `{projectId, keyword?}` | 表列表（id/tableName/comment） | `TableService` 按项目查询 |
| `describe_table` | `{tableId}` | 表 + 字段清单（字段名/类型/是否查询字段/列表字段/input 类型等配置） | `TableService` + `TableFieldService` |
| `list_datasources` | `{}` | 数据源摘要（id/name/dbType，**绝不含密码**） | `DatasourceService` |
| `list_template_groups` | `{type?}` | 模板组列表（id/name/type），type=项目/表/枚举 | `TemplateGroupService` |
| `list_enums` | `{projectId}` | 枚举列表（id/name/items） | `EnumService` |

### 5.2 动作型生成工具（返回结构化文件树）

| Tool name | 入参 | 返回 | 复用方法 |
|-----------|------|------|---------|
| `preview_code` | 对齐 `CodePreviewQuery`（`generatorProductType` + `previewProductId`） | `List<PreviewTemplateVO>` 树形文件结构（filePath/templateContent/templateType/children），框架自动 JSON 序列化 | **`GeneratorService.preview(...)`（已是公开方法，返回结构化树）** |
| `generate_table` | `projectId` + `tableIdList` + `templateGroupId` + `generatorFunction` 等（对齐 `GeneratorTableQuery`） | 同上 | 循环对每个 tableId 调用 `preview(TABLE, id)`；或（可选）在 `GeneratorService` 补一个 `List<TemplateContentVO> generateTableTree(GeneratorTableQuery)` 公开方法——**additive，不改现有逻辑** |
| `generate_project` | 对齐 `GeneratorProjectQuery` | 同上 | 走 `preview(PROJECT, projectId)` 或上述可选补方法 |
| `generate_enum` | 对齐 `GeneratorEnumQuery` | 同上 | 走 `preview(ENUM, enumId)` 或上述可选补方法 |

> 复用点结论：**MCP 层的主复用入口是 `GeneratorService.preview(CodePreviewQuery)`**——这个公开方法已经把内部私有的 `tablePreview`/`buildProjectPreview`/`enumPreview` 的产物 `List<TemplateContentVO>` 通过 `buildTree` 组装成 `List<PreviewTemplateVO>` 结构化树。MCP 直接消费这个树，**service 层可以零改动**（批量/自定义 templateGroupId 场景如需绕过单 product 限制，可考虑切片 3 时补一个 additive 公开方法）。

## 6. 结构化文件树回传（复用已有 preview）

### 6.1 已有事实（复用点）

公开方法 `GeneratorService.preview(CodePreviewQuery)` 返回 `List<PreviewTemplateVO>`，**这本身就是一棵结构化文件树**：每个节点含 `filePath`、`fileName`、`templateContent`（文本内容/二进制 base64）、`templateType`（0-目录/1-模板文件/2-二进制文件）、`children`（子节点）。`preview` 内部已把私有的 `buildProjectPreview`/`tablePreview`/`enumPreview` 产出的 `List<TemplateContentVO>` 经 `buildTree` 组装为该树。

### 6.2 回传形态（默认零新增 DTO）

- **默认直接复用 `List<PreviewTemplateVO>`** 作为工具返回值，由 Spring AI 自动序列化为 JSON。Agent 侧遍历 `children` 即可还原目录结构与文件内容。
- 若发现树里 `BaseVO`/`templateGroupType`/`level` 等对 Agent 冗余，可在 MCP 层加一个**扁平化裁剪**（只保留 `path/type/content`）而非新增重量 DTO——是否裁剪留到切片 3 依实际 token 体量决定。

### 6.3 工具返回序列化

`@Tool` 方法直接 `return List<PreviewTemplateVO>`（或裁剪后的扁平列表），框架自动转为 MCP tool result；二进制文件 content 维持 base64，Agent 侧解码。**不手写 `CallToolResult`/`ObjectMapper`。**

### 6.4 零回归保证

- **不修改** `preview` 及私有预览方法的返回类型与逻辑。
- **不触碰** `downloadZip` / `downloadLocal` 两条旧链路。
- MCP 层只是 `preview` 的又一个消费端；如需批量/自定义 templateGroupId，最多在 `GeneratorService` **新增**一个公开方法（additive），**服务层现有行为完全不变**。

## 7. 安全与鉴权

现状：`AuthController.login` 返回写死 token、无拦截器强制校验，等于后端接口当前基本无鉴权。

MCP 端点独立加固，**不复用前端那套假 auth**：

1. **API-Key（Bearer）过滤器**：新增一个作用于 `/mcp/**`（`sse-endpoint` 与 `sse-message-endpoint` 前缀）的 `OncePerRequestFilter`，校验 `Authorization: Bearer <key>`；开启开关复用框架的 `spring.ai.mcp.server.enabled`，key 从自定义配置读取：
   ```yaml
   spring.ai.mcp.server.enabled: true   # 见第 4 节，生产默认 false
   mcp:
     api-key: ${MCP_API_KEY:}           # 自定义项，从环境变量注入，不写死进仓库
   ```
   key 缺失/不匹配 → 直接 401，不进入 MCP 协议层。注意需放行框架内置的 SSE/ message 端点路径。
2. **默认关闭信任边界**：`spring.ai.mcp.server.enabled` 默认 `false`，避免未加固时随主应用无差别暴露；对接期由开发者显式开启。
3. **风险面收窄**：MCP 工具全为只读/纯生成、服务器不落盘（第 6 节），相较现有 `downloadLocal`（写服务器磁盘）风险更低；且 `list_datasources` 明确不返回密码字段。

> 若后续要接公网多客户端，再在此过滤器上叠加真实 token 体系；本期 API-Key 足够。

## 8. 错误处理

- `@Tool` 方法内 `try/catch` 包裹 Service 调用，失败时返回可读错误（或由框架把抛出的异常转为 `isError=true` 的工具结果）：
  - 业务异常（`BusinessException` 等，如"模板渲染失败/表达式执行异常"）→ 以携带可读 message 的工具错误返回，**不抛协议级异常**，让 Agent 能读懂失败原因并自我修正。
  - 参数校验失败（缺 `projectId`、`tableIdList` 空等）→ 同样返回字段级说明。
- 复用现有全局异常里对 `BusinessException.getMessage()` 的语义，不新造错误码体系。
- 未捕获异常兜底为工具错误，日志按后端现有 logback 记录。

## 9. 端到端数据流（示例：为某表生成 CRUD）

```
Agent: tools/list                                  → 拿到全部工具与 inputSchema
Agent: list_projects                              → [{id:1, name:"demo", tableTemplateGroupId:5 ...}]
Agent: list_tables {projectId:1}                    → [{id:10, tableName:"user" ...}]
Agent: describe_table {tableId:10}                  → 字段与查询/列表/input 配置
Agent: preview_code {generatorProductType:TABLE, previewProductId:10}
        → [PreviewTemplateVO 树：filePath/templateContent/templateType/children]  // 先审
Agent: generate_table {tableIdList:[10], templateGroupId:5, ...}  → 同一文件树
Agent: 遍历文件树写入自身工作区                       → 服务器全程不落盘
```

## 10. 与 Qoder 的接入配置

MCP Server 采用 HTTP+SSE 远程传输，在 Qoder 的用户级 MCP 配置中新增（示意，实际以 Qoder MCP 配置格式为准）：

```json
{
  "mcpServers": {
    "yanggu-code-generator": {
      "url": "http://localhost:8888/code-generator/mcp/sse",
      "headers": { "Authorization": "Bearer ${MCP_API_KEY}" }
    }
  }
}
```

- `context-path` 为 `/code-generator`（见 README 后端访问地址），SSE 端点路径由 `spring.ai.mcp.server.sse-endpoint` 配置（本方案取 `/mcp/sse`），故完整 SSE 地址为 `{host}:{port}/code-generator/mcp/sse`。
- 连通性验收：Qoder 侧能看到 server 连接成功且 tools 列表出现第 5 节工具。

## 11. 落地切片（后续 writing-plans 依据）

| 切片 | 内容 | 验收 |
|------|------|------|
| **切片 0** | **Spring Boot 3.3.1 → 3.5.x 升级 + knife4j/springdoc 适配**（MCP 的前置地基，独立可验证） | 应用正常启动；knife4j `doc.html` 可访问；现有前端页面调用后端接口全部回归通过；若有不兼容按第 2 节退路降 3.4.x |
| **切片 1** | 引入 Spring AI BOM + `spring-ai-starter-mcp-server-webmvc`；`application.yaml` 配 `spring.ai.mcp.server.*` + API-Key 过滤器；先只暴露一个 `list_projects` 只读 `@Tool` | MCP Inspector / Qoder 能连上、`tools/list` 见 `list_projects`、`tools/call` 返回真实项目 JSON；`enabled=false` 时端点不可达 |
| **切片 2** | 补齐全部发现型只读工具（5.1） | 逐个工具在 Inspector 调用通过；`list_datasources` 断言无密码字段 |
| **切片 3** | 基于 `GeneratorService.preview` 的 `preview_code`/`generate_table`/`generate_project`/`generate_enum`（含是否展平裁剪、是否补 additive 方法） | 对真实表调用 `generate_table` 返回文件树与网页端预览内容逐文件一致；服务器无新增落盘文件 |
| **切片 4** | 错误映射完善（8）、README/接入文档、Qoder 连通实测 | 故意传错参数得到 `isError=true` 可读消息；文档可复现接入 |

## 12. 测试策略

- **单元**：`CodeGeneratorMcpService` 的 `@Tool` 方法，对 mock 的 Service 断言输入解析、preview 树消费/裁剪、异常→工具错误转换。
- **契约**：`npx @modelcontextprotocol/inspector` 连 `/mcp/sse`，核对 `tools/list` 的 name/inputSchema、`tools/call` 返回结构。
- **集成**：Qoder 实接，跑第 9 节完整数据流；与网页端同参数预览做**内容一致性对比**。
- **回归（重点）**：① Boot 3.3.1→3.5.x 升级后，现有前端所有接口调用、knife4j 文档、静态资源全量回归；② zip 下载 / 服务器本地生成两条旧链路不受影响；③ MCP 端点自动注册未劫持 `/gen/**` 等既有路由。

## 13. 风险与待办

| 项 | 说明 | 处置 |
|----|------|------|
| **Boot 升级兼容性** | 3.3.1→3.5.x 跨 2 个 minor，knife4j 4.5.0 / springdoc-openapi、hutool 7.0.0-M2 等需确认适配 | 切片 0 独立验证并全量回归；不兼容则同步升 springdoc 或降 Boot 到 3.4.x（仍满足 Spring AI 1.0.3） |
| Spring AI 版本联动 | BOM 传递依赖与 Boot BOM 的 Jackson/reactor 版本需一致 | 切片 1 用 `mvn dependency:tree` 核查冲突，交由两个 BOM 协调版本 |
| 端点自动注册 | Spring AI 在 DispatcherServlet 下注册 SSE/message 路由 | 集成回归确认未劫持 `/gen/**`、knife4j、静态资源；API-Key 过滤器正确放行端点 |
| Qoder 远程 HTTP+SSE 兼容 | 首个目标客户端行为需实测 | 切片 1 即用 Qoder 验证连接，不通再评估 stdio sidecar 兜底 |
| 生成内容体积 | 大项目全量 preview 树 JSON 可能过大 | 工具按 table/枚举粒度调用；必要时在 MCP 层展平裁剪字段（切片 3 评估） |

## 14. 目录与文件改动清单（预期）

```
yanggu-code-generator-backend/
  pom.xml                                        [改] Boot 3.3.1→3.5.x + Spring AI BOM + MCP WebMVC starter
  src/main/resources/application.yaml           [改] spring.ai.mcp.server.* + mcp.api-key
  src/main/java/com/yanggu/code/generator/
    config/McpToolConfig.java                   [新] 仅一个 ToolCallbackProvider bean（MethodToolCallbackProvider 聚合工具）
    mcp/McpApiKeyFilter.java                    [新] /mcp/** Bearer 校验（enabled 复用 spring.ai.mcp.server.enabled）
    mcp/CodeGeneratorMcpService.java            [新] @Tool 方法集合（薄壳，注入复用 service 与 GeneratorService.preview）
    （可选）mcp/dto/GeneratedFileDTO.java         [新] 视 preview 树对 Agent 是否冗余而定
  —— service/controller/生成逻辑：不改动（回归零风险；如需批量/自定义 templateGroupId 在 GeneratorService 补一个 additive 公开方法）——
docs/superpowers/specs/2026-11-12-mcp-server-integration-design.md  [新] 本文档
```
