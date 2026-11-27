# 模板内容回滚功能 Spec（模板化改造样例）

> 本文件由 [`2026-11-25-template-content-rollback-design.md`](./2026-11-25-template-content-rollback-design.md) 按 [`spec-template.md`](../templates/spec-template.md) 改造而来。
> 原设计文档保留不动（其设计推导与线框图信息量大，作为 §2/§6 的支撑材料被引用）。

---

## ⚠️ 改造说明（非 spec 正文，交付前删除本节）

### 原稿质量评价

**先说结论：原稿在设计推导层是高质量的，问题全部集中在验收层。**

| 维度 | 原稿表现 |
|---|---|
| 决策与否决方案 | ✅ 极强。6 个决策都写了备选与否决理由，这是多数 spec 缺失的 |
| 复用盘点 | ✅ 极强。「已具备的机制」表逐条标注位置与关系，直接可用做 §2 |
| 改动面控制 | ✅ 强。「四域零改动 / 仅新增一个纯函数」就是 §6.1 diff 预算的雏形 |
| 边界枚举 | ✅ 强。stale key、空集合、二进制/目录、激活态刷新都覆盖到了 |
| **非目标** | 🟡 有，但混在「设计目标与非目标」的散文里，AI 容易当成背景描述而非硬约束 |
| **验收标准** | ❌ **致命**。「验证方案」是散文段落，无编号、无 ID、无命令 → 无法回答"哪条标准被测试覆盖" |
| **回滚方案** | ❌ 缺失。功能是"回滚别人的内容"，但本变更自己如何回滚没写 |
| **约束的显式化** | 🟡 v1 冻结、数据域零弹窗、func-style 都散落在正文，未成约束清单 |

### 改造引入的核心价值：AC 覆盖率统计

把散文式验证方案拆成 **28 条 AC**，并**逐条把验证命令实际跑一遍**后，真实情况第一次变得可见：

| 状态 | 数量 | 占比 | 含义 |
|---|---|---|---|
| ✅ 命令可跑且现状已满足 | 5 | 18% | AC-21/24/25/26/28（diff 与静态类，**零新代码**即可获得保护） |
| 🟡 命令存在但用例待写 | 12 | 43% | `vitest run` 已验证可用，对应 case 未存 |
| ⬜ 暂无法机器验证 | 11 | 39% | 依赖 jsdom / Playwright，或只能人工与审查 |

进一步按"能否下沉"分类，得到本样例最重要的结论：

| 分类 | 数量 | AC |
|---|---|---|
| **零成本可下沉到单测**（纯函数 / node 环境可跑） | 12 | AC-01~08、12、13、15、16、23 |
| **需 `// @vitest-environment jsdom`**（组件/菜单渲染） | 8 | AC-09/10/11/17/18/19/20/22 |
| **需 e2e 自动化**（Playwright，当前未引入） | 1 | AC-14（编辑区可视刷新） |
| **已可满足（静态/diff）** | 4 | AC-24/25/26/28 |
| **纯回归抽验** | 1 | AC-27 |
| **现状已被既有测试覆盖** | 1 | AC-21（`joinFileName` 已有用例） |

**为什么这件事值钱**：原稿写着"验证方案：单测 + 浏览器实测"，看起来是有验证的。拆成表格并跑过命令后暴露出——

1. **真正已有测试保护的只有 AC-21 一条**（`joinFileName` 的 6 个现成用例）；
2. **决策 2 里明确标注的红线（AC-13：禁止误用 `applyEditRollback`）完全没有测试保护** —— 而这正是"回滚变成把未保存内容确认为新基线"这种反向事故会漏过去的地方；
3. **12 条只需写单测就能从 ⬜/🟡 变绿**，成本比原稿预估的"全靠浏览器实测"低得多。

### 改造中实际发现的 4 处 spec 缺陷

| 位置 | 缺陷 | 影响 | 建议 |
|---|---|---|---|
| 决策 6 + 交互流程 2 | 称单个入口"天然不可达，无需兜底"，但可达性完全依赖 `visible` 条件（`templateType === 1 && isEdited`）；而 `rollbackNode` 内已有"双保险守卫" | 措辞自相矛盾；`visible` 条件若被改错，会出现"可点但无反应"或"对干净文件弹确认" | 改为明确 AC：**必须有用例证明 `visible` 条件正确**（见 AC-07/AC-08），并保留守卫 |
| 验证方案「单测」段 | 未区分"激活 tab 回滚后编辑区刷新"（依赖 CodeMirror v-model，node 环境测不了）与"基线不推进"（纯数据，可单测） | 该 AC 实际处于无覆盖状态，但读起来像已覆盖 | 按模板 C7 规则成对拆分：AC-13（unit）+ AC-14（e2e-manual） |
| 决策 1 / 边界 6 | "已保存进库的内容回不来"是强用户预期风险，但只作为文案承诺，无验证条款 | 用户误认为可回到数据库历史版本 → 线上投诉 | 补 AC-16（unit）+ §4 明确"不提供版本历史"为永久范围外 |
| 接口契约「tree-utils 新增纯函数」 | 手写了 `walk` 递归，但**同模块已有 `buildFileList`**（用例描述："收集所有非目录节点并拍平，深度优先"）可直接复用 | 重复实现已有能力；新增测试面变大 | 改为 `collectDirtyFiles = buildFileList(子树).filter(n => n.isEdited)` + 去重，见决策 3 补充 |

> 第 4 条是本次改造里最有代表性的发现：**它不是逻辑错误，而是"没盘点已有能力"**——而这正是 AI 实现时最高频的问题（原稿已经列了机制复用表，却漏了自己模块内的函数）。

---

## 0. 元信息

| 项 | 值 |
|---|---|
| ID | SPEC-2026-11-25-001 |
| 状态 | 已评审（设计已确认，待实施） |
| 关联 plan | 待补：`docs/superpowers/plans/2026-11-25-template-content-rollback.md` |
| 关联 SDD ledger | 待建：`.superpowers/sdd/2026-11-25-template-content-rollback/progress.md` |
| 基线 commit | 待补（实施前记录 BASE SHA） |
| owner / 评审人 | 待补 |
| **分层落点** | `frontend-only` |
| 影响数据库 | **否**（纯内存操作，零接口调用） |

**支撑材料**：设计推导、页面线框、状态对照图见原稿 [`2026-11-25-template-content-rollback-design.md`](./2026-11-25-template-content-rollback-design.md) §「设计决策」「页面设计」「交互流程与边界」。

---

## 1. Goal

**一句话**：在模板配置抽屉 v2 提供 3 个回滚入口（树工具栏批量、树节点右键单个、tab 右键单个），把脏文件的 `templateContent` 恢复到内存基线 `originalTemplateContent`，执行后红点双向熄灭、基线不推进、**全程零写接口调用**。

### 背景与动机

用户在 CodeMirror 里改模板内容后，目前唯一的"丢弃修改"路径是**退出路径的副产品**：关 tab 或关抽屉时选「否(N)」才走 `discardEdits`。想丢弃修改但继续工作，必须先关再开——操作成本高且丢失打开状态。需要一个"操作路径"上的丢弃能力：内容回基线，但文件保持打开、tab 不关闭、可继续编辑。

### 成功判据

在 dev 环境对 1 个脏文件从任一入口执行回滚 → 编辑区显示基线内容、树与 tab 红点同时熄灭、浏览器 Network 面板**零 PUT 请求**、可立即继续编辑产生新脏态。

---

## 2. Inputs（上下文锚点）

全部位于 `yanggu-code-generator-frontend/src/views/gen/template/`。

| 类型 | 路径 / 名称 | 与本次的关系 |
|---|---|---|
| 复用不改 | `tree-utils.ts` → `Tree.originalTemplateContent` | 回滚的目标版本，**不新增字段** |
| 复用不改 | `tree-utils.ts` → `joinFileName` | 确认文案的文件名折叠口径（>3 折叠） |
| 复用不改 | `use-template-edit-tabs.ts` → `discardEdits(tabs)` | 回滚的落标记动作，语义完全一致 |
| 复用不改 | `use-template-tree-state.ts` → `treeData.findById` | 勾选 key 反查节点，参数注入用 |
| 复用不改 | `components/context-menu` | 两个新菜单项零组件成本 |
| 复用不改 | tree-panel 删除链路的 `getCheckedKeys` 勾选口径 | 批量范围与之对齐 |
| **新增** | `tree-utils.ts` → `collectDirtyFiles(checkedKeys, findById)` | 纯函数，收集待回滚脏文件 |
| **新增** | `use-template-tree-state.ts` → `rollbackNode` / `rollbackCheckedNodes` / `confirmAndRollback` | 编排层，弹窗唯一所在地 |
| 改动 | `tree-panel.vue` | 工具栏按钮 + `treeMenuItems` 追加一项 |
| 改动 | `editor-panel.vue` | `tabMenuItems` 顶部追加一项 |
| **零改动** | `use-template-edit-tabs.ts` / `use-template-tree-data.ts` / `use-template-persistence.ts` | 域边界承诺，见 AC-24 |
| **零改动** | `tree-v2.vue` | 不涉及 |
| **已不存在** | `tree.vue`（v1） | ⚠️ **已于 `4a27750b` 删除**。本 spec 初稿把它写成“冻结/零改动”，属于引用死路径，见 §5.3 与 pitfalls P-022 |
| 已有测试 | `tree-utils.test.ts`（**32 个现成用例**，已覆盖 `findByIdFromTree` / `getFullPathById` / `buildTree` / `buildFileList` / `joinFileName`） | `collectDirtyFiles` 用例落点；且 `buildFileList` 可直接复用（见决策 3 补充） |
| 已有测试 | `use-template-edit-tabs.ts` 的 `discardEdits` / `applyEditRollback` 反向语义 | **当前无测试，是红线**（AC-13） |

**共同上游 / 关键链路**：`discardEdits` 同时被「关 tab 选否」与本次「回滚」复用。二者共用底层动作、入口与文案分开，**不互相改造**（原稿「现有关闭确认中否(N)的区别」一节）。改 `discardEdits` 会同时影响两条链路。

---

## 3. Constraints

### 3.1 项目固有约束（不可协商）

| # | 约束 | 违反后果 | 对应 AC |
|---|---|---|---|
| C1 | **v1 已不存在**：`tree.vue` 已于 `4a27750b` 删除，不得复活，也不得再依赖“v1/v2 双跑对照”作为约束 | 引用死路径会使 diff 门禁**静默真空通过**（空输出 + exit 0） | AC-24 |
| C2 | **数据域零弹窗**：弹窗/提示只允许在编排层 `use-template-tree-state.ts` | 违反状态域拆分设计，弹窗逻辑散落多域无法复用 | AC-25 |
| C3 | **纯函数惯例**：`tree-utils.ts` 内不得依赖响应式；外部查询用参数注入（`findById`） | 该模块不可单测，node 环境跑不起来 | AC-26 |
| C4 | **禁止误用 `applyEditRollback`**：与 `discardEdits` 方向相反，误用等于把未保存内容确认为新基线 | **反向事故**，用户改动被静默固化 | AC-13 |
| C5 | ESLint `func-style` 强制函数表达式 | lint 红 | AC-28 |
| C6 | `vitest.config.ts`：`environment: 'node'`，`include: ['src/**/*.{test,spec}.ts']`；组件测试须加 `// @vitest-environment jsdom` | 测试跑不起来或找不到用例 | §8 |
| C7 | **前端无 Playwright**：UI 级 AC 必须成对拆分「逻辑层（可 unit）」+「表现层（manual/待自动化）」 | AC 处于"看起来有验证、实际无覆盖"状态 | AC-13/14 |

### 3.2 本次变更特有约束

- **零接口调用**：回滚不得经过任何写接口（`PUT updateContent` / `updateContentList`），也不得调用 `detailData` 拉库内最新。
- **不承诺版本历史**：基线是内存值（打开时或本次会话最近一次保存成功后），不承诺回到数据库更早版本。
- **不改既有链路的任何一行**：`handleCloseTabs` / `handleClose` 的确认链保持原样，回滚是新增并行链路而非改造。
- **不新造图标文件**：若无现成 rollback 类 svg，统一退化为 EP `RefreshLeft`。
- **会话域零改动**：不新增标记通道。

---

## 4. Out of scope（明确不做）

> 这一节是防蔓延的唯一防线。AI  implementations 中若触碰以下任一项，视为越权，回退重做。

- [ ] **数据库版本回滚**：不建历史版本表、不存快照、不调 `detailData`
- [ ] **撤销栈**：不做 Ctrl+Z 级多次回退，只有"一步到基线"
- [ ] **结构操作回滚**：新增 / 删除 / 拖拽移动 / 重命名 不可回滚（已即时落库，无事务可退）
- [ ] **改造「否(N)」关闭链路**：共用底层动作但入口分开，不合并、不重构
- [ ] **修删除链路已登记缺陷**（K 系）：不纳入本次 AC
- [ ] **复活 v1 `tree.vue`** 或以其为对照依据（已于 `4a27750b` 删除）
- [ ] **二进制文件与目录的回滚入口**：无编辑通道不会变脏，天然不可达
- [ ] 后端代码、模板产物、数据库 schema：**零变更**

---

## 5. Acceptance Criteria

### A. 脏文件收集（`collectDirtyFiles`，纯函数 → unit）

| AC-ID | 验收标准（可观测结果） | 验证命令 | 层级 | 状态 | 覆盖 |
|---|---|---|---|---|---|
| AC-01 | 勾选 1 个目录（含 2 个脏文件 + 1 个干净文件）→ 返回该目录下 2 个脏文件，**不含**干净文件与目录本身 | `cd yanggu-code-generator-frontend; npx vitest run src/views/gen/template/tree-utils.test.ts -t "collectDirtyFiles 目录递归只收脏文件"` | unit | 🟡 待补 | — |
| AC-02 | 父目录与其子文件同时勾选 → 结果按 `id` 去重，长度 1 | `... -t "collectDirtyFiles 父子同时勾选去重"` | unit | 🟡 待补 | — |
| AC-03 | 勾选 key 在树上已不存在（删除后未刷新）→ `findById` 返回 undefined，**静默过滤且不抛异常** | `... -t "collectDirtyFiles stale key 静默过滤"` | unit | 🟡 待补 | — |
| AC-04 | 勾选范围内无任何脏文件 → 返回空数组，长度 0 | `... -t "collectDirtyFiles 全干净返回空数组"` | unit | 🟡 待补 | — |
| AC-05 | 勾选单个文件节点（`templateType===1` 且 `isEdited`）→ 直接入集合，不做子节点递归 | `... -t "collectDirtyFiles 文件节点直接入集合"` | unit | 🟡 待补 | — |
| AC-06 | `templateType===2`（二进制）即便被勾选也**永不入集合** | `... -t "collectDirtyFiles 排除二进制节点"` | unit | 🟡 待补 | — |

### B. 入口显隐与禁用（菜单条件，逻辑可下沉 → unit/jsdom）

| AC-ID | 验收标准 | 验证命令 | 层级 | 状态 | 覆盖 |
|---|---|---|---|---|---|
| AC-07 | 脏文件节点 / 脏 tab → `visible` 返回 `true` | 抽出 `treeMenuItems`/`tabMenuItems` 的 visible 谓词为可导入函数，`npx vitest run src/views/gen/template/menu-visibility.test.ts` | unit | 🟡 待补 | — |
| AC-08 | **干净文件、目录、二进制 → tree 右键不显示回滚项**（隐藏，而非 disabled） | 同 AC-07 谓词测试 | unit | 🟡 待补 | ⚠️ 原稿称"天然不可达无需兜底"，实为依赖此条件，必须验证 |
| AC-09 | **干净 tab → tab 右键不显示回滚项**，且不显示在「关闭系列」之前 | 同 AC-07；DOM 断言需 `// @vitest-environment jsdom` | unit(jsdom) | ⬜ | — |
| AC-10 | 未勾选任何节点 → 批量「回滚」按钮 `disabled === true`（与「删除」完全同口径） | jsdom 组件测试 `tree-panel.test.ts` | unit(jsdom) | ⬜ | — |
| AC-11 | 按钮顺序为 刷新 → 回滚 → 删除；type=`warning`，图标 `RefreshLeft` | jsdom 组件测试 | unit(jsdom) | ⬜ | — |

### C. 回滚执行语义（会话域 → unit，需在 node 环境可跑）

| AC-ID | 验收标准 | 验证命令 | 层级 | 状态 | 覆盖 |
|---|---|---|---|---|---|
| AC-12 | 执行回滚后 `templateContent === originalTemplateContent` 且 `isEdited === false` | `npx vitest run src/views/gen/template/use-template-edit-tabs.test.ts -t "内容回基线并清标记"` | unit | ✅ 已覆盖 | 原函数已存在但**无测试**；2026-11-26 补齐 |
| AC-13 | **回滚后 `originalTemplateContent` 不变**（与 `applyEditRollback` 方向相反的边界，红线） | `... -t "discardEdits 不推进基线"`；并在 `rollbackNode` 调用点以 `// 决策 2：禁止改用 applyEditRollback` 注释锚定 + review | unit + review | ✅ 已覆盖 | **本次最高风险缺口**；注释锚点已在 `use-template-tree-state.ts:113` |
| AC-14 | 目标含当前激活文件时，编辑区内容**可见地**刷新为基线 | 手工：改 1 个字符 → 回滚 → 编辑器显示基线，无测试标记字符 | e2e-manual | ⬜ 暂无法机器验证 | 依赖 CodeMirror v-model，需 Playwright |
| AC-15 | 回滚集合含 N 个文件时，**逐个显式回滚全部 N 个**，不依赖 content-watch（watch 只观察当前激活 tab） | `... -t "批量清标记不依赖 watch"` | unit | ✅ 已覆盖 | 原稿「既有评审结论，勿依赖 watch」，已固化成测试 |
| AC-16 | 编辑 → 保存（基线前进）→ 再编辑 → 回滚 → 内容回到**上次保存后**版本，而非打开时版本 | `... -t "保存推进基线后 discardEdits 回到新基线"` | unit | ✅ 已覆盖 | 原稿边界 6 |
| AC-17 | tab 与树上的红点**同时熄灭**（同一对象引用，mutate 一次） | jsdom 渲染断言；或 unit 断言 `tabList[i] === treeList[j]` 同引用 | unit + unit(jsdom) | 🟡 待补 | 依赖既有 SAME_REF 机制 |

### D. 取消与空集合路径（失败路径，必测）

> **修订（2026-11-26，以已交付代码为准）**：设计原稿要求两态「确定回滚 / 取消」，但 `b3a665e1` 实际交付为三态 `distinguishCancelAndClose: true` + 「是(Y) / 否(N)」，点 X 无反馈。经确认「已提交代码不随意改动、AC 可能滞后」，本节 AC 回写为交付代码的实际语义。设计原稿的两态意图作为历史记录保留在 `2026-11-25-template-content-rollback-design.md`，不再作为验收依据。

| AC-ID | 验收标准 | 验证命令 | 层级 | 状态 | 覆盖 |
|---|---|---|---|---|---|
| AC-18 | 勾选范围内无脏文件 → **不弹确认框**，直接 `ElMessage.info('勾选范围内没有未保存的修改')` | jsdom：断言 `ElMessageBox.confirm` **未被调用**、`ElMessage.info` 被调用 1 次 | unit(jsdom) | ⬜ | 原稿决策 6 |
| AC-19 | 确认框点「否(N)」→ `ElMessage.info('回滚已取消')`，且 `templateContent` / `isEdited` / 基线**三者零变化** | jsdom：断言 `discardEdits` 未被调用 + 状态快照一致 | unit(jsdom) | ⬜ | 原稿交互流程 1（按交付代码修订：三态，取消按钮为「否(N)」） |
| AC-20 | 确认框点 X（关闭）→ **无任何反馈**（不提示、不回滚、状态零变化），与「否(N)」区分（三态 `distinguishCancelAndClose: true`，`action === 'close'` 时 no-op） | jsdom 触发 close | unit(jsdom) | ⬜ | 按 `b3a665e1` 交付代码修订（原稿两态未落地） |
| AC-21 | 确认文案由 `joinFileName` 生成：脏文件 >3 时折叠为「前 3 个...等 N 个模板文件」；含「此操作不可恢复」 | `npx vitest run src/views/gen/template/tree-utils.test.ts -t "joinFileName"`（折叠口径已有用例）+ 补 confirmAndRollback 文案拼装断言 | unit | ✅ 已覆盖（拼装部分 🟡） | 实测修正：原标为待补，实际 `joinFileName` 已有现成用例 |
| AC-22 | 主按钮文案为「是(Y)」、取消按钮为「否(N)」（三态，与关闭确认同口径；**非**「确定回滚 / 取消」） | jsdom 文本断言 | unit(jsdom) | ⬜ | 按 `b3a665e1` 交付代码修订（原稿「确定回滚」未落地） |

### E. 零副作用与域边界（本次变更的核心承诺）

| AC-ID | 验收标准 | 验证命令 | 层级 | 状态 | 覆盖 |
|---|---|---|---|---|---|
| AC-23 | 回滚全程**零写接口调用**：Network 面板无 `PUT .../updateContent`、`PUT .../updateContentList`，无 `detailData` | `npx vitest run src/views/gen/template/rollback-net.test.ts`（MSW 注册写接口 handler 并断言调用次数为 0） | unit(msw) | 🟡 待补 | 原稿「网络层断言」仅写在手工步骤里，实际可自动化——项目已装 MSW |
| AC-24 | 三个域文件的 diff 为空（**不再包含 `tree.vue`**，它已不存在） | 先验 pathspec 存活：`git cat-file -e "HEAD:yanggu-code-generator-frontend/src/views/gen/template/use-template-edit-tabs.ts"`（逐个，exit 0）；再断言：`git diff --exit-code <BASE>..HEAD -- ".../use-template-edit-tabs.ts" ".../use-template-tree-data.ts" ".../use-template-persistence.ts"` | static | ✅ 可跑（**已修正**） | ⚠️ 初版带 `tree.vue` 死路径，靠“空输出”假通过，见 §5.3 |
| AC-25 | **本次新增的**组件层代码不含弹窗调用（弹窗只允许在编排层 `use-template-tree-state.ts`） | `git diff <BASE>..HEAD -- "yanggu-code-generator-frontend/src/views/gen/template/tree-panel.vue" "yanggu-code-generator-frontend/src/views/gen/template/editor-panel.vue" \| Select-String -Pattern "^\+.*(ElMessageBox\|ElMessage\.)"` → **无输出** | static | ✅ 可跑 | C2 的机器化。⚠️ 必须基于 diff：全文 grep 会被删除链路的既有合规弹窗假阳性命中（见 §5.3） |
| AC-26 | `collectDirtyFiles` 为纯函数：不导入响应式/API/弹窗模块，外部查询经参数注入 | `Select-String -Path ".../tree-utils.ts" -Pattern "from '(vue\|pinia\|element-plus\|@/api)"` → **无匹配**（已实测：该模块**当前 0 条 import**，149 行 / 115 行代码，是零依赖纯模块；新增函数不得破坏此性质） | static + unit | ✅ 已验证 | C3 的机器化，且现状已合规 |

### F. 回归承诺（不该被影响的链路）

| AC-ID | 验收标准 | 验证命令 | 层级 | 状态 | 覆盖 |
|---|---|---|---|---|---|
| AC-27 | 未触碰其代码的三条链路行为不变：关 tab「否(N)」丢弃、关抽屉确认链、删除链路 | 全量单测 `npm run test` + 手工抽验 1 轮（原稿回归项 5） | unit + e2e-manual | 🟡 部分 | 手工部分 ⬜ |
| AC-28 | 静态门禁全绿 | `cd yanggu-code-generator-frontend; npm run type-check; npm run lint; npm run test` | static | ✅ 可跑 | C5 |

### 5.1 三类"非 happy path"覆盖确认

| 类别 | 本 spec 对应 AC | 是否覆盖 |
|---|---|---|
| **失败路径**（取消 / 报错 / 拒绝） | AC-18（空集合不弹）、AC-19（「否(N)」零变化）、AC-20（点 X 无反馈）、AC-03（stale key 静默过滤） | ✅ |
| **空集与边界** | AC-04（空数组）、AC-02（去重）、AC-21（>3 折叠）、AC-06/AC-08（类型边界）、AC-10（零勾选） | ✅ |
| **回归承诺** | AC-24（diff 为空）、AC-27（三链路）、AC-28（全量静态） | ✅ |

---

### 5.3 验证命令实测记录（本节是模板规则在本 spec 上的真实应用）

写命令时逐条跑了一遍，结果如下——**其中一条我自己写错了，正好印证了模板 §5.2 的规则**：

| 命令 | 实测结果 | 结论 |
|---|---|---|
| `npx vitest run src/views/gen/template/tree-utils.test.ts -t "collectDirtyFiles"` | ✅ 可用：`Test Files 1 passed`，`32 tests \| 25 skipped`，`7 passed` | `-t` 定向过滤有效，AC-01~06 的命令形态成立 |
| `Select-String -Path tree-panel.vue,editor-panel.vue -Pattern "ElMessageBox\|ElMessage\."` | ❌ **假阳性 5 处命中**（L200/L203/L212/L226/L273，均属于删除/移动链路的**合规既有代码**） | 命令写错了：应当断言「本次**新增**行」而非「全文」。已改为 diff 基版（AC-25） |
| ↑ 修正后的 diff 版命令（**双向验证**） | 负向：对当前工作区执行 → **无输出**（基线合规代码被正确忽略）；正向：在真实引入弹窗的 commit `63110161` 上执行 → **命中 6 行**新增弹窗调用 | ✅ 命令已双向验证过：既能避开存量合规代码，又确实能抓住越界新增。**这才是"命令跑通"与"断言正确"的区别** |
| 命令里的 PowerShell 反引号续行 | ❌ 初版把 `git diff ... \`` 换行接 `\| Select-String`，PowerShell 会将管道符当字符串解析并报错 | 已改写为单行（模板 §5.2 新增此规则） |
| `Select-String -Path tree-utils.ts -Pattern "^import"` | ✅ 无匹配 —— 该模块 **0 条 import** | AC-26 现状已合规，命令可直接作为门禁 |
| `tree-utils.test.ts` 用例清单 | 32 个用例，已覆盖 `findByIdFromTree` / `getFullPathById` / `buildTree` / `buildFileList` / **`joinFileName`** | **修正：AC-21 的折叠口径已有现成用例**，状态由 `🟡 待补` 上调为 `✅ 已覆盖`（仅需补 confirmAndRollback 的文案拼装断言） |
| AC-24 里的 `tree.vue` pathspec | ❌ **死路径**。`git cat-file -e "HEAD:.../tree.vue"` 报 `does not exist in 'HEAD'`；v1 已于 `4a27750b` 删除 | 命令仍“通过”是因为 **`git diff` 对匹配不到文件的 pathspec 静默返回空且 exit 0**（已实测：臆造文件名同样空输出 + exit 0，加 `--exit-code` 也一样）。AC-24 已去掉死路径并改用退出码断言（P-022） |
| **AC-24 修正后的双向验证** | 存活检查：3 个域文件均 `exit=0`。零改动断言 `a6e6d586..b3a665e1` → `exit=0`（应通过）。**正向对照**：范围放宽到含域拆分 commit（`b67bb8a1~1..b3a665e1`）→ `exit=1` | ✅ 双向均符合预期。正向对照这一步是初版**完全没有的**——没有它，就无法区分“真的零改动”与“根本没监控到文件” |
| **AC-25 修正后的双向验证** | 基线范围 `a6e6d586..b3a665e1` 无输出；正向 commit `63110161` → 实测 **6 行**命中 | ✅ 复测通过，与初版结论一致 |
| §8 门禁块与 §5 正文不一致 | ❌ §8 里的 AC-25 **仍是不合格的全文 grep 版**（上表已判定为假阳性），而 AC-25 正文早已改成 diff 版 | 门禁脚本没跟着修正文改 = 门禁本身错。已把 §8 两条都同步为修正后的版本 |

**本次改造的最大收益就在这里**：如果不拆成 AC 表并逐条跑命令，`AC-25` 那条会一直显示为"失败"并被忽略，而 `AC-21` 会被低估（重复写已有测试）；而 AC-24 会作为一个**仍显示绿色的死门禁**继续存活——它比 AC-25 更危险，因为假阳性能被人发现，假阴性没人怀疑。散文式"验证方案"两个问题都暴露不出来。

---

## 6. 影响面与风险

### 6.1 改动面清单（diff 预算）

| 文件 | 预期改动 | 行数级别 |
|---|---|---|
| `tree-utils.ts` | **仅新增** `collectDirtyFiles` | +20 |
| `use-template-tree-state.ts` | 新增 `rollbackNode` / `rollbackCheckedNodes` / `confirmAndRollback` + facade 导出 | +35 |
| `tree-panel.vue` | 1 个按钮 + 1 个菜单项 + 解构追加 | +15 |
| `editor-panel.vue` | 1 个菜单项 + 解构追加 | +10 |
| `tree-utils.test.ts` | 新增 `collectDirtyFiles` 用例（AC-01~06） | +60 |
| `use-template-edit-tabs.test.ts` | **新增文件**：AC-12/13/15/16 | +50 |
| `rollback-net.test.ts` | **新增文件**：AC-23（MSW） | +40 |
| `menu-visibility.test.ts` | **新增文件**：AC-07/08 | +30 |

**diff 越界即回退**（超出上表的文件视为 AI 越权）：
```powershell
git diff --stat <BASE>..HEAD
```

### 6.2 数据库 / 配置 / 依赖变更

| 项 | 内容 |
|---|---|
| DDL | **无** |
| 配置项 | **无** |
| 新增依赖 | **无**（`@vue/test-utils` 若做 jsdom 组件测试则需新增，属可选切片） |

### 6.3 风险与降级

| 风险 | 概率 | 影响 | 应对 |
|---|---|---|---|
| 误用 `applyEditRollback` 替代 `discardEdits` | 中（两个名字相似且都带 "rollback"） | **高**：未保存内容被静默固化为新基线，反向事故 | AC-13 单测 + 调用点注释锚定 + review 必查项 |
| 依赖 content-watch 清标记，批量时漏清除非激活节点 | 中 | 中：红点残留但内容已回滚，状态不一致 | AC-15 显式批量用例 |
| `visible` 条件写错导致入口不可达或误可达 | 中 | 中：功能"消失"或对干净文件弹确认 | AC-07/08/09，禁止以"天然不可达"跳过验证 |
| 用户对"回滚"理解为版本回退 | 中 | 中：投诉 | 文案明写「上次保存（或打开时）的内容，此操作不可恢复」+ AC-16 + §4 永久排除版本历史 |
| 图标库无 rollback svg | 低 | 低 | 退化 `RefreshLeft`，不新造文件（§3.2） |

---

## 7. 回滚方案

| 步骤 | 操作 | 验证回滚成功 |
|---|---|---|
| 1 | `git revert b3a665e1`（本功能交付 commit）或 `git checkout <BASE> -- <§6.1 前 4 个文件>` | `git diff --stat <BASE>..HEAD` 对 `tree-panel.vue`/`editor-panel.vue`/`use-template-tree-state.ts` 无输出 |
| 2 | 删除新增文件：`collectDirtyFiles` 及 3 个新增测试文件 | `npx vitest run` 全绿且用例数回到基线值 |
| 3 | 无需数据库/缓存清理 | 影响库为空（§6.2） |

**兼容性**：纯前端增量，无接口与 schema 变更 → 回滚零数据风险。本功能只动 v2 组件集与纯函数模块，三个域文件零改动（AC-24）。

---

## 8. Verification Hooks 汇总（门禁映射）

**静态检查**（每次交付必过）
```powershell
cd yanggu-code-generator-frontend; npm run type-check
cd yanggu-code-generator-frontend; npm run lint
```

**单元测试**
```powershell
cd yanggu-code-generator-frontend; npm run test
cd yanggu-code-generator-frontend; npx vitest run src/views/gen/template/    # 本模块定向
```

**边界/越界检测**（本 spec 独有，机器化最强）
```powershell
# AC-24 零改动域承诺。先验 pathspec 存活（否则死路径会让断言真空通过，P-022）
git cat-file -e "HEAD:yanggu-code-generator-frontend/src/views/gen/template/use-template-edit-tabs.ts"; git cat-file -e "HEAD:yanggu-code-generator-frontend/src/views/gen/template/use-template-tree-data.ts"; git cat-file -e "HEAD:yanggu-code-generator-frontend/src/views/gen/template/use-template-persistence.ts"
# 再断言零改动（--exit-code：0=无改动、1=有改动，不依赖人眼看空输出）
git diff --exit-code <BASE>..HEAD -- "yanggu-code-generator-frontend/src/views/gen/template/use-template-edit-tabs.ts" "yanggu-code-generator-frontend/src/views/gen/template/use-template-tree-data.ts" "yanggu-code-generator-frontend/src/views/gen/template/use-template-persistence.ts"; Write-Output "exit=$LASTEXITCODE"

# AC-25 弹窗只在编排层（已修正为 diff 基版；全文 grep 会假阳性命中删除链路的合规存量代码）
git diff <BASE>..HEAD -- "yanggu-code-generator-frontend/src/views/gen/template/tree-panel.vue" "yanggu-code-generator-frontend/src/views/gen/template/editor-panel.vue" | Select-String -Pattern "^\+.*(ElMessageBox|ElMessage\.)"
```

**手工验证**（dev 前端 + 后端 Debug 实例，模板组 80 或指定测试组）

| 步骤 | 操作 | 预期现象 | 阻塞 | 已发现缺陷 |
|---|---|---|---|---|
| 1 | 编辑区输入标记文本造脏 → 三个入口分别回滚 | 红点熄灭（el-badge 需 isVisible 过滤）、编辑区回基线、内容无标记文本 | ☐ | |
| 2 | 打开 Network 面板执行 AC-14 | 零 PUT 请求 | ☐ | |
| 3 | 勾选含干净文件 → 批量回滚 | 确认框只列脏文件 | ☐ | |
| 4 | 全干净范围点批量回滚 | 走 info，不弹确认（AC-18） | ☐ | |
| 5 | 弹窗点「否(N)」→ 再点 X 试一次 | 「否(N)」提示回滚已取消、状态零变化（AC-19）；X 无任何反馈（AC-20） | ☐ | |
| 6 | 非激活 tab 回滚后切过去 | 显示基线 | ☐ | |
| 7 | 回滚当前激活 tab | 编辑区即时刷新（AC-14） | ☐ | |
| 8 | 编辑→保存→再编辑→回滚 | 回到"保存后"版本（AC-16） | ☐ | |
| 9 | 回归：关 tab「否(N)」/ 关抽屉 / 删除链路 | 行为不变（AC-27） | ☐ | |

**待自动化清单（流程债务台账，必须提交）**

| AC-ID | 当前层级 | 目标层级 | 补齐方式 | 优先级 |
|---|---|---|---|---|
| AC-13 | ✅ unit | ✅ unit | `discardEdits 不推进基线` 用例（2026-11-26 已补） | **P0**（红线，事故代价最高） |
| AC-15 | ✅ unit | ✅ unit | 批量清标记不依赖 watch（2026-11-26 已补） | **P0** |
| AC-23 | unit 待补 | ✅ unit(msw) | 用已有 MSW 断言零写调用 | **P1**（把原稿"手工网络断言"变成自动） |
| AC-01~06 | ✅ unit | ✅ unit | `collectDirtyFiles` 7 个用例已覆盖（随 `b3a665e1` 交付） | P1 |
| AC-12/16 | ✅ unit | ✅ unit | 会话域基线语义（2026-11-26 已补） | P1 |
| AC-17 | unit 待补 | ✅ unit | 红点同引用断言 | P1 |
| AC-07/08 | unit 待补 | ✅ unit | 抽出 `visible` 谓词为可导入函数 | P1（**2026-11-26 取消本轮**：需改已提交组件代码，维持手工验证） |
| AC-14 | e2e-manual | e2e-auto | 引入 Playwright（独立决策，覆盖全项目 Top5 流程） | P2 |
| AC-09/10/11/18/19/20/22 | ⬜ | unit(jsdom) | `// @vitest-environment jsdom` + `@vue/test-utils` | P2（AC-20/22 按交付代码的三态语义编写） |
| AC-27（手工抽验部分） | e2e-manual | e2e-auto | Playwright 回归脚本 | P2 |

---

## 9. 交付检查清单

- [ ] §5 共 28 条 AC：5 条 ✅ / 12 条 🟡 / 11 条 ⬜。⬜ 那 11 条须确认已在 §8 待自动化清单登记并给出优先级（模板规则允许"已登记债务"，不允许"沉默缺口"）
- [ ] **AC-13（禁止误用 `applyEditRollback`）与 AC-15（不依赖 watch）两条 P0 用例已补齐并跑通**——它们分别是反向事故红线与静默不一致风险
- [ ] 已确定决策 3 的补充选型（方案 Ａ 手写 walk / 方案 Ｂ 复用 `buildFileList`），并据此调整 AC-01~06 的用例数量
- [ ] `git diff --stat <BASE>..HEAD` 与 §6.1 预期改动面完全一致，无越界文件
- [ ] AC-24 / AC-25 / AC-26 三条边界命令输出符合预期（零改动 / 无新增弹窗匹配 / import 面干净）
- [ ] §8 手工 9 步全部执行且「已发现缺陷」列为空
- [ ] §7 回滚方案已实际演练一次
- [ ] SDD ledger 建齐：`.superpowers/sdd/2026-11-25-template-content-rollback/progress.md`（**原 spec 缺此项**）
- [ ] plan 建齐：`docs/superpowers/plans/2026-11-25-template-content-rollback.md`（**原 spec 缺此项**）
- [ ] v1 已删除事实已回写 `AGENTS.md` §3.4 / §6 与 `docs/pitfalls.md`（P-013 已改写），后续 spec 不得再引用 v1 对照约束

---

## 10. 决策记录（ADR-lite）

> 原稿 6 个决策全部保留（这部分是原稿最强的地方），仅补充「重新评估触发条件」。

### 决策 1：基线取内存 `originalTemplateContent`，不取数据库当前版本
- **选择**：回滚目标 = 内存基线，非数据库版本。
- **理由**：语义诚实——按钮叫「回滚」，实现是"丢弃未保存修改"；不冒充版本管理。
- **否决方案**：`detailData` 拉库内最新覆盖（等价 `refreshAfterTest`）。
- **否决理由**：需逐文件发请求、结果依赖会话期间是否有人改库、行为不确定，且与用户对"内存基线"的预期不符。
- **重新评估触发条件**：引入模板历史版本表（DDL 变更）时。**注意这会同时推翻 AC-23 的"零接口调用"承诺。**

### 决策 2：复用 `discardEdits`，不新增会话域动作，禁止与 `applyEditRollback` 混用
- **选择**：回滚 = `discardEdits`（内容回基线、基线不动）；保存成功 = `applyEditRollback`（基线前进、清红点）。
- **理由**：两者方向相反，回滚误用后者等于把未保存内容确认为新基线，是反向事故。
- **配套要求**：必须显式逐个回滚全部目标节点（content-watch 只观察当前激活 tab）。
- **重新评估触发条件**：出现第三种"基线"语义（如"回滚到最近一次测试运行结果"）时。
- **→ 落到 AC-12 / AC-13 / AC-15**

### 决策 3：批量范围 = 勾选节点 + 目录后代文件，再过滤 `isEdited`
- **选择**：与工具栏「删除」的 `getCheckedKeys` 口径完全一致。
- **理由**：用户不需要学第二套勾选语义；复用 `handleClose` 的递归收集写法。
- **否决方案**：自定义勾选范围（如只收当前层级）。
- **否决理由**：与删除链路不一致，造成同页两套语义。
- **重新评估触发条件**：勾选模型改为 el-tree 的 `check-strictly` 或引入半选传递规则时。
- **→ 落到 AC-01 / AC-02 / AC-05 / AC-06**

- **⚠️ 本次改造补充（实现前必须先敲定的选项）**：原稿给的是手写 `walk` 递归，但**同模块已有 `buildFileList`**（其现有用例描述："收集所有非目录节点并拍平，深度优先顺序"）。两种实现：

  | 方案 | 写法 | 评价 |
  |---|---|---|
  | Ａ（原稿） | 自带 `walk`，按 `templateType` 分流 | 与 `handleClose` 写法一致；但新增一段递归逻辑 + 新增测试面 |
  | Ｂ（建议） | 对每个勾选节点取子树 → `buildFileList(子树)` → `.filter(n => n.isEdited)` → 按 `id` 去重 | **复用已测能力**，新代码量更小；语义等价（都是"非目录 + 拍平"） |

  **选型影响 AC 的方式**：选 Ａ 则 AC-01~06 承载新逻辑；选 Ｂ 则目录拍平行为已由 `buildFileList` 的 2 个现成用例保护，新用例只需聚焦"去重 + 脏态过滤 + stale key"三条。**这恰好是模板 §2「复用盘点」要拷问的东西：原稿列了跳模块的复用表，却漏了同模块内的函数。**

### 决策 4：入口布局 = 工具栏按钮 + 两处右键菜单项，脏态控制显隐/禁用
- **选择**：批量用按钮（未勾选时 disabled）；单个用菜单项（不满足条件时**隐藏**而非 disabled）。
- **理由**：右键点击不改变勾选集合，菜单里操作"勾选范围"与直觉相左且批量入口不可发现；而树上无可回滚对象时该动作"不存在"，与 tab 菜单「关闭未改」按 `some` 条件隐藏同理。
- **重新评估触发条件**：树右键改为"右键即勾选该节点"时，可考虑合并入口。
- **→ 落到 AC-07 ~ AC-11**（注意：原稿"天然不可达无需兜底"的说法已被本次改造判定为缺陷，见改造说明）

### 决策 5：回滚只覆盖内容，不覆盖结构操作
- **选择**：新增/删除/移动/重命名不可回滚。
- **理由**：这些即时落库，无事务可退。确认文案必须写明「此操作不可恢复」。
- **重新评估触发条件**：为抽屉引入操作事务（会话内暂存、统一提交）时——那是架构级变更，不是本功能的扩展。
- **→ 落到 §4 Out of scope**

### 决策 6：空集合与全干净的处理
- **选择**：范围内无脏文件 → 不弹确认，直接 `ElMessage.info('勾选范围内没有未保存的修改')`。
- **理由**：弹了也只能是空文件名列表。
- **附带判定（本次改造新增）**：单个入口"天然不可达、无需兜底"的结论**不成立**，其可达性完全依赖 `visible` 条件，须以 AC-07/AC-08 验证；`rollbackNode` 的双保险守卫因此是**必要**而非冗余。
- **→ 落到 AC-18 / AC-04**

---

## 11. 偏差与后续登记

| 类型 | 内容 | 处理方式 |
|---|---|---|
| **已过期约束** | v1 `tree.vue` 已于 `4a27750b` 删除；本 spec 初稿（及原稿、`2026-11-24` 系列设计文档）中的“v1 冻结 / v1与v2 双跑差分 / 预期行为差异”约束**均已失效** | 历史文档**不回改**（它们是变更快照，改了反而破坏可追溯）；但新 spec 不得再引用；已在 `AGENTS.md` §3.4 与 pitfalls P-013 标注过期 |
| **门禁假通过** | AC-24 初版靠“diff 输出为空”断言，其中 `tree.vue` 路径已不存在→ git 静默返回空且 exit 0 | 改用 `--exit-code` 拿退出码 + `git cat-file -e` 预验 pathspec 存活（P-022） |
| 已知未修缺陷 | 删除链路 K 系缺陷 | 登记 ID，不纳入本次 AC（§4） |
| 流程债务 | 11 条 AC 暂无法机器验证（依赖 jsdom / Playwright）；12 条零成本可下沉到单测 | 见 §8 待自动化清单，**AC-13 / AC-15 两条 P0 随本次交付补齐** |
| 资产归位 | `t2-suite-*.js`、`vitest-out.txt` 位于 `docs/superpowers/specs/`，AI 检索会读到噪音 | 建议迁至 `yanggu-code-generator-frontend/tests/`，使其成为可自动执行的资产 |
| spec 归位 | 原 design 文档与本 spec 并存 | 本 spec 为验收真相源，原稿保留为设计推导支撑材料；后续新 spec 直接用模板，不再另出 design 文档 |
