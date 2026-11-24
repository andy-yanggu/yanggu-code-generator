# 模板树组件（tree.vue）拆分设计

## 概述

`views/gen/template/tree.vue`（1508 行）单文件承载了 9 块职责：抽屉容器与初始化复位、树渲染/搜索/展开态、勾选删除与拖拽移动、树右键菜单、Tab 管理（含 Sortable 与 6 种批量关闭）、编辑区（CodeMirror/二进制预览/保存/测试）、变量面板（目录加载 + 宽度 px 体系）、伪全屏、纯函数（已抽出至 `tree-utils.ts`）。本设计将其拆为「容器 + 2 个子组件 + 3 个 composable」，并把与 `test.vue` 逐字重复的变量面板宽度体系收编为共享 composable。

拆分遵循两条既有事实约束：

1. **对象引用同一性**：`tabList` 的元素来自 `buildFileList(treeList)`，与 `treeList` 节点是同一批对象。编辑内容、`isEdited`、保存确认全靠共享引用生效，因此**核心状态必须由容器持有的 composable 管理**，子组件只接 props / 发事件 / 窄 surface expose，不得持有副本。
2. **跨面板回调链**：变量面板插入文本 → 编辑器的 `codeMirrorRef.insertAtCursor`；测试按钮 → `templateTestRef.init(...)`。这两条链穿过组件边界时统一走「容器转发函数」，不引入 provide/inject。

本文件是**设计文档（spec）**；逐任务的实现计划由 writing-plans 另出，并引用本文件作为论证依据。

**实施策略（2026-11-24 增补）**：不直接修改 tree.vue，而是新建 v2 版本并行存在、功能对照（见决策 7）：旧版全程冻结作为行为基准，v2 验收通过后才执行切流收尾（消费端指向 v2、删旧版、test.vue 复用共享件）。

## 现状盘点

### 职责与行数分布

| # | 职责 | 脚本行数（约） | 模板行数（约） | 主要耦合 |
|---|------|--------------|--------------|---------|
| 1 | 抽屉容器 + `init()` 复位 + Loading + `handleClose` 关闭确认 | 90 | 10 | 全部状态 |
| 2 | 树渲染/搜索过滤/展开态持久化/节点定位滚动 | 80 | 55 | `treeRef`（DOM）、`treeList` |
| 3 | 勾选删除 + 拖拽移动（allow-drag/drop/drop 后落库） | 90 | — | `treeRef`、`updateTemplate` |
| 4 | 树右键菜单（状态、`calculateTemplateMenu` 定位、显隐、4 个入口） | 100 | 20 | `contextMenu`、`formRef` |
| 5 | Tab 管理（push/移除/Sortable/6 种批量关闭 + `handleCloseTabs` 保存确认） | 260 | 55 | `tabList`、`activeItemId`、`treeList` 共享引用 |
| 6 | 编辑区（CodeMirror/二进制预览/保存/测试/编辑态侦测 watch/路径显示） | 110 | 60 | `activeTabItem`、`codeMirrorRef`、`templateTestRef` |
| 7 | 变量面板（catalog 加载、宽度 px 体系、竖柄、插入转发） | 110 | 45 | `editorSplitterRef` 实测宽度 |
| 8 | 伪全屏（CSS 铺满 + 原生全屏状态联动） | 15 | 5 | `treeFullScreen` |
| 9 | 纯函数 | — | — | `tree-utils.ts`（已抽出） |

### 与 test.vue 的重复

`test.vue` L269-312 与 tree.vue L466-508 是**逐字重复**的变量面板宽度体系（`editorFloor` / `clampedVariableSize` / `editorPanelSize` / 首次展开 28%），当时注释明确「第三处重复出现时再合并」——本次拆分即合并时机。两处收起态竖柄（`.variable-edge` 模板 + 样式）也结构一致。

### 已知缺陷（拆分时一并根治，不扩大范围）

1. 两段顶层 `document.addEventListener('mousedown')`（L1015、L1093）挂在组件 setup 全局，实例销毁不摘除 → 监听器泄漏。
2. 树菜单与 tab 菜单共用类名 `.context-menu`，「点击外部关闭」用 `document.querySelector('.context-menu')` 判定 → 两菜单同开时串扰。

以上两点已由既有 spec《通用右键菜单（ContextMenu）抽象设计》（`docs/superpowers/specs/2026-11-16-context-menu-abstraction-design.md`，下称 **11-16 spec**）定稿方案（flip+clamp 定位、按实例管理监听、Teleport 去串扰），但**尚未实施**（`src/components/context-menu/` 不存在）。本设计将其作为拆分的前置任务复用，不重复设计。

### 对照期已知共有缺陷（T2 运行时实测发现，v1/v2 行为逐条一致，对照期一律不修）

登记背景：F09 拖拽、F08 勾选删除、F04 树搜索、F22-F26 变量面板、F06/F30 红点专项、F02/F03/F07/F14/F16/F19 状态机（Suite S）、F05/F17/F18/F20/F21/F25 布局交互（Suite N）七轮双跑中撞到。判定原则同决策 7 —— **两版表现一致即不属 v2 回归**，登记在此，修复排到 T4 切流归一之后单开批次，避免对照期改动基准。

| 编号 | 现象（实测证据） | 根因锚点 | 修复方向 |
|------|------------------|---------|---------|
| K1 | 删除「已改未保存」的节点时，**行已删完才弹**「XXX模板文件已修改未保存，是否保存后再关闭？」；选 `是(Y)` 对 `is_delete=1` 的行发 `PUT /updateContentBatch`，逻辑删除过滤使更新命中 0 行、接口回 `code:200`，前端却 toast「保存成功」（库里内容实测未变） | `deleteCheckedNode` 的 `forEach handleTabRemove`（tree.vue L762-793 / tree-panel.vue L190-221）复用 `handleCloseTabs` 未保存确认 | 删除链路改为「先静默摘 tab、跳过未保存确认，再落库删除」；或 `handleCloseTabs` 增 `skipDirtyConfirm` 入参 |
| K2 | 上述确认框点右上角 X（`distinguishCancelAndClose` 下 `action='close'`）不执行 `closeTab`：节点已离开树与库，tab 头却残留；`activeTabItem` 由 `findByIdFromTree(激活id, treeList)` 反查失败回落 `{templateType:-1}` → 编辑区两个 `v-if` 分支都不命中而**整块空白**、保存按钮置灰、tab 红点仍亮（幽灵 tab） | `use-template-tree-state.ts` L28-35、L236-243；`editor-panel.vue` L68-82 | 反查失败时同步摘除该 tab；或 X 分支也走 `closeTab`（丢弃语义与 `否(N)` 统一并在文案言明） |
| K3 | 勾选多个「已改未保存」文件一起删除，`forEach` 同步发多次 `ElMessageBox.confirm`，EP **不排队** → 实测同屏 2 个 `.el-message-box`（2 个可见 overlay）直接叠加，只能按 DOM 末尾逐个作答 | `deleteCheckedNode` 的 `forEach` 逐个调 `handleTabRemove` | 一次调用 `handleCloseTabs` 传入全部脏 tab（`joinFileName` 本就支持多文件名拼接），单次确认批量处理 |
| K4 | 拖拽到顶级根时 `newParentId=0`，后端 `updateParent` 抛「目标父节点不存在」（HTTP 200 + `code:500`），但 EP 在 emit `node-drop` **之前**已就地改动本地树数据且 `updateParent(...).then()` 无 `.catch()`/回滚 → 界面把该节点挂成 `.el-tree` 顶层、库里没变，只有点「刷新」或重开抽屉才恢复，期间抛 1 条未捕获 console error | EP `useDragNode.mjs` L126-151；`handleNodeDrop` L796-863 / L250-285 | `node-drop` 的接口调用补 `.catch()`，失败时按拖拽前快照回滚本地节点（或改为成功后才落本地） |
| K5 | **搜索过滤不会取消勾选**：勾上 `BaseUserVO.java(2142)` 后搜 `PageVO` 把它过滤隐藏，节点仍保持 checked、「删除」按钮仍 `disabled=false`，`getCheckedKeys()` 里带着**看不见的 id** → 用户按可见列表判断删除范围，实际提交集更大（与 K1/K2 串在同一条删除链路上叠加） | `filterNode` 只控可见性（tree.vue L744-750 / tree-panel.vue L153-159），EP `getCheckedKeys()` 走数据层、`show-checkbox` 无「只提交可见」口径 | 删除前用 `getCheckedNodes()` 把**将被删的文件名全列**写进确认文案；或过滤变化时把勾选收敛到可见节点（代价：丢失用户不可见部分的勾选，需在文案言明） |
| K6 | 关闭确认点 `否(N)`（丢弃）后 **`isEdited` 不清、`templateContent` 不回滚**：Suite S 实测 tab 已关、激活回落，但节点 `edited=Y` 且树红点残留（`F14_noBranch=.../dot=on/edited=Y`、尾部 `treeDots=2`）→ 该文件重新点开即带着脏（保存按钮直接可点），后续删除/关闭确认会把它算进 `editTabs`，弹出一个「内容其实已丢弃」的确认框。重开抽屉会被 `init` 重拉数据掩盖（S4 `dots=0`），所以只在**本次会话内**可见。「再点保存会把已丢弃内容写回库」一句为源码推导，实测取证到的是脏标记与红点残留本身 | `handleCloseTabs` 的 `action === 'cancel'` 分支（v1 `tree.vue` L1315-1322 / v2 `use-template-tree-state.ts` L246-253，已逐字核对为同构）只调 `closeTab()`；而 `是(Y)` 分支（L1305-1308 / L236-239）做了 `tab.isEdited = false` + `originalTemplateContent = templateContent`。又因 tab 与树节点是同一对象引用（F06 `SAME_REF` 已证），回滚与清除必须同时落到树侧 | 丢弃分支按 `originalTemplateContent` 回滚 `templateContent` 并清 `isEdited`；**注意不能简单直接赋值**——编辑侦测 watch 监听内容变化，回滚会被再侦测成脏，需与 watch 早退条件对齐（先置 `isEdited=false` 再写内容，或回滚期间屏蔽 watch）；同时把「丢弃」语义写进文案，与 X 分支（K2）一并统一 |
| K7 | **tab 拖拽排序整体错位一格**：`el-tabs` 为 `type="card"`，EP **不渲染** `.el-tabs__active-bar`（实测 `hasActiveBar=false`），而 Sortable `onEnd` 仍按「nav 第一个子节点是激活条」的假设做 `evt.oldIndex!-1 / evt.newIndex!-1` → 索引全体前移一位。实测把 2891 拖到 2136 之前得到 `2142,2136,2865,2891`（**真正被移动的是被拖项的前一项** 2142），且 `activeIsMoved=N`、`stable=N`（再拖一次会继续错）；按 `-1` 规则预测的序与实测逐位吻合 | v1 `tree.vue` L359-389 / v2 `editor-panel.vue` L180-192（已逐字核对为同构） | 按 `tabs type` 是否渲染 active-bar 决定是否 `-1`；或改为用 nav 子项 `id` 直接与 `itemIds` 对齐求真实索引，并在排序后同步激活项 |

补充：`isDescendant`、「不能将节点拖到自己下面」、「文件节点不能作为父级」三道后端校验经 UI **不可达**（EP 自身在 dragover 阶段已按 `contains` 拦截），属死代码而非缺陷；`deleteCheckedNode` 里「未勾选告警」分支同理不可达（两版删除按钮均绑 `:disabled="checkedNodeListLength === 0"`）。

变量面板双跑（F22-F26）新增两条同类观察，**均不是 v2 回归**（两版行为逐字一致），只登记不修，归入 T4 收尾清单：

| 观察 | 事实 | 归属 |
|------|------|------|
| `panel-body.vue` 的 `.insert-button` 守卫与样式无对应 DOM | L171 `closest('.insert-button')` 恒为 false，L287/L292 两条 CSS 无宿主（实测 `D2_hasInsertButtonEl=false`）——插入按钮已被行级双击方案取代，守卫与样式残留 | 共用件既有，v1/v2 同件；T4 删除守卫与死样式，或把按钮恢复渲染（二选一，需产品口径） |
| `disabled=true`（非模板文件不开放插入）在模板树抽屉内不可达 | 组 80 无 `template_type=2` 行，且点目录不改激活项（`handleTreeNodeClick` 仅 `templateType!=0` 激活）→ `canInsertVariable` 始终 true | 共用件同一份早退逻辑，两版等价；要跑该分支只能到测试页（属 F27/测试页口径） |

## 设计目标与非目标

**目标**

- tree.vue 收敛为「容器 + 组装」，目标 ≤ 350 行；每个拆分单元可用一句话说明职责。
- 核心树/Tab 状态收编为单一 composable，边界处以显式 props/emits/expose 契约表达。
- 变量面板宽度体系与竖柄抽为共享件，tree.vue 与 test.vue 双处消费，消除逐字重复。
- 消除监听器泄漏与菜单串扰（经 11-16 spec 的 ContextMenu 组件落地）。
- 对外契约零变化：`template-group/index.vue` 消费的 props + `defineExpose({ init })` 不动，本次不改该文件。

**非目标**

- 不改任何接口语义与外部行为（菜单项集合、显隐条件、保存确认文案、面板尺寸规格全部不变）。
- 对照期不动 `tree.vue`（全程冻结作基准）、不动 `test.vue` / `form.vue` 的内部结构（test.vue 的共享件替换延至收尾任务，且仅限宽度体系与竖柄）。
- 不做导航栏标签菜单 #3 接入（属 11-16 spec 第三步）。
- 不引入新依赖。

## 设计决策

### 决策 1：状态留容器、UI 下子组件（vs 状态随 UI 下沉）

`treeList/tabList/activeItemId/expandedKeys` 及其全部变更动作收进 `use-template-tree-state.ts`，由容器 tree-v2.vue 调用并把返回的状态对象下传。**理由**：约束 1 的对象引用同一性是全组件最核心的正确性来源（tab 红点、编辑侦测 watch、关闭确认遍历的是 treeList 节点），一旦随 UI 拆散成两份持有，`isEdited` 同步会退化成事件乒乓。子组件得到的都是**同一 reactive 对象的引用**，不是副本。

被否选：把状态放进 tree-panel、editor-panel 各自持有、经 emits 同步——改动面与回归风险显著更高，收益为零。

### 决策 2：按「左右分栏」切 UI，而非按功能碎片化切

模板按物理边界切成两半：

- `tree-panel.vue`：搜索框 + 刷新/删除按钮 + el-tree + 树右键菜单 + template-form 弹窗。DOM 耦合（`treeRef` 定位滚动、勾选 keys、Sortable 无关）全部内聚于此。
- `editor-panel.vue`：头部按钮组 + el-tabs + tab 右键菜单 + CodeMirror/二进制预览 + 编辑侦测 watch + 保存/测试（`template-test` 及其 `@refresh-data-list` 回刷也在 panel 内闭环）。`codeMirrorRef` 内部持有，仅 expose `insertAtCursor` 窄 surface。

容器保留：抽屉、双 splitter 布局与宽度计算、变量面板、全屏、init/handleClose。变量面板（variable-panel + 竖柄 + `useVariableCatalog`）仍由容器直接渲染，宽度体系也在容器——这与 test.vue 的同款分层一致。

**理由**：两块 UI 的 DOM ref 依赖几乎不交叉（唯一交叉是插入转发，走容器函数）；再往下切（如 tabs 独立组件）会把 `activeItemId` 的 v-model 链拉长，违背 YAGNI。

### 决策 3：跨边界回调统一走容器转发函数

- 变量插入：容器持有 `editorPanelRef`，向 tree-panel（内含 variable-panel 消费点在其右侧，实际为容器直接渲染竖柄与面板）传 `insert = (text, caret) => editorPanelRef.value?.insertAtCursor(text, caret) ?? false`。语义与现状 `insertVariable` 完全一致（返回 false 供面板提示「编辑器未就绪」）。
- 测试：`editor-panel` 内部自持 `templateTestRef` 与 `<template-test>`，「测试」按钮点击直接调用本组件内的 `testRender()`，无需上抛——它与 tab、编辑器是同一职责簇。容器不再感知 test.vue。
- 表单刷新：`template-form` 的 `@refresh-data-list` 由 tree-panel 消化后 `emit('refresh-data-list', tree)`，容器接到后执行现状 `initAfterHandler` 的剩余逻辑（激活项、initData）。

### 决策 4：右键菜单接入 11-16 spec 的 ContextMenu，作为前置任务

树菜单与 tab 菜单不抽 `use-*-context-menu` composable，而是按 11-16 spec 直接接入 `components/context-menu/index.vue`（items schema 渲染 + 实例级监听 + Teleport）。**理由**：该 spec 已定稿且明确覆盖 tree.vue 的 #1/#2 改造对照表。v2 策略（决策 7）下该组件作为独立共享件先行新建（T0），只被 v2 消费，v1 菜单代码因冻结不动。任务顺序见「实施计划」。

### 决策 5：宽度体系抽 `use-variable-panel-size.ts`，竖柄抽 `variable-edge.vue`

- composable 契约（对齐 variable-panel 既有「契约与实现同目录」惯例）：

```typescript
interface UseVariablePanelSizeOptions {
    splitterRef: Ref<HTMLElement | undefined> // el-splitter 实例 ref
    visible: Ref<boolean>                     // 面板显隐（toRef 宿主状态）
    floor?: number                            // 编辑区下限，默认 320
    panelMin?: number                         // 面板下界，默认 240
    panelMax?: number                         // 面板上界，默认 560
    initRatio?: number                        // 首次展开取容器比例，默认 0.28
}
// 返回：panelSize（用户拖拽宽度 ref）、clampedSize、editorFloor、editorPanelSize、
//       onResized（供宿主 @update:size 回写）、resetInit()（init 复位用）
```

- `countVariables` 的口径差异（test.vue 传 `true`）留在各宿主，不入 composable。
- 竖柄抽成 `variable-panel/variable-edge.vue`（props：`count: number | null`；emits：`toggle`），样式现状直搬。

### 决策 6：编辑侦测 watch 归 editor-panel，数据动作归 state composable

`watch([id, content, type])` 那段时间耦合的 isEdited 侦测（L654-680）依赖 DOM 内容变化节奏，留在 `editor-panel.vue`；`tabPush / handleTabRemove / close*Tabs / handleCloseTabs / save* / deleteCheckedNode / updateParent(拖拽)` 等纯数据+API 动作进 `use-template-tree-state.ts`。`checkedNodeListLength` 依赖 `treeRef` DOM，改由 tree-panel 本地 computed，删除按钮显隐在 tree-panel 内闭环。

### 决策 7：不改 tree.vue，新建 v2 并行对照（本策略覆盖其余决策的实施形态）

旧版 `tree.vue` 全程冻结（一行不改，作为行为基准与回退保障），拆分产物全部以新文件落地：

- **切换器**：`template-group/index.vue` 顶部一行 import 指向切换（`// v1 | v2` 注释标记），其余零改动——这是对照期唯一被触碰的存量文件；切换后两套组件的 props/`init` 契约完全一致，调用链路不受影响；
- **对照方法**：同一会话内先跑 v1 记录行为、再切 v2 逐项复现（以 F01-F30 清单为对照表），两套结果逐条勾平；已知缺陷（监听器泄漏/菜单串扰）预期差异除外，其余行为差异均视为 v2 缺陷；
- **共享新建件**：ContextMenu（T0）、`use-variable-panel-size`、`variable-edge` 新版直接消费；对照期 test.vue 不动，其内重复逻辑待收尾任务（T4）再换共享件，避免对照期间引入无关变量；
- **零冲突约束**：新建件样式不得用 `.context-menu` 裸类名、`.tree-scroll-wrapper` 等 v1 scoped 类名（v1/v2 可能同页渲染，全局样式会互相污染），ContextMenu 按 11-16 spec 用独立命名；
- **收尾即归一**：v2 全量验收后，切 import → 删 v1 tree.vue → test.vue 换共享件 → 全量回归一轮，目录回到单版本结构。

被否选：①直接改 tree.vue（用户要求，且丢失实时行为基准）；②新旧双入口并存长期保留（违背拆分目的，技术债加倍）；③另建路由页面挂载 v2（额外入口维护成本，而切换器方案只需一行 import）。

## 目标结构与职责分配

```
views/gen/template/
├── tree.vue                      v1 全程冻结不动，作行为基准（T4 收尾时删除）
├── tree-v2.vue                   新容器：抽屉、init 复位、handleClose、双 splitter、
│                                 宽度 composable 消费、变量面板、全屏、组装（≤350 行）
├── tree-panel.vue                新：左树（tree-v2 私有）搜索/刷新/删除/el-tree/拖拽/树菜单/template-form
├── editor-panel.vue              新：右编辑区（tree-v2 私有）头部按钮/tabs/Sortable/tab 菜单/CodeMirror/
│                                 二进制预览/编辑侦测 watch/保存/测试（内嵌 template-test）
├── use-template-tree-state.ts    新：核心状态与动作（无 DOM 依赖）
├── tree-utils.ts                 纯函数（v1/v2 共用，不动）
├── variable-panel/
│   ├── use-variable-panel-size.ts    新：宽度 px 体系（v2 消费；test.vue 于 T4 换用）
│   └── variable-edge.vue             新：收起态竖柄（同上）
├── form.vue / test.vue           对照期均不动；test.vue 于 T4 替换宽度体系与竖柄为共享件
└── components/context-menu/          新（11-16 spec 落地，独立命名避 v1 样式冲突）
```

### 状态归属矩阵

| 状态/资源 | 归属 | 消费方 |
|-----------|------|--------|
| `treeList / tabList / activeItemId / expandedKeys` | `use-template-tree-state`（容器持有） | 两子组件以 props 接同一引用 |
| `activeTabItem / fullFilePath` computed | state composable | editor-panel |
| `treeRef / 勾选 keys / 节点定位滚动` | tree-panel 内部 | — |
| `codeMirrorRef / insertAtCursor` | editor-panel 内部 + expose | 容器转发给变量面板 |
| `templateTestRef / <template-test>` | editor-panel 内部 | — |
| `formRef / useInitForm` | tree-panel 内部 | — |
| 宽度 px 体系 | `use-variable-panel-size`（容器持有；test.vue 各自持有） | 容器模板 |
| `variableCatalog`（useVariableCatalog） | 容器（现状不动） | 容器 → variable-panel |
| `treeFullScreen / isFullscreen` | 容器 | 容器模板 |
| `submitLoading` | editor-panel | — |

### 组件契约

- **tree-panel props**：`state`（use-template-tree-state 返回对象）、`templateGroupId`；**emits**：`refresh-data-list(tree)`（表单保存后，容器接到后执行现状 `initAfterHandler` 的剩余逻辑）。搜索框、刷新/删除按钮、勾选删除、拖拽移动全部在 tree-panel 内闭环（`checkedNodeListLength` 是 panel 内部 computed），不经 emit 上抛。
- **editor-panel props**：`state`、`templateGroupId`、`templateGroupType`；**expose**：`insertAtCursor(text, caret): boolean`。
- **对外契约一致**：tree-v2.vue 的 props（templateGroupId/Name/Type）与 `defineExpose({ init })` 与 v1 完全一致，`template-group/index.vue` 仅切换器一行 import 指向可变（决策 7），其余零改动。

### init() 时序（保持现状语义）

`init` 仍在容器：复位状态（含 state composable 的 `reset()` 与宽度 composable 的 `resetInit()`）→ `genTemplateApi.treeData` → `buildTree/buildFileList` → `visible=true` → `nextTick` → 恢复 expandedKeys（经 tree-panel expose 的 `setNodeExpanded(id, expanded)`，替代容器直接摸 `treeRef`）→ 首个文件激活 → `refreshTabData`。

## 数据流与错误处理

- 保存失败：沿用 axios 拦截器统一报错 + `.finally` 复位 loading，不改。
- `handleCloseTabs` / `handleClose` 的「未保存确认」逻辑原样迁移，`distinguishCancelAndClose` 行为不变。
- 变量目录 `countVariables` 畸形数据撤徽标的 try/catch 策略留在容器（现状），不动。
- v2 经 ContextMenu 后不含任何顶层 `document.addEventListener`（v1 因冻结仍保留原缺陷，属预期差异，见功能清单 T0 专项）。

## 功能基线清单（拆分前现状确认，供逐任务回归对照）

以当前 tree.vue 实现为基准（behaviour freeze；v1 全程冻结，本表即 v1/v2 对照表）。「验证」列含义：**全量** = 改动直接涉及，必须逐项验；**抽查** = 仅受搬运影响，抽样验证；**不动** = 本次不触碰其代码。

### A. 抽屉生命周期

| 编号 | 功能点 | 现状行为要点 | 代码锚点 | 涉及任务 | 验证 |
|------|--------|-------------|---------|---------|------|
| F01 | 打开入口 | 模板组列表行操作「模板」按钮 → 赋当前组（id/名称/类型）→ `nextTick` 调 `init(row.id)`，抽屉标题带组名 | template-group/index.vue L183-188 | T1 | 抽查 |
| F02 | 关闭确认 | 遍历 `treeList` 找 `isEdited`；无改动直接关；有则三态确认：是=批量 `updateContentList` 后关 / 否=丢弃直接关 / X=留在抽屉 | L683-733 | T3 | 全量 |
| F03 | 重复打开复位 | 每次 `init` 手动清 tabList/激活项/展开态/搜索词/全屏/变量面板；展开态恢复上次；首个文件自动打开进 tab | L576-614 | T3 | 全量 |

### B. 树区

| 编号 | 功能点 | 现状行为要点 | 代码锚点 | 涉及任务 | 验证 |
|------|--------|-------------|---------|---------|------|
| F04 | 树搜索 | 输入实时 `filter`，清空恢复全树；占位文案「请输入目录/文件名称」 | L735-750 | T1 | 抽查 |
| F05 | 单击节点 | 文件（type≠0）：设为激活 + 进 tab + 节点 scrollIntoView + 编辑器滚顶；已开文件再点只切激活、tab 数不变；目录：**不改勾选、只翻展开**（`check-on-click-leaf=false` 且未配 `check-on-click-node`，无预览）——原记「目录仅切勾选无预览」经 Suite N 实测纠正（`checkedFlip=N`） | L753-759, L330-354, L36 | T1 | 全量 |
| F06 | 红点/编辑状态 | 内容变化→红点（tab 与树节点同步，靠对象引用同一性）；改回原文→红点消失；仅 templateType===1 参与 | L654-680 | T3 | 全量 |
| F07 | 刷新 | 确认「丢失未保存」→ 清搜索/激活/tab/展开 → 重新 `init`；取消提示 info | L627-651 | T1 | 抽查 |
| F08 | 勾选批量删除 | 未勾选告警；确认 → `deleteList` 落库 → 对应 tab 关闭（走 F15 释放确认通道）+ 清展开态 → `initData` | L762-793 | T1 | 全量 |
| F09 | 拖拽移动 | 禁：同父 inner、同父 prev/next；允：跨目录/换层级 → `updateParent` → 提示 + 激活被拖节点 + tabPush + `initData`；新父不变则告警「不能移动到自身」 | L796-863 | T1 | 全量 |
| F10 | 节点右键菜单 | 节点上：修改 +（目录时）新建目录/模板文件/二进制文件；空白处：三个新建（根级 parentId=0、路径 `/`）；越界夹取定位；点外/Esc 关；项点击后自动关 | L86-104, L956-1013 | **T0** | 全量 |

### C. 模板表单弹窗（form.vue 通道）

| 编号 | 功能点 | 现状行为要点 | 代码锚点 | 涉及任务 | 验证 |
|------|--------|-------------|---------|---------|------|
| F11 | 字段随类型显隐 | 目录/文件名称 label 互斥切换；条件表达式 type∈{0,1}；模板名称仅 1；上传仅 2；必填文案随类型——**Suite G 实测代证**：三个新建入口的 dump 哈希两版同值（`B2F585E6_149`/`1E508800_181`/`593FB31A_151`），模板类型恒渲染 3 个 radio（`r=3`）且预置选中项跟入口；G5 二进制未上传文件被前端校验拦下（`errors=6AF02BD2_6`、`dlgStillOpen=Y`、`writeReq=NONE`） | form.vue L68-97 | 不动 | 不动 |
| F12 | 入口上下文注入 | 新增/修改的 parentId/templateType/templatePath 由右键入口决定（不信详情回传）；路径去尾段计算——**Suite G 实测**：空白右键 `pathIsRoot=Y`（parentId=0、路径 `/`）、节点新建路径取节点全路径（目录锚上为 `${backendPath}`）、「修改」`pathNoTail=Y` | L1026-1037, form.vue L129-136 | T0/T1 | 全量（已双跑） |
| F13 | 保存后链路 | `emit('refreshDataList', data)` → `tabPush` + `initData` + 文件类节点激活——**Suite G 实测（含写库）**：目录分支 `tabDelta=0/activeUnchanged=Y/pathUnchanged=Y`；文件分支 `tabDelta=1/activeIsNew=Y/cmMounted=Y`；`PUT /update` 后 `tabDelta=0/cmTextUnchanged=Y` 且 tab 与树描述同值；两版请求体逐字同构（新建 12 键、修改 18 键整份 VO 回传） | L1072-1082 | T1 | 全量（已双跑） |

### D. Tab 区

| 编号 | 功能点 | 现状行为要点 | 代码锚点 | 涉及任务 | 验证 |
|------|--------|-------------|---------|---------|------|
| F14 | 切换/关闭单 tab | 点击切换激活；`tab-remove` 走释放确认，关闭后优先激活右侧、无则左侧末尾 | L1145-1188 | T1 | 全量 |
| F15 | tab 右键菜单 6 项 | 关闭当前/其他/左/右/未改/全部，各项显隐条件逐项对应现状；经 `tab-{id}` DOM 反查定位目标 | L189-216, L1104-1142 | **T0** | 全量 |
| F16 | 批量关闭 + 未保存释放 | `handleCloseTabs`：含 isEdited 则三态确认（是=批量落库清红点 / 否=丢弃 / X=不关）；「关闭未改」直接过滤无确认 | L1281-1325 | T3 | 全量 |
| F17 | tab 拖拽排序 | Sortable 句柄 `.tab-label`、排除关闭图标；`oldIndex/newIndex` 各 -1 ——**实测为缺陷**：card 型 tabs 无 active-bar，`-1` 使映射整体错位一格，实际移动的是被拖项的前一项且不切激活（原记「拖后激活被拖 tab」经 Suite N 实测纠正，见共有缺陷 **K7**） | L359-389 | T1 | 全量 |
| F30 | tab 标签构成与红点 | `#label` 插槽：`templateDesc` tooltip（无描述禁用）+ 文件图标（`getIcon` 传 `{ expanded: false }` 占位，文件类不依赖）+ 文件名；红点 `el-badge is-dot` 由 `tabItem.isEdited` 驱动，与 F06 同一对象引用双向同步；`is-fixed.is-dot` 定位 right:0、6×6px | L172-186, L545→L1419-1431 | T1 | 全量 |

### E. 编辑区

| 编号 | 功能点 | 现状行为要点 | 代码锚点 | 涉及任务 | 验证 |
|------|--------|-------------|---------|---------|------|
| F18 | 路径显示 | `fullFilePath` 实算，parentId 变化可重算；过长省略 + tooltip | L547-552, L132 | T1 | 抽查 |
| F19 | 保存 | 仅 isEdited 启用（禁用态 tooltip 提示「无改动」）；点击与 Ctrl+S；loading；成功后清红点 + 同步 original | L142-146, L925-948, L225 | T1 | 全量 |
| F20 | 测试试渲染 | 仅 templateType∉{0,2} 显示；`templateTestRef.init(组id, 组类型, 模板id, 当前编辑内容)`；测试页回传编辑过的 id 列表 → 逐个 `detailData` 刷内容清红点 | L136-138, L897-922 | T1 | 全量 |
| F21 | 伪全屏 | 仅 CSS 铺满（`position:fixed` + `z-index:3000`，不走原生全屏，实测 `nativeFs=NULL`、`fschDelta=0`）；**ESC 并非「无效」，而是被 `el-drawer` 默认的 `close-on-press-escape` 吃掉——真 ESC 直接关掉宿主抽屉**（Suite N 实测 `escClosedMain=Y`，重开后 `EXITED` 不残留全屏态）；原生 `isFullscreen` 回 false 时同步撤销铺满态 | L147-153, L409-423, L1497-1507 | T1/T2 | 全量 |

### F. 变量面板

| 编号 | 功能点 | 现状行为要点 | 代码锚点 | 涉及任务 | 验证 |
|------|--------|-------------|---------|---------|------|
| F22 | 双入口显隐 | 头部「变量」按钮 + 收起态右缘 28px 竖柄控制同一状态；默认收起 | L154-159, L257-264, L462-464 | T1/T2 | 全量 |
| F23 | 徽标计数 | 竖柄显示 `countVariables` 总数；目录未就绪或计数异常（畸形 children:null）时无徽标不连坐抽屉 | L441-450 | T1 | 抽查 |
| F24 | 目录懒加载 | 抽屉打开才 `ensureLoaded`，按 templateGroupId 缓存不重复请求；面板内失败可重试 | L428-460 | 不动 | 抽查 |
| F25 | 宽度 px 体系 | 首次展开=容器 28%；面板 [240,560]、编辑区下限 320、窄容器退让优先保 240；全屏/拖拽/缩窗不触发等比缩放异常；Suite N 实测夹紧链 `raw→editorFloor→upper→clamped` 两版逐字同值：SE=949 首次展开取 `round(949×0.28)=266`、SE=599 走 RETAIN（守 266 不重算 240）、SE=473 走 EDITOR_YIELDS（面板守 240、编辑区退到 233，`floor=233/upper=240`）；注意 `.variable-edge` 是**占流元素（28px）**，收起态挤窄分栏容器，故 28% 取的是含竖柄的窄容器；**2026-11-24 T4 分项**已把 `test.vue` 换为同一共享件并实跑取证：28% 取的是**收起态**容器宽（watch 触发时 `useElementSize` 尚未跟上竖柄释放后的新宽度），1441 视口下 `SE_closed=1023→286`、1200 视口下 `SE_closed=842→raw 236→夹紧到下界 240` | L466-508 | **T1**（v2 消费新建共享件；test.vue **已于 T4 换用（2026-11-24）**） | 全量 |
| F26 | 变量插入 | 仅 templateType===1 可插；集合元素行不提供插入；表达式拼接（父路径.字段）；编辑器未就绪返回 false 提示 | L510-521, variable-panel/* | T1（仅转发链） | 全量 |

### G. 其他

| 编号 | 功能点 | 现状行为要点 | 代码锚点 | 涉及任务 | 验证 |
|------|--------|-------------|---------|---------|------|
| F27 | 二进制展示 | 图片扩展名 inline 预览，其余「暂不支持预览」；图标按扩展名区分（image/unknown）。**Suite P 双跑实测（2026-11-24）**：图片分支取组 79 存量 `404.png`（data URI 直填 `src`、len=68926 与 `CHAR_LENGTH` 相等、1100×731、`objFit=fill`）；非图片分支取夹具 `fixture.jar`（提示文案命中、`imageCount=0/cm=0`、「测试」按钮缺席）；P1 `B7EF1754`/894、P2 `2312EB5A`/572 两版逐字全等，X=空集。**共有观察**：`imageTypeList` 含 `'git'`（疑为 `gif` 的笔误，`gif` 已在其之前）且 `endsWith` 为字面比较 → **大写扩展名会落非图片分支**；v1 L545 ≡ v2 state L35，不构成差异，属 T4 清理项 | L228-237, L545, L136 | T1 | 全量（已跑） |
| F28 | 空态引导 | 无数据时提示空白处右键新建。**Suite P 双跑实测（2026-11-24）**：库里原本**没有任何 0 模板的组**（早前据列表页「0个」列的推断已误读更正——该列是 `groupPropertyCount`），故自建夹具组造真空态；`el-empty` 两行文案（`ECCFA594_30`）+ 空编辑器态 `tabs=0/cm=0`，空白处真实右键开三项菜单并能从该入口端到端新建成功；P0 `C4A4FDA8`/346 两版逐字全等（零写入），夹具已物理回收 | L68-81 | T1 | 全量（已跑） |
| F29 | 暗色与层级 | context-menu 全走 EP 语义变量；菜单 z-index 高于抽屉 popup（2000+）。**Suite H 双跑实测（2026-11-24）**：亮/暗两套 computed 值两版逐字全等（行为串 `E066C103`/`E7E21F11`/`9D4A93A1`/`1F44806A`），hover 与 EP 变量等值由 `H1_varEq=bg:Y,clr:Y` 正面证明；真实差异仅两项且属 T0 既定新增（x1 限高滚动、x2 Esc 关闭通道）；**原列的「无菜单串扰」实测不成立**（x5：两版均可同开；真实右键必带 pointerdown/mousedown，两版都会先关上一个） | L1377-1418 | **T0** | 全量（已跑） |

### 验证通道与专项观察点

- 回归通道：dev（真实后端）与 MSW mock 两套各跑一遍主链路；对照期每套均需 v1/v2 各跑一遍（同一模板组数据，结果逐条勾平）；
- **T0 后**：ContextMenu/宽度 composable 以单测为准，不动任何存量文件；
- **T2 专项（v1/v2 对照）**：以 F01-F30 为对照表逐条执行；预期差异仅限 F10/F15/F29（v2 翻转定位、监听器随实例卸载、~~无菜单串扰~~），其余任何行为差异均判 v2 缺陷；**F02 两处文案（带/不带「模板文件」）原列为预期差异，Suite S 实测两版逐字相同（单文件串长度差恰为 4），已不属差异项**；**「无菜单串扰」经 Suite H 的 x5 探针实测后也已移出差异清单：两版在无外部指针事件时都能两菜单同开（v1=2、v2=2），真实右键路径下又都会先关掉上一个（两版 `visible=1`、`closeAll=0`），行为面不可观测；v2 的改进只在结构层（定位与外点判定不再取首个 `.context-menu` 匹配 + 监听器随实例卸载）**。另，**实测证实 v1 全文无 Esc 处理器**，F10 基线「点外/Esc 关」的 Esc 部分对 v1 不成立，属 T0 共享件新增的关闭通道而非 v2 回归。**探针口径**（Suite S 修正）：两套组件在 `template-group/index.vue` 里**同时挂载**（各自持有一个 `el-drawer` 与一份 state），读取当前状态必须从根实例 BFS 全组件树收集候选后**按 state 对象归组**：v2 重构后 `visible` 已上移到容器，**不能单独以 `state.visible === true` 作判据**，改为「同一 state 对象组内任一实例报 open 即认定该 state 属于活动抽屉」，并记录 `refShare/objCount` 自证（实测 v2=`3/objCount=2`、v1=`1/objCount=2`：既确认两版取到的是两份**不同** state、差分不会假性成立，也正面证明 v2 三个子组件共享同一对象）；也不能取遍历到的第一个候选，否则 v2 实例排在 v1 之前会读错一侧。
- **Suite N 追加两条探针纪律**（均为环境/结构事实，不是被测缺陷）：①**布局断言只用宽高 + 类名 + computed style，禁用 `left/top`** —— MCP 标签页里 `requestAnimationFrame` 500ms 只跳 1 次（合成帧饥饿），带 transition 的 `transform` 会停在起始值，实测 `.el-drawer.rtl.open` 的 `translateX` 一度为 1441px、元素整体在视口外却 `checkVisibility()=true`；每段跑前需 `page.bringToFront()`；②**所有 tab / cm-editor / nav 查询必须排除 `.template-test-drawer`** —— test.vue 的 `el-drawer` 未开 `append-to-body`，「模板测试」子抽屉整棵树挂在主抽屉 DOM 内部，全子树查询会把子抽屉的 tab 与编辑器混进来（曾造成 N4 假告警 `testDrawerGone=STILL`，实为固定 `sleep(900)` 短于 ~1.25s 的 overlay 置隐延迟）。计时类数值一律不进行为串，只进身份串。
- **Suite G 追加两条探针纪律**：①**请求体 spy 不得先截断再解析**——`PUT /update` 正文 432 字符，首版 `slice(0,400)` 使 `bodyKeys`/`bodyContent` 双双 `UNPARSEABLE`，恰好废掉「修改分支到底带了哪些字段」这条取证；现采「截断副本进身份串、完整原文另存供解析」。②**「页面挂载了几套组件实例」不是行为**——将这类环境计数拼进行为串会产生假差异（G3 曾因 `cand=2/1` 使两版指纹不同而串长完全相等）；一律归身份串。
- **Suite H 追加三条探针纪律**（F29 暗色与层级批次）：①**合成右键必须忠实于真实事件序列 `pointerdown → mousedown → contextmenu`**——v1 的外点关闭挂 `document mousedown`、v2 挂 `document pointerdown`（捕获），只发其中一种会把「关闭通道实现不同」误读成「行为不同」（首版只发 mousedown，导致 v2 的外点关一次也没验到）；要单测「同开态」则另开一条只发 `contextmenu` 的探针（x5），两者不得复用。②**上一段算出的坐标不得当作下一段的指针停靠点**——两版菜单锚点差 5px，指针原地不动会使 H2 的 item hover 态分叉，产生 1 字符伪差异（`D9273DC2`/491 vs `D7053679`/492）；跨段前先把指针移到与版本无关的固定点。③**CSS 变量等值不能比字面量**——`rgb(235,237,240)` 与 `#ebedf0` 直接字符串比较恒为 N（废断言）；应用同一变量建探针元素、比两个 computed 值。
- **预期差异项可单列一串**：当某功能点（如 F29）在写脚本前就能从源码列出必然差异，则不得把它们混进 `fpBehav` 后整体豁免，而是拆成 F（必等）/ I（身份）/ **X（预期差异，逐值落档）** 三串：X 成员及理由需在跑批前由源码定死，否则剩下该等的值就没有约束力（x4 列表标记就是靠「在同一语义元素上取 ls」归一化后归入 F 并实测全等）。
- **T3 后**：切流确认（模板组入口即 v2），template-group 页面主链路抽查；
- **T4 后**：删 v1 后全局搜残留引用（待做，依赖 T3）；test.vue 换共享件后测试页变量面板全量（F22/F25 口径）**已于 2026-11-24 实跑**（双证见风险表同名行）；v2 全清单收尾回归（待做，依赖 T3）。

## 实施计划（v2 并行对照，5 个任务各自独立可验证；决策 7）

| 任务 | 内容 | 触碰存量文件 | 验证 |
|------|------|-------------|------|
| T0 | 新建共享件：`utils/context-menu.ts` + `components/context-menu/*`（按 11-16 spec，独立命名避 v1 样式冲突）+ `use-variable-panel-size.ts` + `variable-edge.vue`，含纯函数/composable 单测 | 无 | 单测 + `vue-tsc` |
| T1 | 新建 v2 文件集：`tree-v2.vue` / `tree-panel.vue` / `editor-panel.vue` / `use-template-tree-state.ts`，消费 T0 共享件与既有 `tree-utils` | 仅 `template-group/index.vue` 加切换器一行（默认指 v1） | `vue-tsc` + Vitest |
| T2 | v1/v2 双跑对照：按功能清单 F01-F30 先跑 v1 记录、切 v2 复现，逐条勾平（dev + MSW 双通道）。**进度（2026-11-24 Suite P 后，30/30 = 100%）**：已勾平 **30 项**（F01-F30 全量）；**未跑 0 项**——原拟标「口径外」移交 T4 的 F27/F28，已改用自建空态夹具组（跑完物理回收）+ 组 79 存量二进制节点实跑闭合。布局交互批次 Suite N 十一段行为指纹两版**逐字全等**（串长 206/122/131/61/312/172/246/270/292/108/148；本批**零写库**）；表单通道批次 Suite G（F11/F12/F13，七段 G0-G6）行为串共 62 行、合计 3025 字符两版**逐字全等**，为本任务**唯一必须写库**的批次：每版恰 3 次写（`POST /add` ×2 + `PUT /update` ×1）且只写自建 fixture，四轮固定指纹 SQL 复核均回基线值、收尾 `fixture_left=0`；最强等价证据为**请求体同构**（新建 12 键纯表单、修改 18 键整份详情 VO 回传、`templateContent` 带空串而非省略）；F11 原定静态 props/emits 比对已由 G0 字段矩阵 + G5 校验拦截双证代替。暗色与层级批次 Suite H（F29，5 段）四段行为串两版**逐字全等**（亮/暗取色、hover 变量等值、z-index/命中、现场与暗色残留自证），**全程零写入**（会话仅 2 个 GET），真实差异仅 x1 限高滚动与 x2 Esc 通道；并把原列为预期差异的「无菜单串扰」实测推翻（两版同形）。身份串差异均为可解释项（`treePanel` 组件名、`menuDom` 菜单项类名、N4 关闭耗时 v1 382ms / v2 1516ms、G3 `stateCand` 实例计数、H 菜单挂载链与 li 宽 121/119 的图标尺寸差）；本轮新增共有缺陷 **K7**，并纠正 F05/F17/F21 三条旧预期；风险表「splitter 宽度计算」「Teleport 层级」「全局样式互污染」三项均已由实测正面关闭。二进制与空态批次 Suite P（F27/F28，4 段）四段行为串两版**逐字全等**（`C4A4FDA8`/346、`B7EF1754`/894、`2312EB5A`/572、`E5E41DAA`/321），且 **X 串（预期差异）跑批前即定为空集、实测亦逐字相同**（依据：v1 `tree.vue` L68-81/L228-237 ≡ v2 `tree-panel.vue` L56-69 / `editor-panel.vue` L71-80，且新建表单两版共用同一 `form.vue`）；每版恰 1 条写（`POST /gen/template/add`）且只写夹具组 93，跑完物理删除夹具模板与夹具组，组表 `cnt=54/sum_id=3396/max_id=92/deleted=44`、模板全表 `1596` 行、`stray_fixtures=0` 均回基线；本批新增三条探针纪律（`POST /gen/templateGroup/page` 属只读、本轮新建行自增 id 只进身份串、`el-image` 的 `is-loading` 需先等落定）。明细见同名 feature-record 第四节 | 无 | 全量对照表 |
| T3 | 切流：切换器默认指向 v2，业务继续走 v2 | `template-group/index.vue` 一行 | 主链路抽查 |
| T4 | 收尾归一：删 v1 `tree.vue`；test.vue 宽度体系/竖柄换共享件；全量回归一轮。**分项进度（2026-11-24）**：`test.vue` 换共享件 **已完成**（`useVariablePanelSize` + `variable-edge`，git diff `+10/−96`，无旧标识符残留；静态 `vue-tsc`/`vitest`（5 文件 79 用例）/ESLint 三项全绿；运行时取证见风险表同名行），**未删 `tree.vue`、未跑 T4 全量回归**（两项依赖 T3 切流先落地） | `test.vue`（已改），`tree.vue`（未删） | 清单全量 + test.vue 专项（test.vue 专项已跑） |

T2 对照不通过则改 v2 重跑，期间可随时切回 v1（零成本回退）；各任务间 v1 始终可用，不存在功能真空期。

## 测试策略

- **单测**：`computeMenuPosition`（T1，用例清单见 11-16 spec）；`use-variable-panel-size` 的 clamp 边界（窄容器退让、28% 初始化、`resetInit`）补 co-location 单测；既有 `tree-utils.test.ts`、`variable-panel` 相关测试零回归。
- **手工全链路回归**：按「功能基线清单」逐项执行（各任务对应项见其「涉及任务」列）；全量级序列：打开抽屉 → 树搜索(F04) → 单击文件开 tab(F05) → 编辑出红点(F06) → 撤销内容红点消失(F06) → 保存(F19) → 拖拽移动节点(F09) → 勾选删除(F08) → 右键新建目录/模板/二进制(F10/F12) → tab 六种关闭含未保存确认三分支(F15/F16) → tab 标签红点/tooltip/图标与关闭联动清徽(F30/F06) → 变量面板展开/拖宽/收起竖柄/插入(F22/F25/F26) → 全屏铺满与退出(F21) → 关闭抽屉未保存确认(F02) → 测试按钮试渲染(F20) → 重复打开状态复位(F03)。

## 风险评估

| 风险 | 等级 | 缓解 |
|------|------|------|
| 对象引用同一性在搬运中被破坏（如某处不小心 spread/clone 了节点） | ~~高~~ **已关闭（2026-11-24 T2 专项）** | state composable 全部原地 mutate；T1 写 v2 时全局搜 `tabList`/`treeList` 赋值点逐一对照 v1；**R suite 专项已跑完**：运行时直接比较 `findByIdFromTree(2142, treeList) === tabList.find(id===2142)`，两版均为 `SAME_REF`；编辑后同一快照 `tree=on/tab=on/save=ena`，逐字改回原文后同一快照 `tree=off/tab=off/save=dis`；反向通道由抽屉关闭确认弹窗承担（文案取自 `treeList` 递归，两版 FNV=`49583903`/len=35 相同）——双向同步与同帧性均成立，不再列为开放风险 |
| `nextTick` 时序依赖（expandedKeys 恢复、formInitHandle、Sortable 初始化）跨组件后失配 | 中 | 相关 DOM 操作随 ref 下沉到持有该 ref 的子组件内部，容器只经 expose 单点调用 |
| ContextMenu Teleport 后 z-index 与抽屉层级（现状菜单注释明确要求 > 2000） | ~~中~~ **已正面关闭（2026-11-24 Suite H）** | 沿用 11-16 spec 样式方案；实测两版 `z-index` 同为 9999，且 `elementFromPoint(菜单中心)` 两版均 `IN_MENU` —— v1 菜单嵌在抽屉 DOM 内 9 层深、v2 Teleport 到 body 直下，**两种挂载点都压得过抽屉**；亮/暗两套 computed 取色两版逐字全等（`H2_core` 暗色 `bg=rgb(29,30,31)`、`bd/rgb(76,77,79)`），hover 与 EP 变量等值由 `H1_varEq=bg:Y,clr:Y` 证明 |
| splitter 宽度计算对 DOM 挂载次序敏感（el-splitter-panel 卸载/挂载与 composable 初始化竞态） | ~~中~~ **已正面关闭（2026-11-24 Suite N）** | v2 逐字照搬现状数值体系与声明位置（决策 5 契约）；窄窗口与全屏切换已实测：SE=949/599/473 三段两版行为串逐字全等（首次 28% 取 266、SE=599 RETAIN、SE=473 EDITOR_YIELDS）且复原后 `seBack=Y`；F21 全屏↔退出尺寸 977×690↔1441×770 两版 `restored=Y`。残余不确定性仅为 `.variable-edge` 28px 释放时机导致的 `epw` 233/261 抖动，已登记为环温噪声轴（同版本内可复现，不影响 F25 真断言量 `pw`） |
| ~~test.vue 替换共享件引入无关回归~~ **已关闭（2026-11-24 T4 分项实跑）** | ~~低~~ | 逐字对照迁移，行为参数（`countVariables(catalog, true)`）留在宿主。**双证**：①静态——`vue-tsc --noEmit` 零输出、`vitest run` 5 文件 79 用例全过（含 `use-variable-panel-size.test.ts` 13 项）、`eslint test.vue` exit 0，且旧标识符（`variablePanelSize`/`clampedVariableSize`/`panelSizeInitialized`/`ArrowLeft`/`.variable-edge*` 样式）全库零残留；②运行时（组 80 经 v2 入口 → 测试抽屉，全程只读，会话尾部仅 `page/tree/variableCatalog/detail/cascaderData` 五类读请求、零写）——竖柄确由 `VariableEdge` 渲染（`__vueParentComponent` 上溯链顶为 `VariableEdge`，外裹 `ElTooltip` 系，tooltip 生效 `aria-describedby` 存在），computed 与旧内联版逐值相同（`28px`/`flex:none`/column center center/`gap 8px`/`pointer`/`border-left 1px solid #ebeef5`/`bg #fafafa`/`clr #909399`/`user-select none`，arrow `12px`，text `vertical-lr / 4px / 12px`）；目录未就绪时 `count=null` → 徽标不渲染（降级口径保留）；首次展开 `SE_closed=1023 → round(1023×0.28)=286 → pw=286`，收起态 `epw=1023=SE_closed` 铺满、竖柄回归；改视口 1441×770→1200×800 后关抽屉重开，`SE_closed=842 → raw=236 → 夹紧到下界 240`（不是沿用上一轮的 286），正面证明 `resetInit()` 生效 |
| 双版本并行期间误改 v1，行为基准失真 | 中 | v1 冻结纪律：对照期对 `tree.vue` 的改动只允许出现在 revert 中；T2 对照差异先排除「切换器指错」再判 v2 缺陷 |
| v1/v2 同页共存期全局样式互污染（`.context-menu` 等裸类名） | ~~低~~ **已实测无污染（2026-11-24 Suite H）** | 决策 7 零冲突约束：新建件独立命名；暗色下两版菜单外观已做同套量测对照：亮/暗各 13 项 computed 取值两版逐字全等（且 v2 根类名 `.context-menu` 与 v1 完全同名同层叠，未出现 v2 样式覆盖 v1 或反向污染的取值漂移）；唯一尺寸差是菜单项宽 121 vs 119，已溯源到 v1 树菜单的 svg-icon 未写 size（默认 `1em`=12px）而 v2 统一 `10px`，**v2 反而消除了 v1 自身两菜单的图标尺寸不一致** |
| 数据受阻项无法取证（靶点组 80 无二进制节点、且库里无任何 0 模板组，F27/F28 一度只能源码推导并拟移交 T4） | ~~中~~ **已关闭（2026-11-24 Suite P）** | 换用**自建夹具**而非等待数据：夹具模板组（`__T2FIXTURE_EMPTY__`）提供真空态，非图片分支用夹具组内新建的 `fixture.jar`（页内 `DataTransfer` + 固定 8 字节，无需外部文件），图片分支用组 79 存量 `404.png`（零写入）；写账限每版 1 条且全部落在自建夹具上，跑完 `DELETE` 模板行与夹具组，四项指纹（组表 cnt/sum/max/deleted、模板全表行数、夹具残留 0、组 80 `fp=d07878b5db8bea76510d99946695ae6f`）全部回基线。**纪律**：此类项不得靠列表页展示值反推数据存在性（「0个」列实为组属性数量，曾导致误判），必须先走 `GROUP BY ... HAVING` 与源码两处取证 |

## 约束

- 对照期不修改任何存量业务文件（`tree.vue`/`test.vue`/`form.vue`），唯一例外是 `template-group/index.vue` 的切换器一行；不改后端接口与 `genTemplateApi`；不引入新依赖。
- 代码风格对齐现有约定：const 箭头函数（ESLint func-style）、Tab 缩进、单引号、无分号、全标签非自闭合；注释密度与「坑位注释」随代码搬运，不得丢失（如 splitter min/max、红点、z-index 等注释记录的是行为边界原因）。
- `vue-tsc --noEmit` 零新增错误；既有单测零回归。
- v1 外部行为全程为对照基准（含所有文案、确认分支、尺寸规格）；v2 与 v1 的唯一预期差异是已知缺陷修复项（F10/F15/F29）。

## 改动文件清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `src/utils/context-menu.ts` + `.test.ts` | 新建（T0，按 11-16 spec） | `computeMenuPosition` 纯函数 |
| `src/components/context-menu/{types.ts,use-context-menu.ts,index.vue}` | 新建（T0，按 11-16 spec） | 定位壳组件（独立命名避 v1 冲突） |
| `src/views/gen/template/variable-panel/use-variable-panel-size.ts` + `.test.ts` | 新建（T0） | 宽度 px 体系 |
| `src/views/gen/template/variable-panel/variable-edge.vue` | 新建（T0） | 收起态竖柄 |
| `src/views/gen/template/use-template-tree-state.ts` | 新建（T1） | 核心状态与动作 |
| `src/views/gen/template/tree-panel.vue` | 新建（T1） | 左树面板（tree-v2 私有） |
| `src/views/gen/template/editor-panel.vue` | 新建（T1） | 右编辑区面板（tree-v2 私有） |
| `src/views/gen/template/tree-v2.vue` | 新建（T1） | v2 容器，与 v1 并行可切换 |
| `src/views/gen/template-group/index.vue` | 修改（T1 加切换器 / T3 切流） | 仅 import 指向，对照期唯一触碰的存量业务文件（T4 除 test.vue 外） |
| `src/views/gen/template/tree.vue` | **对照期不动**；T4 删除 | v1 行为基准 |
| `src/views/gen/template/test.vue` | **修改（仅 T4）已完成（2026-11-24）** | 宽度体系与竖柄换共享件；`countVariables(..., true)` 与 `canInsertVariable`/`insertVariable` 仍留宿主 |
