# 通用右键菜单（ContextMenu）抽象设计

## 概述

将项目中散落、重复且各含 bug 的多套右键菜单，抽象为一个「定位壳组件 `ContextMenu` + 可选 items schema」的两层结构，统一四角/四边边界定位、显隐生命周期与样式，消除定位错误、监听器泄漏、双菜单 querySelector 串扰三类缺陷。本期（第二步）只收编 `tree.vue` 内的模板树菜单与 tab 菜单两套，导航栏标签菜单留作第三步的压力测试。

本文件是**设计文档（spec）**；逐任务的 TDD 实现计划由 writing-plans 另出，并引用本文件作为论证依据。

## 现状盘点

项目当前存在 3 套真实右键菜单 + 1 处死样式：

| # | 位置 | 触发 | 菜单项 | 尺寸获取 | 边界处理 | 关闭方式 |
|---|------|------|--------|----------|----------|----------|
| 1 | `tree.vue` `contextMenu` | 树节点右键 / 空白右键 | 修改、新建目录、新建模板文件、新建二进制文件 | 实测 `offsetWidth/Height` | `calculateTemplateMenu` 仅夹取右下、留 10px | `document` mousedown + 手动 hide，**无卸载清理** |
| 2 | `tree.vue` `tabContextMenu` | 编辑区 tab 右键 | 关闭当前/其他/左/右/未改/全部 | 实测 menuEl | 内联 `Math.min` 仅夹取右下 | `document` mousedown，**无卸载清理** |
| 3 | `tag-bar.vue` + `tag-menu.vue` | 导航栏标签右键 | 刷新、打开窗口、页面全屏、固定/取消固定、关闭当前/左/右/其他/全部（含分割线分组） | **硬编码 `80×200`** ❌ | `Math.min` 夹取、**无 margin** | `useEventListener(document,'click')` |
| — | `preview/index.vue` | 无绑定 | — | — | — | 仅有 `.context-menu` CSS，无逻辑，**疑似复制残留死样式** |

已确认的缺陷：

1. **三套定位算法互不相同**，且 #3 用硬编码尺寸——菜单实际尺寸偏离 80×200 时右下边界算错、溢出屏幕。
2. **三套都只做「夹取(clamp)」，无一做「翻转(flip)」**——靠近上边/左边时不会朝空闲侧展开；极端小屏下 clamp 后 `top` 可能为负，仍溢出。
3. #1/#2 的顶层 `document.addEventListener('mousedown')` **未做卸载清理**（内存泄漏）。
4. #1 与 #2 共用类名 `.context-menu`，其「点击外部关闭」用 `document.querySelector('.context-menu')` 判断点击落点——两个菜单同时打开时只取 DOM 第一个，**串扰**（另一个关不掉/误判）。

## 设计目标与非目标

**目标**
- 统一四角/四边定位（flip 优先 + clamp 兜底 + 内部滚动），尺寸实测。
- 收编显隐生命周期：监听器随组件实例注册/销毁，根治泄漏与串扰。
- 内容渲染支持现有全部形态：异构图标、动态 label/icon、按目标条件显隐、分组分割线。
- 样式（含暗黑模式）集中一处。

**非目标**
- 本期不改导航栏标签菜单 #3（第三步再接入，作为通用性压力测试）。
- 不引入第三方 click-outside / popover 定位库。
- 不做键盘方向键导航（现有菜单均无，YAGNI）。

## 设计决策

### 决策 1：两层结构「定位壳 + 可选 items」，沿用「schema 边界 + 插槽逃生」哲学

菜单内容恰恰是每个菜单都不同的部分，强行 100% schema 化会既表达不了全案又比原码难读。故：

- **Layer 1（强制共享）= `ContextMenu` 定位壳**：只负责「每个菜单都一样、且现在写错/写漏」的部分——flip+clamp 定位、实测尺寸、Teleport、点击外部关闭、Esc、监听器生命周期。默认插槽把「当前目标」透出，调用方自渲染 `<ul>/<li>`。仅用上这层即可消除定位 bug / 串扰 / 泄漏。
- **Layer 2（可选糖）= items schema**：简单菜单（#1/#2）用 `:items` 自动渲染 ul/li；带分割线/动态项的复杂菜单（#3）可用 schema，也可退回默认插槽自写 li，由可读性权衡，组件不强迫。

### 决策 2：定位算法 flip 优先 + clamp 兜底

见「四角/四边边界」章节。不提供 `strategy` 开关——三套都应使用更稳的 flip，无保留 clamp-only 旧行为的价值（YAGNI）。

### 决策 3：`target` 注入而非闭包 reactive 状态

`open(event, target)` 时把「本次右键命中的对象」存入组件；items 的 `visible/label/icon/onClick` 一律以 `(target) => ...` 求值。如此 items 定义可保持静态（用 `computed` 定义一次），避免每次 open 重建，也替代了现状 `v-if="tabContextMenu.index > 0"` 这类直接闭包组件状态的写法。

`target` 用可辨识联合覆盖各来源：

```typescript
type MenuTarget =
	| { kind: 'treeNode'; node: Tree }
	| { kind: 'blank' }
	| { kind: 'tab'; item: Tree; index: number }
```

### 决策 4：异构图标用 `IconSpec = string | Component` 收口

`string` → 渲染为项目 `<svg-icon :icon>`；`Component`（如 `CloseBold`）→ 渲染为 `<el-icon><component :is/></el-icon>`。动态图标（#3 的 pin↔cancel-pin）用 `Dyn<T, IconSpec>`，即 `IconSpec | ((target: T) => IconSpec)`。

### 决策 5：分割线用 `dividerBefore`，仅当所依附项自身可见时才渲染

#3 关闭组的第一项携带 `dividerBefore: true` 且其 `visible` 含 `!pinned`，分割线随该组头一项自然显隐，无需引入 section 两级结构（保持 items 为扁平数组）。

### 决策 6：分期落地

- 第二步（本计划）：新建通用组件 + `computeMenuPosition` 纯函数及其单测；`tree.vue` 的 #1/#2 接入。
- 第三步：接入 #3，验证 schema 强度；删除 `preview/index.vue` 的 `.context-menu` 死样式。

### 决策 7：`Teleport to="body"`

菜单渲染进 body，根治「fixed 被祖先 `transform/filter/perspective` 劫持」并配合每实例 ref 彻底消除 #1/#2 的 querySelector 串扰。tree.vue 现有两处菜单无 transform 祖先，Teleport 非「非加不可」，但作为通用组件的默认能力予以固化。

## 四角 / 四边边界

文字原型（`▉`=光标，`┌─┐`=菜单）：

```
① 左上角（默认朝右下）      ② 右上角（水平翻转朝左）
▉┌───────┐                  ┌───────┐▉
 └───────┘                  └───────┘

③ 左下角（垂直翻转朝上）    ④ 右下角（水平+垂直均翻转）
   ┌──────┘                  └──────┐
   ▉                                ▉
```

算法（`computeMenuPosition`，纯函数，供单测）：

```typescript
computeMenuPosition(cursor, menu, viewport, margin = 8):
  // 水平：默认贴光标朝右；右溢出则整块移到光标左侧；仍溢出（比视口宽）则夹到 [margin, vw-w-margin]
  let left = cursor.x
  if (left + menu.width > viewport.width - margin) left = cursor.x - menu.width
  left = clamp(left, margin, Math.max(margin, viewport.width - menu.width - margin))

  // 垂直：默认贴光标朝下；下溢出则整块移到光标上方；仍溢出则夹到 [margin, vh-h-margin]
  let top = cursor.y
  if (top + menu.height > viewport.height - margin) top = cursor.y - menu.height
  top = clamp(top, margin, Math.max(margin, viewport.height - menu.height - margin))

  return { left, top }
```

- `Math.max(margin, ...)` 保证「菜单尺寸 > 视口」时上界不小于下界，`clamp` 结果恒 ≥ margin。
- 配套：菜单容器 `max-height: calc(100vh - 2*margin); overflow-y: auto` 作为「翻上来仍嫌高」的最后防线。
- 尺寸在内容渲染后经 `nextTick` 读 `offsetWidth/Height` 获得（每次 open 内容静态，无需 ResizeObserver；如后续内容会随 open 内部交互增高再加）。

## 接口设计

### `src/utils/context-menu.ts`（纯函数）

```typescript
export interface Size {
	width: number
	height: number
}
export interface Point {
	x: number
	y: number
}
export interface Rect {
	left: number
	top: number
}
export const computeMenuPosition = (cursor: Point, menu: Size, viewport: Size, margin?: number): Rect
```

### `src/components/context-menu/types.ts`

```typescript
import type { Component, Ref } from 'vue'

export type IconSpec = string | Component
export type Dyn<T, R> = R | ((target: T) => R)

export interface ContextMenuItem<T = any> {
	key: string
	label: Dyn<T, string>
	icon?: Dyn<T, IconSpec>
	dividerBefore?: boolean
	visible?: (target: T) => boolean
	disabled?: (target: T) => boolean
	onClick: (target: T) => void
}

export interface UseContextMenuReturn {
	visible: Ref<boolean>
	target: Ref<any>
	position: Ref<{ x: number; y: number }>
	open: (event: MouseEvent, target: any) => void
	close: () => void
}
```

### `src/components/context-menu/use-context-menu.ts`

- `useContextMenu(): UseContextMenuReturn`
- `open(event, target)`：记录 `event.clientX/Y` 与 `target`，置 `visible=true`。
- `close()`：置 `visible=false`。
- 定位测量与 flip+clamp 由组件在内容渲染后完成（见下），composable 仅持有原始光标坐标与状态。

### `src/components/context-menu/index.vue`

**Props：**
- `items?: ContextMenuItem[]` — 提供则内置渲染 ul/li；不提供则仅渲染默认插槽（Layer 1 模式）
- 无强制坐标 props：组件自身持有 `useContextMenu`，通过 `expose` 暴露 `open/close`

**Slots：**
- 默认具名作用域插槽 `v-slot="{ target }"` — Layer 1 自渲染内容
- `#[item.key]="{ target }"` — 单项内部结构逃生（覆盖该项默认 li 内容）

**Emits：**
- `closed` — 菜单关闭（点击外部/Esc/项点击后）时

**Expose：**
- `open(event, target)` / `close()`

**内置行为：**
- `<Teleport to="body">` + `position: fixed`
- 内容渲染后 `nextTick` 实测尺寸 → `computeMenuPosition` → 落位
- `visible` 时经 `onScopeDispose` 管理注册 `document` 的 `pointerdown`（capture）与 `keydown`（Esc）；点击/聚焦到本实例 `$el` 之外才 `close`（用实例 ref 判定，不用全局类名 querySelector）
- `items` 为空数组时不渲染
- 集中承载 `.context-menu` 及 `html.dark` 样式

## 渲染模型（内置 li）

```vue
<ul class="context-menu">
	<template v-for="item in items" :key="item.key">
		<el-divider v-if="item.dividerBefore && isVisible(item)" class="context-menu__divider"></el-divider>
		<li v-if="isVisible(item)" :class="['context-menu__item', { 'is-disabled': isDisabled(item) }]" @click="handleClick(item)">
			<slot :name="item.key" :item="item" :target="target">
				<el-icon v-if="isComponent(resolveIcon(item))" size="10">
					<component :is="resolveIcon(item)"></component>
				</el-icon>
				<svg-icon v-else-if="isString(resolveIcon(item))" :icon="resolveIcon(item)" size="10px"></svg-icon>
				<span>{{ resolveLabel(item) }}</span>
			</slot>
		</li>
	</template>
</ul>
```

## 调用点改造对照（#1/#2）

| 现状 | 接入后 |
|------|--------|
| `contextMenu` / `tabContextMenu` 两个 reactive + 各自 x/y/visible | 两个 `ContextMenu` 组件实例，各自 `ref.open(e, target)` |
| `calculateTemplateMenu` / `showTabMenu` 内联定位 | 删除，组件统一 flip+clamp 实测 |
| `handleNodeRightClick` / `handleScrollWrapperRightClick` 手写 `addEventListener('click')` | 只调 `treeMenuRef.open(e, { kind:'treeNode', node })` / `{ kind:'blank' }` |
| 两段 `div v-if + ul/li` 模板 + 约 120 行共享 CSS | `<context-menu :items="treeMenuItems()"></context-menu>` 一行；CSS 迁到组件 |
| `updateTemplate/newDir/...` 里手动 `hideContextMenu()` | 项 `onClick` 后组件自动 close |
| tab 菜单 6 个 `<li v-if>` 条件 | `tabMenuItems` computed 内各项 `visible: (t) => ...` 逐项对应 |
| 顶层 `document.addEventListener('mousedown')` × 2（未清理） | 删除，监听器由组件按实例管理 |

`treeMenuItems` 示例（条件映射，逐项对照现状 v-if）：

```typescript
const treeMenuItems = computed<ContextMenuItem<MenuTarget>[]>(() => [
	{ key: 'edit', label: '修改', icon: 'icon-edit', visible: t => t.kind === 'treeNode', onClick: t => updateTemplate(t.node) },
	{ key: 'newDir', label: '新建目录', icon: 'icon-folder-add', visible: t => t.kind === 'blank' || (t.kind === 'treeNode' && t.node.templateType === 0), onClick: t => newDir(t) },
	{ key: 'newFile', label: '新建模板文件', icon: 'icon-file-add', visible: /* 同上 */, onClick: t => newTemplateFile(t) },
	{ key: 'newBinary', label: '新建二进制文件', icon: 'icon-file-unknown', visible: /* 同上 */, onClick: t => newBinaryFile(t) }
])
```

## 测试策略

`computeMenuPosition` 为纯函数，单测覆盖（对齐现有 Vitest co-location 约定）：

- **正常**：光标居中且空间充足 → `left==x, top==y`
- **右溢出翻转**：`x + width > vw - margin` → `left == x - width` 且 ≥ margin
- **下溢出翻转**：`y + height > vh - margin` → `top == y - height` 且 ≥ margin
- **右下同时**：四角之右下角 → 水平、垂直均翻转
- **比视口大（极端）**：`menu.width > viewport.width` → `left` 夹为 margin，不产生负值
- **margin 生效**：贴左上角 `x=0,y=0` 但空间充足 → 结果不小于 margin（按算法定义确认是否强制 margin 内缩，实现期以用例锁定实际约定）

组件层（Layer 1）在 #3 接入或后续引入 `jsdom` + `@vue/test-utils` 再补测翻转后 DOM 坐标；本期以纯函数 + 手工回归 F/G 组为准。

## 风险评估

| 风险 | 等级 | 缓解措施 |
|------|------|---------|
| 实测尺寸时序（内容渲染后才能测） | 中 | 内容先渲染（可 `visibility:hidden` 占位），`nextTick` 后测量定位，避免首帧闪跳 |
| `pointerdown` capture 与菜单项 `click` 竞态 | 中 | 点击落在本实例 `$el` 内不触发 close；close 仅在外部点击/项点击处理完毕后发生 |
| Teleport 后样式作用域 | 低 | `.context-menu` 与 `html.dark` 规则置于组件非 scoped 或用 `:deep`，与现状 tree.vue 处理一致 |
| 动态 label/icon 求值性能 | 低 | items 用 computed 定义，仅在 open 时按 target 求值，规模极小 |
| 与既有 `Sortable`/拖拽交互 | 无 | 右键菜单与拖拽、tab 排序互不相干，不改其逻辑 |

## 约束

- 不引入新依赖（定位、click-outside 全自研，复用 `useEventListener`/`onScopeDispose`）。
- `computeMenuPosition` 与类型契约分文件、显式标注返回类型（对齐 composable 契约/实现分文件规范）。
- 全标签、非自闭合；const 箭头函数；Tab 缩进、单引号、无分号；暗黑样式走 CSS 变量。
- `vue-tsc --noEmit` 零新增错误；既有 tree-utils 25 单测不回归。
- tree 的 #1/#2 外部行为完全不变（菜单项集合、显隐条件、点击结果、暗黑外观）。

## 改动文件清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `src/utils/context-menu.ts` | **新建** | `computeMenuPosition` 纯函数 + 尺寸/坐标类型 |
| `src/utils/context-menu.test.ts` | **新建** | flip/clamp 纯函数单测 |
| `src/components/context-menu/types.ts` | **新建** | `ContextMenuItem`/`IconSpec`/`Dyn`/`UseContextMenuReturn` 契约 |
| `src/components/context-menu/use-context-menu.ts` | **新建** | 状态与生命周期 composable |
| `src/components/context-menu/index.vue` | **新建** | 定位壳组件 + 内置 items 渲染 + 集中样式 |
| `src/views/gen/template/tree.vue` | 修改 | #1/#2 接入组件；删除内联定位/监听器/菜单 CSS/菜单模板 |

> 后续（第三步，另出计划）：接入 `tag-bar.vue`/`tag-menu.vue` #3；删除 `preview/index.vue` 死样式。
