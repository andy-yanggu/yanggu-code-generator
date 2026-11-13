# yanggu-code-generator 前端开发文档

Vue 3.5 + Vite 8（rolldown）+ TypeScript（strict）+ Element Plus 的低代码生成器前端。
本文是前端**开发约定的入口文档**，聚焦 CRUD 组件体系：三层 Schema 体系、插槽前缀约定、hooks 契约。深入细节请按文末的设计文档索引查阅。

## 快速开始

```bash
pnpm install
pnpm dev          # 开发服务器，端口 5000（端口占用时可改 vite.config.ts 的 server.port）
pnpm build        # 生产构建（Oxc 压缩器已配置打包剔除 console/debugger）
pnpm preview      # 本地预览构建产物
pnpm lint         # ESLint 检查并自动修复
pnpm type-check   # vue-tsc 全量类型检查
```

- 环境变量：`.env`（公共）/ `.env.development` / `.env.production`；`VITE_ENABLE_MOCK=true` 时开发环境自动注入 MSW mock（零侵入，见 `vite-plugin-msw-inject.ts`）。
- 路径别名：`@` → `src/`。
- Vue / VueRouter / Pinia / vueuse / ElMessage 等 API 已自动导入（unplugin-auto-import），无需手写 import。

## 目录速览

| 目录 | 职责 |
|---|---|
| `src/components/crud/` | CRUD 组件体系：`CrudPage`（组合容器）、`QueryTable` / `CrudDialog`（纯展示）、`DataTable` / `SearchForm`、`internal/`（DynamicField、FormField、ColumnSettingsPanel 等） |
| `src/hooks/` | `use-crud.ts`（统一入口）、`use-crud-table.ts`、`use-crud-form.ts` 及其他工具 hooks |
| `src/types/schema/` | 三视图 Schema 类型体系与归一化函数（`normalizeSearchField` / `normalizeFormField`） |
| `src/types/hooks/` | `UseCrudOptions` / `UseCrudTableOptions` / `UseCrudFormOptions` 等配置类型契约 |
| `src/config/` | 集中化默认值常量（crud / env / layout / menu / router），大写下划线命名 |
| `src/constant/` | 业务枚举（enum-plus 定义）与通用常量 |
| `src/utils/request.ts` | axios 封装：统一错误分层（HTTP 状态码 / 业务错误 / 网络错误 / Blob 下载）、401 重定向登录、`noErrorMessage` 开关 |
| `src/router/` | Hash 路由 + 动态路由（`import.meta.glob` 组件映射表）+ 登录/进度条/标签页守卫 |
| `src/store/` | Pinia stores，经 pinia-plugin-persistedstate 持久化 |

---

## 核心：CRUD 组件体系

### 1. 架构分层

```
页面（.vue）
 ├─ const { tableHook, formHook } = useCrud(配置对象)     ← 逻辑层（唯一配置入口）
 └─ <crud-page :table-hook="tableHook" :form-hook="formHook">  ← 组件层
      ├─ QueryTable（纯展示）── DataTable / SearchForm
      └─ CrudDialog（纯展示）── FormField ── DynamicField
```

- **页面直调 hook**：配置全部来自页面传入的 hook options，组件不再持有配置 props。
- **组件纯展示**：`CrudPage` 只接收 `{ tableHook, formHook }` 两个 prop 并向下透传；`QueryTable` / `CrudDialog` 只接收 hook 返回值渲染状态。
- 承载 hook 返回值的 prop 统一带 **Hook 后缀**（`tableHook` / `formHook`，模板写 `:table-hook` / `:form-hook`）；注意 hook 返回值**内部**分组名仍是 `table` / `form`（如 `tableHook.table.data`、`formHook.form.data`），两者是两回事。
- 这三个透传组件的 prop 类型故意声明为 `any`（hook 是泛型函数，用 `ReturnType<typeof useCrudTable>` 会因泛型实参不同触发 TS2719），不要"顺手优化"成 ReturnType。

已迁移页面（可作为范例）：`field-type`、`datasource`、`base-class`、`enum-item`、`template-group`、`template-group-property`（弹窗式子组件，同样复用 CrudPage）。

### 2. 统一字段声明（fields 模式）与派生链路

页面只声明一份 `fields: CrudField[]`，由 `use-crud.ts` 的三个派生函数自动生成三份 schema：

| 派生函数 | 产物 | 筛选条件 |
|---|---|---|
| `buildFormSchema` | `formSchema` | `inForm: true` |
| `buildSearchSchema` | `searchSchema` | `inSearch: true` |
| `buildTableColumns` | `tableColumns`（含内置固定列） | `inTable: true` |

`CrudField` 结构（`src/types/schema/index.ts`）：

- **角色标记**：`inForm` / `inSearch` / `inTable`，默认 false。
- **顶层共享属性**：`component` / `options` / `filterable` / `clearable` / `tooltip`——派生时自动注入 form 与 search 子对象（Table 不消费输入类属性，不注入）。
- **视图专属子对象**：`form:` / `search:` / `table:`，用于各视图独立覆盖（如 `search: { width: 260 }`、`table: { sortable: 'custom' }`）；子对象同名属性优先级高于顶层共享值。
- **多级表头**：`children` 仅表格视图生效，声明 children 的字段本身是分组节点（不进表单/查询），派生 formSchema/searchSchema 时叶子自动拍平。
- **key 必填**：派生出的每个 schema/column 条目都保留 `key`，它是插槽命名与列配置持久化的标识，缺失会导致 `table-` / `query-` / `form-` 插槽静默失效。

最小示例：

```ts
const { tableHook, formHook } = useCrud({
  fields: [
    { key: 'name', label: '名称', inForm: true, inSearch: true, inTable: true,
      form: { ruleList: [{ required: true, message: '名称不能为空', trigger: 'blur' }] } },
    { key: 'type', label: '类型', component: 'select', options: typeOptions, inForm: true, inTable: true },
  ],
  tableConfig: reactive({ dataListApi: pageApi, deleteApi: deleteApi }),
  formConfig: reactive({
    addApi: saveApi, updateApi: saveApi, detailApi: detailApi,
    initFormData: () => ({
      name: '',
      type: undefined
    })
  }),
})
```

传统模式（直接传 `formSchema` / `searchSchema` / `tableColumns`）仍向后兼容；`fields` 与显式 schema 并存时显式 schema 优先（派生用的是 `??=`）。

### 3. 三层 Schema 类型体系

```
BaseFieldSchema（身份 + 通用展示：key? / prop? / label / tooltip? / width?）
 ├─ InputFieldSchema（输入交互：component? / placeholder? / options? / clearable? / filterable? / ruleList?）
 │   ├─ SearchFieldSchema（+ rangeFields?）
 │   └─ FormFieldSchema（+ span? / detailFormatter?）
 └─ TableFieldSchema（列专属：key 必填 / type / minWidth / fixed / align / sortable / formatter / visible / children…）
```

- 属性归属原则：**Table 不消费输入类属性**，所以 TableFieldSchema 直接继承 Base，不背 component/ruleList。若未来实现表格行内编辑，TableFieldSchema 应改为 extends InputFieldSchema，届时重新评估该分层。
- `key` 与 `prop` 互为兜底（`prop: field.prop ?? key`），由 `normalizeSearchField` / `normalizeFormField` 补全；两者都缺会抛错（防止 `field-undefined` 插槽与校验静默失效）。
- `SearchFieldSchema` 同样支持 `ruleList`（搜索前字段级校验），校验规则不只表单独有。
- 区间组件（`date-range` / `datetime-range`）渲染为 el-date-picker range 形态；搜索字段默认宽 160，区间组件默认 260 / 380。`width` 的生效点在 DynamicField 注入输入组件的 style，而非表单项容器。

### 4. 内置固定列与开关

五个固定列由 `buildTableColumns` 自动注入（selection、index 前插；createTime、updateTime、operation 后插），页面不再手写。

| 开关（tableConfig 上声明） | 控制列 | 默认 |
|---|---|---|
| `showSelection: false` | 多选列 | 开 |
| `showIndex: false` | 序号列 | 开 |
| `showCreateTime: false` / `showUpdateTime: false` | 时间列 | 开 |
| `showOperation: false` | 操作列 | 开 |

判定式统一为 `options?.showXxx !== false`——**默认全开，仅显式 false 关闭**。纯展示表格写 `{ showSelection: false, showOperation: false }` 即可。

### 5. 插槽前缀约定

页面通过 `CrudPage` 自定义各区域渲染，命名统一为 **`前缀-[key]`** 两段式：

| 插槽名 | 区域 | 转发路径 |
|---|---|---|
| `table-toolbar-left` | 工具栏左侧 | CrudPage 显式处理（默认：新增 + 批量删除） |
| `table-action` | 操作列 | CrudPage 显式处理（默认：修改 + 行删除） |
| `table-[列key]` | 表格列自定义渲染 | CrudPage **保留前缀** → QueryTable 剥 `table-` → DataTable |
| `query-[字段key]` | 搜索字段自定义 | CrudPage **保留前缀** → QueryTable 剥 `query-` → SearchForm |
| `form-[字段key]` | 表单字段自定义 | CrudPage 剥 `form-` **补 `field-`** → CrudDialog → FormField |

必须知道的三个坑：

1. **替换式而非追加**：提供 `table-toolbar-left` / `table-action` 后内置按钮整体消失，自定义内容需把默认按钮一并重写。
2. **前缀剥在分流点**：`table-*` 与 `query-*` 不在 CrudPage 剥前缀（列 key 与搜索字段 key 同名时会碰撞覆盖），由 QueryTable 分流时各自剥——排查插槽不生效时先确认转发链每一层实际查找的槽名形态。
3. **失效是静默的**：插槽名拼错、字段缺 `key` 都不会报编译错误，只能运行时肉眼验证。新增插槽类型前先做选型：**只是文案/开关类数据的需求走 schema 加字段（如 `tooltip`），不要新增插槽**。

### 6. Hooks 契约

**`useCrud(options)`** —— 统一入口，返回 `{ tableHook, formHook }`：

- 入参结构：顶层共享 `primaryKey`（默认 `'id'`，可省略）/ `subject`（默认 `'数据'`）+ `tableConfig` / `formConfig` 两个子配置对象（类型即 `UseCrudTableOptions` / `UseCrudFormOptions`）。
- 页面侧 `tableConfig` / `formConfig` 与 useCrud 入参**一律用 `reactive()` 包裹**（配置可能运行时修改；hook 内 defaultsDeep 依赖可写代理）。
- hook 内部：注入共享字段 → 分别调子 hook → 自动关联 `formConfig.afterSubmit ?? tableHook.query.getDataList()`（表单提交后刷新表格，页面显式提供则不覆盖）。

**`useCrudTable` 返回值分组**：`search`（查询表单）/ `query`（getDataList 等）/ `table`（data、columns 状态）/ `selection`（多选）/ `action`（delete、export、import）/ `columnConfig`（列设置持久化）/ `refs`（el-table 实例、primaryKey）。

**`useCrudForm`**：管理弹窗表单，`FormMode` 支持 `add / update / copy / detail`（copy 内置 detailApi 回填 + 自动清主键）；返回值额外含 `formSchema` 供 CrudDialog 渲染。

配置项要点：

| 配置 | 归属 | 说明 |
|---|---|---|
| `dataListApi` | tableConfig | 必填（fields 模式下 `tableColumns` / `searchSchema` 可省略，仅传统模式必填） |
| `initQueryData` | tableConfig | 可选，默认 `() => ({})`，无搜索页可省 |
| `deleteApi` | tableConfig | 可选，无删除场景本已支持 |
| `mountedGetData: false` | tableConfig | 关闭首屏自动加载（弹窗式子组件场景，由外部触发查询） |
| `queryContext` | tableConfig | 跨视图上下文过滤（如子表按 groupId 查询） |
| `rules` | formConfig | **插槽承载的控件校验规则必须写这里**——控件不走 FormField 默认渲染时，条目上的 ruleList 静默不生效 |
| `successMessage` | formConfig | 字符串或按 mode 的函数；函数返回空串/undefined 会显示空白文案 |
| `beforeOpen` / `afterOpen` / `afterSubmit` | formConfig | 生命周期回调；旧版 `initAfter` 语义对应 `afterOpen` |

**默认值机制**：各层 hook 统一以 `defaultOptions()` 工厂 + `lodash-es defaultsDeep(opts, defaultOptions())` 原地深合并，只补 `undefined`，页面显式值（含 `false`）不被覆盖。工厂返回类型必须写成 `Partial<...>`（否则缺必填项触发 TS2352）。视图级默认值集中在 `src/config/crud.ts`（form/search 默认 `{ filterable: true, clearable: true }`，table 默认 `{ align: 'center', showOverflowTooltip: true }`）。

### 7. Schema 能力边界（走插槽手写的场景清单）

以下需求**当前无法用 schema 表达**，需走 `form-[字段key]` 插槽在页面手写控件（并记得把校验规则挪到 `formConfig.rules`）：

1. **password 字段**：`ComponentType` 集合不含 password，需插槽手写 `el-input type=password`。
2. **数字 0/1 契约的 switch**：`DynamicField.getComponentProps` 对 switch 只透传 baseProps，不透传 `active-value / inactive-value`，需插槽手写 `el-switch` 并做 number/boolean 双向转换（范例：template-group-property 的 `required`）。
3. **编辑态禁用某字段**：`FormFieldSchema` 无 disabled，需插槽内按 `formHook.dialog.mode` 控制控件 disabled（范例：template-group 的 `type`）。
4. **虚拟组合字段**：仅表单组合布局、不对应真实数据列的字段（如 base-class 的 fullClassName = packageName + className）不纳入统一 fields，单独追加进 formSchema + 插槽承载。

其他约定：删除确认——操作列常驻按钮用 Popconfirm，**收进"更多"下拉菜单的删除必须改用 ElMessageBox**（`action.delete.execute`），否则下拉关闭后 popconfirm 移位。

---

## Layout / Router / Store

### Layout（`src/layout/`）

三层骨架：`index.vue` → `main/index.vue`（sidebar + navbar + 内容区）→ 各子组件。

- **sidebar**：`logo` + 菜单三件套（`menu-index` 滚动定位 / `menu-item` 递归项 / `menu-item-content` 标题渲染 / `menu-link` 内外链分流）。
- **navbar**：`breadcrumb`、`tag-bar`（标签页，sortablejs 拖拽排序）、`system-setting`（菜单/标签/工具栏/其他四组设置）、`menu-preference`（菜单位置偏好）、`theme-switch`（View Transition 圆形扩散暗色切换）、`menu-search`、`full-screen`、`layout-size`、`refresh-current-page`、`user-dropdown`。
- **内容区**：keep-alive 按 `route.meta.cache` 控制缓存（配合 cache-store）；外链菜单走 `iframe-container` 渲染 `router/iframe-page.vue`。

### Router（`src/router/`，Hash 模式）

| 文件 | 职责 |
|---|---|
| `index.ts` | createRouter + scrollBehavior（前进后退恢复位置/否则置顶）+ 静态路由；创建后立即注册守卫 |
| `constant.ts` | 静态路由（登录、redirect、40x/500、首页等） |
| `guards/auth.ts` | 登录校验 + 按 `userStore.menuList` 调 `addDynamicRoutes` 动态挂路由 |
| `guards/loaders.ts` | `import.meta.glob('@/views/**/*.vue')` 预构建组件映射表（O(1) 查找），未命中告警并兜底 404；type=3 走 iframe、type=4 新窗口不建路由 |
| `guards/ui.ts` | NProgress 进度条（受 `isOpenProgress` 开关控制）、标签页增删、页面缓存、文档标题 |

路由配置常量（白名单 `ROUTE_WHITE_LIST`、`DEFAULT_HOME_PATH`、`ROUTE_META_DEFAULTS`、鉴权开关 `ENABLE_AUTH_CHECK` 等）集中在 `src/config/router.ts`，不在守卫里散写魔法值。

### Store（`src/store/`，Pinia setup-store + 持久化）

全部为 `defineStore(id, () => {...})` setup 风格；需要持久化的 store 各自定义 `getPersistConfig()` 显式声明 key/storage/omit 字段（如 app-store 始终忽略 `globalLoading / isDark / isFullscreen`）。

| store | 职责 |
|---|---|
| `user-store` | token、用户信息、菜单/权限列表（驱动动态路由与 v-auth） |
| `app-store` | 布局尺寸、折叠、暗色、全屏、全局 loading |
| `tag-store` / `cache-store` | 标签页列表 / keep-alive 缓存组件名列表 |
| `system-setting-store` | 菜单、标签栏、工具栏、其他四组界面设置 |
| `menu-preference-store` | 菜单位置偏好（通过 event-bus 广播变更） |
| `table-settings-store` | 表格列设置按页面持久化到 localStorage |

跨组件广播走 `src/utils/event-bus.ts`（基于 @vueuse/core `useEventBus` + 类型化 Symbol key：`titleChangeBus` / `cacheChangeBus` / `hideMenuChangeBus` / `hideTabChangeBus`，事件负载为 `PreferenceFieldEvent` 字段级 before/after），设置类变更用事件总线通知 layout，而不是层层 props。

---

## 组件清单与注册机制

组件注册三条途径（页面里没有 import 也能用，不是 bug）：

1. **Element Plus 组件**：unplugin-vue-components + ElementPlusResolver 按需引入；样式在 main.ts 全量导入（`importStyle: false`）。
2. **Vue/Pinia/vueuse API + ElMessage / ElMessageBox / ElNotification / ElLoading**：unplugin-auto-import，声明文件 `auto-imports.d.ts`；页面里**不要再手写 `import { ElMessage } from 'element-plus'`**。必须开 `vueTemplate: true`：只在 `<template>` 里用到的 API（如 `@cancel="ElMessage.info('删除已取消')"`）默认不会被注入，会编译成 `_ctx.ElMessage` 而运行时 undefined；且 `vue-tsc` / ESLint 因 `auto-imports.d.ts` 的全局声明会一律放行，这类断链只能看 Vite 编译产物（或运行时）才能发现。
3. **全局注册**：`svg-icons-register`（`<svg-icon>` 可用）、`setupEcharts`（`<chart>`）、`v-auth` 指令。

`src/components/` 一览：

| 分组 | 组件 | 说明 |
|---|---|---|
| `crud/` | CrudPage、QueryTable、DataTable、SearchForm、CrudDialog + internal（DynamicField、FormField、ColumnSettingsPanel/Item） | 见上文 CRUD 章节 |
| 文本/图标 | `text-tooltip`（溢出省略+tooltip）、`icon-text-tooltip`（图标+文本，面包屑/菜单/预览页在用）、`svg-icon`、`icon-button` | tooltip 类的基础组件 |
| 表单辅助 | `form/label-tooltip`、`form/divider`、`form/search-grid`（+toggle-more） | 被 CRUD 内部消费（如 FormField 的 #label 用 label-tooltip） |
| 数据展示 | `dict-tag`、`option`、`popover/list`、`echarts` | dict-tag 把带 type/effect 的枚举项渲染成彩色 el-tag |
| 路由配套 | `router/iframe-page.vue`、`router/redirect.vue` | 动态路由 type=3 / 刷新中转 |
| 旧 UI 层 | `table/config-table`、`table/search-form`、`table/tool-bar` | 前两者当前无引用；tool-bar 被 DataTable 使用 |

`src/business/`：`code-mirror`（编辑器封装）、`preview`（代码预览），业务级复用组件放这里而非 components。

## Types / Constant / Config

- **`src/types/`** 按域分目录：`schema/`（三视图 Schema + CrudField + BuiltinColumnOptions）、`hooks/`（Use*Options / Use*Return 契约，与 `src/hooks/` 一一对应）、`api/`（Entity/VO/DTO，按后端模块分文件）、`store/`（持久化状态形状）、`router/`（RouteMetaData、MenuInfo）、`common/`、`axios.d.ts`（请求配置扩展 `noErrorMessage` / `rawResponse`）。页面里不写行内类型，配置契约先进 types/hooks。
- **枚举体系（enum-plus）**：`src/constant/enum.ts` 用 `Enum({ X: { value, label, type, effect } })` 定义业务枚举（DB_TYPES、TEMPLATE_TYPES、ATTR_TYPES 等 14 组）；`type`/`effect` 用 `TagType`/`TagEffect` 联合类型标注，**颜色名拼错编译期即报错**；展示侧统一由 `dict-tag` 消费。选项数组（如 TAG_TYPE_OPTIONS）不加 `as const`（readonly 数组赋给 `T[]` 契约属性会类型报错）。
- **配置集中化**：所有默认值/魔法数收在 `src/config/`——`crud.ts`（CRUD 全链路默认值，hook 与组件共用同一真源）、`router.ts`、`layout.ts`（菜单展开/收起宽度）、`menu.ts`（内置菜单）、`env.ts`。改一处生效全局，新增默认值先查这里有没有现成常量。

## 工具类库

| 库 | 用法约定 |
|---|---|
| axios（`utils/request.ts`） | 统一实例 + 错误分层（HTTP 状态码/业务/网络/Blob）；下载走 `downloadFile` / `downloadBigFile` / `loadBlob` 三件套，不在页面手写 blob |
| lodash-es | 对象/数组合并与查询表单转换，**注意用项目内 `@/utils/tool` 包装的 `defaultsDeep`**；只按需 import 单函数，不引全量 lodash |
| @vueuse/core | 优先用它替代手写监听/剪贴板等（`copyToClipboard` 等工具里在用） |
| dayjs | 时间格式化，不引 moment |
| sortablejs | 标签栏拖拽排序 |
| enum-plus | 业务枚举统一方案（见上节） |
| codemirror 6 + vue-codemirror | 模板编辑/代码预览，走 `business/code-mirror` |
| nprogress | 路由进度条，受界面设置开关控制 |
| msw | 仅开发环境 mock（`VITE_ENABLE_MOCK=true` 注入），生产构建不含 |
| 自研小工具 | `utils/tool.ts`（isNotEmpty / isBlank / isNotBlank / setTitle / copyToClipboard，并 re-export lodash 的 isEmpty / isObject / defaultsDeep）、`utils/menu.ts`（findMenuByPath / processMenuList）、`utils/event-bus.ts`（vueuse useEventBus 封装） |

---

## 工程约定

- **代码风格**：Prettier（单引号、无分号、tab 缩进、printWidth 150）+ ESLint（typescript-eslint、unused-imports、`vue/no-mutating-props` shallowOnly）。函数声明统一箭头函数形式（func-style: expression）。
- **书写习惯**：不含嵌套对象/数组的 schema 条目写单行，含 `ruleList` / `validator` 嵌套的条目保持多行；但 `initFormData` / `initQueryData` **一律多行、一行一个字段**（哪怕字段很少），因为页面加字段时这两处是必改点，单行写法会让 diff 挤在一行、也容易漏改。
- **加字段清单**：新增业务字段需同步改 Entity/VO 类型、`fields` 条目、`initFormData`、`initQueryData`（若参与搜索），后端还要补 Mapper resultMap / SQL 列，缺一个都是静默不生效。
- **console**：生产构建已由 `vite.config.ts` 的 `rolldownOptions.output.minify.compress.dropConsole` 打包剔除；开发期调试日志发布前清理。
- **提交前检查**：目前无 husky / CI，需**手动**跑 `pnpm lint && pnpm type-check`（二者通过才可提交）。
- **注释风格**：中文注释；关键设计决策与踩坑点必须写"为什么"（为什么这样做、踩过什么坑、曾考虑过什么被否），长文件用 `// ======== 区块名 ========` 分节。
- **弹窗底部按钮**：统一"取消在左、确认在右"。

## 已知技术债（迭代时顺手清理）

- `hooks/use-table.ts` / `use-table-v2.ts` 与 `components/table/config-table`、`table/search-form` 已无任何引用，确认后删除；`use-init-form.ts` 仍被 5 个旧页面使用（enum、project、table、template/test、template/tree），随页面迁移逐步淘汰。
- `use-crud-table.ts`（577 行）职责偏重（数据获取/交互操作/列配置杂居），可按需拆分。
- `api/gen/generator.ts` 等处存在 any 残留；`enum/template/template-index.vue` 仍直调 `useCrudTable`（未走 useCrud）。

## 设计文档索引

- [CRUD 组件体系设计](../docs/superpowers/specs/2026-09-30-crud-component-system-design.md) —— 各组件详细设计与测试页
- [统一字段声明改造方案](../.qoder/plans/统一字段声明改造方案.md) —— CrudField 派生逻辑与改造前后对比
- [CRUD tooltip 设计](../docs/superpowers/specs/2026-10-13-crud-tooltip-design.md)
- [DataTable 拆分设计](../docs/superpowers/specs/2026-11-09-datatable-split-design.md) —— ColumnSettingsPanel 独立组件
