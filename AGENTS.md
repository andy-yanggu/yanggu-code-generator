# AGENTS.md — yanggu-code-generator 项目级约束

> 本文件是 AI agent 进入本仓库时的**唯一规则入口**。每次会话开始应完整读取本文件。
> 详细导航见 [`docs/agents/PROJECT_MAP.md`](docs/agents/PROJECT_MAP.md)；已知坑库见 [`docs/pitfalls.md`](docs/pitfalls.md)。
>
> **规则来源**：全部提取自仓库现有代码、配置与设计文档，非凭空拟定。若代码与本文件冲突，**先改本文件并说明**，不要静默按代码行事。

---

## 0. 硬门禁（交付前必须全绿，逐条实际运行）

| # | 检查项 | 命令 | 预期 |
|---|---|---|---|
| G1 | 前端类型 | `cd yanggu-code-generator-frontend; npm run type-check` | 退出码 0 |
| G2 | 前端规范 | `cd yanggu-code-generator-frontend; npm run lint` | 退出码 0（注意 `--fix` 会改文件） |
| G3 | 前端单测 | `cd yanggu-code-generator-frontend; npm run test` | 全部通过 |
| G4 | 后端编译 | `cd yanggu-code-generator-backend; mvn -q test-compile` | 退出码 0 |
| G5 | 后端单测 | `cd yanggu-code-generator-backend; mvn -q test` | 全部通过 |
| G6 | diff 越界 | `git diff --stat <BASE>..HEAD` 与 spec §6.1 改动面清单比对 | 无清单外文件 |
| G7 | 凭据泄漏 | `git diff <BASE>..HEAD \| Select-String -Pattern "password\s*[:=]\s*['\"]?\S"` | 无命中（**已有反例，见 pitfalls P-008**） |
| G8 | commit 规范 | `git log --no-merges --format="%s" <BASE>..HEAD \| Where-Object { $_ -notmatch "^(feat\|fix\|perf\|refactor\|docs\|test\|chore\|style\|build\|revert)(\(.+\))?: " }` | **无输出**（有输出即不合规行） |
| G9 | pathspec 存活 | `git cat-file -e "<HEAD>:<路径>"` 逐个验证门禁里的每个文件路径 | exit 0（不存在则门禁会静默假通过） |

**执行纪律**：
- 未跑过 G1–G9 不得声称"已完成"。报告里必须粘贴命令与实际输出摘要。
- 上述命令中的 `\|` 是表格转义符，实际执行时写成普通 `|` 即可，**并保持单行**。
- G7 必须基于 **diff**（只看新增行），不能全文 grep —— 存量代码里本就有违规凭据，全文匹配会让门禁永远红而被忽略（同构错误见 pitfalls P-004）。
- `<BASE>` 取开始本次变更前记录的 `git rev-parse HEAD`，**禁止用 `HEAD~1`**（多 commit 变更会静默丢失前面的 commit）。
- **G9 不是可选项**：实测 `git diff --stat <BASE>..HEAD -- <不存在的文件>` 返回**空输出 + exit 0**，且加 `--exit-code` 也一致。
  任何"diff 必须为空"类断言（如零改动域承诺），只要 pathspec 里混入被删/改名的路径，就会**永远真空通过**。
  历史上 AC-24 就因此带了一条死路径（`tree.vue`，已删）。详见 pitfalls P-022。

---

## 1. 技术栈事实（版本已实测核对，勿凭印象判断）

| 组件 | 版本 | 影响 |
|---|---|---|
| **Spring Boot** | **4.1.2** | ⚠️ 已是 Boot 4.x。starter 命名已迁移：用 `spring-boot-starter-webmvc`（**不是** `spring-boot-starter-web`） |
| Java | 21（`maven.compiler.source/target`） | 可用 record、模式匹配、虚拟线程 |
| MyBatis-Plus | 3.5.18 | |
| Hutool | **7.0.0-M8** | 里程碑预发布版，API 可能不稳定 |
| knife4j | **knife4j-next 5.8.2** | 注意 artifactId 是 `knife4j-next` |
| MapStruct | 1.6.3 | |
| FreeMarker | 直接依赖 `freemarker`（非 Boot starter） | 模板渲染核心；沙箱已禁用 `?new` |
| 前端 | Vue 3.5 + Vite 8 + TS 5.8 + Element Plus 2.11 + Pinia 4 | |
| 测试 | 前端 vitest 4（+ MSW 2）；后端 `spring-boot-starter-test` | |

> **已失效的设计文档结论**：[`2026-11-12-mcp-server-integration-design.md`](docs/superpowers/specs/2026-11-12-mcp-server-integration-design.md) §2 计划"升级 Boot 3.3.1 → 3.5.x 以采用 Spring AI 1.0.3，并排除 Spring AI 2.0（要求 Boot 4.0，跨度大）"。
> **该前提已不成立**：仓库实际已是 Boot 4.1.2。若现在做 MCP 集成，应直接评估 **Spring AI 2.x**，而不是回退到 1.0.3 路线。同时该文档的 `sse-endpoint`/`sse-message-endpoint` 选型也被 MCP 2026-07-28 规范判为过时（HTTP+SSE 列为弃用候选，应改 Streamable HTTP）。

---

## 2. 后端约定

### 2.1 分层与包结构

```
com.yanggu.code.generator
├── controller      13 个，仅做参数校验 + 调用 service，禁止写业务逻辑
├── service         接口；impl 子包；support 子包放支撑组件
├── mapper          MyBatis-Plus Mapper
├── mapstruct       DTO ↔ Entity ↔ VO 转换
├── domain
│   ├── entity / dto / vo / query / bo / model
└── common
    ├── config / exception / response / validation / mybatis / mapstruct / domain
    └── domain.{entity,vo,query}   跨模块基类（含 PageVO）
```

### 2.2 命名与接口模式（照此写，勿自创）

- 后缀：`XxxEntity` / `XxxDTO` / `XxxVO` / `XxxQuery` / `XxxService` / `XxxServiceImpl` / `XxxMapper`
- Controller 路径：`@RequestMapping("/gen/<模块>")`，如 `/gen/template`
- **标准端点集（含动词约定，已核对多个 Controller）**：

  | 端点 | HTTP 动词 | 入参 | 返回 |
  |---|---|---|---|
  | `/add` | `@PostMapping` | `@RequestBody @Validated(InsertGroup.class) XxxDTO` | `XxxVO` |
  | `/update` | **`@PutMapping`** | `@RequestBody @Validated(UpdateGroup.class) XxxDTO` | `XxxVO` |
  | `/delete` | **`@DeleteMapping`** | id / id 集合 | — |
  | `/detail` | **`@GetMapping`** | id | `XxxVO` |
  | `/detailList` | `@PostMapping` | id 集合 | `List<XxxVO>` |
  | `/page` | `@PostMapping` | `@Validated XxxQuery` | `PageVO<XxxVO>` |
  | `/list` | `@PostMapping` | `XxxQuery`（可省） | `List<XxxVO>` |

  个别模块另有专有端点（如 `/tree`、`/batchDelete`、生成相关的 preview/download）。
  ⚠️ **不要把 `/update`、`/delete` 误写成 `@PostMapping`**，也不要给 `/detail` 用 POST——项目一致遵循 RESTful 动词。
- 每个端点必须带 `@Operation(summary = "中文说明")`
- **校验用分组**：`@RequestBody @Validated(InsertGroup.class) XxxDTO dto`
  更新用 `UpdateGroup.class`；查询对象用 `@Validated XxxQuery query`
  （分组定义在 `common/validation/group`）
- **返回值直接返回业务对象，不要自己包 `Result<T>`**：
  `common/response/ResultAdvice` 是 `@RestControllerAdvice(annotations = RestController.class)` 实现的
  `ResponseBodyAdvice`，会统一包装。需要**跳过包装**时用 `@IgnoreResult` 注解（类级或方法级均支持）。
- 分页返回 `PageVO<XxxVO>`；错误码见 `common/response/ResultEnum`；业务异常抛 `BusinessException`，由 `GlobalExceptionHandler` 处理。

### 2.3 生成链路的复用入口（改动前必读）

`GeneratorService.preview(CodePreviewQuery)` → `List<PreviewTemplateVO>` **是三条链路的共同上游**：
zip 下载、服务器本地写盘、以及规划中的 MCP 工具面。

- 改 `preview` = 同时改三处消费端 → 必须在 spec 的回归承诺里列出全部三条。
- 新增消费端优先做 **additive**（加公开方法），不要改现有返回类型或私有方法。
- 产物节点 `templateType`：`0`=目录 / `1`=模板文件 / `2`=二进制文件（base64）。

---

## 3. 前端约定

### 3.1 目录职责

`src/` 下：`api`（按域分：`auth`/`common`/`gen`）、`types`（镜像结构：`api`/`common`/`hooks`/`router`/`schema`/`store`/`utils`）、`business`、`components`、`hooks`、`store`(Pinia)、`layout`、`views`、`utils`、`directive`、`config`、`constant`、`mocks`。

### 3.2 ESLint 强制规则（`eslint.config.js`，不可绕过）

| 规则 | 配置 | 后果 |
|---|---|---|
| `func-style` | `['error','expression']` | **只能用 `const fn = () => {}`**，写 `function fn(){}` 直接报错 |
| `curly` | `'error'` | 单行 `if` 也必须加花括号 |
| `unused-imports/no-unused-imports` | `'error'` | 未用 import 是 error，不是 warn |
| `vue/multi-word-component-names` | `'off'` | **允许** `tree.vue`、`index.vue` 等单词组件名，别"修复"它 |
| `vue/no-mutating-props` | `['error',{shallowOnly:true}]` | ⚠️ **见 3.3，禁止改成默认配置** |
| 未用变量 | `argsIgnorePattern:'^_'` | 有意不用的参数/变量必须 `_` 前缀 |

格式：Prettier 接管，**缩进用 Tab**。`eslint-config-prettier` 放最后关掉冲突项。

### 3.3 关键架构约定：CRUD hook 下传（AI 高频破坏点）

`vue/no-mutating-props` 之所以放开到 `shallowOnly`，是因为本项目 CRUD 体系的既定架构：

> **hook 由页面层创建，以 props 传给纯展示组件，父子共享同一个 reactive 对象。**
> 子组件写 `formHook.dialog.visible` 改的是 hook 内部的 ref，props 引用并未被替换，数据不会不同步 —— 这属于架构约定，不是 props 突变。
> `shallowOnly: true` 保留"整体替换 prop"（`props.x = v`）的检测，只放行"改 prop 内部属性"。

**禁止**以任何理由把该规则恢复为默认值、或把 CRUD 组件重构成 provide/inject。要动必须先出 spec 并经人工确认。

### 3.4 模板树：v1 已删除，双跑阶段已结束

- v2 文件集：`tree-v2.vue` + `tree-panel.vue` + `editor-panel.vue` + `use-template-tree-state.ts`（域拆分 + facade 编排层）
- ⚠️ **v1 `tree.vue` 已于 commit `4a27750b` 删除**（`git cat-file -e HEAD:.../tree.vue` 返回不存在）。
  旧设计文档里“v1 冻结 / v1与v2 双跑差分 / v1/v2 预期行为差异”类约束**均已过期**，不得再作为 AC 或改动约束写入新 spec。
- 历史价值保留：`2026-11-24-template-tree-v1-feature-record.md` 仍是 v1 行为清单的参考，但它是**只读历史**，无文件可对照。

### 3.5 状态域拆分边界

编排层 `use-template-tree-state.ts` 是**弹窗与提示的唯一所在地**。数据域（`use-template-tree-data.ts`）、会话域（`use-template-edit-tabs.ts`）、持久化域（`use-template-persistence.ts`）**零弹窗**，组件层（tree-panel / editor-panel）只允许加按钮和菜单项声明。

验证方式必须基于 diff：`git diff <BASE>..HEAD -- "<组件路径>" | Select-String -Pattern "^\+.*(ElMessageBox|ElMessage\.)"` 应无输出（这两个文件里**存量**删除链路的弹窗调用是合规的，全文 grep 会假阳性）。

### 3.6 纯函数模块惯例

`tree-utils.ts`、`variable-utils.ts` **当前是 0 条 import 的零依赖模块**（已实测）。新增函数须保持：
- 不依赖响应式、不调 API、不弹窗
- 需要外部查询时**用参数注入**（如 `findById: (id: number) => Tree | undefined`）

这保证它们能在 vitest 的 `environment: 'node'` 下直接单测。

---

## 4. 测试约定

| 项 | 现状 | 约束 |
|---|---|---|
| vitest 配置 | `environment: 'node'`，`include: ['src/**/*.{test,spec}.ts']` | 测 `.vue` 组件须在文件顶部加 `// @vitest-environment jsdom` |
| 已有单测 | 前端 5 文件（`tree-utils.test.ts` 含 32 用例）；后端 9 文件（全在 builder/service 计算层） | **Controller 与 Mapper 层零覆盖** |
| Mock | MSW 2 已安装 | 断言"零写接口调用"这类需求应写 MSW handler 计调用次数，而不是手工看 Network |
| e2e | **无 Playwright** | UI 级 AC 必须成对拆「逻辑层（可 unit）」+「表现层（manual/待自动化）」，禁止用一条 UI 级 AC 承载可单测的逻辑 |
| 后端集成测试 | 无（依赖本机 MySQL） | 加 Testcontainers 前先出 spec |

**写测试前先查有没有现成用例可复用** —— `tree-utils.test.ts` 已覆盖 `findByIdFromTree` / `getFullPathById` / `buildTree` / `buildFileList` / `joinFileName`。

---

## 5. 工作流约定

### 5.1 三份文档归位

| 产物 | 位置 | 说明 |
|---|---|---|
| spec（设计/验收真相源） | `docs/superpowers/specs/YYYY-MM-DD-<主题>.md` | **必须用** [`spec-template.md`](docs/superpowers/templates/spec-template.md)：Goal / Inputs / Constraints / Out of scope / AC 表（含验证命令）/ 改动面预算 / 回滚方案 |
| plan（实施计划） | `docs/superpowers/plans/YYYY-MM-DD-<主题>.md` | 每个 Task 自带可直接抄的代码与命令 |
| SDD 账本 | `.superpowers/sdd/<plan名>/progress.md` | 由脚本自动创建，**已 git-ignore**，完成后可删 |

spec 的核心纪律：**每条 AC 必须配一个实际跑过的验证命令**；写不出命令的 AC 要么改写成可观测结果，要么删掉。详见模板 §5.2。

### 5.2 Git

- 当前仅 `master` 单分支且直推 origin —— **变更前必须先建 feature 分支或 worktree**，不要在 master 上直接实施。
- `.worktrees/` 已在 `.gitignore` 中，可安全用于隔离工作区。
- commit message：`<type>(<scope>): <中文摘要>`，type 取 `feat`/`fix`/`perf`/`refactor`/`docs`/`test`/`chore`。
  **禁止**"代码优化"这类无信息 message —— 它让变更回溯与 AI 会话归因完全失效。
- **AI 不得自行 commit/push**，除非用户明确要求。
- commit message 是硬门禁 **G8**（历史中 78.7% 不合规，见 pitfalls P-018）。
  type 允许集含 `style`（已验证实例：`d5316dbc style: TemplatePathResolver 通配符 import 与 toList 简化（IDEA 调整）`）。
  **G8 已双向验证过**：取最近 12 个 commit，反向过滤输出 2 行非法的 `代码优化`，正向输出全为合规行。

### 5.3 Windows 环境硬约束（会直接导致失败）

- **`bash` 在 PATH 上被 WSL 劫持**（`C:\Windows\system32\bash.exe`）。WSL 会把 `C:\Users\...` 的反斜杠当转义符吃掉，脚本路径碎成一坨并报 `No such file or directory`。
  → 跑 superpowers 的 shell 脚本必须显式用 Git Bash，且**脚本路径用正斜杠**：
  ```powershell
  & "E:\software\Git\bin\bash.exe" "C:/Users/76584/.qoder/plugins/cache/qoder-marketplace/superpowers/6.3.0/skills/subagent-driven-development/scripts/task-brief" "docs/superpowers/plans/<plan>.md" 1
  ```
- **PowerShell 里不要用反引号续行接管道**：`git diff ... `` 换行 `| Select-String` 会把管道符当字符串解析并报错。命令写单行。

---

## 6. 禁区清单（触碰需先出 spec 并经人工确认）

| 对象 | 原因 |
|---|---|
| `tree.vue`（v1） | ⚠️ **已于 `4a27750b` 删除**，不再是禁区；但不得复活它或把 v1 对照类约束写进新 spec |
| `eslint.config.js` 的 `vue/no-mutating-props` / `vue/multi-word-component-names` | 有明确架构理由，见 3.2/3.3 |
| `GeneratorService.preview` 的返回类型与私有预览方法 | 三条链路共同上游 |
| `discardEdits` ↔ `applyEditRollback` 的语义边界 | 方向相反，混用会把未保存内容固化为新基线（反向事故） |
| FreeMarker 沙箱 `?new` 禁用 | 安全修复，勿"优化"回去 |
| 删除链路的 K 系已知缺陷 | 已登记，不夹带修复 |
| `AuthController.login` 的写死假 token | 现状即假，改动需单独 spec（涉及安全边界） |

---

## 7. 遇到规则冲突时

1. 代码 vs 本文件 → 停下来报告，说明哪个是对的，等确认。
2. spec vs plan → 以 **spec 为权威**，plan 只是它的论证；按 spec 裁决并记进账本 `Ruling:` 行。
3. 本文件 vs pitfalls.md → pitfalls 更新，冲突时以 pitfalls 为准并回头修本文件。

**不要静默妥协**。任何"我先按代码里的写法来"都必须显式说出来。
