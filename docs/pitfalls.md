# pitfalls — 已知坑库

> **用途**：AI 不会读 git 历史，也不会读会话记忆。本文件是让它避免重复踩坑的**唯一可靠通道**。
> **纪律**：每条必须有 `ID`、`触发条件`、`错误做法`、`正确做法`。**修复了新坑必须回写本文件**（见 [`spec-template.md`](../superpowers/templates/spec-template.md) §9 交付清单）。
> **引用方式**：AGENTS.md、spec、plan 中按 `P-0xx` 编号引用，不复制正文。

---

## 环境与工具链

### P-001 · `bash` 在 PATH 上被 WSL 劫持，脚本路径被拆解

- **症状**：`/bin/bash: C:Users76584.qoderplugins...: No such file or directory`；并伴随 `wsl: 检测到 localhost 代理配置，但未镜像到 WSL`。
- **根因**：Windows 上 `Get-Command bash` 解析到 `C:\Windows\system32\bash.exe`（WSL）。WSL 把 Windows 路径的反斜杠**当转义符吃掉**，路径碎成一坨。
- **错误做法**：`bash scripts/task-brief ...`
- **正确做法**：显式调用 Git Bash，且**脚本路径用正斜杠**：
  ```powershell
  & "E:\software\Git\bin\bash.exe" "C:/Users/.../skills/subagent-driven-development/scripts/task-brief" "docs/superpowers/plans/<plan>.md" 1
  ```
- **验证**：已实测三个脚本（`sdd-workspace` / `task-brief` / `review-package`）在 Git Bash 下全部正常。
- **触发条件**：任何调用 superpowers shell 脚本的场景。

### P-002 · PowerShell 反引号续行 + 管道 → 管道符被当字符串

- **症状**：命令报错或无输出，看起来像"命令通过"。
- **根因**：`` ` `` 续行后接 `| Select-String`，PowerShell 解析异常。
- **正确做法**：验证命令一律**写成单行**（spec 模板 §5.2 已收此规则）。
- **触发条件**：写 spec 的验证命令列时。

### P-003 · `HEAD~1` 会静默丢失多 commit 变更

- **症状**：review/diff 只看到最后一个 commit，前面的改动凭空消失，评审通过但功能缺失。
- **正确做法**：记录变更前 `git rev-parse HEAD` 作为 `<BASE>`，用 `<BASE>..HEAD`。SDD 的 `review-package` 脚本正是为此强制要求显式 BASE。
- **触发条件**：任何生成 diff、review 包、回滚范围的场景。

---

## 验证与门禁写法

### P-004 · 全文 grep 当门禁 → 假阳性导致门禁永久红并被忽略

- **实例**：为"组件层不许有弹窗"写
  `Select-String -Path tree-panel.vue -Pattern "ElMessageBox|ElMessage\."`
  → **命中 5 处**（L200/L203/L212/L226/L273），全部是删除/移动链路的**合规存量代码**。
- **根因**：约束是「**新增**代码不许有 X」，命令却在断言「全文不许有 X」。
- **正确做法**：断言**变化**而非状态，基于 diff：
  ```powershell
  git diff <BASE>..HEAD -- ".../tree-panel.vue" | Select-String -Pattern "^\+.*(ElMessageBox|ElMessage\.)"
  ```
- **额外纪律**：验证命令自己也要双向测试 —— 负向例（应无输出）+ 正向例（应命中）。
  上例已在 commit `63110161` 上正向验证命中 6 行新增弹窗调用。
- **触发条件**：写 spec 的 AC 验证命令时（最高频）。

### P-005 · 未盘同模块已有能力 → 重复实现已有函数

- **实例**：设计 `collectDirtyFiles` 时手写了 `walk` 递归，而**同模块 `tree-utils.ts` 已有 `buildFileList`**（用例描述："收集所有非目录节点并拍平，深度优先顺序"）。
- **根因**：跨模块复用盘点做了，同模块内没盘。AI 尤其容易漏——它倾向于"从零写一段"而不是先查邻居。
- **正确做法**：spec 的 §2「Inputs 上下文锚点」（见 [`spec-template.md`](../superpowers/templates/spec-template.md)）必须包含一栏**同模块兄弟函数**；实现前先 grep 函数名。
- **触发条件**：新增任何纯函数/工具方法。

---

## 后端

### P-006 · FreeMarker 沙箱 `?new` 与 Oracle 方言重复 ORDER BY（勿"优化"回去）

- **来源**：commit `0fca2478` `fix(backend): FreeMarker 模板沙箱禁 ?new，修 Oracle 表名查询重复 ORDER BY`
- **两个独立坑**：
  1. `?new` 是模板注入面，已禁用。任何"让模板更灵活"的改动不得放开。
  2. Oracle 表名查询存在**重复 ORDER BY**，方言分支行为与 MySQL 不同。
- **风险放大因素**：后端测试**零 DB 集成覆盖**（9 个测试全在 builder/service 计算层）。SQL 拼接类改动没有测试网。
- **触发条件**：改 FreeMarker 模板、改方言相关 SQL。

### P-007 · 模板测试接口的入参校验缺失与 N+1 查询

- **来源**：commit `2e944e9b` `fix(backend): 模板测试接口补入参校验并消除两处 N+1 查询`
- **教训**：该接口的入参校验曾完全缺失；循环内查询造成 N+1。
- **正确做法**：新接口必须有 `@Validated` + 分组；列表查询先想批量。
- **触发条件**：新增/修改 `Generator` 或模板测试相关接口。

### P-008 · 凭据硬编码入库（存量违规，勿模仿）

- **事实**：`application.yaml` 已入 git，含 `username` / `password` 真实值；远端为 gitee 公开域仓库。
- **为什么是 AI 特有问题**：**AI 会模仿仓库已有写法**。一旦某种坏习惯进了仓库，之后每轮生成都会忠实地复制它，而且复制得很有说服力。
- **正确做法**：新配置一律 `${DB_PASSWORD}` 走环境变量。**不要顺手复制存量 `application.yaml` 的写法。**
- **触发条件**：改配置、加数据源、加第三方 key。
- **门禁**：AGENTS.md §0 的 **G7** 已用 diff 方式检测。

---

## 前端

### P-009 · `vue/no-mutating-props` 的 `shallowOnly` 是架构必需

- **事实**：`eslint.config.js` 里该规则配 `['error', { shallowOnly: true }]`，并在配置内写了完整理由注释。
- **架构**：CRUD hook 由页面层创建 → 以 props 下传 → 父子共享同一 reactive 对象。子组件写 `formHook.dialog.visible` 改的是 hook 内部 ref，props 引用未被替换，**数据不会不同步**。
- **错误做法**：把它"修"成默认配置；或把 CRUD 组件重构为 provide/inject。
- **后果**：整个 CRUD 体系 lint 全红，或大范围无谓重构。
- **触发条件**：AI 看到"违反 Vue 最佳实践"的代码时（高频诱惑）。

### P-010 · `vue/multi-word-component-names: off` 是有意的

- **事实**：项目大量使用 `tree.vue` / `index.vue` / `tree-panel.vue` 等单词/kebab 组件名。
- **错误做法**：为"符合 Vue 官方规范"而重命名组件 —— 会连带破坏路由、keep-alive 缓存 key 与 `defineOptions` 名称。
- **触发条件**：lint 或 review 建议改组件命名时。

### P-011 · keep-alive 缓存页下拉数据陈旧

- **来源**：commit `c3640de4` `fix(frontend): 新增 useOnActivatedRefresh hook，修复缓存页下拉数据陈旧`
- **症状**：页面被缓存，重新激活时数据源/枚举下拉仍是旧值。
- **正确做法**：缓存页的激活刷新走 `useOnActivatedRefresh`，不要各自手写 `onActivated`。
- **触发条件**：新增任何带下拉数据的缓存页面。

### P-012 · iframe 复用组件的命名坑

- **来源**：commit `a7f89e0e` `refactor(frontend): 修复 iframe 复用组件命名并清理死代码`
- **教训**：iframe 复用场景下组件名影响实例识别，命名不能随意。改动需连带清理死代码。
- **触发条件**：涉及 iframe 嵌入/复用的页面。

### P-013 · v1 基线被删除后，“v1 对照类约束”全部静默失效

- **事实**：v1 `tree.vue` 已于 commit `4a27750b`（`refactor(template): 删除 v1 单体 tree.vue，模板组入口切至拆分后的组件集`）从仓库删除。
  已用 `git cat-file -e "HEAD:yanggu-code-generator-frontend/src/views/gen/template/tree.vue"` 确认不存在。
- **症状**：大量文档与规则仍以 v1 为依据（“v1 冻结”“v1/v2 双跑差分”“v1/v2 预期行为差异”），而它们引用的文件已不在。
  更坑的是：拿它当 pathspec 的验证命令**不会报错**，而是返回空输出 + exit 0（见 P-022），所以门禁继续“绿”。
- **错误做法**：① 在新 spec 里继续写 v1 对照约束；② 反过来，去改 `2026-11-24` 系列历史设计文档“把过期说法改掉” ——
  它们是**变更快照**，回改会破坏可追溯性。
- **正确做法**：历史文档不动；在现行 spec 的 §11 登记为已过期；在 `AGENTS.md` 里把该节改写为“已删除”事实。
  **任何写进规则的文件引用，都要配一个机器可查的探活探针，并定期跑**。
- **触发条件**：引用具体文件路径作为约束/AC/门禁时；以及任何“双跑/对照/灰度”阶段结束时。

### P-014 · `discardEdits` 与 `applyEditRollback` 方向相反，混用是反向事故

- **语义**：`discardEdits` = 内容回基线、**基线不动**；`applyEditRollback` = 基线前进到当前内容、清红点。
- **事故形态**：回滚误用后者 → 未保存内容被静默固化为新基线，用户改动"看起来丢了又看起来在"。
- **加重因素**：两个名字都含 rollback 语义，极易选错。**当前该边界零测试保护**（见 SPEC-2026-11-25-001 的 AC-13，P0）。
- **另一条**：content-watch 只观察当前激活 tab → 批量操作必须显式逐个处理，勿依赖 watch 清标记。
- **触发条件**：模板编辑/tab/回滚相关改动。

---

## 设计文档时效性

### P-015 · MCP 集成设计文档的技术前提已失效

- **文档**：`2026-11-12-mcp-server-integration-design.md`
- **文档假设**：Boot 3.3.1 → 升 3.5.x，采用 Spring AI **1.0.3**；**明确排除** Spring AI 2.0（要求 Boot 4.0，"跨度大、迁移成本高"）。
- **实际现状**：`pom.xml` 已是 **`spring-boot.version = 4.1.2`**，且 starter 命名已迁移（用 `spring-boot-starter-webmvc`）。
- **结论反转**：当年排除 2.0 的理由（不愿升 Boot 4）**已不存在**。若现在做 MCP，应直接评估 Spring AI 2.x。
- **传输层也已过时**：文档选 `HTTP+SSE`（`sse-endpoint`/`sse-message-endpoint`）。MCP **2026-07-28** 规范把 HTTP+SSE 列为弃用候选，新实现应用 **Streamable HTTP**；`initialize`/session 握手也移向无状态，鉴权转向 OAuth 2.1（文档里的裸 API-Key 过滤器需重估）。
- **触发条件**：重启 MCP 集成工作前。

---

## 数据与部署

### P-016 · 无数据库迁移工具

- **事实**：只有 `sql/code_generator.sql` 全量脚本；pom 无 Flyway/Liquibase。
- **风险**：**AI 改了 Entity 而 schema 不同步是这个项目最容易出的静默故障**，且现在靠手工同步，不可复现、无法回溯。
- **正确做法**：改 Entity 时，必须按 [`spec-template.md`](../superpowers/templates/spec-template.md) §6.2「数据库 / 配置 / 依赖变更」显式写 DDL 与迁移方式；改完实际执行一次。
- **触发条件**：任何 Entity / 表结构改动。

### P-017 · 后端测试依赖本机 MySQL

- **事实**：数据源为 `localhost:3306`，测试无容器化。
- **后果**：`mvn test` 的可复现性取决于本机数据库状态；换环境可能失败或假通过。
- **触发条件**：CI 化、换机器、加集成测试前需先决定 Testcontainers 方案。

---

## 流程

### P-018 · 无信息 commit message 让 AI 会话无法归因（已实测：占历史 79%）

- **实测数据**（共 522 个 commit）：
  - **411 个（78.7%）不符合 conventional 规范**
  - 完全相同的 message 重复次数：`前端代码优化` **23 次**、`代码优化` **19 次**、`提交sql` 6 次、`优化前端代码` 3 次、`后端代码优化` 3 次
- **为什么这对 AI 工作流是致命的**：
  1. 无法判断某次 AI 改动对应哪份 spec → spec 与代码失去双向可追溯
  2. 出问题时无法二分定位是哪轮会话引入 → 只能重跑全量测试
  3. **`git log` 是 agent 恢复上下文的主要手段之一**（P-019）；log 里全是“代码优化”，等于恢复手段本身坏了
- **正确做法**：一个 commit 对应一个 spec 切片，`<type>(<scope>): <中文摘要>`。
  项目里已有大量规范例，照着写即可：
  `0fca2478 fix(backend): FreeMarker 模板沙箱禁 ?new，修 Oracle 表名查询重复 ORDER BY`
  `2e944e9b fix(backend): 模板测试接口补入参校验并消除两处 N+1 查询`
  `c3640de4 fix(frontend): 新增 useOnActivatedRefresh hook，修复缓存页下拉数据陈旧`
- **不要求清理历史**（重写 522 个 commit 代价过高）。只要求**从今往后**新 commit 合规；已列为硬门禁 **G8**（见 AGENTS.md §0）。
- **触发条件**：每次 commit 前。

### P-019 · SDD 账本未按 plan 留存（会话压缩后最贵的失败）

- **症状**：上下文被 compact 后，编排器**把整个已完成的任务序列重新跑了一遍**。
- **事实**：`.superpowers/sdd/` 下只剩 `2026-09-30-crud-component-system/progress.md` 一份（且只完成到 pre-flight 阶段），而 specs 有 20+ 份。
- **正确做法**：每个 plan 一个 workspace（脚本自动），账本首行写 `# SDD ledger — plan: <路径>` 做身份绑定；恢复时**信任账本与 `git log`，不信任自己的记忆**。
- **注意**：`git clean -fdx` 会摧毁 workspace（它是 git-ignored 暂存），需从 `git log` 恢复。

### P-020 · 产物污染文档目录，恶化 AI 检索质量

- **实例**：`docs/superpowers/specs/` 混有 `2026-11-24-t2-suite-*.js`、`_fnv.js`、`_peek.js`、`_run-suite-H.js`；仓库根有 `vitest-out.txt`。
- **后果**：语义检索读到噪音，检索质量下降 → 生成质量下降。且这些 suite 脚本作为"文档"存在，**无法被自动执行**，等于质量资产被浪费。
- **正确做法**：测试与脚本归 `tests/`（前端）；输出物不进 `docs/`。

### P-021 · 删除链路已登记缺陷（勿夹带修复）

- **内容**：删除链路的 **K 系缺陷**已登记，明确不在回滚功能（SPEC-2026-11-25-001）范围内。
- **原因**：夹带修复会让 diff 越界、回归面失控、review 无法聚焦。
- **正确做法**：发现邻近缺陷 → 写进 spec 的 §11「偏差与后续登记」（见 [`spec-template.md`](../superpowers/templates/spec-template.md)），另开 spec。

---

## 门禁本身的失效

### P-022 · pathspec 匹配不到文件时 `git diff` 静默返回空且 exit 0（假阴性死门禁）

- **实测结论**（四个对照实验）：

  | 命令 | 输出 | 退出码 |
  |---|---|---|
  | `git diff --stat <B> <H> -- <已删除的 tree.vue>` | 空 | 0 |
  | `git diff --stat <B> <H> -- <臆造的不存在文件名>` | 空 | 0 |
  | `git diff --exit-code <B> <H> -- <已删除的 tree.vue>` | 无 | **0**（换成退出码断言也拦不住死路径，所以必须先做存活检查） |
  | `git diff --exit-code <B> <H> -- <确实有改动的 tree-panel.vue>` | 无 | **1**（说明退出码机制本身有效） |

  **git 不报错、不警告、不看拼写。你无法从输出区分“真的没改”和“路径写错了”。**
- **为什么比 P-004 更危险**：P-004 是假阳性（永远红 → 被人当噪音忽略）；本条是**假阴性（永远绿 → 没有人会怀疑一个绿的门禁）**。假绿是门禁最危险的失效形态。
- **受害面**：所有「XX 文件不得变动 / diff 必须为空」类断言。尤其是 pathspec 为**多个文件的列表**时：
  只要其中一个是死路径，那一项就不受监控，而整个命令依旧“通过”。本项目的 AC-24 就是这么带了一条 `tree.vue` 死路径的。
- **正确做法（两层缺一不可）**：
  1. **预验存活**：对门禁里每个路径跑 `git cat-file -e "<HEAD>:<路径>"`，exit 非 0 则门禁无效。
  2. **用退出码而非空输出**：`git diff --exit-code <BASE> <HEAD> -- <路径>`，然后断言 `$LASTEXITCODE`（0=干净，1=有改动）。人眼看“输出为空”在任何截断/重定向下都不可靠。
  已写进 AGENTS.md §0 作为硬门禁 **G9**。
- **额外注意**：重命名/删除类变更会让**上一轮 spec 里的路径集体失效**。所以每次写 diff 型 AC 都要重新跑一遭存活检查，不能复用上次的命令。
- **触发条件**：写任何基于 `git diff` + pathspec 的断言；以及任何涉及文件改名/删除的变更之后。
