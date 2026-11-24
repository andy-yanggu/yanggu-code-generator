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
| `initData` | L138-L145 | tree-data | 原样；`refreshTabData` 调用上移到消费方（决策 3 通道②） |
| `imageTypeList` | L32 | 倾向移 `tree-utils.ts` 为 `IMAGE_TYPE_LIST` 常量（Q3） | 若移：tree-v2/tree-panel/editor-panel 三处消费点同步改 |
| `activeTabItem` | L35-L42 | edit-tabs（`activeTab`），facade 以同名导出包装 | 占位对象回落语义不变 |
| `fullFilePath` | L44-L49 | 编排层（`getFullPath(activeTab.id)`） | 依赖建立写法不变 |
| `canInsertVariable` | L52 | 编排层 | 表达式不变 |
| `getIcon` / `resolveNodeIcon` 绑定 | L55 | 编排层（绑定清单来源） | 不变 |
| `getElementFromTabList` | L57-L63 | edit-tabs | 原样 |
| `tabPush` | L66-L75 | edit-tabs | 原样 |
| `refreshTabData` | L77-L106 | edit-tabs | 原样（含 splice 换引用 + 编辑态保留） |
| content-watch | L109-L135 | edit-tabs | 取值源从 tree 反查改为 tab 引用（决策 4，行为等价论证见该节） |
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
| K6 | 「否(N)」丢弃不回滚内容 | 同上 | 原样迁移 |
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
| `tree.vue`（v1）/ `test.vue` / `form.vue` | **不动** | 对照期冻结 |

## 实施顺序建议（供 writing-plans）

1. 新建 tree-data（含切片工厂与单测可行性最高的纯部分）；
2. 新建 edit-tabs（含 watch 前移，行为等价论证逐条对照现 watch 三分支）；
3. 新建 persistence（batchSave 上收为唯一新增结构点，需重点对照落库参数）；
4. 改写编排层 facade（导出名/语义核对清单 = 迁移对照表全表）；
5. 回归：按功能基线清单中 F06/F14/F19/F22（红点）、F08（删除）、F05（保存）、F30（状态机 Suite S）相关项双跑对照；
6. 静态门禁：`vue-tsc` + ESLint（func-style 函数表达式约束）全绿。
