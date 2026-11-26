# 模板内容回滚功能设计（v2 树形批量 + tab 单个）

## 概述

在模板配置抽屉（v2：`tree-v2.vue` + `tree-panel.vue` + `editor-panel.vue` + `use-template-tree-state.ts` 域体系）新增**模板内容回滚**能力：

- **树形侧批量回滚**：勾选目录/文件后一键回滚勾选范围内的全部脏文件（目录连带后代文件）；
- **树形侧单个回滚**：文件节点右键菜单增加「回滚」项；
- **tab 侧单个回滚**：tab 右键菜单顶部增加「回滚」项，仅脏 tab 显示。

回滚基线 = **内存基线 `originalTemplateContent`**：即打开抽屉装载时的库内内容，或本次会话内最近一次保存成功后的内容。回滚是纯前端内存操作，**零接口调用**；执行前统一弹 `ElMessageBox.confirm` 确认。

范围口径延续对照期约定：**仅动 v2 文件集，v1 `tree.vue` 全程冻结**。本文件是设计文档（spec），实施计划另出。

## 现状盘点

### 已具备的机制（本设计直接复用，不新造）

| 机制 | 位置 | 与本次的关系 |
|------|------|-------------|
| 内存基线字段 `originalTemplateContent` | `tree-utils.ts` Tree 接口；`buildTree` 装载时初始化；`applyEditRollback` 保存成功后推进 | 回滚的目标版本就是它，无需新增字段 |
| 丢弃编辑 `discardEdits(tabs)` | `use-template-edit-tabs.ts`：`templateContent = originalTemplateContent` + `isEdited = false`，**不推进基线** | 回滚的落标记动作与它语义完全一致，直接复用 |
| 保存成功推进基线 `applyEditRollback(tabs)` | 同上：`originalTemplateContent = templateContent` + 清红点 | 与 discardEdits 方向相反，二者边界见决策 2 |
| 脏红点双向同步 | tabList 元素与 treeList 节点同一批对象引用（F06 SAME_REF） | mutate 一次，tab 与树上的红点同时熄灭 |
| 文件名提示折叠 `joinFileName` | `tree-utils.ts`：超 3 个折叠为「前 3 个...等 N 个模板文件」 | 回滚确认文案复用同一口径 |
| 通用右键菜单 `ContextMenu` | `components/context-menu`，items 声明式 | 两个新菜单项零组件成本 |
| 勾选收集 `getCheckedKeys` | tree-panel 删除链路 | 批量回滚沿用同一勾选口径 |

### 现有关闭确认中「否(N)」的区别

关 tab / 关抽屉确认选「否(N)」同样走 `discardEdits` 丢弃未保存内容，但它是**退出路径的副产品**（内容回基线 + tab 摘除 / 抽屉关闭）。本次的回滚是**操作路径**：文件保持打开、tab 不关闭、编辑区可见地回到基线，用户可继续编辑。两者共用底层动作、入口与提示文案分开，不互相改造。

## 设计目标与非目标

**目标**

- 三个入口（树工具栏批量、树节点右键单个、tab 右键单个）共用同一条「收集脏文件 → 确认弹窗 → 执行回滚 → 结果提示」编排链。
- 弹窗与提示一律收在编排层 `use-template-tree-state.ts`，符合状态域拆分设计「数据域零弹窗」边界；tree-panel / editor-panel 只加按钮和菜单项声明。
- 会话域零改动：`discardEdits` 原样复用，不新增标记通道。
- 回滚当前激活文件时编辑区内容可见地刷新（CodeMirror v-model 由 `activeTabItem.templateContent` 的 mutate 驱动）。

**非目标**

- 不做数据库版本回滚（不存历史版本、不调 `detailData` 拉库内最新，见决策 1）。
- 不改「否(N)」关闭链路的现有行为，不修删除链路已登记缺陷（K 系）。
- 不动 v1 `tree.vue`；不回滚节点结构类操作（新增/删除/拖拽移动/重命名不可回滚，见决策 5）。
- 不提供撤销栈（Ctrl+Z 级别的多次回退），回滚只有"一步到基线"。

## 设计决策

### 决策 1：基线取内存 `originalTemplateContent`，不取数据库当前版本

- 用户明确选定：回滚前 el-confirm 提示、回滚到内存状态。
- 语义诚实：按钮叫「回滚」，实现是"丢弃未保存修改"。已保存进库的内容回不来——要回已保存的版本，唯一路径是「测试页回刷」或重新编辑，本功能不冒充版本管理。
- 备选（否决）：`detailData` 拉库内最新覆盖（等价 `refreshAfterTest` 链路）。否决理由：需要逐文件发请求、结果依赖在会话期间是否有人改库，行为不确定；且与"内存基线"的用户预期不符。

### 决策 2：复用 `discardEdits`，不新增会话域动作，禁止与 `applyEditRollback` 混用

- `discardEdits`：内容回到基线、**基线不动**（回滚/丢弃语义）。
- `applyEditRollback`：基线前进到当前内容、清红点（保存成功语义）。
- 两者方向相反，回滚若误用后者等于"把未保存内容确认为新基线"，是反向操作。实施时以注释锚定，不改两函数任何一行。
- 必须显式逐个回滚全部目标节点：content-watch 只观察当前激活 tab，批量回滚中的非激活节点不会自动清标记（既有评审结论，勿依赖 watch）。

### 决策 3：批量范围 = 勾选节点 + 目录后代文件，再过滤 `isEdited`

- 与工具栏「删除」的勾选口径一致（`getCheckedKeys`，勾目录自动连带其后代），用户不需要学第二套勾选语义。
- 实现复用 `handleClose` 里"递归扫树收集 isEdited 节点"的写法：对每个勾选 key 反查节点后向下扫描收集脏文件；父目录与其子文件同时勾选时按 id 去重。
- 干净文件不弹、不提示、不 mutate——确认框只列**待回滚集合内**的脏文件，与关闭确认的文案边界口径一致。

### 决策 4：入口布局——工具栏按钮 + 两处右键菜单项，脏态控制显隐/禁用

| 入口 | 位置 | 显隐/禁用规则 | 说明 |
|------|------|--------------|------|
| 批量回滚 | tree-panel 工具栏，位于「刷新」与「删除」之间 | `checkedNodeListLength === 0` 时 disabled | 与删除按钮同口径常驻可见，warning 色 + 回退图标 |
| 单个回滚（树） | treeMenuItems，排在「修改」之后 | `t.kind === 'treeNode' && t.node.templateType === 1 && t.node.isEdited` | 目录、二进制、干净文件不显示（它们不可能脏） |
| 单个回滚（tab） | tabMenuItems 数组顶部 | `t.item.isEdited` | 置于关闭系列之前，突出"救火"动作属性 |

- 备选（否决）：仅右键菜单不加工具栏按钮——右键点击不改变勾选集合，菜单里操作"勾选范围"与用户直觉相左，且批量入口不可见难以发现。
- tree 右键对**干净文件/目录/二进制**不显示回滚项（而非 disabled），理由与 tab 菜单「关闭未改」按 `some` 条件隐藏同理：树上无可回滚对象时该动作不存在。

### 决策 5：回滚只覆盖内容，不覆盖结构操作

新增、删除、拖拽移动、重命名都即时落库，无事务可回退。回滚动作只 mutate `templateContent`，确认文案必须写明「此操作不可恢复」，避免用户把"回滚"理解成对整个会话的撤销。

### 决策 6：空集合与全干净的处理

点击批量回滚但勾选范围内没有任何脏文件时，不弹确认框（弹了也只能是空文件名列表），直接 `ElMessage.info('勾选范围内没有未保存的修改')`。单个入口因菜单项按 `isEdited` 显隐，天然不可达，无需兜底。

## 接口契约

### tree-utils.ts 新增纯函数（可单测，不依赖响应式）

```typescript
// 从勾选 key 收集待回滚脏文件：反查节点 -> 目录递归后代文件 / 文件直接入集合 -> 按 id 去重 -> 过滤 isEdited
// findById 经参数注入，保持本模块纯函数惯例
export const collectDirtyFiles = (checkedKeys: number[], findById: (id: number) => Tree | undefined): Tree[] => {
	const map = new Map<number, Tree>()
	const walk = (node: Tree) => {
		if (node.templateType !== 0) {
			if (node.isEdited) {
				map.set(node.id, node)
			}
		} else {
			node.children?.forEach(walk)
		}
	}
	checkedKeys.forEach(key => {
		const node = findById(key)
		if (node) {
			walk(node)
		}
	})
	return [...map.values()]
}
```

### 编排层新增（use-template-tree-state.ts，弹窗唯一所在地）

```typescript
// 单个回滚：树节点右键 / tab 右键共用（tab 元素与树节点同一引用，无需区分来源）
const rollbackNode = (node: Tree) => {
	// 双保险守卫：非文本模板或未编辑直接 return（菜单显隐已保证不可达）
	if (node.templateType !== 1 || !node.isEdited) {
		return
	}
	confirmAndRollback([node])
}

// 批量回滚：入参为 getCheckedKeys 的勾选 key 列表
const rollbackCheckedNodes = (checkedKeys: number[]) => {
	const dirtyNodes = collectDirtyFiles(checkedKeys, treeData.findById)
	if (dirtyNodes.length === 0) {
		ElMessage.info('勾选范围内没有未保存的修改')
		return
	}
	confirmAndRollback(dirtyNodes)
}
// 共用编排链：确认 → 回滚 → 提示（handleCloseTabs/handleClose 的既有链不动）
const confirmAndRollback = (dirtyNodes: Tree[]) => {
	ElMessageBox.confirm(
		`${joinFileName(dirtyNodes)}存在未保存的修改，回滚将丢弃这些修改并恢复到上次保存（或打开时）的内容，此操作不可恢复，确定回滚吗？`,
		'提示',
		{ confirmButtonText: '确定回滚', cancelButtonText: '取消', type: 'warning' }
	)
		.then(() => {
			editTabs.discardEdits(dirtyNodes)
			ElMessage.success('已回滚到上次保存的内容')
		})
		.catch(() => {
			ElMessage.info('回滚已取消')
		})
}
```

facade 追加导出：`rollbackNode`、`rollbackCheckedNodes`（命名与现有 `closeXxxTabs` 家族风格对齐）。

### 域文件改动面

| 域 | 改动 |
|----|------|
| use-template-edit-tabs.ts（会话域） | **零改动**，复用 `discardEdits` |
| use-template-tree-data.ts（数据域） | **零改动**，编排层用现有 `treeData.findById` |
| use-template-persistence.ts（持久化域） | **零改动**，回滚不经任何写接口 |
| tree-utils.ts | 仅新增纯函数 `collectDirtyFiles`（见上），既有函数零改动 |

### 组件层改动点

**tree-panel.vue**

1. 工具栏「刷新」与「删除」之间加按钮（解构追加 `rollbackCheckedNodes`）：

```html
<el-button size="small" type="warning" :icon="RefreshLeft" :disabled="checkedNodeListLength === 0" @click="rollbackCheckedNodes(treeRef.getCheckedKeys())"> 回滚 </el-button>
```

2. `treeMenuItems` 在 `edit` 项之后追加：

```typescript
{
	key: 'rollback',
	label: '回滚',
	icon: 'icon-rollback', // 若无现成 svg，退化为 EP 图标 RefreshLeft（与按钮同源，避免造空图标）
	visible: t => t.kind === 'treeNode' && t.node.templateType === 1 && !!t.node.isEdited,
	onClick: t => {
		if (t.kind === 'treeNode') {
			rollbackNode(t.node)
		}
	}
}
```

**editor-panel.vue**

`tabMenuItems` 数组顶部追加（解构追加 `rollbackNode`）：

```typescript
{
	key: 'rollback',
	label: '回滚',
	icon: RefreshLeft,
	visible: t => !!t.item.isEdited,
	onClick: t => rollbackNode(t.item)
}
```

## 页面设计（纯文字线框）

### 1. 树面板工具栏（批量入口）

```
┌────────────────────────────────────────────────────────┐
│  [🔍 请输入目录/文件名称          ]  [刷新] [回滚] [删除] │
│                                        ↑ 新增            │
│ ─────────────────────────────────────────────────────── │
│  ▼ 📁 src                                                │
│     ● 📄 BaseUserVO.java        ← 红点=脏，回滚候选       │
│     ○ 📄 PageVO.java            ← 干净，不受影响          │
│  ▶ 📁 pom                                              │
└────────────────────────────────────────────────────────┘
```

- 「回滚」按钮：type=warning、图标 RefreshLeft；未勾选任何节点时 disabled（与「删除」完全同口径）；勾选含脏文件时 hover 提示可用。
- 按钮顺序：刷新 → 回滚 → 删除。回滚与删除相邻，都是"批量 + 需确认"的危险倾向操作，视觉上以 warning/danger 区分等级。

### 2. 树节点右键菜单

```
右键：脏文件节点 BaseUserVO.java          右键：目录 / 干净文件 / 二进制
┌──────────────┐                          ┌──────────────┐
│ 修改          │                          │ 修改          │
│ ▣ 回滚   ←新增│                          │ 新建目录      │
│ 新建目录      │                          │ 新建模板文件  │
│ 新建模板文件  │                          │ 新建二进制文件│
│ 新建二进制文件│                          └──────────────┘
└──────────────┘   回滚项不出现
```

### 3. tab 右键菜单

```
右键：脏 tab                              右键：干净 tab
┌──────────────┐                          ┌──────────────┐
│ ▣ 回滚  ←新增│                          │ 关闭当前      │
│ 关闭当前      │                          │ 关闭其他      │
│ 关闭其他      │                          │ ...          │
│ 关闭左侧      │                          └──────────────┘
│ 关闭右侧      │   回滚项不出现
│ 关闭未改      │
│ 关闭全部      │
└──────────────┘
```

### 4. 确认弹窗

批量（脏文件 > 3 个，`joinFileName` 折叠口径）：

```
┌──────────────── 提示 ─────────────────┐
│  ⚠                                     │
│  BaseUserVO.java、PageVO.java、        │
│  UserController.java...等5个模板文件     │
│  存在未保存的修改，回滚将丢弃这些修改并   │
│  恢复到上次保存（或打开时）的内容，此操   │
│  作不可恢复，确定回滚吗？               │
│                                        │
│              [ 取消 ]  [ 确定回滚 ]      │
└────────────────────────────────────────┘
```

单个：`XXX.java模板文件存在未保存的修改，回滚将丢弃…确定回滚吗？`（同一模板，仅文件名部分由 joinFileName 自然生成）。

- 主按钮文案用「确定回滚」而非默认「确定」，与关闭确认的是/否三态文案区分开，避免误点。
- 只有取消/确定两态（无 distinguishCancelAndClose）：关闭链的三态语义是"保存与否+是否继续关闭"，回滚没有第三态，点 X 等同取消。

### 5. 回滚前 / 回滚后状态对照

```
回滚前                                  回滚后
tab: [● BaseUserVO.java ✕] 红点亮       tab: [○ BaseUserVO.java ✕] 红点灭
树 : ● BaseUserVO.java     红点亮       树 : ○ BaseUserVO.java     红点灭
编辑区: 用户改过的内容                   编辑区: originalTemplateContent（可见刷新）
基线  : 打开时/上次保存的内容            基线  : 不变（仍可继续编辑产生新脏）
```

## 交互流程与边界

1. **批量回滚**：勾选 → 点「回滚」→（范围内无脏文件 → info 提示，结束）→ 确认框 → 确定 → `discardEdits` 逐个回滚 → 成功提示；取消 → info「回滚已取消」，状态零变化。
2. **单个回滚（树/tab）**：菜单项只对脏文件出现 → 确认框 → 同上执行链。tab 回滚**不关闭 tab**；树上回滚不影响任何 tab 开关状态。
3. **激活态刷新**：回滚集合含当前激活文件时，`activeTabItem.templateContent` 被 mutate → CodeMirror v-model 同步显示基线内容；非激活文件仅数据回滚，切换过去自然正确。
4. **勾选 stale key**：勾选后未刷新树又经删除等链路导致节点已不在树上 → `findById` 返回 undefined，静默过滤，不报错。
5. **二进制/目录**：无编辑通道不会变脏，所有入口按类型/脏态天然不可达。
6. **基线推进提示**：回滚到的"基线"可能是本次会话内已保存的内容（保存成功即推进基线），确认文案已声明"上次保存（或打开时）的内容"，不承诺回到数据库更早版本。
7. **`refreshTabData` 交叠**：回滚后若紧接着点「刷新」（全量重拉），内容以库为准，与回滚结果一致（回滚 = 丢弃未保存，库内本就未变），无冲突。

## 验证方案

- **静态**：`vue-tsc` 类型检查 + ESLint（注意 func-style 规则强制函数表达式）。
- **浏览器实测**（dev 前端 + 后端 Debug 实例，操作组 80 或指定测试组）：
  1. 造脏（CodeMirror 输入标记文本）→ 三个入口分别执行回滚 → 断言：树/tab 红点熄灭（el-badge 需 isVisible 过滤）、编辑区回到基线、内容无测试标记；
  2. 网络层断言回滚全程**零写接口请求**（无 PUT updateContent/updateContentList）；
  3. 边界：勾选中含干净文件只列脏文件、范围全干净走 info 提示不弹确认、取消弹窗状态零变化、非激活 tab 回滚后切换过去显示基线、回滚当前激活 tab 编辑区即时刷新；
  4. 基线推进场景：编辑 → 保存（基线前进）→ 再编辑 → 回滚 → 内容回到"保存后"版本而非打开时版本；
  5. 回归：关 tab「否(N)」丢弃、关抽屉确认链、删除链路行为不受影响（未动其代码，抽验即可）。
- **单测**：`collectDirtyFiles`（勾选 key + 目录后代 + id 去重 + stale key 过滤 + 干净文件排除）为纯函数，在 `tree-utils.test.ts` 补用例；弹窗编排链按既有测试口径以全链路实测为主。

## 偏差与后续登记

- v1 `tree.vue` 对照期冻结不加回滚，回滚将成为 v1/v2 的**第二处预期行为差异**（第一处为「否(N) 丢弃回滚」），双跑差分时需显式排除，与既有登记口径一致。
- 若图标库无现成 rollback 类 svg，树菜单项与两处按钮统一退化为 EP `RefreshLeft`，不新造图标文件。
