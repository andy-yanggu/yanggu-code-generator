# 模板内容回滚 实施计划

> **补记说明**：本功能已于 commit `b3a665e1` 交付（BASE = `a6e6d586`）。本 plan 是交付后按 spec 权威补记的中间产物；复选框反映**实际落地状态**，`[ ]` 即未交付项。偏差与实测证据见 `.superpowers/sdd/2026-11-25-template-content-rollback/review.md`。
>
> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在模板配置抽屉 v2 提供 3 个回滚入口（树工具栏批量、树节点右键单个、tab 右键单个），把脏文件的 `templateContent` 恢复到内存基线 `originalTemplateContent`，执行后红点双向熄灭、基线不推进、全程零写接口调用。

**Architecture:** 纯函数 `collectDirtyFiles` 负责"勾选范围 → 脏文件集合"（`tree-utils.ts`，零依赖可单测）；编排层 `use-template-tree-state.ts` 独占弹窗与提示（`confirmAndRollback` 共用链）；两个组件只加按钮与菜单项声明。会话域复用既有 `discardEdits`，**零改动**。

**Tech Stack:** Vue 3.5 / TypeScript 5.8 / Element Plus 2.11 / vitest 4（node 环境）/ MSW 2.15（P1 债务用）

**Spec:** `docs/superpowers/specs/2026-11-26-template-content-rollback-spec.md`（验收真相源；设计推导见 `2026-11-25-template-content-rollback-design.md`）

## Global Constraints

以下约束来自 spec §3，逐条约束每个任务：

- **v1 已不存在**：`tree.vue` 已于 `4a27750b` 删除，不得复活、不得引用 v1/v2 双跑对照（AC-24）。
- **数据域零弹窗**：弹窗/提示只允许出现在编排层 `use-template-tree-state.ts`（AC-25）。
- **纯函数惯例**：`tree-utils.ts` 保持零 import；需要外部查询时用参数注入（如 `findById`）（AC-26）。
- **禁止误用 `applyEditRollback`**：它是"保存成功推进基线"，与 `discardEdits`（"回滚不推进基线"）方向相反（AC-13）。
- **ESLint `func-style: ['error','expression']`**：只能用 `const fn = () => {}` 写法。
- **vitest `environment: 'node'`**：组件级测试须在文件顶部加 `// @vitest-environment jsdom`，且需先新增 `@vue/test-utils` + `jsdom`（当前 package.json 均未安装）。
- **前端无 Playwright**：UI 级 AC 必须成对拆分「逻辑层（unit）」+「表现层（manual/待自动化）」（AC-13/14）。
- **零接口调用**：不调 `detailData`，不发 `PUT updateContent` / `updateContentList`。
- **不改既有链路**：`handleCloseTabs` / `handleClose` 一行不动；会话域零改动。

## File Structure

| 文件 | 动作 | 职责 |
|---|---|---|
| `yanggu-code-generator-frontend/src/views/gen/template/tree-utils.ts` | 修改：新增 1 个纯函数 | 勾选 key → 待回滚脏文件集合 |
| `yanggu-code-generator-frontend/src/views/gen/template/use-template-tree-state.ts` | 修改：新增 3 个函数 + facade 导出 | 确认 → 回滚 → 提示编排（弹窗唯一所在地） |
| `yanggu-code-generator-frontend/src/views/gen/template/tree-panel.vue` | 修改：工具栏按钮 + 树右键菜单项 | 批量入口 + 树侧单入口 |
| `yanggu-code-generator-frontend/src/views/gen/template/editor-panel.vue` | 修改：tab 右键菜单项 | tab 侧单入口 |
| `yanggu-code-generator-frontend/src/views/gen/template/tree-utils.test.ts` | 修改：新增 7 用例 | `collectDirtyFiles` 单测 |
| `yanggu-code-generator-frontend/src/views/gen/template/use-template-edit-tabs.test.ts` | **新建（未交付）** | AC-12/13/15/16，含 2 条 P0 |
| `yanggu-code-generator-frontend/src/views/gen/template/menu-visibility.test.ts` | **新建（未交付）** | AC-07/08 显隐谓词 |
| `yanggu-code-generator-frontend/src/views/gen/template/rollback-net.test.ts` | **新建（未交付，P1 债务）** | AC-23 MSW 零请求断言 |
| `use-template-edit-tabs.ts` / `use-template-tree-data.ts` / `use-template-persistence.ts` | **零改动** | AC-24 diff 断言 |

> 所有命令默认在仓库根目录执行；前端命令先 `cd yanggu-code-generator-frontend`。

---

### Task 1: `collectDirtyFiles` 纯函数 + 单测

**Files:**
- Modify: `yanggu-code-generator-frontend/src/views/gen/template/tree-utils.ts`（插在 `buildFileList` 之后、`joinFileName` 之前）
- Test: `yanggu-code-generator-frontend/src/views/gen/template/tree-utils.test.ts`

**Interfaces:**
- Consumes: `Tree`（含 `id` / `templateType` / `isEdited` / `children`）、`findByIdFromTree`
- Produces: `collectDirtyFiles(checkedKeys: number[], findById: (id: number) => Tree | undefined): Tree[]`

- [x] **Step 1: 写失败用例（7 条）**

在 `tree-utils.test.ts` 的 import 中加入 `collectDirtyFiles`，并追加：

```typescript
describe('collectDirtyFiles', () => {
	// 样例树：1=根目录(子:2脏文件, 3子目录(子:4脏文件, 5脏二进制)), 6=根层级脏文件, 7=根层级干净二进制；8=干净文件
	const buildDirtyFixture = (): Tree[] => [
		makeNode({ id: 1, fileName: 'root', templateType: 0, isEdited: false, children: [makeNode({ id: 2, fileName: 'a.ftl', templateType: 1, parentId: 1, isEdited: true }), makeNode({ id: 3, fileName: 'sub', templateType: 0, parentId: 1, children: [makeNode({ id: 4, fileName: 'b.ftl', templateType: 1, parentId: 3, isEdited: true }), makeNode({ id: 5, fileName: 'logo', templateType: 2, parentId: 3, isEdited: true })] })] }),
		makeNode({ id: 6, fileName: 'top.ftl', templateType: 1, isEdited: true }),
		makeNode({ id: 7, fileName: 'lib', templateType: 2, binaryOriginalFileName: 'app.jar' }),
		makeNode({ id: 8, fileName: 'clean.ftl', templateType: 1, isEdited: false })
	]

	const makeFinder = (treeList: Tree[]) => (id: number) => findByIdFromTree(id, treeList)

	it('勾选目录连带后代脏文件，排除干净文件与二进制', () => {
		const tree = buildDirtyFixture()
		const result = collectDirtyFiles([1], makeFinder(tree))
		// id5 是脏二进制（不会真脏，防御性排除），只应收到后代脏文件 2、4
		expect(result.map(item => item.id)).toEqual([2, 4])
	})

	it('勾选文件节点直接入集合（仅脏文件）', () => {
		const tree = buildDirtyFixture()
		const result = collectDirtyFiles([6, 8], makeFinder(tree))
		expect(result.map(item => item.id)).toEqual([6])
	})

	it('父目录与子文件同时勾选按 id 去重', () => {
		const tree = buildDirtyFixture()
		const result = collectDirtyFiles([1, 2, 4], makeFinder(tree))
		expect(result.map(item => item.id)).toEqual([2, 4])
	})

	it('stale key（已不在树上）静默过滤不报错', () => {
		const tree = buildDirtyFixture()
		const result = collectDirtyFiles([1, 999], makeFinder(tree))
		expect(result.map(item => item.id)).toEqual([2, 4])
	})

	it('全部干净返回空数组', () => {
		const tree = buildDirtyFixture()
		expect(collectDirtyFiles([7, 8], makeFinder(tree))).toEqual([])
	})

	it('空勾选返回空数组', () => {
		const tree = buildDirtyFixture()
		expect(collectDirtyFiles([], makeFinder(tree))).toEqual([])
	})

	it('返回的是原节点引用（回滚靠共享引用双向同步，禁止 clone）', () => {
		const tree = buildDirtyFixture()
		const result = collectDirtyFiles([1], makeFinder(tree))
		expect(result[0]).toBe(findByIdFromTree(2, tree))
	})
})
```

- [x] **Step 2: 跑测试确认失败**

Run: `cd yanggu-code-generator-frontend; npx vitest run src/views/gen/template/tree-utils.test.ts -t "collectDirtyFiles"`
Expected: FAIL —— `collectDirtyFiles` 未导出（`is not a function` / TS 报错）

- [x] **Step 3: 最小实现**

```typescript
// 回滚-勾选脏文件收集：每个勾选 key 经 findById 反查（stale key 静默过滤），
// 目录递归收集后代脏文件、文件节点直接判定；只收模板文件（1），目录/二进制（2）无编辑通道天然不可达。
// 返回原节点引用（tabList 与 treeList 共享引用，红点双向同步靠此同一性，禁止 clone）；Map 按 id 去重保持首次插入顺序
export const collectDirtyFiles = (checkedKeys: number[], findById: (id: number) => Tree | undefined): Tree[] => {
	const map = new Map<number, Tree>()
	const walk = (node: Tree) => {
		if (node.templateType !== 0) {
			if (node.templateType === 1 && node.isEdited) {
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

- [x] **Step 4: 跑测试确认通过**

Run: `cd yanggu-code-generator-frontend; npx vitest run src/views/gen/template/tree-utils.test.ts -t "collectDirtyFiles"`
Expected: PASS —— 7 passed

- [x] **Step 5: 提交**

```bash
git add yanggu-code-generator-frontend/src/views/gen/template/tree-utils.ts yanggu-code-generator-frontend/src/views/gen/template/tree-utils.test.ts
git commit -m "feat(template): 新增 collectDirtyFiles 纯函数与单测"
```

（实际交付时并入 `b3a665e1` 单 commit。）

---

### Task 2: 编排层 `rollbackNode` / `rollbackCheckedNodes` / `confirmAndRollback`

**Files:**
- Modify: `yanggu-code-generator-frontend/src/views/gen/template/use-template-tree-state.ts`（`refreshActiveItemContent` 之后、`handleCloseTabs` 之前）
- Modify: 同文件 facade 返回值追加 `rollbackNode` / `rollbackCheckedNodes`

**Interfaces:**
- Consumes: `collectDirtyFiles`（Task 1）、`editTabs.discardEdits(tabs: Tree[])`、`treeData.findById`、`joinFileName(tabs: Tree[])`
- Produces: `rollbackNode(node: Tree): void`、`rollbackCheckedNodes(checkedKeys: number[]): void`

- [x] **Step 1: 新增共用编排链（按 spec 两态文案）**

```typescript
// 共用编排链：确认 → 丢弃回滚 → 提示。复用会话域 discardEdits（与关 tab「否(N)」同一落点），
// 禁止误用 applyEditRollback——那是保存成功推进基线的反方向动作（design 决策 2）。
const confirmAndRollback = (dirtyNodes: Tree[]) => {
	ElMessageBox.confirm(`${joinFileName(dirtyNodes)}存在未保存的修改，回滚将丢弃这些修改并恢复到上次保存（或打开时）的内容，此操作不可恢复，确定回滚吗？`, '提示', {
		confirmButtonText: '确定回滚',
		cancelButtonText: '取消',
		type: 'warning'
	})
		.then(() => {
			editTabs.discardEdits(dirtyNodes)
			ElMessage.success('已回滚到上次保存的内容')
		})
		.catch(() => {
			ElMessage.info('回滚已取消')
		})
}

// 单个回滚：树节点右键 / tab 右键共用（tab 元素与树节点同一引用，无需区分来源）；
// 菜单显隐已保证不可达，此处守卫为双保险
const rollbackNode = (node: Tree) => {
	if (node.templateType !== 1 || !node.isEdited) {
		return
	}
	confirmAndRollback([node])
}

// 批量回滚：入参为 getCheckedKeys 的勾选 key 列表（口径与工具栏删除一致：勾目录连带后代文件）
const rollbackCheckedNodes = (checkedKeys: number[]) => {
	const dirtyNodes = collectDirtyFiles(checkedKeys, treeData.findById)
	// 全干净不弹空确认框，直接 info 收口（design 决策 6）
	if (dirtyNodes.length === 0) {
		ElMessage.info('勾选范围内没有未保存的修改')
		return
	}
	confirmAndRollback(dirtyNodes)
}
```

- [x] **Step 2: facade 追加导出**

在 `use-template-tree-state` 的 return 对象中 `refreshActiveItemContent` 之后加 `rollbackNode, rollbackCheckedNodes,`。

- [x] **Step 3: 确认框两态 +「确定回滚」文案（AC-20 / AC-22）——已取消（2026-11-26 裁决）**

> **裁决：以已交付代码为准，不改代码，改 AC。** 用户明确"已提交的代码不要随便改动，AC 可能落后"，`b3a665e1` 的三态实现（`distinguishCancelAndClose: true` + 「是(Y)」/「否(N)」，X 无反馈）视为验收真相；`2026-11-26` spec 的 AC-19/20/22 已回写为三态语义。本 Step 不再执行，review F1 关闭为"spec 修订"而非代码缺陷。

- [x] **Step 4: 类型检查**

Run: `cd yanggu-code-generator-frontend; npm run type-check`
Expected: 退出码 0

---

### Task 3: 树面板工具栏按钮 + 树右键菜单项

**Files:**
- Modify: `yanggu-code-generator-frontend/src/views/gen/template/tree-panel.vue`（工具栏）
- Modify: 同文件 `treeMenuItems`（`edit` 项之后）

**Interfaces:**
- Consumes: `rollbackCheckedNodes` / `rollbackNode`（Task 2 facade）、`checkedNodeListLength`、`treeRef.getCheckedKeys()`
- Produces: 入口 UI（无导出接口）

- [x] **Step 1: 工具栏按钮（刷新 → 回滚 → 删除）**

```html
<el-button size="small" type="primary" :icon="Refresh" @click="emit('refresh')">刷新</el-button>
<!-- 回滚＝丢弃未保存修改恢复到内存基线，未勾选置灰，与删除按钮同口径 -->
<el-tooltip content="丢弃勾选范围内未保存的修改" placement="bottom">
	<el-button size="small" type="warning" :disabled="checkedNodeListLength === 0" @click="rollbackCheckedNodes(treeRef.getCheckedKeys())">
		<svg-icon icon="icon-rollback" style="margin-right: 5px"></svg-icon>
		回滚
	</el-button>
</el-tooltip>
<el-button size="small" type="danger" :icon="Delete" :disabled="checkedNodeListLength === 0" @click="deleteCheckedNode()"> 删除 </el-button>
```

（原两列栅格 `:span="14"/"span="10"` 相应改为 `10/14` 以容纳第三个按钮。）

- [x] **Step 2: 解构追加 facade 方法**

```typescript
const { state, getIcon, tabPush, initData, handleTabRemove, rollbackNode, rollbackCheckedNodes } = props.treeState
```

- [x] **Step 3: `treeMenuItems` 追加回滚项**

```typescript
{
	key: 'rollback',
	label: '回滚',
	icon: 'icon-rollback',
	// 目录/二进制/干净文件不显示：只有脏模板文件有可丢弃的编辑（与 tab 菜单「关闭未改」按脏态隐藏同理）
	visible: t => t.kind === 'treeNode' && t.node.templateType === 1 && !!t.node.isEdited,
	onClick: t => {
		if (t.kind === 'treeNode') {
			rollbackNode(t.node)
		}
	}
}
```

> 图标说明：`icon-rollback` 已在 `src/icons/iconfont/modules/navigation.ts` 注册（已核实），符合 spec §3.2「有现成 svg 则直接用」分支；无 svg 时才退化 `RefreshLeft`。

- [x] **Step 4: 类型检查**

Run: `cd yanggu-code-generator-frontend; npm run type-check`
Expected: 退出码 0

---

### Task 4: tab 右键菜单项

**Files:**
- Modify: `yanggu-code-generator-frontend/src/views/gen/template/editor-panel.vue`（`tabMenuItems` 数组顶部）
- Modify: 同文件解构追加 `rollbackNode`

**Interfaces:**
- Consumes: `rollbackNode`（Task 2 facade）
- Produces: 入口 UI（无导出接口）

- [x] **Step 1: 菜单项置顶**

```typescript
// 回滚置顶：只丢内容不关 tab（区别于关闭确认的「否(N)」退出链路），仅脏 tab 显示
{ key: 'rollback', label: '回滚当前', icon: 'icon-rollback', visible: t => !!t.item.isEdited, onClick: t => rollbackNode(t.item) },
```

- [x] **Step 2: 解构追加**

在 `props.treeState` 解构末尾加 `rollbackNode`。

- [x] **Step 3: 类型检查**

Run: `cd yanggu-code-generator-frontend; npm run type-check`
Expected: 退出码 0

---

### Task 5: P0/P1 测试补齐（未交付）

> spec §9 要求 AC-13 / AC-15 两条 P0 随本次交付补齐；实际三个计划测试文件均未创建。以下为待执行任务。
>
> **2026-11-26 裁决（以已提交代码为准）**：只执行 Step 5.1 —— 纯新增测试文件，不触碰 `b3a665e1` 的任何生产代码。Step 5.2 需要给 `tree-panel.vue` / `editor-panel.vue` / `tree-utils.ts` 动刀以抽出谓词，取消；AC-07/08 保持手工验证口径。Step 5.3 的提交动作也取消（AI 不自行 commit）。

**Files:**
- Create: `yanggu-code-generator-frontend/src/views/gen/template/use-template-edit-tabs.test.ts`（AC-12/13/15/16）
- Create: `yanggu-code-generator-frontend/src/views/gen/template/menu-visibility.test.ts`（AC-07/08）
- Modify: `tree-utils.ts` 新增 `isRollbackVisible`；`tree-panel.vue` / `editor-panel.vue` 改用该谓词

- [ ] **Step 5.1: 会话域反向语义测试（含 2 条 P0）**

```typescript
import { describe, expect, it } from 'vitest'
import { useTemplateEditTabs } from './use-template-edit-tabs'
import type { Tree } from './tree-utils'

const makeNode = (over: Partial<Tree> & { id: number }): Tree =>
	({
		id: over.id,
		fileName: 'a.ftl',
		templateType: 1,
		templateContent: '',
		originalTemplateContent: '',
		isEdited: false,
		...over
	}) as Tree

describe('discardEdits / applyEditRollback 反向语义', () => {
	it('AC-12 discardEdits 内容回基线并清标记', () => {
		const node = makeNode({ id: 1, templateContent: 'edited', originalTemplateContent: 'base', isEdited: true })
		const domain = useTemplateEditTabs(id => (id === node.id ? node : undefined))
		domain.discardEdits([node])
		expect(node.templateContent).toBe('base')
		expect(node.isEdited).toBe(false)
	})

	it('AC-13 discardEdits 不推进基线（红线）', () => {
		const node = makeNode({ id: 1, templateContent: 'edited', originalTemplateContent: 'base', isEdited: true })
		const domain = useTemplateEditTabs(id => (id === node.id ? node : undefined))
		domain.discardEdits([node])
		expect(node.originalTemplateContent).toBe('base')
	})

	it('AC-15 批量清标记不依赖 watch（非激活节点也逐个落标记）', () => {
		const a = makeNode({ id: 1, templateContent: 'x', originalTemplateContent: 'b1', isEdited: true })
		const b = makeNode({ id: 2, templateContent: 'y', originalTemplateContent: 'b2', isEdited: true })
		const domain = useTemplateEditTabs(id => (id === a.id ? a : undefined))
		domain.discardEdits([a, b])
		expect([a.isEdited, b.isEdited]).toEqual([false, false])
		expect([a.templateContent, b.templateContent]).toEqual(['b1', 'b2'])
	})

	it('AC-16 保存推进基线后 discardEdits 回到新基线', () => {
		const node = makeNode({ id: 1, templateContent: 'v1', originalTemplateContent: 'v0', isEdited: true })
		const domain = useTemplateEditTabs(id => (id === node.id ? node : undefined))
		domain.applyEditRollback([node])
		expect(node.originalTemplateContent).toBe('v1')
		node.templateContent = 'v2'
		node.isEdited = true
		domain.discardEdits([node])
		expect(node.templateContent).toBe('v1')
	})
})
```

Run: `cd yanggu-code-generator-frontend; npx vitest run src/views/gen/template/use-template-edit-tabs.test.ts`
Expected: PASS —— 4 passed

- [x] ~~**Step 5.2: 抽出显隐谓词并测试（AC-07/08）**~~ —— 已取消（2026-11-26）：需改已提交的 `tree-utils.ts` / `tree-panel.vue` / `editor-panel.vue`，违反"已提交代码不随意改动"；AC-07/08 维持手工验证。以下内容保留为历史方案，不再执行。

`tree-utils.ts` 追加：

```typescript
// 回滚入口显隐：仅脏模板文件可回滚（目录/二进制/干净文件没有可丢弃的编辑）
export const isRollbackVisible = (node: Tree) => node.templateType === 1 && !!node.isEdited
```

`tree-panel.vue`：import 追加 `isRollbackVisible`，菜单项 `visible` 改为 `t => t.kind === 'treeNode' && isRollbackVisible(t.node)`。

`editor-panel.vue`：import 追加 `isRollbackVisible`，菜单项 `visible` 改为 `t => isRollbackVisible(t.item)`。

新建 `menu-visibility.test.ts`：

```typescript
import { describe, expect, it } from 'vitest'
import { isRollbackVisible, type Tree } from './tree-utils'

const makeNode = (over: Partial<Tree> & { id: number; fileName: string; templateType: number }): Tree =>
	({ id: over.id, fileName: over.fileName, templateType: over.templateType, ...over }) as Tree

describe('isRollbackVisible', () => {
	it('AC-07 脏模板文件 → true', () => {
		expect(isRollbackVisible(makeNode({ id: 1, fileName: 'a.ftl', templateType: 1, isEdited: true }))).toBe(true)
	})

	it('AC-08 干净模板文件 → false', () => {
		expect(isRollbackVisible(makeNode({ id: 2, fileName: 'b.ftl', templateType: 1, isEdited: false }))).toBe(false)
	})

	it('AC-08 目录 → false', () => {
		expect(isRollbackVisible(makeNode({ id: 3, fileName: 'dir', templateType: 0 }))).toBe(false)
	})

	it('AC-08 二进制（即便 isEdited 也防御性排除）→ false', () => {
		expect(isRollbackVisible(makeNode({ id: 4, fileName: 'logo', templateType: 2, isEdited: true }))).toBe(false)
	})
})
```

Run: `cd yanggu-code-generator-frontend; npx vitest run src/views/gen/template/menu-visibility.test.ts`
Expected: PASS —— 4 passed

- [ ] **Step 5.3: 提交测试**

```bash
git add yanggu-code-generator-frontend/src/views/gen/template/use-template-edit-tabs.test.ts yanggu-code-generator-frontend/src/views/gen/template/menu-visibility.test.ts yanggu-code-generator-frontend/src/views/gen/template/tree-utils.ts yanggu-code-generator-frontend/src/views/gen/template/tree-utils.test.ts yanggu-code-generator-frontend/src/views/gen/template/tree-panel.vue yanggu-code-generator-frontend/src/views/gen/template/editor-panel.vue
git commit -m "test(template): 补齐回滚 P0 谓词与基线语义单测"
```

---

### Task 6: 静态门禁与 diff 校验（交付时执行）

- [x] **type-check**：`cd yanggu-code-generator-frontend; npm run type-check` → 退出码 0（实测通过）
- [x] **单测**：`cd yanggu-code-generator-frontend; npm run test` → 5 files / 86 tests passed（实测通过）
- [ ] **lint**：`cd yanggu-code-generator-frontend; npm run lint` → **当前红**：`src/components/crud/SearchForm.vue:68` `rangeFields` unused（既有问题，最后改动 `ece0ad10`，不在本次 diff；详见 review F4）
- [x] **AC-24 域文件零改动**：`git cat-file -e` 三文件存活（均 OK）+ `git diff --stat a6e6d586..b3a665e1 -- <三域文件>` 输出为空；正向对照 `tree-utils.ts` 非空（实测通过）
- [x] **AC-25 组件层零新增弹窗**：`git diff a6e6d586..b3a665e1 -- tree-panel.vue editor-panel.vue | grep -E '^\+.*(ElMessageBox|ElMessage\.)'` → 无输出；正向对照（state 文件）命中 4 行（实测通过）
- [x] **AC-26 纯函数**：`grep -n '^import' src/views/gen/template/tree-utils.ts` → 无匹配（实测通过）

> 执行注意：diff 类命令必须在**仓库根目录**、用**正确 pathspec** 执行；`git diff` 对匹配不到文件的 pathspec 静默返回空且 exit 0（pitfalls P-022），必须配正向对照。

---

## Out of plan scope（对应 spec §8 待自动化清单）

以下 AC 明确不在本 plan 交付范围，按 spec §8 登记为流程债务：

| AC | 内容 | 补齐方式 | 优先级 |
|---|---|---|---|
| AC-23 | 回滚全程零写接口（MSW 断言） | 先搭 `msw/node` server + 树数据 fixture，再写 `rollback-net.test.ts`（独立切片） | P1 |
| AC-09/10/11/18/19/20/22 | 菜单/按钮/弹窗的 jsdom 组件断言 | 需新增 `@vue/test-utils` + `jsdom` 依赖 | P2 |
| AC-14 | 编辑区可见刷新 | Playwright（独立决策） | P2 |
| AC-17 | tab/树红点同引用 | 可先以引用相等断言下沉为 unit | P1 |

## Self-Review

**1. Spec coverage**

| spec 小节 | 对应 Task | 状态 |
|---|---|---|
| AC-01~06 | Task 1 | ✅ 已交付 |
| AC-12/13/15/16 | Task 5.1（实现已在 Task 2 复用 `discardEdits`） | ❌ 测试未交付（P0） |
| AC-07/08 | Task 5.2 | ❌ 未交付 |
| AC-18/19/21 | Task 2 实现 | 🟡 实现正确，测试未交付 |
| AC-09/10/11/20/22 | Task 3/4 实现 | ❌ 偏差（AC-20/22）或未测试 |
| AC-23/14/17 | Out of plan scope | 债务已登记 |
| AC-24~28 | Task 6 | ✅（AC-28 的 lint 为既有红） |

**2. Placeholder scan**：无 TBD / "稍后补" 式占位；未覆盖项均已显式归入 Out of plan scope 或标注未交付。

**3. Type consistency**：`collectDirtyFiles(checkedKeys: number[], findById: (id: number) => Tree | undefined): Tree[]` 在 Task 1 定义、Task 2 使用一致；`rollbackNode(node: Tree)` / `rollbackCheckedNodes(checkedKeys: number[])` 在 facade 与两个组件中的调用形式一致；`isRollbackVisible(node: Tree)` 在 Task 5.2 两组件中使用一致。

---

## 执行交接

Plan 已保存至 `docs/superpowers/plans/2026-11-25-template-content-rollback.md`。剩余工作为 **Task 5（P0 测试补齐）**，两种执行方式：

1. **Subagent-Driven（推荐）**：每个 task 派新 subagent，任务间我 review —— 适合把 5.1/5.2 并行派发（两者文件不相交）。
2. **Inline Execution**：本会话按 executing-plans 批量执行，检查点 review。

选哪种？
