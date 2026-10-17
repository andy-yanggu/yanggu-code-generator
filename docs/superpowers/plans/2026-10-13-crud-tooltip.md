# CRUD Tooltip 全视图覆盖 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 CRUD 组件体系扩展 tooltip 能力，覆盖表单、表格列头、查询表单三个视图，通过 `formTableTooltip` 顶层声明实现 form + table 共用，各视图支持独立覆盖。

**Architecture:** Schema 层在三个接口各加一个可选属性；Hook 层在 fields 派生时按传播规则注入 tooltip；渲染层 DataTable 和 SearchForm 复用 FormLabelTooltip 组件实现条件渲染。

**Tech Stack:** Vue 3 + TypeScript + Element Plus + FormLabelTooltip 组件

**Spec:** `docs/superpowers/specs/2026-10-13-crud-tooltip-design.md`

## Global Constraints

- vue-tsc 基线 22 个错误，改动后零新增
- Vue/HTML 标签禁止自闭合（项目规范）
- FormLabelTooltip 复用现有组件 `src/components/form/label-tooltip/index.vue`，不新建
- icon-button / tooltip 属性命名一致性：新增 tooltip 相关属性一律用 `tooltip` 命名

---

### Task 1: Schema 类型扩展

**Files:**
- Modify: `yanggu-code-generator-frontend/src/types/schema/index.ts`

**Interfaces:**
- Consumes: 无（纯类型变更）
- Produces: `CrudField.formTableTooltip?: string`、`TableColumnSchema.tooltip?: string`、`SearchFieldSchema.tooltip?: string`

- [ ] **Step 1: CrudField 接口新增 `formTableTooltip` 属性**

在 `src/types/schema/index.ts` 的 `CrudField` 接口中，`label` 属性之后、视图角色标记之前，新增：

```ts
/** 字段说明，自动传播到表单 label 和表格列头（不传播到 query） */
formTableTooltip?: string
```

- [ ] **Step 2: TableColumnSchema 接口新增 `tooltip` 属性**

在 `TableColumnSchema` 接口的 `showOverflowTooltip` 属性之后，新增：

```ts
/** 列头提示文字，有值时渲染问号图标 + el-tooltip */
tooltip?: string
```

- [ ] **Step 3: SearchFieldSchema 接口新增 `tooltip` 属性**

在 `SearchFieldSchema` 接口的 `ruleList` 属性之前，新增：

```ts
/** 搜索字段提示文字，有值时渲染问号图标 + el-tooltip */
tooltip?: string
```

- [ ] **Step 4: 确认类型无报错**

Run: `cd yanggu-code-generator-frontend && npx vue-tsc --noEmit 2>&1 | Select-String "schema/index"`
Expected: 无匹配（schema 文件零错误）

---

### Task 2: Hook 层 tooltip 传播

**Files:**
- Modify: `yanggu-code-generator-frontend/src/hooks/use-crud.ts`

**Interfaces:**
- Consumes: `CrudField.formTableTooltip`（Task 1 产出）
- Produces: `FormFieldSchema.tooltip` / `TableColumnSchema.tooltip` / `SearchFieldSchema.tooltip` 自动注入

- [ ] **Step 1: buildFormSchema 注入 formTableTooltip**

在 `use-crud.ts` 的 `buildFormSchema` 函数中，`shared` 对象构建处新增 tooltip 传播：

```ts
const buildFormSchema = (fields: CrudField[]): FormFieldSchema[] => {
	return fields
		.filter(f => f.inForm)
		.map(f => {
			// 顶层共享属性注入 form 子对象
			const shared: Partial<FormFieldSchema> = {}
			if (f.component !== undefined) shared.component = f.component
			if (f.options !== undefined) shared.options = f.options
			if (f.formTableTooltip !== undefined) shared.tooltip = f.formTableTooltip
			return defaultsDeep({ key: f.key, prop: f.prop ?? f.key, label: f.label, ...shared, ...f.form }, defaultFormFieldOptions())
		})
}
```

关键：`shared.tooltip` 在 `...f.form` 之前展开，因此 `f.form.tooltip` 会覆盖顶层 `formTableTooltip`。

- [ ] **Step 2: buildTableColumns 注入 formTableTooltip**

在 `buildTableColumns` 函数的 `.map()` 中，`f.table` 展开之前注入 tooltip：

```ts
cols.push(
	...fields
		.filter(f => f.inTable)
		.map(f => {
			const base: Partial<TableColumnSchema> = {}
			if (f.formTableTooltip !== undefined) base.tooltip = f.formTableTooltip
			return defaultsDeep({ key: f.key, prop: f.prop ?? f.key, label: f.label, ...base, ...f.table }, defaultTableColumnOptions())
		})
)
```

关键：`base.tooltip` 在 `...f.table` 之前展开，因此 `f.table.tooltip` 会覆盖顶层。

- [ ] **Step 3: buildSearchSchema 无需改动**

`search.tooltip` 已自然通过 `...f.search` 展开注入（`SearchFieldSchema` 新增 `tooltip` 属性后，`f.search.tooltip` 自动生效），不需要额外传播逻辑。

- [ ] **Step 4: 确认类型无报错**

Run: `cd yanggu-code-generator-frontend && npx vue-tsc --noEmit 2>&1 | Select-String "use-crud"`
Expected: 无匹配（use-crud 文件零错误）

---

### Task 3: DataTable 列头 tooltip 渲染

**Files:**
- Modify: `yanggu-code-generator-frontend/src/components/crud/DataTable.vue`

**Interfaces:**
- Consumes: `TableColumnSchema.tooltip`（Task 1 产出）、`FormLabelTooltip` 组件
- Produces: 表格列头有 tooltip 时自动渲染问号图标

- [ ] **Step 1: 导入 FormLabelTooltip 组件**

在 `DataTable.vue` 的 `<script setup>` 的 import 区域新增：

```ts
import FormLabelTooltip from '@/components/form/label-tooltip/index.vue'
```

- [ ] **Step 2: 为 el-table-column 添加条件 #header 插槽**

在模板中，将现有的 el-table-column 循环：

```vue
<el-table-column v-for="col in visibleColumns" :key="col.key" v-bind="toColumnProps(col)">
	<!-- 操作列：使用 operation 插槽 -->
	<template v-if="col.key === 'operation'" #default="scope">
		<slot name="operation" v-bind="scope"></slot>
	</template>
	<!-- 其他列：透传命名插槽 -->
	<template v-else-if="$slots[col.key]" #default="scope">
		<slot :name="col.key" v-bind="scope"></slot>
	</template>
</el-table-column>
```

改为：

```vue
<el-table-column v-for="col in visibleColumns" :key="col.key" v-bind="toColumnProps(col)">
	<!-- 自定义列头：有 tooltip 时渲染问号图标 -->
	<template v-if="col.tooltip" #header>
		<form-label-tooltip :label="col.label" :tooltip="col.tooltip" />
	</template>
	<!-- 操作列：使用 operation 插槽 -->
	<template v-if="col.key === 'operation'" #default="scope">
		<slot name="operation" v-bind="scope"></slot>
	</template>
	<!-- 其他列：透传命名插槽 -->
	<template v-else-if="$slots[col.key]" #default="scope">
		<slot :name="col.key" v-bind="scope"></slot>
	</template>
</el-table-column>
```

- [ ] **Step 3: 确认类型无报错**

Run: `cd yanggu-code-generator-frontend && npx vue-tsc --noEmit 2>&1 | Select-String "DataTable"`
Expected: 无匹配

---

### Task 4: SearchForm 查询字段 tooltip 渲染

**Files:**
- Modify: `yanggu-code-generator-frontend/src/components/crud/SearchForm.vue`

**Interfaces:**
- Consumes: `SearchFieldSchema.tooltip`（Task 1 产出）、`FormLabelTooltip` 组件
- Produces: 查询字段有 tooltip 时自动渲染问号图标

- [ ] **Step 1: 导入 FormLabelTooltip 组件**

在 `SearchForm.vue` 的 `<script setup>` import 区域新增：

```ts
import FormLabelTooltip from '@/components/form/label-tooltip/index.vue'
```

- [ ] **Step 2: el-form-item label 改为条件渲染**

将模板中的：

```vue
<el-form-item v-for="field in normalizedFields" :key="field.key" :label="field.label" :prop="field.prop">
```

改为（去掉 `:label`，改用 `#label` 插槽条件渲染）：

```vue
<el-form-item v-for="field in normalizedFields" :key="field.key" :prop="field.prop">
	<template #label>
		<form-label-tooltip v-if="field.tooltip" :label="field.label" :tooltip="field.tooltip" />
		<span v-else>{{ field.label }}</span>
	</template>
```

- [ ] **Step 3: 确认类型无报错**

Run: `cd yanggu-code-generator-frontend && npx vue-tsc --noEmit 2>&1 | Select-String "SearchForm"`
Expected: 无匹配

---

### Task 5: Datasource 页面迁移（可选）

**Files:**
- Modify: `yanggu-code-generator-frontend/src/views/gen/datasource/index.vue`

**Interfaces:**
- Consumes: `CrudField.formTableTooltip`（Task 1 产出）
- Produces: connName 字段的 tooltip 同时出现在 form 和 table

- [ ] **Step 1: 将 form.tooltip 提升为 formTableTooltip**

在 `datasource/index.vue` 的 fields 数组中，将 connName 字段：

```ts
{
	key: 'connName',
	label: '连接名称',
	inForm: true,
	inSearch: true,
	inTable: true,
	form: {
		tooltip: '连接名称具有唯一性',
		ruleList: [{ required: true, message: '连接名称不能为空', trigger: 'blur' }]
	}
}
```

改为：

```ts
{
	key: 'connName',
	label: '连接名称',
	formTableTooltip: '连接名称具有唯一性',
	inForm: true,
	inSearch: true,
	inTable: true,
	form: {
		ruleList: [{ required: true, message: '连接名称不能为空', trigger: 'blur' }]
	}
}
```

- [ ] **Step 2: 启动项目验证**

Run: `cd yanggu-code-generator-frontend && pnpm dev`
验证：
1. 数据源管理页面表单弹窗中，"连接名称"label 旁有问号图标，hover 显示"连接名称具有唯一性"
2. 表格列头"连接名称"旁有问号图标，hover 显示"连接名称具有唯一性"

---

### Task 6: vue-tsc 基线差分验证

**Files:** 无新增改动

- [ ] **Step 1: 全量 vue-tsc 检查**

Run: `cd yanggu-code-generator-frontend && npx vue-tsc --noEmit`
Expected: 错误数 ≤ 22（既有基线），且新增文件（schema/index.ts、use-crud.ts、DataTable.vue、SearchForm.vue）零错误
