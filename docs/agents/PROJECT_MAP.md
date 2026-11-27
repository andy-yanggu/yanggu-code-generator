# PROJECT_MAP — 代码库导航

> 目的：消灭每轮会话的重复探索成本，并防止"漏改共同上游"。
> 本文件是 [`AGENTS.md`](../../AGENTS.md) §2/§3 的详细版。**改任何标为「共同上游」的东西前先读这里。**

---

## 1. 模块总览

```
yanggu-code-generator/
├── AGENTS.md                        规则入口（每次会话必读）
├── docs/pitfalls.md                 已知坑库（按编号引用）
├── docs/agents/PROJECT_MAP.md       本文件
├── docs/superpowers/specs/          spec（设计 + 验收真相源）
├── docs/superpowers/plans/          plan（实施计划，Task 自带可抄代码）
├── docs/superpowers/templates/      spec-template.md
├── .superpowers/sdd/<plan>/         SDD 账本与 brief/diff（git-ignored）
├── .worktrees/                      隔离工作区（git-ignored）
├── sql/code_generator.sql           全量脚本，⚠️ 无迁移工具（P-011）
├── template/default/                代码生成模板库（业务产物，不是本项目源码）
├── yanggu-code-generator-backend/    Spring Boot 4.1.2 / Java 21
└── yanggu-code-generator-frontend/   Vue 3.5 + Vite 8 + TS 5.8
```

---

## 2. 后端包地图（`com.yanggu.code.generator`）

### 2.1 分层

| 包 | 职责 | 依赖方向 |
|---|---|---|
| `controller` | 参数校验 + 调 service，**无业务逻辑** | → service |
| `service` / `service.impl` | 业务逻辑 | → mapper, service.support, mapstruct |
| `service.support` | 生成链路的支撑组件（Builder 类，如 `DataModelBuilder`、`PreviewBuilder`） | 被 service.impl 消费 |
| `mapper` | MyBatis-Plus Mapper | → entity |
| `mapstruct` | DTO ↔ Entity ↔ VO 转换 | 纯映射 |
| `domain.{entity,dto,vo,query,bo,model}` | 业务域对象 | 无依赖 |
| `enums` | 业务枚举 | 无依赖 |
| `util` | 业务工具 | 无依赖 |
| `config` | 应用配置 | — |
| `common.*` | 框架级公共设施 | 被全层依赖 |

### 2.2 `common` 子包（公共设施，勿重复实现）

| 子包 | 关键类 | 用途 |
|---|---|---|
| `common.response` | **`ResultAdvice`**（`ResponseBodyAdvice`）、`Result`、`ResultEnum`、**`@IgnoreResult`** | 统一响应包装。**Controller 直接返回业务对象** |
| `common.exception` | `BusinessException`、`GlobalExceptionHandler`、`IException`、`IResultError` | 全局异常出口 |
| `common.validation.group` | `InsertGroup`、`UpdateGroup` | 校验分组 |
| `common.validation.code` / `.path` | — | 自定义校验注解 |
| `common.domain.vo` | **`PageVO`** | 分页返回类型 |
| `common.domain.entity` / `.query` | — | 跨模块基类 |
| `common.mybatis.*` | `config` / `mapper` / `typehandler` / `util` | MyBatis-Plus 增强 |
| `common.mapstruct` | — | Mapper 基类/配置 |

### 2.3 Controller 清单（13 个）

`Auth` `BaseClass` `Datasource` `Enum` `EnumItem` `FieldType` `Generator` `Project` `Table` `TableField` `Template` `TemplateGroup` `TemplateGroupProperty`

⚠️ `AuthController.login` 返回**写死的假 token**，后端接口当前基本无鉴权（见 AGENTS.md §6 禁区）。

---

## 3. 核心数据流：代码生成链路

**这是全项目最重要的一张图。改这里等于改多个消费端。**

```
                    GeneratorService.preview(CodePreviewQuery)
                              │  内部私有：tablePreview / buildProjectPreview / enumPreview
                              │            → List<TemplateContentVO>
                              │            → buildTree
                              ▼
                    List<PreviewTemplateVO>   ← 【共同上游】结构化文件树
                    节点字段：filePath / templateContent / templateType / children
                    templateType: 0=目录  1=模板文件  2=二进制(base64)
                              │
        ┌─────────────────────┼─────────────────────┐
        ▼                     ▼                     ▼
   downloadZip          downloadLocal         （规划中）MCP 工具面
   （浏览器下载）        （服务器磁盘写入）      返回文件树由 Agent 落盘
```

**改动纪律**：
- 改 `preview` 或其私有预览方法 → 回归必须覆盖全部三条消费端。
- 新增消费端优先 **additive**（加公开方法），不改现有返回类型。
- `downloadLocal` 会写服务器磁盘，是三者中风险最高的面。

### 3.1 渲染与元数据链路

```
Table / TableField（字段配置：查询字段、列表字段、input 类型）
   + FieldType（DB 类型 → Java 类型映射，内置大量基础数据）
   + Enum / EnumItem（枚举，生成 SexType → UserTypeEnum 之类）
   + TemplateGroup / Template / TemplateGroupProperty（模板组与模板变量）
        ↓
   service.support 的 Builder 层（DataModelBuilder / PreviewBuilder / VariableCatalogBuilder）
        ↓
   FreeMarker 渲染  ⚠️ 沙箱已禁用 ?new（安全修复，P-005）
        ↓
   template/default/**  产物结构（目录名含 ${backendPath} / ${frontendPath} 占位）
```

`VariableCatalog`（变量目录）是模板变量的可发现目录，前后端各有对应实现，改动需同步。

### 3.2 数据库方言

支持 MySQL 与 **Oracle**。已知差异点：Oracle 表名查询曾出现**重复 ORDER BY**（P-006）。任何涉及 SQL 拼接的改动都要验证两个方言分支，而**当前后端测试零 DB 集成覆盖**。

---

## 4. 前端地图（`yanggu-code-generator-frontend/src`）

### 4.1 目录职责

| 目录 | 职责 |
|---|---|
| `api/<域>` | 接口调用封装，域为 `auth` / `common` / `gen` |
| `types/<层>` | 类型定义，镜像结构：`api` `common` `hooks` `router` `schema` `store` `utils` |
| `business` | 业务组件 |
| `components` | 通用组件（含 `components/context-menu` 声明式右键菜单） |
| `hooks` | 通用 composable |
| `store` | Pinia（+ `pinia-plugin-persistedstate` 持久化） |
| `layout` | 布局与 keep-alive |
| `views` | 页面，`views/gen/**` 是代码生成器主功能区 |
| `utils` | 工具（含 `context-menu.test.ts`） |
| `directive` / `config` / `constant` / `mocks` / `icons` / `styles` | 支撑层 |

### 4.2 最复杂的一块：模板配置抽屉（v2）

```
views/gen/template/
├── tree-v2.vue                    抽屉入口（v2）
├── tree-panel.vue                 树面板：工具栏 + 右键菜单 + 勾选
│   （v1 `tree.vue` 已删除，不在目录中；`tree-utils.test.ts` 仍保留）
├── editor-panel.vue               编辑面板：CodeMirror + tab + tab 菜单
├── use-template-tree-state.ts     ★ 编排层 facade（弹窗唯一所在地）
├── use-template-tree-data.ts      数据域
├── use-template-edit-tabs.ts      会话域（discardEdits / applyEditRollback）
├── use-template-persistence.ts    持久化域
├── tree-utils.ts                  ★ 零 import 纯函数模块（buildTree / buildFileList /
│                                     findByIdFromTree / getFullPathById / joinFileName）
├── tree-utils.test.ts             32 个用例
└── variable-panel/                变量目录面板（use-variable-catalog / use-variable-panel-size）
```

**关键不变式**（改这块前必读 AGENTS.md §3.3–3.6）：
1. `tabList` 元素与 `treeList` 节点是**同一批对象引用**（SAME_REF）→ mutate 一次，tab 与树上红点同时变化。
2. `discardEdits` = 内容回基线、**基线不动**；`applyEditRollback` = 基线前进、清红点。**方向相反，混用是反向事故。**
3. content-watch **只观察当前激活 tab** → 批量操作必须显式逐个处理，勿依赖 watch 清标记。
4. ⚠️ **v1 `tree.vue` 已于 `4a27750b` 删除**，双跑阶段已结束 —— 新变更不得写“v1 零改动 / v1对照”类约束（P-013）。

### 4.3 CRUD 组件体系

页面层创建 hook → 以 props 下传给纯展示组件 → 父子共享同一 reactive 对象。
因此 `vue/no-mutating-props` 配 `shallowOnly: true` 是**架构必需**，不是 lint 漏洞（P-009）。

---

## 5. 测试资产分布

| 位置 | 内容 | 环境 |
|---|---|---|
| `frontend/src/**/*.test.ts` | 5 文件 40+ 用例（`tree-utils` / `variable-utils` / `use-variable-catalog` / `use-variable-panel-size` / `context-menu`） | vitest `node` |
| `frontend/vitest.config.ts` | `environment:'node'`，`include:['src/**/*.{test,spec}.ts']` | 测组件需 `// @vitest-environment jsdom` |
| `backend/src/test/java` | 9 文件，全在 builder/service 计算层（`GeneratorQueryTest`、`DataModelBuilderTest`、`PreviewBuilderRenderTest`、`ProductGeneratorTest`、`VariableCatalog*Test`、`DownloadHandlerTest` 等） | `spring-boot-starter-test` |
| **空白区** | Controller / Mapper / DB 集成 / 性能 / e2e 自动化 / eval 黄金集 | — |

---

## 6. 文档与流程资产

| 位置 | 内容 |
|---|---|
| `docs/superpowers/specs/` | 设计文档。⚠️ 混有 `.js` suite 脚本与 `_peek.js`/`_fnv.js` 工具，会污染检索（P-012） |
| `docs/superpowers/plans/` | 5 份实施计划 |
| `docs/superpowers/templates/spec-template.md` | spec 模板：AC 表 + 验证命令 + diff 预算 |
| `docs/pitfalls.md` | 坑库，AGENTS.md 与各 spec 按 `P-0xx` 编号引用 |
| `.superpowers/sdd/` | SDD 账本 / task brief / review package（脚本自动创建，已 self-ignore） |
| `vitest-out.txt`（根目录） | ⚠️ 测试输出被放在仓库根，属产物污染（P-012） |

---

## 7. 常用命令

```powershell
# 前端
cd yanggu-code-generator-frontend
npm run dev | build | preview | test | test:watch
npm run lint          # 会 --fix，注意会改文件
npm run type-check    # vue-tsc --noEmit
npx vitest run src/views/gen/template/tree-utils.test.ts -t "用例名片段"   # 定向

# 后端
cd yanggu-code-generator-backend
mvn -q test-compile | mvn -q test | mvn clean package
# 产物：java -jar yanggu-code-generator-backend.jar

# SDD 脚本（必须用 Git Bash，见 P-001）
& "E:\software\Git\bin\bash.exe" "C:/Users/76584/.qoder/plugins/cache/qoder-marketplace/superpowers/6.3.0/skills/subagent-driven-development/scripts/task-brief" "docs/superpowers/plans/<plan>.md" <N>
```
