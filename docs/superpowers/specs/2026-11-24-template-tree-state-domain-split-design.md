# 模板树状态层按数据域拆分设计（use-template-tree-state → 四文件）

## 概述

`views/gen/template/use-template-tree-state.ts`（约 450 行）在 v2 第一轮拆分中把全部状态与动作收进了单一 composable，形成"上帝对象"：tree 操作、tab 操作、接口调用、UI 确认弹窗、派生查询六类职责同居一处。本设计将其按**数据域**纵向拆为四个文件：

```
use-template-tree-data.ts       数据域：treeList 的装载与刷新（只读接口 + 纯结构状态）
use-template-edit-tabs.ts       会话域：tabList / activeItemId / 红点 isEdited + 全部 tab 动作
use-template-persistence.ts     持久化域：内容写接口调用 + 测试回刷 + submitLoading
use-template-tree-state.ts      编排层（保留原名）：组装三域 + 确认弹窗编排 + 对外 facade
```

拆分动机来自评审确认的三个坏味道：①tab 与 tree 操作同居且互摸对方数据；②state 文件内直连 `genTemplateApi` 四个端点；③单 hook 导出 25 个成员、消费方面对整包对象不知边界在哪。

本文件是**设计文档（spec）**；实施计划另出。改造范围**仅限 v2 文件集**，v1 `tree.vue` 继续全程冻结（对照期行为基准）。

## 现状问题盘点

### 职责分布（按当前符号归属归类）

| 职责簇 | 现有符号 | 问题 |
|--------|---------|------|
| 树数据装载 | `loadTreeData`、`initData` | 内嵌 `treeData` 接口直调，装载与 IO 不可分 |
| Tab 会话 | `tabList`、`activeItemId`、`tabPush`、`refreshTabData`、`getElementFromTabList`、7 个关闭动作、content-watch | 与 tree 同居；`refreshTabData` 与 watch 直接读 `treeList` |
| 内容持久化 | `saveTemplateContent`、`handleCloseTabs`/`handleClose` 内的 `updateContentList` 直调、`refreshActiveItemContent`、`submitLoading` | 写接口散落在 tab 动作和抽屉关闭逻辑里，无单一收口 |
| 派生查询 | `activeTabItem`、`fullFilePath`、`canInsertVariable`、`getIcon` | `activeTabItem` 横跨两域（tab 存 id、tree 存数据） |
| UI 编排 | `handleCloseTabs`/`handleClose` 的 `ElMessageBox` 确认、`ElMessage` 提示 | 交互决策与数据变更焊死 |
| 视图态 | `expandedKeys`（已并入 state reactive）、`imageTypeList` | 非树数据也非 tab，归属含糊 |

### 跨域耦合点（拆分必须显式处理的）

1. **对象引用同一性**：`tabList` 元素来自 `buildFileList(treeList)`，与 `treeList` 节点同一批对象；红点、编辑侦测、保存 payload 全靠共享引用。这是**领域事实**，拆分不消除它，只把它收进显式契约（决策 3）。
2. **`refreshTabData(dataList)`**：入参是 tree 域产物（文件列表），行为是 tab 域同步——跨域调用点。
3. **content-watch**：监听 `activeTabItem`（tree 反查 + tab 定位），mutate 节点上的 `isEdited`/`originalTemplateContent`。
4. **`handleCloseTabs` 保存支链**：确认弹窗（session）→ 批量写接口（persistence）→ 清红点回滚（edit-tabs）。
5. **`handleClose` 全树扫描**：遍历 `treeList` 收集 `isEdited` 节点（tree 数据 + tab 会话标记）。
6. **`reset()` 整表覆盖**：state 合并了四个域的反应式字段，工厂演进必须按域进行（决策 6）。

## 设计目标与非目标

**目标**

- 每个域文件可用一句话说明数据主权；接口调用只出现在 tree-data（读）、persistence（写）两处。
- `ElMessageBox` / `ElMessage` 只出现在编排层；三个域文件 import 面不含 `element-plus` 的 UI 原语。
- 跨域访问全部经函数参数显式注入，禁止 import 反向持有对方 reactive。
- 对外 facade 零变化：tree-v2 / tree-panel / editor-panel 三个消费方的解构与模板绑定不动（决策 5）。

**非目标**

- 不改任何行为语义：接口语义、确认文案、红点规则、激活位选取、reset 口径与现版逐条一致（行为冻结延续）。
- 不动 v1 `tree.vue`、`test.vue`、`form.vue`。
- 不修对照期已登记缺陷（K1/K2/K3/K6 位于本文件者，原样搬运，见风险表）。
- 不引入新依赖（Pinia 评估后否决，决策 1）。

## 设计决策

### 决策 1：多 composable 按数据域纵向拆分，不引入 Pinia、不按操作横向切

- **vs Pinia store**：本状态是抽屉内会话态（destroy-on-close 即弃），项目虽装有 pinia 但全仓库零 `defineStore`，且 composable 体系（`use-variable-catalog`、`use-crud` 等）已是既有惯例——store 只增加生命周期管理成本，无共享收益。
- **vs 按操作切（tree 动作 / tab 动作两个筐）**：红点机制依赖 tab 元素与 tree 节点同一批对象，按操作切两边都要 mutate 对方数据，等于没拆。按**数据主权**切：`treeList` 归 tree-data，`tabList/activeItemId/isEdited` 归 edit-tabs，跨界只能经注入的函数，耦合从"隐式共对象"变成"显式契约"。

### 决策 2：接口调用按读写归位

- 读链路 `genTemplateApi.treeData` → `use-template-tree-data`（`loadTreeData`/`initData` 保留在此，IO 与装载本属同簇）；
- 写链路 `updateContent` / `updateContentList` / `detailData` → `use-template-persistence`。`refreshActiveItemContent`（测试回刷）虽先查接口再改 tab 标记，但触发源是测试写链路，整体归 persistence，落标记经注入的 `applyEditRollback` 回调交 edit-tabs 执行；
- `handleCloseTabs` / `handleClose` 里内嵌的 `updateContentList` 直调**上收**到 persistence 的 `batchSave(list)`，编排层只余「确认 → 调 saver → 调 rollback → done」的流程串联。这是本次唯一的结构性行为搬运，落库参数与时序不变。

### 决策 3：跨域三通道——注入回调 / 消费方直连 / facade 组合 computed

1. **注入回调**（域 → 域）：persistence 需要落 tab 标记时，由编排层组装时注入 edit-tabs 的 `applyEditRollback`；
2. **消费方直连**（组件 → 多域）：`refreshTabData(newFileList)` 是 tree 域刷新后的 tab 同步，调用方本就是容器/面板，改为 `initData` 只返回文件列表、消费方自行调 `refreshTabData`——消除 tree-data 对 edit-tabs 的反向依赖；
3. **facade 组合 computed**（编排层）：`activeTabItem` 横跨"tab 存 id、tree 存数据"，作为组合产物放编排层，域文件内部一律用注入的 getter 而非直接摸对方状态（content-watch 随之前移到 edit-tabs，见决策 4）。

### 决策 4：内容侦测 watch 前移到 edit-tabs

现 watch 监听 `[id, content, type]` 三元组（依赖 tree 反查）。edit-tabs 本就持有 `tabList/activeItemId/isEdited` 全部要素，且 `templateContent` 就挂在 tab 引用的节点对象上——改为监听 `[activeTabId, activeTabContent]`（tab 侧取值），`type===1` 守卫保留。行为差异论证：content 与 isEdited 的读写目标是同一对象引用（F06 已证 SAME_REF），从"tree 反查项"取值与从"tab 引用项"取值等价。

### 决策 5：对外 facade 零变化，state 合并对象保留

编排层 `useTemplateTreeState` 保留原名、原入参（`templateGroupId: Ref<number>`）、原返回成员名；`visible` 维持上轮改造位置（容器持有）。tree-panel / editor-panel / tree-v2 的解构语句与模板绑定**一行不动**，拆分收益全部体现在内部结构，组件层改动面为零。

### 决策 6：reset 工厂演进为 per-slice 工厂 + 合并覆盖

延续上轮「无参工厂、init 复位整表覆盖」口径：每个域文件自带 `createXxxSlice()` 无参工厂，各自只清自家字段；`reset()` 留在编排层，按依赖序调用（persistence 先清 `submitLoading`，再清 edit-tabs、tree-data），避免单一大工厂重新制造耦合点。向 state 增删字段时改对应域工厂一处。

## 目标结构与接口契约

### use-template-tree-data.ts（数据域）

职责：持有 `treeList`，封装树数据的远程装载（读接口）与结构派生；不认识 tab。

```typescript
export const useTemplateTreeData = (templateGroupId: Ref<number>) => {
	// 切片工厂：createTreeDataSlice = () => ({ treeList: [] as Tree[] })
	return {
		treeData: reactive(createTreeDataSlice()),          // { treeList }
		loadTreeData: () => Promise<Tree[]>,                // treeData 接口 + buildTree，返回文件列表（不触碰 tab）
		initData: () => Promise<Tree[]>,                    // 同上（两函数现状实现逐字相同，是否合并见开放问题 Q1）
		findById: (id: number) => Tree | undefined,         // findByIdFromTree(id, treeList)
		getFullPath: (id: number) => string                 // getFullPathById(id, treeList)
	}
}
```

### use-template-edit-tabs.ts（会话域）

职责：持有 `tabList / activeItemId / expandedKeys` 与节点上的编辑标记（`isEdited` / `originalTemplateContent` 的 mutate 通道），实现全部 tab 动作与红点侦测；不认识 HTTP 与弹窗。

```typescript
export const useTemplateEditTabs = (getNode: (id: number) => Tree | undefined) => {
	// 切片工厂：() => ({ tabList: [], activeItemId: -1, expandedKeys: [] })
	return {
		editTabs: reactive(createEditTabsSlice()),
		tabPush: (tree: Tree) => void,
		getElementFromTabList: (id: number) => Tree | undefined,   // 含 id=-1 回落占位对象，现状语义
		refreshTabData: (dataList: Tree[]) => void,                // 编辑态保留逻辑原样
		activeTab: computed<Tree>,                                 // getNode(activeItemId) ?? 占位对象
		// 内容侦测 watch 在模块内注册（决策 4 前移到 tab 引用取值）
		// 7 个关闭动作：closeTab / closeCurrentTab / closeOtherTabs / closeLeftTabs /
		//   closeRightTabs / closeUnedited / closeAll —— 统一签名 (item, index?) => void，
		//   纯数据变更，保存确认由编排层的 confirmAndClose 系列包装（见 facade）
		applyEditRollback: (tabs: Tree[]) => void,                 // isEdited=false + originalTemplateContent=content
		markAllEditedFalse: () => void,                            // 供 closeUnedited 等现状内部清标记路径（按实际符号核对）
		reset: () => void
	}
}
```

> 关闭动作的拆分口径：现 `handleTabRemove/close*Tabs` 内部各自调 `handleCloseTabs`（弹窗）。拆为两层——**纯数据关闭**（本文件）与**带确认的关闭编排**（编排层包装，`distinguishCancelAndClose` 三分支文案与逻辑逐字保持）。消费方继续接包装层，facade 不变。

### use-template-persistence.ts（持久化域）

职责：内容写接口的唯一收口 + 测试回刷链路 + `submitLoading`。

```typescript
export const useTemplatePersistence = (deps: {
	getActiveTab: () => Tree                       // 取当前 tab（保存 payload 数据源）
	getActiveId: () => number
	applyEditRollback: (tabs: Tree[]) => void      // 保存成功后落标记，回调注入（决策 3 通道①）
	findNodeById: (id: number) => Tree | undefined // 测试回刷定位节点（tree-data 注入）
}) => {
	return {
		submitLoading: ref(false),
		saveActive: () => void,                             // 原 saveTemplateContent：updateContent + finally 复位 loading
		batchSave: (list: { id: number; templateContent?: string }[]) => Promise<void>,  // updateContentList 唯一收口
		refreshAfterTest: (editTemplateIdList: number[]) => void  // 原 refreshActiveItemContent：detailData → 写节点内容 → applyEditRollback
	}
}
```

### use-template-tree-state.ts（编排层，保留原名）

职责：组装三域；持有全部 UI 交互决策（MessageBox 确认、消息提示）；导出与现版同名的 facade。

```typescript
export const useTemplateTreeState = (templateGroupId: Ref<number>) => {
	const treeDataDomain = useTemplateTreeData(templateGroupId)
	const editTabsDomain = useTemplateEditTabs(treeDataDomain.findById)
	const persistence = useTemplatePersistence({ ...组装注入 })

	return {
		// state 合并视图（消费方解构不变）：getter 代理三域切片，或直接暴露各切片
		state,                    // { treeList, tabList, activeItemId, expandedKeys } —— 形态见开放问题 Q2
		getIcon,                  // imageTypeList 归属见 Q3
		activeTabItem, fullFilePath, canInsertVariable,
		tabPush, refreshTabData, getElementFromTabList,
		loadTreeData, initData, reset,
		saveTemplateContent, submitLoading, refreshActiveItemContent,
		handleCloseTabs, handleTabRemove,
		closeCurrentTab, closeOtherTabs, closeLeftTabs, closeRightTabs,
		closeUneditedTabs, closeAllTabs,
		handleClose               // 全树扫描 isEdited → 确认 → batchSave → rollback → done()
	}
}
```

确认编排（留本文件，内部改调 persistence.batchSave 而非直连 api）：
- `handleCloseTabs`：脏检查 → `joinFileName` 文案 → 三态确认 →「是」`batchSave` + `applyEditRollback` + `closeTab()`；「否」`closeTab()`；「X」不动。
- `handleClose`：`findAndClose` 全树扫描 → 同上确认链。

## 迁移对照表（use-template-tree-state.ts 现有符号 → 去向）

| 现符号 | 现行号锚点 | 去向 | 改动说明 |
|--------|-----------|------|---------|
| `createTreeState` / `state` / `reset` | L20-L26, L30, L156 | 三域各建 `createXxxSlice` + 编排层 `reset()` 依序调用 | 单工厂拆为 per-slice 工厂（决策 6） |
| `loadTreeData` | L148-L153 | tree-data | 原样（返回文件列表，不再被期望触 tab） |
| `initData` | L138-L145 | tree-data | **原定“刷新上移到消费方”作废**：跨 await 上移会引入 microtask 间隙导致丢未保存内容（评审 B1），改由数据域 `fetchTreeData(onLoaded?)` 同步回调 |
| `imageTypeList` | L32 | 倾向移 `tree-utils.ts` 为 `IMAGE_TYPE_LIST` 常量（Q3） | 若移：tree-v2/tree-panel/editor-panel 三处消费点同步改 |
| `activeTabItem` | L35-L42 | edit-tabs（`activeTab`），facade 以同名导出包装 | 占位对象回落语义不变 |
| `fullFilePath` | L44-L49 | 编排层（`getFullPath(activeTab.id)`） | 依赖建立写法不变 |
| `canInsertVariable` | L52 | 编排层 | 表达式不变 |
| `getIcon` / `resolveNodeIcon` 绑定 | L55 | 编排层（绑定清单来源） | 不变 |
| `getElementFromTabList` | L57-L63 | edit-tabs | 原样 |
| `tabPush` | L66-L75 | edit-tabs | 原样 |
| `refreshTabData` | L77-L106 | edit-tabs | 原样（含 splice 换引用 + 编辑态保留） |
| content-watch | L109-L135 | edit-tabs | 取值源**实际仍为 tree 反查**（决策 4 所述“改 tab 引用”未落地，也不宜落地，否则丢树侧内容侦测）；仅归属下沉，见评审修正记录 |
| `saveTemplateContent` / `submitLoading` | L161-L184, L33 | persistence（`saveActive`） | 成功后落标记改调注入的 `applyEditRollback` |
| `refreshActiveItemContent` | L187-L204 | persistence（`refreshAfterTest`） | 写内容后落标记经回调（决策 3 通道①） |
| `handleCloseTabs` | L207-L250 | 编排层保留 | 内嵌 `updateContentList` 直调上收 `persistence.batchSave` |
| `handleTabRemove` / `closeCurrentTab` / `closeOtherTabs` / `closeLeftTabs` / `closeRightTabs` / `closeUneditedTabs` / `closeAllTabs` | L253-L365 | 纯数据部分 → edit-tabs；确认包装留编排层同名导出 | 两层化口径见 edit-tabs 契约节 |
| `handleClose` | L367-L416 | 编排层保留 | 同 `handleCloseTabs`，`updateContentList` 换 `batchSave` |

### tree-utils.ts 的处置

| 符号 | 处置 |
|------|------|
| `Tree` 类型 | 保留共用不拆类型（行为冻结）；`isEdited`/`originalTemplateContent` 两字段加注释标注**归属域=edit-tabs**，为 T4 后「会话态字段拆扩展类型」留锚点 |
| `findByIdFromTree` / `getFullPathById` / `buildTree` / `buildFileList` / `joinFileName` / `getIcon` | 全部不动（已是纯函数层，符合分层） |

`getIcon` 的 `imageTypeList` 参数化注入设计（评审 M 轮产物）保留。

## 风险与开放问题

| # | 项 | 说明 | 建议 |
|---|----|------|------|
| K1 | 删除链路串 `handleCloseTabs` 弹窗 | 对照期登记缺陷，本次搬运不修 | 原样迁移，缺陷行号在文档留档更新 |
| K2 | 确认框「X」不摘幽灵 tab | 同上 | 原样迁移 |
| K3 | 多脏 tab 逐个弹确认 | 同上（`forEach handleTabRemove`） | 原样迁移 |
| K6 | 「否(N)」丢弃不回滚内容 | 对照期登记缺陷；**2026-11-25 复核定调：v2 侧已修（`discardEdits`），v1 冻结不动 → 该点 v1/v2 行为分叉** | 见「评审修正记录」K6 处置 |
| Q1 | `initData` 与 `loadTreeData` 现状实现逐字相同（差异是前者内部调 `refreshTabData`）；改造后 loadTreeData 保留，initData 变为薄包装或合并为一个 | 属接口重塑决策 | 倾向：合并为 `fetchTreeData(): Promise<Tree[]>`，两个现名由 facade 别名保留 |
| Q2 | facade 的 `state` 合并对象如何生成：保留单一 reactive（域直接写它，牺牲域自治）vs 三切片 + 编排层浅暴露 | 影响 reset 工厂与消费方模板 | 倾向：三切片各自 reactive，编排层 `state` 用 `computed` 属性 getter 聚合只读视图——消费方模板绑定零改动 |
| Q3 | `imageTypeList` 归 tree-data（图标是节点属性）还是提为 tree-utils 常量 | 微小归属分歧 | 倾向：常量移出 reactive——清单从不运行期变更 |

## 文件清单（改造后 v2 状态层全貌）

| 文件 | 动作 | 职责一句话 |
|------|------|-----------|
| `use-template-tree-data.ts` | **新建** | `treeList` 数据主权 + 树读接口装载 |
| `use-template-edit-tabs.ts` | **新建** | `tabList/activeItemId/expandedKeys` + 红点与会话标记 + 纯数据 tab 动作 |
| `use-template-persistence.ts` | **新建** | 内容写接口唯一收口 + 测试回刷 + submitLoading |
| `use-template-tree-state.ts` | **改造** | 组装三域 + MessageBox/Message 交互编排 + 对外 facade（导出名全保留） |
| `tree-utils.ts` | **注释级微调** | Tree 两字段归属标注；（若 Q3 定稿）+`IMAGE_TYPE_LIST` 常量 |
| `tree-v2.vue` / `tree-panel.vue` / `editor-panel.vue` | **不动**（Q3 若定稿移出常量则三处 import 微调） | facade 零变化约束（决策 5） |
| `tree.vue`（v1） | ~~**不动**~~ → **已删除**（2026-11-25，见「v1 删除与对照期结束」） | 对照期已提前终止 |
| `test.vue` / `form.vue` | **不动**（仅更新指向已删文件的失效注释） | — |

## 实施顺序建议（供 writing-plans）

1. 新建 tree-data（含切片工厂与单测可行性最高的纯部分）；
2. 新建 edit-tabs（含 watch 前移，行为等价论证逐条对照现 watch 三分支）；
3. 新建 persistence（batchSave 上收为唯一新增结构点，需重点对照落库参数）；
4. 改写编排层 facade（导出名/语义核对清单 = 迁移对照表全表）；
5. 回归：按功能基线清单中 F06/F14/F19/F22（红点）、F08（删除）、F05（保存）、F30（状态机 Suite S）相关项双跑对照；
6. 静态门禁：`vue-tsc` + ESLint（func-style 函数表达式约束）全绿。

## 实施与验证记录（2026-11-25）

### Q1-Q3 定稿

- **Q1 采纳倾向**：数据域内合并为 `fetchTreeData(): Promise<Tree[]>`；facade 保留 `loadTreeData`（直通）与 `initData`（fetch + refreshTabData 薄包装）两名，消费方零感知。
- **Q2 采纳倾向**：三切片各自 reactive，编排层 `state` 用 getter **+ 透传 setter** 聚合（activeItemId 有 v-model/手工写，treeList/tabList/expandedKeys 存在整体赋值点，纯只读 getter 不够）；存储仍按域分权，编排层自身零存储。
- **Q3 采纳倾向**：`IMAGE_TYPE_LIST` 提为 tree-utils 常量；消费点改动实际只有 editor-panel 一处（tree-v2/tree-panel 本就经 getIcon 间接使用，未动）。

### 实施偏差登记（相对设计文档契约）

- `activeTabItem` 实为 `ComputedRef<Tree>`（仅属性写无 ref 本体写，显式 Writable 标注被 vue-tsc 拒）；
- `persistence.reset` 额外清 `submitLoading`（现版 reset 不清），属安往全方向的对齐，非行为回归；
- 单文件保存的成功提示时序微调：先落红点回滚再弹「保存成功」（现版同帧内顺序相反），功能等价；
- 会话域 content-watch 取值源用 tab 引用项（决策 4 既定口径），依赖 F06 SAME_REF 与树侧同对象等价。

### 验证结果

- 静态：`vue-tsc --noEmit` 零错；IDE 诊断零错；tree-utils 单测 25/25 通过。
- 浏览器实测（Docker MySQL 故障，走 MSW 纯 mock，临时 handlers 见 `src/mocks/handlers/template.ts`，后端恢复后在 index.ts 注释切回）：
  - 首开装载：树 6 节点、首项 tab 自动激活、路径栏、变量徽标计数、保存钮置灰均正常；
  - 展开/收起：箭头态 + `icon-folder ↔ icon-folder-open` 切换 + 子层显隐往返正常；
  - 激活切换：点节点新增 tab 并激活、点 tab 回切、二进制图片预览分支正常；
  - 右键菜单：文件节点「修改」、目录节点「修改/新建目录/新建模板文件/新建二进制文件」且新建弹窗可开；tab 六项菜单齐全，「关闭其他」纯数据动作生效且无脏不弹确认；
  - 脏关闭链：编辑器输入→watch 侦测点亮保存钮→点 × 弹三态确认→「是(Y)」走 batchSave→「保存成功」→tab 关闭；
  - 重开抽屉全新状态（per-slice reset 跨域生效）；控制台无错误（仅既有 ElLoading svg warn）。
- 待补：真后端就绪后按功能基线清单 F06/F14/F19/F22/F08/F30 与 v1 双跑对照（本次 mock 验证覆盖不了库落盘与接口参数口径）。
- 2026-11-25 真后端复测（MySQL 恢复，mock 已停用）：F05 保存落盘链路通过——脏标记→三态确认→updateContent 写库 detail 实测 6214→6231 字节→tab 异步关闭→红点清除，测试数据已还原并逐字校验一致；首开装载/展开收起/激活切换/右键菜单（文件修改、目录四项、tab 五项）/重开复位均走真接口通过；控制台零 error（仅存量 ElLoading innerHTML warn）。

### v1/v2 双跑对照（2026-11-25，试验组 80「单体java项目模板_复制」，真后端）

方法：同一份探针脚本仅改入口按钮文案（v2=`模板v2`，v1=`模板`），逐功能项采集行为指纹串对比。结论：**v2′（拆分后）与 v1（冻结基准）行为全等，含三个共有缺陷 K2/K6 同构复现，无回归差异。**

| 功能项 | v2′ 实测 | v1 实测 | 判定 |
|--------|---------|---------|------|
| F06 红点·编辑同帧 | 输入后树红点 + tab 红点同帧亮、保存钮点亮 | 同 | 全等 |
| F06 红点·还原同帧灭 | 删标记回原文后红点同帧灭（CM 文本节点合并噪声不影响判定） | 同 | 全等 |
| F06 保存后基线推进 | 保存后 originalTemplateContent 推进，再插再退红点仍亮（符合语义） | 同四步串逐字全等 | 全等 |
| F14 无脏直关 | 干净 tab 点 × 不弹框直接关 | 同 | 全等 |
| F14 关闭回落规则 | 关中间→右侧优先激活、关末尾→回落左侧 | 同 | 全等 |
| F14 脏关闭点 ×(K2) | 弹框消失但 tab 保留、红点仍在 | 同（tabStill=true） | 共有缺陷复现 |
| F14 否(N)丢弃(K6) | tab 关闭但树红点残留、重开带脏 | 同（tabs=0 + treeDot 残留） | 共有缺陷复现 |
| F16 tab 右键菜单显隐 | 单 tab 全脏仅「关闭当前」；全脏时「关闭未改」隐藏 | 同场景同为「关闭当前」 | 全等（早期「四项 vs 一项」经复测确认为 tab 数不同，非差异） |
| F16 关闭全部含脏 | 弹框文案「1111.java、BaseUserVO.java模板文件已修改未保存，是否保存后再关闭？」→是(Y)→updateContentBatch 落库→tabs=0/treeDots=0 | 同文案同落库 | 全等 |
| F02/F03 抽屉关闭三态 | × 不关、否(N) 关、重开复位（K6 脏被 init 重拉掩盖） | 同 | 全等 |
| F08 删除链 | 级联勾选 3 节点→确认文案「此操作将删除目录或者文件, 是否继续?」→deleteList 请求体父在前→树移除 + 对应 tab 联动关闭 + 删除成功 toast | 同构，deleteList=[900129,900130] | 全等 |

数据复原自证：1111.java(2891)=基线、BaseUserVO.java(2142)=868 字节、pom.xml(2118)=6214 字节，三文件 REGEXP 探测测试标记均 `has_mark=0`；夹具 900126–900130 已物理清理（`remaining=0`）。注：探针造夹具经 deleteList 走 UI 删除后仍残留库中，收尾用 SQL 直删兜底确认。

## 评审修正记录（2026-11-25，独立复核后）

### B1：装载与刷 tab 之间的 microtask 间隙导致未保存内容静默丢失（已修）

- **现象链**：`initData` 拆成「`await fetchTreeData()` → `refreshTabData`」后，`treeList` 整表替换与 `tabList` 重指向之间多了一次 await。`activeTab` 走 treeList 反查，content-watch（`flush: 'pre'`）在下一个 microtask 就 flush，此时 tab 侧仍是旧对象，于是 `content(新树=DB 原文) === tab.originalTemplateContent(DB 基线)` 成立 → 旧 tab 的 `isEdited` 被抹掉 → `refreshTabData` 的 `editStatusSet` 收集不到脏项 → 未保存内容被库内原文覆盖，**无报错无提示**。HEAD 版两个动作在同一同步段内完成，不存在该窗口。
- **触发入口**：`tree-panel.vue` L215（批量删除后）/ L283（拖拽改父级后）/ L374（新增·修改表单保存后），即“边改边整理目录结构”这一常规工作流。
- **修法**：`fetchTreeData(onLoaded?)` 增加**同步**回调，在「换树 → buildTree → buildFileList」同一同步段内调完；编排层 `initData = () => treeData.fetchTreeData(editTabs.refreshTabData)`，`loadTreeData` 不传回调（首开 tabList 为空）。数据域仍只认泛化函数，不 import 会话域，主权边界未破。
- **运行时双证**（组 80 / 2891 `1111.java`，全程只走 `GET /gen/template/tree`，零写接口）：
  - 复现旧次序（`loadTreeData()` 后再 `refreshTabData(files)`）：置脏 176 字节→**树替换后 `isEdited` 已翻 false**→刷 tab 后内容回落 **165 字节（标记丢失）**；
  - 修复后（`initData()`）：置脏 183 字节→`initData()` 后仍 **183 字节且 `isEdited` 仍 true**，同时 `activeTabItem.value === tabList 项` 引用同一性保持；收尾已将内存态还原为 165/未脏、保存钮回置 disabled。
- **新增不变量**（后续任何改动必须守住）：**「treeList 整表替换」与「tabList 重指向」之间不得出现 microtask 间隙**；锚点注释已分别落在 `use-template-tree-data.ts`、`use-template-tree-state.ts`、`use-template-edit-tabs.ts`。

### 注释纠偏（B1 的成因性误导）

- `activeTab` 原注释称「取值源为 tab 引用项」不实：实为 `getNode` → 遍历 `treeList` 反查（保留树侧反查是为了树上的内容变更也能驱动侦测，以及 `fullFilePath` 随 `parentId` 变化）；
- content-watch 原注释「SAME_REF 已证故与旧版等价」论证不成立：同一性只在树未重载的稳态成立，装载中间态是两批对象。

两处注释已改写为如实口径并指向上面的原子性不变量。

### K6 处置：修复保留（本轮定调）

`handleCloseTabs` 的「否(N)」分支新增 `discardEdits`（内容回滚到基线 + 清红点；多 tab 一起丢弃时必须逐个显式回滚，watch 只观察当前激活项）与 info 提示，`handleClose` 的 cancel 分支补 info 提示——**经确认按正确行为保留，不回退**。由此：

- 上方双跑表「F14 否(N)丢弃(K6)｜共有缺陷复现」是修复落地**之前**测得的历史记录，对最终代码不再成立；
- K6 在 v2 已修、v1 仍保留该缺陷 → v1/v2 在这一点行为分叉，T3 切流前若继续拿 v1 当行为基准，该行需重测。

### 其他

- **B2**：`src/mocks/handlers/template.ts` 两个保存 handler 的 `request.json()` 缺请求体类型（4 个 TS 错）→ 加 `ContentPayload` 与 `readJson<T>` 断言助手。注：`_shared` 的 `http` 是 Proxy 包装（返回类型退化为 `(url, ...args: any[])`），走不了 `http.put<Params, Body>` 泛型，所以只能对 `json()` 结果做断言。同文件的节点写接口（updateParent/add/update/delete/deleteList）仍是**空成功占位**，已在文件内注明不能用作「节点操作后刷新」类场景的取证依据。
- facade 成员数 24 → 23（移除 `imageTypeList`，改由 `editor-panel` 直接 import `IMAGE_TYPE_LIST` 常量），唯一消费点已同步迁移，无断链。
- 静态门禁：`vue-tsc --noEmit` 零错、既有单测 79/79（5 文件）通过。**注：本行原写「ESLint 零问题」不实**——`src/components/crud/SearchForm.vue:68` 的 `rangeFields` unused-var error 是存量问题（该文件自 `ece0ad10` 未改动，`ece0ad10` 是 `b67bb8a1` 的祖先提交），与本次拆分无关，详见「v1 删除与对照期结束」。
- 三个新 composable 仍无单测；B1 这类跨域时序问题只有「脏态下调 `initData` + `await`」这一条用例能静态拦住（断言前的 `await` 正是给 flush 让路，旧代码会红、新代码会绿），尚未补。
- 本轮已改：`persistence.reset` 原注释称「保持空实现不触 submitLoading」与函数体矛盾，已改写为如实说明。
- 保留不回退的行为差异：`saveActive` 的落标记对象由「响应时的激活项」改为「请求发起时捕获的项」。现版在请求期间切 tab 会把红点清到新激活项上（误清未保存标记），新版更安全，属有意改进，不归入行为回归。

## v1 删除与对照期结束（2026-11-25）

### 改动

- 删除 `src/views/gen/template/tree.vue`（1509 行）。
- `template-group/index.vue`：移除 v1 按钮、v1 弹窗块、v1 import、`treeV2UpdateRef`、`treeDataV2()`；操作列由「v1 + v2 双入口三行」重排为 2×2（模板 / 属性 / 修改 / 更多）；剩余弹窗统一用 `<template-tree>` 标签，`import TemplateTree` 现指向 `tree-v2.vue`，ref 名与 `treeData()` 沿用不带版本号的写法。
- 本轮只删不改名：`tree-v2.vue` 文件名、`GenTemplateTreeV2` 组件名、`use-template-*` 三文件均保持原状。**该口径已定**：入口层（index.vue 的标签名、ref、方法名与按钮文案）已统一为不带版本号的写法，文件与组件名里的 v2 后缀仅作历史来源标记保留，不再跟进重命名。
- 注释纠偏（均为指向已删文件的失效说明）：`tree-utils.ts`（删「v1 tree.vue 冻结不动」一行）、`form.vue`（宿主 v1 tree.vue → `tree-panel.vue` / `test.vue`）、`test.vue`（两处 → `tree-v2.vue`）、`panel-body.vue`（宿主 → `tree-v2.vue` / `test.vue`，并删去与 v1 `display` 的对比措辞）、`use-variable-catalog.test.ts`、`use-variable-panel-size.ts`（含「对齐 v1 init」）、`tree-v2.vue`（抽屉全局样式注释由「不再依赖 v1 注入」改为直接说明为何是非 scoped 块）。

### 依赖核查（删除是否留孤儿）

- `tree.vue` 的唯一引用点是 `template-group/index.vue`；无路由、无单测、无异步组件引用。
- v1 自身依赖（CodeMirror、`form.vue`、TextTooltip、SvgIcon、`test.vue`、variable-panel、`tree-utils`、`use-variable-catalog`、`countVariables`、`LOADING_MASK_OPTIONS`）全部为 v2 与 test.vue 共用件，删后无孤儿文件。
- 样式：v1 的非 scoped 块只有一条 `.template-tree-drawer-body { padding: 10px 20px 20px }`，`tree-v2.vue` 已自带逐字副本（拆分时就为这一步预埋）；其余均为 scoped，不外泄。

### 验证

- `vue-tsc --noEmit` 零错；既有单测 79/79（5 文件）通过。
- ESLint 剩 1 error 为**存量**、与本轮无关：`components/crud/SearchForm.vue:68` `rangeFields` is defined but never used。已核实该文件自 `ece0ad10` 起未改动，且 `ece0ad10` 是 `b67bb8a1` 的祖先提交，故上一节「ESLint 零问题」的表述不实，已就地更正。本轮未顺手修，以免把无关改动混进删除提交。
- **工具约束提醒**：`tsconfig` 未开 `vueCompilerOptions.strictTemplates`，vue-tsc 不校验模板标签与组件的映射。本轮这类「标签名与 import 别名不一致 / 闭合标签残留」错误静态检查看不住，必须靠运行时冒烟兜底。
- 运行时冒烟（dev 5000 + 真后端 8888，组 80）：操作列按钮实测为「模板 / 属性 / 修改 / 更多」；点「模板」打开抽屉「模板配置（单体java项目模板_复制）」，DOM 内实例为 `GenTemplateTreeV2` 且 treeState 就绪（树 23 节点、首项 tab 自动激活）；全 DOM 扫描 v1 组件名 `GenTemplateTree` 命中 0；`.template-tree-drawer-body` 计算样式实测 `10px 20px 20px 20px`；控制台 0 error。网络仅 `POST gen/templateGroup/page` + `GET gen/template/tree` + `GET gen/template/variableCatalog`，零写接口。

### 对照期终止与影响

- v1/v2 双跑对照通道永久消失：T2 的探针脚本（`docs/superpowers/specs/2026-11-24-t2-suite-*.js`、`_run-suite-H.js`）靠 `window.__BTN = '模板'` 打开 v1，现已无入口，v1 侧不可再跑；后续回归只能在 v2 单侧做绝对验证。
- 风险表 K6 行「v1 仍保留该缺陷 → v1/v2 行为分叉」失效：基准侧已不存在，分叉不再可比，v2 的修复即最终行为。
- 上一节的双跑表（F02/F03/F05/F06/F08/F14/F16）作为拆分时的等价性证据保留，但不再具备「随时复跑」能力。
- 原计划 T3（切流）与 T4（删 v1 + 全量回归）合并落地为本次删除，切流默认口径直接落到 v2。
- 未补的跨域时序回归用例（脏态下调 `initData`）现在更没有对照基准，价值反而更高，建议下一步补上。
