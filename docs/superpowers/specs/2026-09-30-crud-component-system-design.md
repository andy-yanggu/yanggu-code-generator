# CRUD 组件系统重构设计

## 一、目标

将现有 useTableV2 + config-table + search-form + tool-bar 分散组合模式，封装为四层配置驱动组件树。页面只需提供 schema 配置和 API 引用，不再持有 table/form 实例、不手工绑定事件。

## 二、组件架构

```
CrudPage.vue（最外层，内置 useCrudTable + useCrudForm）
├── QueryTable.vue（SearchForm + DataTable 聚合）
│   ├── SearchForm.vue（搜索表单，searchSchema 驱动）
│   └── DataTable.vue（全新表格 + 工具栏 + 列设置面板 + 分页）
│       └── ColumnSettingsPanel（列设置 Popover 内容）
└── CrudDialog.vue（新增/修改/详情弹窗）
    └── FormField.vue（表单字段包装，编辑/详情模式）
        └── DynamicField.vue（最底层，COMPONENT_MAP 渲染单个字段）
```

## 三、Schema 类型体系

新建 `src/types/schema/index.ts`。

### 3.1 ComponentType

```ts
export type ComponentType =
  | 'input' | 'select' | 'textarea'
  | 'radio' | 'checkbox' | 'switch'
  | 'date-picker' | 'input-number'
  | 'tree-select' | 'cascader'
```

无 `el-` 前缀，渲染层通过 COMPONENT_MAP 映射到 Element Plus 组件对象。

### 3.2 SearchFieldSchema

```ts
export interface SearchFieldSchema {
  prop: string
  label: string
  component?: ComponentType   // 默认 'input'
  placeholder?: string
  width?: number | string
  options?: { label: string; value: any }[]
  filterable?: boolean
  rangeFields?: [string, string]
}
```

### 3.3 FormFieldSchema

```ts
export interface FormFieldSchema {
  prop: string
  label: string
  component?: ComponentType   // 默认 'input'
  placeholder?: string
  options?: { label: string; value: any }[]
  filterable?: boolean
  span?: number               // el-col 栅格占比，默认 24
  detailFormatter?: (value: any) => string
}
```

### 3.4 TableColumnSchema

```ts
export interface TableColumnSchema {
  key: string
  label: string
  prop?: string
  type?: 'selection' | 'index'
  width?: number | string
  minWidth?: number
  fixed?: 'left' | 'right' | boolean
  align?: 'left' | 'center' | 'right'
  headerAlign?: 'left' | 'center' | 'right'
  sortable?: boolean | 'custom'
  formatter?: (row: any, column?: any, cellValue?: any) => any
  visible?: boolean
  disabled?: boolean
  showOverflowTooltip?: boolean
}
```

与现有 ColumnConfig 保持 1:1 对应，确保列设置持久化逻辑可复用。

### 3.5 FormMode

```ts
export type FormMode = 'add' | 'update' | 'detail'
```

### 3.6 Pagination

```ts
export interface Pagination {
  current: number
  size: number
  total: number
}
```

## 四、各组件详细设计

### 4.1 DynamicField.vue

路径：`src/components/crud/internal/DynamicField.vue`

**职责：** 根据 `field.component` 渲染对应的 Element Plus 表单控件。

**Props/Model：**
- `v-model`：`defineModel<any>()` — 绑定字段值
- `field: FormFieldSchema`：字段配置
- `placeholder?: string`：占位符（可选覆盖）

**COMPONENT_MAP 策略：**
- 值必须是显式 import 的组件对象（不能传字符串，否则渲染空白）
- select/radio/checkbox 等需要子组件的，用显式 `v-if/v-else-if` 写模板标签（不能依赖 options prop + `<component :is>`）

**模板结构：** 每个 component 类型一个 v-if 分支：
- `textarea` → `<el-input type="textarea">`
- `input`（默认）→ `<el-input>`
- `select` → `<el-select>` + `<el-option>` 循环
- `radio` → `<el-radio-group>` + `<el-radio>` 循环
- `checkbox` → `<el-checkbox-group>` + `<el-checkbox>` 循环
- `switch` → `<el-switch>`
- `date-picker` → `<el-date-picker>`
- `input-number` → `<el-input-number>`
- `tree-select` → `<el-tree-select>`
- `cascader` → `<el-cascader>`

### 4.2 FormField.vue

路径：`src/components/crud/internal/FormField.vue`

**职责：** 包装单个表单字段（el-form-item），处理编辑/详情模式切换。

**Props/Model：**
- `v-model`：`defineModel<Record<string, any>>({ required: true })` — 表单数据对象
- `field: FormFieldSchema`
- `mode: FormMode`

**行为：**
- 编辑模式（add/update）：渲染 DynamicField，`v-model="model[field.prop]"`
- 详情模式（detail）：渲染纯文本（`detailFormatter` 或直接显示值）
- 支持插槽覆盖：父级可通过 `#field-{prop}` 覆盖特定字段

### 4.3 SearchForm.vue

路径：`src/components/crud/SearchForm.vue`

**职责：** 由 searchSchema 驱动渲染查询表单，内部校验后 emit 事件。

**Props/Model：**
- `v-model`：`defineModel<Record<string, any>>({ required: true })` — 查询表单数据
- `fields: SearchFieldSchema[]`
- `visible: boolean`
- `loading?: boolean`

**行为：**
- 循环 fields 渲染 el-form-item + DynamicField
- 查询按钮：内部先 `formRef.validate()` 通过后才 emit `search`
- 重置按钮：`formRef.resetFields()` + emit `reset`
- 支持 `#field-{prop}` 插槽覆盖单个字段
- 校验职责随表单下沉到组件内部（hook 侧做空值守卫）

### 4.4 DataTable.vue

路径：`src/components/crud/DataTable.vue`

**职责：** 全新表格组件，内置工具栏 + 列设置面板 + 分页。

**Props：**
- `columns: TableColumnSchema[]` — 列配置
- `data: any[]` — 表格数据
- `loading?: boolean`
- `pagination?: Pagination`
- `tableKey?: string` — 持久化 key
- `pageSizes?: number[]`
- `rowKey?: string`
- `maxHeight?: string`

**defineModel：**
- `v-model:showSearch` — 搜索区域显隐
- `v-model:queryLoading` — 查询加载态
- `v-model:maximized` — 卡片最大化
- `v-model:columns` — 列配置（双向绑定，列设置面板修改）

**内部结构：**
```
el-card（支持最大化）
├── #header → 工具栏
│   ├── 左侧：#toolbar-left 插槽（透传给 CrudPage）
│   └── 右侧：搜索/刷新/列设置/最大化按钮
│       └── 列设置 Popover
│           ├── 复选框（显隐）
│           ├── 固定按钮（左/右）
│           ├── 拖拽排序（sortablejs）
│           ├── 重命名（editingKey 单行编辑模式）
│           └── 重置按钮
├── el-table
│   ├── el-table-column 循环（按 visible columns）
│   ├── 按 column.key 透传命名插槽
│   └── #empty 空数据插槽
└── el-pagination
```

**列设置持久化：**
- 复用 `table-settings-store`，以 tableKey 为维度落 localStorage
- 恢复策略：以 defaultColumns 为基底，仅覆盖 visible/fixed，保留 formatter 等函数字段
- 深拷贝：`cols.map(c => ({...c}))`，禁止 structuredClone

**Events：**
- `@selection-change`、`@sort-change`、`@page-change`、`@size-change`
- `@get-data-list`（刷新）
- `@reset-columns`（重置列设置）

### 4.5 QueryTable.vue

路径：`src/components/crud/QueryTable.vue`

**职责：** 聚合 SearchForm + DataTable，管理搜索与表格的交互。

**Props：** 透传 CrudPage 的相关 props。

**defineModel：**
- `v-model:queryForm` — 查询表单数据
- `v-model:showSearch` — 搜索显隐
- `v-model:maximized` — 最大化状态
- `v-model:columns` — 列配置

**行为：**
- SearchForm 的 `@search` → 触发 query.getDataList()
- SearchForm 的 `@reset` → 触发 query.reset()
- DataTable 的 `@page-change`/`@size-change`/`@sort-change` → 转发给 query 分组
- DataTable 的 `@selection-change` → 转发给 selection.onChange
- 转发 `#toolbar-left`、`#action-column`、`#field-{prop}` 等插槽

### 4.6 CrudDialog.vue

路径：`src/components/crud/CrudDialog.vue`

**职责：** 弹窗容器，由 formSchema 驱动渲染 FormField 列表。

**Props：**
- `visible: boolean`（v-model）
- `mode: FormMode`
- `formSchema: FormFieldSchema[]`
- `formData: Record<string, any>`（v-model）
- `rules?: Record<string, FormItemRule[]>`
- `labelWidth?: string`（默认 '100px'）
- `subject?: string`（标题主体）
- `submitLoading?: boolean`

**行为：**
- 根据 mode 生成标题：add→"新增XXX"、update→"修改XXX"、detail→"XXX详情"
- 循环 formSchema 渲染 FormField
- 详情模式：隐藏 footer 或只显示"关闭"按钮
- 支持 `#field-{prop}` 插槽覆盖

**Events：**
- `@submit` — 提交
- `@close` — 关闭

### 4.7 CrudPage.vue

路径：`src/components/crud/CrudPage.vue`

**职责：** 最顶层页面组件，内置 useCrudTable + useCrudForm，编排所有子组件。

**Props（完整定义）：**

| Prop | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| searchSchema | SearchFieldSchema[] | 否 | [] | 搜索字段配置 |
| formSchema | FormFieldSchema[] | 是 | — | 表单字段配置 |
| columns | TableColumnSchema[] | 是 | — | 表格列配置 |
| dataListApi | (query) => Promise<PageVO> | 是 | — | 分页查询接口 |
| deleteApi | (ids: Key[]) => Promise<void> | 否 | — | 删除接口 |
| submitApi | (data) => Promise<any> | 是 | — | 提交接口 |
| detailApi | (id: Key) => Promise<any> | 否 | — | 详情接口 |
| initQueryForm | () => any | 是 | — | 查询表单初始值工厂 |
| initFormData | () => any | 是 | — | 表单数据初始值工厂 |
| searchColumns | number | 否 | 4 | 搜索表单列数 |
| pageSize | number | 否 | 10 | 每页条数 |
| pageSizes | number[] | 否 | [10,20,50,100,200] | 每页条数选项 |
| subject | string | 否 | '数据' | 主体名称 |
| deleteNameKey | string | 否 | — | 删除确认时展示的名称字段 |
| primaryKey | string | 否 | 'id' | 主键字段 |
| labelWidth | string | 否 | '100px' | 表单标签宽度 |
| rules | Record<string, FormItemRule[]> | 否 | {} | 表单校验规则 |
| tableKey | string | 否 | — | 列设置持久化 key |
| submitAfter | (data) => void | 否 | — | 提交成功回调 |

**插槽：**

| 插槽名 | 作用域 | 默认内容 |
|--------|--------|----------|
| `#toolbar-left` | `{ table, form }` | 新增 + 批量删除按钮 |
| `#action-column` | `{ row, table, form }` | 修改 + 行删除按钮 |
| `#field-{prop}` | `{ model, field }` | 字段级覆盖 |
| 其他插槽 | `{ table, form }` | 透传 |

**内部流程：**
1. `setup()` 中调用 `useCrudTable(props)` 和 `useCrudForm(props)`
2. 模板渲染 QueryTable + CrudDialog
3. 表单提交后自动刷新表格（submitAfter 默认行为）
4. 默认工具栏和操作列可通过插槽覆盖

## 五、Hooks 设计

### 5.1 useCrudTable

路径：`src/hooks/use-crud-table.ts`
类型契约：`src/types/hooks/use-crud-table.ts`

**入参 UseCrudTableOptions：**

```ts
interface UseCrudTableOptions<VO = any> {
  // 必填
  dataListApi: (query: any) => Promise<PageVO<VO>>
  initQueryForm: () => any
  columns: TableColumnSchema[]

  // 搜索
  searchSchema?: SearchFieldSchema[]

  // 分页
  pageSize?: number
  pageSizes?: number[]

  // 删除
  deleteApi?: (ids: Key[]) => Promise<void>
  primaryKey?: string
  subject?: string
  deleteNameKey?: string

  // 列设置
  tableKey?: string

  // 行为
  mountedGetData?: boolean
  resetQueryGetData?: boolean
  queryContext?: Record<string, any>
}
```

**返回值（7 组 reactive 包裹）：**

```ts
{
  search: {
    fields: SearchFieldSchema[],    // 归一化后的搜索字段
    form: Record<string, any>,      // 查询表单数据（reactive）
    visible: boolean                // 搜索区域显隐
  }
  query: {
    getDataList: () => void,        // 查询（重置到第一页）
    reset: () => void,              // 重置查询条件
    onPageChange: (page) => void,
    onSizeChange: (size) => void,
    onSortChange: ({ order, prop }) => void
  }
  table: {
    data: VO[],
    loading: boolean,
    pagination: Pagination          // computed
    index: (index) => number        // 序号计算
  }
  selection: {
    rows: VO[],
    ids: Key[],
    onChange: (selections) => void,
    clear: () => void
  }
  delete: {
    execute: (arg?) => void,        // 批量/单行删除
    loading: boolean
  }
  columnConfig: {
    list: TableColumnSchema[],      // 完整列配置（v-model 给 DataTable）
    visible: TableColumnSchema[],   // 可见列（computed）
    reset: () => void               // 重置列设置
  }
  refs: {
    cardMaximized: boolean
  }
}
```

**核心能力（兼容 useTableV2）：**
- 分页查询 + 排序
- 多选 + 批量删除（含确认弹窗、名称拼接逻辑）
- 列设置持久化（mergeWithDefaults 策略）
- 搜索字段归一化（normalizeSearchField 逻辑）
- date-range 拆解为起止字段
- 分组对象用 `reactive()` 包裹（确保嵌套 ref 在模板中正确解包）

### 5.2 useCrudForm

路径：`src/hooks/use-crud-form.ts`
类型契约：`src/types/hooks/use-crud-form.ts`

**入参 UseCrudFormOptions：**

```ts
interface UseCrudFormOptions {
  submitApi: (data: any) => Promise<any>
  detailApi?: (id: Key) => Promise<any>
  initFormData: () => any
  subject?: string
  rules?: Record<string, FormItemRule[]>
  labelWidth?: string
  submitAfter?: (data: any) => void
}
```

**返回值（3 组 reactive 包裹）：**

```ts
{
  dialog: {
    visible: boolean,
    mode: FormMode,
    open: (mode: FormMode, id?: Key) => void,
    close: () => void
  }
  form: {
    data: Record<string, any>,      // reactive
    rules: Record<string, FormItemRule[]>,
    labelWidth: string,
    formRef: Ref                    // el-form ref（CrudDialog 绑定后赋值）
  }
  submit: {
    execute: () => void,            // 校验 + 提交
    loading: boolean
  }
}
```

**核心流程：**
- `open('add')` → 重置表单数据 → 打开弹窗
- `open('update', id)` → 重置表单 → 打开弹窗 → 调 detailApi 填充
- `open('detail', id)` → 同上，mode='detail'，禁用提交
- `submit()` → formRef.validate() → submitApi → 成功提示 → 关闭 → submitAfter

## 六、文件清单与实现顺序

| 序号 | 文件路径 | 说明 |
|------|----------|------|
| 1 | `src/types/schema/index.ts` | Schema 类型定义（ComponentType、SearchFieldSchema、FormFieldSchema、TableColumnSchema、FormMode、Pagination） |
| 2 | `src/types/hooks/use-crud-table.ts` | useCrudTable 类型契约（UseCrudTableOptions） |
| 3 | `src/types/hooks/use-crud-form.ts` | useCrudForm 类型契约（UseCrudFormOptions） |
| 4 | `src/hooks/use-crud-table.ts` | 表格 hook 实现 |
| 5 | `src/hooks/use-crud-form.ts` | 表单 hook 实现 |
| 6 | `src/components/crud/internal/DynamicField.vue` | 动态字段渲染 |
| 7 | `src/components/crud/internal/FormField.vue` | 表单字段包装 |
| 8 | `src/components/crud/SearchForm.vue` | 搜索表单 |
| 9 | `src/components/crud/DataTable.vue` | 全新表格（含工具栏+列设置面板） |
| 10 | `src/components/crud/QueryTable.vue` | 搜索+表格聚合 |
| 11 | `src/components/crud/CrudDialog.vue` | 弹窗表单 |
| 12 | `src/components/crud/CrudPage.vue` | 顶层页面组件 |
| 13 | `src/views/gen/field-type/test.vue` | 测试页面（纯配置驱动） |

## 七、已知坑点与约束

1. `<component :is>` 传字符串渲染空白 → 必须传组件对象
2. defineModel 吞掉父级手写 update 监听 → 父级必须用 v-model
3. select/radio/checkbox 的 options prop 在 `<component :is>` + v-bind 场景不生效 → 必须用 v-if 显式写子组件
4. hook 分组必须用 `reactive()` 包裹（不能用 computed），否则模板嵌套 ref 不解包
5. 表单校验职责随组件下沉 → hook 侧 formRef 做空值守卫
6. localStorage 恢复列配置 → 以 defaultColumns 为基底 mergeWithDefaults，禁止 structuredClone
7. defineModel 声明的变量不出现在 props 上 → 脚本内用 `xxx.value` 访问
8. HTML 注释不能出现在标签属性列表中 → eslint-disable 注释放 script 中
9. composable 类型契约与实现分文件：types/hooks/use-xxx.ts + hooks/use-xxx.ts
10. types/hooks/*.ts 内部禁止 `from '@/types'`，必须走具体子路径

## 八、测试页面示例

```vue
<template>
  <crud-page
    :search-schema="searchSchema"
    :form-schema="formSchema"
    :columns="columns"
    :data-list-api="genFieldTypeApi.entityPage"
    :delete-api="genFieldTypeApi.deleteList"
    :submit-api="genFieldTypeApi.submit"
    :detail-api="genFieldTypeApi.detail"
    :init-query-form="() => ({ columnType: '', attrType: '' })"
    :init-form-data="() => ({ id: null, columnType: '', attrType: '', packageName: '' })"
    subject="字段类型"
    delete-name-key="columnType"
  />
</template>
```

页面只需声明三个 schema 数组 + 传入 API，约 40 行代码（对比旧写法 116+67=183 行）。
