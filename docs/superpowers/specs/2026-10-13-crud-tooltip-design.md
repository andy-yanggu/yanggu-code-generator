# CRUD 组件 Tooltip 全视图覆盖设计

## 概述

为 CRUD 配置驱动组件体系扩展 tooltip 能力，覆盖三个视图：表单（form）、表格列头（table）、查询表单（query）。通过 `CrudField.formTableTooltip` 顶层声明实现 form + table 共用，各视图子对象支持独立覆盖。

## 现状

| 维度 | 表单（FormFieldSchema） | 表格列（TableColumnSchema） | 查询（SearchFieldSchema） |
|------|------------------------|---------------------------|--------------------------|
| tooltip 属性 | ✅ `tooltip?: string` | ❌ 不存在 | ❌ 不存在 |
| 渲染方式 | FormLabelTooltip 组件 | 原生 el-table-column 纯文本 | 原生 el-form-item 纯文本 |
| CrudField 声明 | `form.tooltip` | `table` 中无 tooltip | `search` 中无 tooltip |

表单侧已有完整实现：`FormField.vue` 判断 `field.tooltip` 后渲染 `FormLabelTooltip`（label + QuestionFilled 图标 + el-tooltip）。表格列头由 `DataTable.vue` 的 `toColumnProps()` 转换为 el-table-column 原生 props，仅传递 label 文本，无自定义 header 插槽。

## 设计决策

### 决策 1：顶层命名 `formTableTooltip`

**选择**：CrudField 顶层属性命名为 `formTableTooltip`，明确表达传播范围仅限 form + table。

**被否决**：使用 `tooltip` 作为顶层属性名。理由：虽然简洁，但传播范围不透明——开发者无法从名称判断是否传播到 query，必须查文档。`formTableTooltip` 自文档化，零学习成本。

### 决策 2：三层优先级

- `formTableTooltip`：顶层默认，自动传播到 form 和 table
- `form.tooltip` / `table.tooltip`：视图级覆盖，优先级高于顶层
- `search.tooltip`：独立配置，不继承 `formTableTooltip`

**传播矩阵**：

| 声明位置 | form | table | query |
|---------|------|-------|-------|
| `CrudField.formTableTooltip` | ✅ 继承 | ✅ 继承 | ❌ 不传播 |
| `form.tooltip` | 覆盖 form | — | — |
| `table.tooltip` | — | 覆盖 table | — |
| `search.tooltip` | — | — | ✅ 独立生效 |

**优先级**：视图子对象 > 顶层 > 无 tooltip。

### 决策 3：视觉形态统一

表格列头和查询表单均复用 `FormLabelTooltip` 组件，保持三个视图的 tooltip 视觉一致：label 文字旁出现 QuestionFilled 问号图标，hover 显示 el-tooltip。

## Schema 层变更

文件：`src/types/schema/index.ts`

### CrudField 新增属性

```ts
export interface CrudField {
  // ... 现有属性保持不变

  /** 字段说明，自动传播到表单 label 和表格列头（不传播到 query） */
  formTableTooltip?: string

  // ... 其余属性保持不变
}
```

### TableColumnSchema 新增属性

```ts
export interface TableColumnSchema {
  // ... 现有属性保持不变

  /** 列头提示文字，有值时渲染问号图标 + el-tooltip */
  tooltip?: string
}
```

### SearchFieldSchema 新增属性

```ts
export interface SearchFieldSchema {
  // ... 现有属性保持不变

  /** 搜索字段提示文字，有值时渲染问号图标 + el-tooltip */
  tooltip?: string
}
```

## Hook 层传播逻辑

文件：`src/hooks/use-crud.ts`（fields → formSchema / tableColumns / searchSchema 派生处）

### formSchema 派生

```ts
// tooltip：优先取 form.tooltip 覆盖，否则继承顶层 formTableTooltip
const formTooltip = field.form?.tooltip ?? field.formTableTooltip
if (formTooltip) {
  // 注入到 formSchema 项
}
```

### tableColumns 派生

```ts
// tooltip：优先取 table.tooltip 覆盖，否则继承顶层 formTableTooltip
const tableTooltip = field.table?.tooltip ?? field.formTableTooltip
if (tableTooltip) {
  // 注入到 tableColumn 项
}
```

### searchSchema 派生

```ts
// tooltip：只取 search.tooltip，不继承 formTableTooltip
const searchTooltip = field.search?.tooltip
if (searchTooltip) {
  // 注入到 searchField 项
}
```

## 渲染层变更

### DataTable 列头 tooltip

文件：`src/components/crud/DataTable.vue`

为 `el-table-column` 添加条件 `#header` 插槽：

```vue
<el-table-column v-for="col in visibleColumns" :key="col.key" v-bind="toColumnProps(col)">
  <!-- 自定义列头：有 tooltip 时渲染问号图标 -->
  <template v-if="col.tooltip" #header>
    <form-label-tooltip :label="col.label" :tooltip="col.tooltip" />
  </template>
  <!-- 操作列 / 其他列插槽保持不变 -->
</el-table-column>
```

导入 `FormLabelTooltip` 组件。

### SearchForm 查询字段 tooltip

文件：`src/components/crud/SearchForm.vue`

`el-form-item` 的 label 改为条件渲染：

```vue
<el-form-item v-for="field in normalizedFields" :key="field.key" :prop="field.prop">
  <template #label>
    <form-label-tooltip v-if="field.tooltip" :label="field.label" :tooltip="field.tooltip" />
    <span v-else>{{ field.label }}</span>
  </template>
  <!-- 插槽 / DynamicField 保持不变 -->
</el-form-item>
```

导入 `FormLabelTooltip` 组件。

## 向后兼容

- 现有页面的 `form: { tooltip: '...' }` 写法**零改动继续生效**，tooltip 仅在表单中显示，不会自动出现在表格列头。
- 若希望 table 列头也显示同一 tooltip，将 `form.tooltip` 提升为顶层 `formTableTooltip` 即可。
- `TableColumnSchema` 和 `SearchFieldSchema` 新增的 `tooltip` 为可选属性，不影响现有声明。

## 迁移示例

### 改造前（datasource/index.vue）

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

### 改造后（可选，非强制）

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
  },
  search: {
    tooltip: '支持精确匹配'  // query 独立配置（可选）
  }
}
```

## 改动文件清单

| 文件 | 改动内容 |
|------|---------|
| `src/types/schema/index.ts` | CrudField 新增 `formTableTooltip`；TableColumnSchema 新增 `tooltip`；SearchFieldSchema 新增 `tooltip` |
| `src/hooks/use-crud.ts` | fields 派生 formSchema/tableColumns/searchSchema 时注入 tooltip（3 处） |
| `src/components/crud/DataTable.vue` | 导入 FormLabelTooltip；el-table-column 添加条件 `#header` 插槽 |
| `src/components/crud/SearchForm.vue` | 导入 FormLabelTooltip；el-form-item label 改为条件渲染 |
| `src/views/gen/datasource/index.vue` | 可选迁移：`form.tooltip` → `formTableTooltip` |

## 约束与注意事项

1. **FormLabelTooltip 复用**：DataTable 和 SearchForm 直接导入 `src/components/form/label-tooltip/index.vue`，不新建组件，保持视觉与表单完全一致。
2. **列设置面板不受影响**：列设置面板中的列名来自 `col.label`，tooltip 只在表格列头渲染，不进入列设置面板。
3. **vue-tsc 基线**：改动完成后跑 `vue-tsc --noEmit`，与既有 22 个错误基线做差分，确保零新增。
