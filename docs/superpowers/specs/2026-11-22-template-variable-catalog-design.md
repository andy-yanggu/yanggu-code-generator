# 模板变量目录面板设计

日期：2026-11-22
状态：待用户评审
前置：建立在已完成的生成接口统一化重构（`2026-11-20-generator-api-unification-design.md`）与 DataTable 拆分之上。

## 1. 背景与目标

### 1.1 现状问题

模板作者（包括作者本人）在 `tree.vue` 编辑模板时，**无法在不切走页面的前提下知道当前模板能取到哪些变量**。现有获取途径只有三条，都不成立：

1. 去读 `domain/model` 下的 Java 类 —— 需要切 IDE，且看不出"这个字段属于哪种模板组"；
2. 抄现有 `.ftl` 模板 —— 只能看到别人用过的，看不到全集，且抄来的变量在当前上下文未必可用；
3. 写完点「测试」渲染 —— 报错 `InvalidReferenceException` 才知道变量不存在，反馈周期最长。

后果是隐性且持续的：`${projectNamePascal}` 在表模板可用、在枚举模板不存在；`${entityBaseClass.className}` 只有表配了基类才有值 —— 这类坑只能靠运行时撞。

### 1.2 目标

- 在模板编辑界面提供**变量目录面板**：按当前模板组类型列出可用变量、Java 类型、中文说明，点击可插入到编辑器光标处；
- 目录内容**由代码结构自动派生**，Model 增删字段后目录自动同步，不产生漂移；
- 三类模板组（项目/表/枚举）各出各的目录，不共用全量；
- 面板是辅助功能，任何失败都不得阻断模板编辑；
- 顺带修复 `DataModelBuilder` 一处导致变量实际不可用的缺陷（见 2.6）。

不做的事见第 8 节。

## 2. 现状盘点（已读码验证）

### 2.1 数据模型无前缀，根级属性即变量

`PreviewBuilder.render` → `TemplateUtil.renderTemplate` 内是 `template.process(dataModel, sw)`，dataModel 直接是 `ProjectModel` / `TableModel` / `EnumModel` 对象本身。因此模板写 `${functionNamePascal}`、`<#list fieldList as field>`，**不存在** `${table.fieldList}` 这种带对象名前缀的写法。

外部参考实现（Velocity 系生成器）的 `$context.xxx` / `$column.xxx` 前缀体系与本项目的数据模型结构不匹配，照搬会把变量名写错。目录的顶层组织方式只能是「根属性 → 嵌套属性」。

### 2.2 字段数量实测（决定 UI 必须能折叠与搜索）

| Model 类 | 声明字段数 |
|---|---|
| `ProjectModel` | 21 |
| `TableModel` | 44 |
| `TableFieldModel` | 34 |
| `EnumModel` | 15 |
| `EnumItemModel` | 4 |
| `ModuleModel` | 3 |
| `BaseClassModel` | 5 |
| **合计** | **126** |

按"每个集合独立完整展开"展开后的目录条目数：

| 模板组类型 | 可达深度 | 静态条目数（不含动态属性 key） |
|---|---|---|
| 枚举模板 | 2 | 15 + 4 = **19** |
| 表模板 | 2 | 44 + 5×34 + 2×5 + 1 = **225** |
| 项目模板 | 4 | 21 + 3 + 225 + 19 = **268** |

表模板的 225 来自 `TableModel` 里五个元素类型同为 `TableFieldModel` 的集合字段（`fieldList` / `primaryList` / `formList` / `gridList` / `queryList`）各自展开 34 条。

结论：**搜索与折叠不是锦上添花，是可用性前提**。

### 2.3 三类模型的填充差异（目录价值所在）

`DataModelBuilder.fillProjectCommon` 有三个重载，填充范围不同：

| | `ProjectModel` | `TableModel` | `EnumModel` |
|---|---|---|---|
| `projectName` / `projectNameUnderline` / `projectNamePascal` / `projectNameUpperSpace` | ✓ | ✓（前三者） | **类里未声明** |
| `projectNameDot` / `projectNameSlash` / `projectPackage` / `projectPackageSlash` | ✓ | ✓ | ✓ |
| `backendPath` / `frontendPath` | ✓ | ✓ | ✓ |

`EnumModel` 确实没有 `projectName` 系列字段，反射类结构得到的结果与实际可用变量一致 —— 这是选反射方案成立的关键前提，已逐字段核对：`ProjectModel` 21 个、`TableModel` 44 个、`EnumModel` 15 个声明字段**全部**在对应 `buildXxxDataModel` 中被赋值（部分为条件赋值，见 2.4）。

### 2.4 条件赋值字段（目录必须标注可空）

反射拿到的是"声明了哪些字段"，而实际渲染时部分字段可能为 null。已核对出的可空集合：

| 变量 | 为 null 的条件 |
|---|---|
| `ProjectModel.databaseDriverClassName` / `databaseUrl` / `databaseUsername` / `databasePassword` | 项目未绑定数据源（`resolveDatasource` 返回 null，整段跳过） |
| `TableModel.entityBaseClass` / `voBaseClass` | 表未配置 Entity / VO 基类 |
| `TableModel.modulePath` / `moduleDesc` | `project.moduleList` 中无同名模块 |
| `TableFieldModel.enumId` / `enumName` / `enumNamePascal` / `enumNameAllUpper` | 字段未关联枚举 |
| `EnumItemModel.enumItemCode` | 类型为 `Object`，实际是 String 或 Integer，由 `GenUtil.convertStringToAppropriateType` 决定 |

作者不知道可空性就会写出渲染失败的模板，因此目录必须把它显式化（见 4.1）。

### 2.5 动态变量：`*TemplatePropertyData`

三个 `Map<String, Object>` 字段的 key 由模板组属性配置决定，运行时才存在：

| 字段 | key 来源 |
|---|---|
| `ProjectModel.projectTemplatePropertyData` | `getData(project.projectTemplateGroupId, project.templateGroupPropertyData)` |
| `TableModel.projectTemplatePropertyData` | 见 2.6，当前是原始实体字段 |
| `TableModel.tableTemplatePropertyData` | `getData(project.tableTemplateGroupId, table.templateGroupPropertyData)` |
| `EnumModel.projectTemplatePropertyData` | 同 `TableModel`，取 `project.getTemplateGroupPropertyData()` |
| `EnumModel.enumTemplatePropertyData` | `getData(project.enumTemplateGroupId, enumEntity.templateGroupPropertyData)` |

`getData` 会把 `TemplateGroupProperty.propDefaultValue` 兜底补进 Map，再叠加产品自身配置值。

### 2.6 缺陷：表/枚举模板的项目属性拿不到默认值

`DataModelBuilder` 第 182、214 行：

```java
tableModel.setProjectTemplatePropertyData(project.getTemplateGroupPropertyData());   // 原始实体字段，未走 getData
enumModel.setProjectTemplatePropertyData(project.getTemplateGroupPropertyData());    // 同上
```

对比第 87、183、215 行都走了 `templateGroupPropertyDataService.getData(...)`。差别是 `getData` 会补齐模板组属性默认值，而这两行不会。

后果：同一个属性 key，在项目模板里 `${projectTemplatePropertyData.xxx}` 取得到（有默认值兜底），在表模板/枚举模板里取不到 → `InvalidReferenceException`。变量目录上线后，目录按属性配置列出 key，用户在表模板里一用就报错 —— 这个缺陷会被面板系统性放大。

**已确认按 bug 处理，本次一并修复**（两行改为走 `getData`，组 id 分别取 `project.getProjectTemplateGroupId()`）。

这是**行为变更**：改动前"项目模板组配了默认值、但项目未显式存该 key"的表/枚举模板渲染报错，改动后取到默认值。原本失败的模板会开始成功，交付说明必须写明。

### 2.7 编辑器现状

`business/code-mirror/index.vue` 只挂 `javascript()` mode，无 FreeMarker/highmark，无 autocomplete 扩展；`handleReady` 已把 `EditorView` 存进局部 `view` ref，但**未对外暴露**。

因此「点击插入」是低成本（一次 `dispatch`），而「输入 `${` 弹自动补全」需要引入 mode + completion source，是另一个量级 —— 本次不做（第 8 节）。

### 2.8 循环变量名是模板作者的既有约定，不可算法推导

从现有 131 个 `.ftl` grep `<#list>` 得到的实际用法：

| 集合 | 既有循环变量名 |
|---|---|
| `fieldList` | `field` |
| `formList` | `formField` |
| `queryList` / `gridList` / `primaryList` | `field` |
| `enumItemList` | `item` |
| `enumModelList` | `enumModel` |
| `importList` | `i` |

另有 5 个集合字段在既有模板里从未出现在 `<#list>` 中（`moduleList` / `tableModelList` / `primaryList` / `generatorFunction` / `BaseClassModel.fieldList`），它们的循环变量名由本次新定，见 4.1 的 `loopVar` 清单。

命名不统一（`formList` 用 `formField` 而非 `field`），所以必须由目录显式携带默认循环变量名，不能靠"去掉复数 s"之类推导。

## 3. 决策记录

均已获用户确认（2026-11-22 三轮问答）。

| # | 决策点 | 结论 | 否决的替代方案 |
|---|---|---|---|
| 1 | 变量目录真值来源 | 反射 Model 类 + 字段注解补中文描述 | 前端静态常量表（会漂移）；返回真实数据模型 JSON（无描述、需先选测试对象）；反射骨架+真实值（实现量最大） |
| 2 | 集合变量交互 | 集合行插循环骨架，子行插完整路径 | 只作文档点击复制（体验差一档）；全量扁平化（插入结果默认是错的） |
| 3 | 注解包位置 | `domain.model`，与 7 个 Model 同包 | `common.annotation`（common 是基础设施语义）；`domain.model.annotation`（多一层包）；顶层 `annotation`（先建包后有人） |
| 4 | `Map` 动态 key 展开范围 | 只展开当前模板组自身属性；跨组的 `projectTemplatePropertyData` 留作不可展开叶子 | 按项目反查并取并集 —— 一个表模板组可被多项目绑定，并集会产生"当前项目其实不存在该 key"的假变量 |
| 5 | 五个 `List<TableFieldModel>` | 各自独立完整展开（条目 225/268） | 按元素类型去重共享子树 —— `path` 失真；且这几个循环用途不同，需分别可见 |
| 6 | 2.6 缺陷 | 确认为 bug，本次一并修 | 保持现状（面板上线即放大该问题） |
| 7 | 可空性标注 | `@VarDesc.nullable()` 显式标注 + 前端「可空」标签 | 不标注（面板会系统性教人写出会挂的模板） |
| 8 | 缺注解的处理 | 照常列出、`desc` 为空，**不报错**；靠单测闸门防遗漏 | 运行时抛异常 —— 面板挂掉不影响代码生成，用户无感，缺失永远得不到纠正 |

### 3.1 对第 2 节原型的修正：顶层分组改为结构自动分区

前一轮口头原型里画的是语义分组（`▾ 项目信息 (9)`、`▾ 表信息 (4)`、`▾ 功能与模块 (7)`）。落文档时否决该方案：语义分组需要一个"字段 → 分组名"的人工映射，要么加进注解（第 4 个注解属性，80 个根字段各写一遍），要么在前端维护 80 条映射表，两者都是新增的漂移源与人工判断成本。

改为**结构自动分区**：顶层标量归入一个「标量变量」组，每个容器变量（集合 / 对象 / Map）自身即一个可展开分组，嵌套层就是 `el-tree` 原生层级。零额外元数据、零人工归类。

代价是表模板会有 34 条标量落在同一个组里，靠搜索消化。若实际使用中确认难扫，再补前端映射表 —— 纯 UI 层增量，不动后端契约，可后置。

## 4. 后端设计

### 4.1 `@VarDesc` 注解

新增 `com.yanggu.code.generator.domain.model.VarDesc`（与 7 个 Model 同包，Model 内使用无需 import）。

```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface VarDesc {

    /** 面向模板作者的中文说明，内容取自该字段现有 javadoc */
    String value();

    /** 集合字段的默认循环变量名，仅对 List/Set 有效；取自现有 .ftl 的 <#list> 约定 */
    String loopVar() default "";

    /** 该变量在渲染时可能为 null，模板需 ?if 保护 */
    boolean nullable() default false;
}
```

标注范围：7 个 Model 类共 126 个字段。`value` 从现有 javadoc 平移，不新增信息；`nullable` 只需 2.4 表列出的 12 处；`loopVar` 需要 12 处 —— 即全部 `List` / `Set` 字段（标量集合同样要插循环骨架）：

| 字段 | loopVar | 依据 |
|---|---|---|
| `TableModel.fieldList` | `field` | 既有模板 11 处 |
| `TableModel.formList` | `formField` | 既有模板 3 处 |
| `TableModel.gridList` / `queryList` / `primaryList` | `field` | 前两者既有模板同用 `field`；`primaryList` 本次新定 |
| `TableModel.importList` | `i` | 既有模板 4 处 |
| `TableModel.generatorFunction` | `funcCode` | 既有 57 处全是 `?seq_contains`，无 `<#list>`，本次新定 |
| `ProjectModel.moduleList` | `module` | 本次新定 |
| `ProjectModel.tableModelList` | `table` | 本次新定 |
| `ProjectModel.enumModelList` | `enumModel` | 既有模板 1 处 |
| `EnumModel.enumItemList` | `item` | 既有模板 2 处 |
| `BaseClassModel.fieldList` | `fieldName` | 本次新定（`List<String>` 属性名列表） |

### 4.2 目录节点 VO

新增 `domain.vo.VariableNodeVO`：

```java
@Data
public class VariableNodeVO {

    /** 属性名，如 fieldList */
    private String name;

    /** 从根算起的完整属性路径，如 tableModelList.fieldList —— 插入语义的地基 */
    private String path;

    /** Java 类型简名，如 List<TableFieldModel>、String、Object */
    private String javaType;

    /** 前端渲染与插入行为的唯一依据 */
    private VariableKindEnum kind;

    /** @VarDesc.value()，缺注解时为空串 */
    private String desc;

    /** kind 为集合类时有效 */
    private String loopVar;

    /** @VarDesc.nullable() */
    private Boolean nullable;

    private List<VariableNodeVO> children;
}
```

`kind` 枚举 `VariableKindEnum`（放 `enums` 包，与 `TemplateGroupTypeEnum` 同级）：

| kind | 判定 | 反射可见性 | 点击插入 |
|---|---|---|---|
| `SCALAR` | String / 数值 / Boolean / `Object` | 叶子 | `${path}` |
| `SCALAR_LIST` | `List<String>`、`Set<String>`、`List<Integer>` | 元素为标量 | 循环骨架；子节点是虚拟 `${loopVar}` 标量 |
| `MODEL_LIST` | `List<某 Model 类>` | 下钻 | 循环骨架；子变量插 `${loopVar.attrName}` |
| `OBJECT` | 某 Model 类（`entityBaseClass`） | 下钻 | 直接 `${entityBaseClass.className}`，无循环 |
| `MAP` | `Map<String, Object>` | children 由属性配置填充 | `${map.key}`，key 非合法标识符时降级 `${map['some-key']}` |

`EnumItemModel.enumItemCode` 声明类型是 `Object`，反射得到 `javaType="Object"`、`kind=SCALAR`，`desc` 里写明实际类型由 `enumCodeType` 指示。

### 4.3 `VariableCatalogBuilder`（纯反射，无 Spring 依赖）

放 `service.support`，与 `DataModelBuilder` / `PreviewBuilder` 同级。

```java
public class VariableCatalogBuilder {
    List<VariableNodeVO> build(Class<?> rootModelClass);
}
```

**刻意不加 `@Component`、不注入任何 service**，理由：沿用 `PreviewBuilderRenderTest` 那种 `new PreviewBuilder()` 直接实例化的纯单测风格，目录构建必须能在不起 Spring 上下文的情况下测。因此 `MAP` 节点在此只产出空壳（`kind=MAP`、`children=空列表`），属性 key 的填充留给上层。

实现要点：

- 逐字段 `getDeclaredFields()`，类型取 `Field.getType()`，集合元素类型取 `ParameterizedType.getActualTypeArguments()`；
- 下钻终止条件：非 `domain.model` 包下的类型；
- 循环引用防御：维护已访问 Class 集合（当前模型图是 DAG 无回边，但保留防御，见 6.3）；
- `path` 拼接：根级 = 字段名；集合子级 = `父path` 的元素变量前缀，即 `fieldList` 的子字段 `path` 为 `fieldList.attrName`，前端按 kind 决定是否加 `field.` 前缀；
- 同一元素类型的多个集合（`TableModel` 里 5 个 `List<TableFieldModel>`）**各自独立反射**，不做子树复制 + path 改写 —— 目录节点总量仅 ~278，反射开销可忽略；引入一套深拷贝反而多一处会写错的地方（行为与 4.2 定义的 `path` 完全一致）。

### 4.4 上层填充与查询编排

新增 `VariableCatalogService` + `VariableCatalogServiceImpl`（`service` / `service.impl`），职责：

1. `templateGroupService.getById(templateGroupId, false)` 取组（第二参 `isIncludeTemplateList=false`，目录不需要模板内容）；查不到 → `BusinessException("模板组不存在")`；
2. 按 `TemplateGroupTypeEnum` 映射根 Model 类：`PROJECT→ProjectModel.class`、`TABLE→TableModel.class`、`ENUM→EnumModel.class`；映射不到 → `BusinessException`；
3. `templateGroupPropertyService.listByGroupId(List.of(templateGroupId))` 取属性清单，把 `propKey` / `propTitle` 填进目录树中**所有** `kind=MAP` 且名称与当前组匹配的节点；
4. 名称匹配规则：表模板组填 `tableTemplatePropertyData`，枚举模板组填 `enumTemplatePropertyData`，项目模板组填 `projectTemplatePropertyData`；其余 `MAP` 节点（如表模板里的 `projectTemplatePropertyData`）保持空 children，`desc` 追加「键来自项目模板组属性」。

第 4 步要覆盖**嵌套层**的 MAP 节点：项目模板目录下 `tableModelList` 元素 `TableModel` 内也有两个 `MAP` 节点，不能只填根级。

**为什么必须是独立 service，而不是塞进 `TemplateService`**（已读码验证的硬约束）：`TemplateGroupPropertyServiceImpl` 已注入 `TemplateService`（其 `checkReference` 第 297 行用 `templateService.list(...)` 做模板内容隐式引用检测）。而目录编排需要 `TemplateGroupPropertyService`。若把 `variableCatalog` 放进 `TemplateServiceImpl`，就形成 `TemplateServiceImpl → TemplateGroupPropertyServiceImpl → TemplateServiceImpl` 的字段注入环，Spring 启动期直接失败。新建的 `VariableCatalogService` 只注入 `TemplateGroupService` + `TemplateGroupPropertyService`，自身处于依赖图叶子且无人回依赖它，零环。

顺带可复用既有惯例：`TemplateServiceImpl.detail` 第 119-121 行已有 `throw new BusinessException("模板组不存在, 模板组id: {}", id)` 的写法与占位符风格，目录接口的同类报错沿用它，不另造文案。

### 4.5 接口

```
GET /gen/template/variableCatalog?templateGroupId={id}
→ List<VariableNodeVO>
```

端点挂 `TemplateController`（URL 前缀 `/gen/template` 与路径语义一致），`@ApiOperationSupport(order = 12)`，`@Operation(summary = "模板变量目录")`，`@Parameter` 与 `@NotNull` 风格与该文件现有端点一致。Controller 注入 `VariableCatalogService`（不注入 `TemplateService`，理由见 4.4）。

### 4.6 后端变更清单

| 文件 | 动作 |
|---|---|
| `domain/model/VarDesc.java` | 新增 |
| `domain/vo/VariableNodeVO.java` | 新增 |
| `enums/VariableKindEnum.java` | 新增 |
| `service/support/VariableCatalogBuilder.java` | 新增（纯反射） |
| `service/VariableCatalogService` + `impl` | 新增（避开 4.4 的循环依赖环） |
| `controller/TemplateController` | 新增端点 + 注入 `VariableCatalogService` |
| 7 个 `domain/model/*.java` | 126 字段加 `@VarDesc` |
| `service/support/DataModelBuilder.java` | 修 182、214 行缺陷 |

## 5. 前端设计

### 5.1 入口与布局

主入口在 `views/gen/template/tree.vue` 右侧编辑区 —— 变量是给正在写模板的人看的，`tree.vue` 是写模板的地方；`test.vue` 已有级联选择与渲染结果，不再挤。

`tree.vue` 的 props 已带 `templateGroupId` 与 `templateGroupType`，目录该展示哪一份是已知的，不需要用户手选。

布局：右侧 `el-splitter-panel` 内容改为内层 `el-splitter`（横向）承载「编辑区容器 + 变量面板」两个 panel。`full-screen-mode` 的 class 从 `el-container` **上移到内层 `el-splitter` 根**，否则全屏只覆盖编辑区、面板被留在外面。全屏本身是 `position: fixed` 脱离文档流，外层 splitter 左 30% 树 + 右 70% 空白的表现与现状一致，无回归。

`test.vue` 接入作为后续增量：面板组件不读宿主内部状态，接进去只是换个宿主。

### 5.2 文字原型（表模板 · 展开态）

```
┌─ 模板配置（默认表模板组）──────────────────────────────────────────────────┐
│ ┌ 目录树 30% ─┐┌ 编辑区 ────────────────────┐┌ 变量面板 ~320px ──────────┐│
│ │ [搜索]      ││ 路径：.../Entity.ftl [测试] ││ 变量  ⛶  [搜索变量名/说明] ││
│ │ [刷新][删除] ││ ───────────────────────────││ ─────────────────────────  ││
│ │ ▸ ${backend…││ [Entity.ftl ×][Mapper.ftl ×]││ ▾ 标量变量            (34) ││
│ │   ▸ src      ││ ───────────────────────────││    projectName  字符串  ⓘ  ││
│ │     Entity ✎ ││  1 <#list fieldList as field>││    ▸ 功能名（大驼峰）      ││
│ │     Mapper   ││  2   ${field.attrName}      ││    ▾ fieldList  列表  ⟶插  ││
│ │              ││  3 </#list>                 ││      attrName  字符串  ⟶插 ││
│ │              ││                             ││      ▸ 属性名称            ││
│ │              ││                             ││    ▾ entityBaseClass 对象⚠││
│ │              ││                             ││ ▾ 模板属性（动态 3）       ││
│ └──────────────┘└─────────────────────────────┘└──────────────────────────┘│
└────────────────────────────────────────────────────────────────────────────┘

收起态：编辑区右缘 28px 竖柄，竖排「变量」+ 总项数徽标，点击展开。默认收起。
```

行内元素：

- **变量名**等宽字体，缩进层级即"它属于哪个循环"；
- **类型** `el-tag size="small" effect="plain"`，按 kind 上语义色：`SCALAR`=info、`MODEL_LIST`/`SCALAR_LIST`=primary、`OBJECT`=warning、`MAP`=success；
- **可空** `el-tag type="warning"`「可空」，tooltip 提示需 `<#if xxx??>` 保护；
- **中文说明**走行尾 `ⓘ` + `el-tooltip`（复用现有 `text-tooltip` 处理长文本），hover 整行也触发；
- **插入按钮** hover 才出现，沿用现有 `.tree-node-actions` 的 hover 显隐模式，`link` 形态。

### 5.3 Element Plus 组件清单及理由

| 区域 | 组件 | 理由 |
|---|---|---|
| 面板与编辑区分栏 | `el-splitter` + `el-splitter-panel`（`collapsible`） | 与现有"树 30% + 编辑区"同构，用户已熟悉拖拽；比抽屉/浮层少一层遮挡，可边看变量边写 |
| 面板标题 | `el-text tag="b"` + `el-tag`（总项数）+ `el-button link :icon` | 与 `tree.vue` 现有头部风格一致；纯图标按钮按项目约定用 `link` 形态 |
| 变量搜索 | `el-input size="small" clearable prefix-icon="Search"` | 268 条无过滤即无用；与上方树搜索框同款，不新增视觉词汇 |
| 变量列表 | **单棵 `el-tree`** | 见下方说明 |
| 类型/可空标签 | `el-tag size="small" effect="plain"` | 语义色区分 kind，符合项目 el-tag 三层区分度约定 |
| 说明浮层 | `el-tooltip`（经现有 `text-tooltip`） | 长中文说明必须浮层；项目已有该封装 |
| 插入按钮 | `el-button link size="small"` | hover 显隐，不常驻占宽 |
| 空/异常态 | `el-empty` + `el-skeleton` | 加载失败或该组无属性时给明确文案与重试，不静默空白 |

**为什么是单棵 `el-tree` 而非 `el-collapse` 套列表**：`el-collapse` 管分组折叠、`el-tree` 管变量层级，两套折叠状态叠加会互相打架 —— 分组收起后，孙层变量的搜索命中就找不回来。单棵 `el-tree` 用顶层虚拟节点当分组，一次 `filter-node-method` 可同时命中分组名、变量名和说明，且 `el-tree` 的 filter 天然保留命中节点的祖先链，这正是 268 条目所需。项目里 `el-tree` 已有大量既有用法（`tree.vue` 本体、`el-tree-select`），不引入新组件词汇。

### 5.4 插入语义

`business/code-mirror/index.vue` 增加 `defineExpose({ insertAtCursor })`，复用已有 `view` ref：

```js
view.dispatch({ changes: { from: sel.from, insert }, selection: { anchor: caret } })
```

`tree.vue` 给 `<code-mirror>` 挂 ref 调用。**不需要任何新状态管理**：dispatch → `v-model` 回写 `activeTabItem.templateContent` → 现有那个监听 `[id, content, type]` 的 watch 自动置 `isEdited` → 保存按钮亮起、tab 红点出现。零侵入。

循环骨架是多行插入，`selection.anchor` 要落在循环体内 `<#list ...>` 换行后的缩进位，不是插到末尾。

子变量插入使用目录携带的 `loopVar` 作前缀（`${field.attrName}`）。作者若手改了循环变量名，面板不知道 —— 处理见 6.4。

### 5.5 前端变更清单

| 文件 | 动作 |
|---|---|
| `src/types/api/gen/template.ts` | 新增 `VariableNode`、`VariableKind` 类型。该文件已存在（49 行），`index.ts` 第 11 行已 `export * from '@/types/api/gen/template'`，**无需改 index.ts** |
| `src/api/gen/template.ts` | 新增 `variableCatalog` |
| `src/views/gen/template/variable-utils.ts` | 新增：纯函数层（插入文本、path 合成、总数、kind→标签色） |
| `src/views/gen/template/variable-utils.test.ts` | 新增 |
| `src/views/gen/template/variable-panel/index.vue` | 新增面板组件 |
| `src/views/gen/template/variable-panel/use-variable-catalog.ts` | 新增：加载 hook，按 templateGroupId 缓存 |
| `src/views/gen/template/tree.vue` | 内层 splitter + 面板挂载 + code-mirror ref |
| `src/business/code-mirror/index.vue` | `defineExpose({ insertAtCursor })` |

## 6. 错误处理与降级

对齐项目既有四层错误链路（组件 catch → `onErrorCaptured` → `app.config.errorHandler` → `window.onerror`），各层职责分离，禁止重复提示。

### 6.1 后端

| 场景 | 行为 |
|---|---|
| `templateGroupId` 为 null | `@NotNull` 参数校验拦截 |
| 模板组不存在 | `BusinessException("模板组不存在")`，不得 NPE |
| 组 type 非 0/1/2 | `EnumUtil.getBy` 返回 null → `BusinessException`，不静默返回空列表 |
| 字段缺 `@VarDesc` | **照常列出**，`desc` 空串，前端不显示说明 tooltip |
| `List` 是无泛型 raw type | 元素类型不可判定 → `kind=SCALAR_LIST`、`children` 为空、不可展开，不抛 |
| 循环引用 | 已访问 Class 集合防御，该分支 children 置空 |
| 模板组未配置属性 | `MAP` 节点 children 空列表，前端显示「该模板组未配置属性」 |

缺注解不报错的理由见决策 8：漂移由单测闸门防（7.1 第 5 条），不靠运行时脆。

### 6.2 前端

- **①层 catch 已知业务错误**：目录接口传 `noErrorMessage: true`，由面板内空态承载提示。用户正在写模板，弹一个与编辑无关的全局 error 属噪音；若全局与面板各提示一次即违反"禁止重复提示"。空态文案带失败原因 + 「重试」按钮。
- **①层 catch 只做状态恢复**：置 `error` 状态、保留上一次成功加载的目录（切模板组失败时仍有内容可看），不再调 `ElMessage`。
- **②层 `onErrorCaptured`**：目录结构畸形导致 `el-tree` 渲染崩时返回 `false` 阻断冒泡，面板局部降级为「变量面板不可用」，抽屉主体（树 + 编辑器）继续工作。这是本功能里该层唯一有价值的落点。
- **插入前置守卫**：`view` 未 ready、或当前 tab 是目录（`templateType===0`）/ 二进制（`===2`）无编辑器时，插入按钮 `disabled` + tooltip 说明原因，不 dispatch、不抛错。
- **未处理 Promise**：hook 内所有加载路径显式 `catch`。

### 6.3 降级矩阵

| 场景 | 面板表现 | 是否阻断模板编辑 |
|---|---|---|
| 模板组不存在 / type 非法 | 空态 + 原因 + 重试 | 否 |
| 字段缺 `@VarDesc` | 行正常显示，无说明 tooltip | 否 |
| 模板组无属性配置 | MAP 节点显示「未配置属性」 | 否 |
| raw type / 循环引用 | 该节点不可展开 | 否 |
| 接口 500 / 超时 | 保留上次目录 + 面板内提示重试 | 否 |
| 面板自身渲染异常 | 局部降级为不可用，主体不受影响 | 否 |
| 当前 tab 是目录/二进制 | 面板 disabled + 原因 | — |

### 6.4 已知不完美（明示，不修）

- 作者手改循环变量名后，点子变量插入的路径前缀仍是目录默认值。P0 接受：面板在集合行明示 `as field`，作者改名后需自行调整；
- `nullable` 靠人工标注，Model 新增条件赋值字段若忘了标，目录会显示为必可用。由 7.1 的注解覆盖单测保证"有注解"，不保证"注解内容准确"。

## 7. 测试范围

### 7.1 后端 `VariableCatalogBuilderTest`

JUnit 5 纯单测，`new VariableCatalogBuilder()` 直接实例化，不起 Spring（沿用 `PreviewBuilderRenderTest` 风格）。

1. 表模板根节点数 == `TableModel` 声明字段数（44）；`fieldList` 的 `kind=MODEL_LIST`、`loopVar=field`、children 数 == `TableFieldModel` 字段数（34）；
2. **四层 path 正确性**：项目模板下 `tableModelList.fieldList.attrName` 的 `path` 拼接 —— 插入语义的地基，必须锁死；
3. `EnumModel` 反射结果**不含** `projectName` / `projectNamePascal` / `projectNameUnderline`（把 2.3 的坑固化成测试，防止以后误加字段）；
4. 五个 `List<TableFieldModel>` 各自独立展开、children 内容相同但 `path` 不同；
5. **注解覆盖闸门**：遍历 7 个 Model 类，断言每个字段都有 `@VarDesc` 且 `value()` 非空 —— 这是防漂移的真正机制（决策 8）；
6. raw type 与循环引用降级不抛。

### 7.2 前端 `variable-utils.test.ts`

vitest，沿用 `tree-utils.test.ts` 的纯函数 + 同目录 colocated 风格与 `makeNode` 式最小工厂写法。

1. `buildInsertText(node)`：`SCALAR` → `${path}`；`MODEL_LIST` → 多行骨架文本 + 光标偏移落在循环体内；
2. MAP key 合法标识符 → `.key`；含 `-`、空格、数字开头 → `['key']`；
3. 总项数统计（面板标题徽标）；
4. kind → `el-tag` type/effect 映射，对齐语义色约定；
5. `nullable` 为 true 时输出「可空」标记位。

组件层不做挂载测试：项目现有 5 个测试文件全在纯函数层，不为本次单独引入新范式。

### 7.3 回归基线

- `code-mirror/index.vue` 是被 `test.vue` 等多处复用的公共组件。加 `defineExpose` 向后兼容（`test.vue` 未传 ref，不受影响），但仍须跑 **vue-tsc 全量**并与改动前基线做差分：比对错误总数与涉及文件分布，一致才算未引入回归；只看"有没有报错"会把存量历史报错误判成本次回归。新增文件（面板、utils、类型）的报错逐条排查，它们不在旧基线里。
- `tree.vue` 内层 splitter 化 + `full-screen-mode` class 上移，实测全屏/退出全屏后面板与编辑区的宽度分配、z-index 正常。
- `DataModelBuilder` 182/214 行修复是行为变更，需专项验证：构造"项目模板组配了属性默认值、项目未显式存该 key"的场景，确认表模板与枚举模板的 `${projectTemplatePropertyData.key}` 从报错变为取到默认值。

### 7.4 端到端

后端经 IDEA MCP 以 Debug 模式启停（不用命令行杀进程），前端 `pnpm dev`。三类模板组各开一次面板，各插一次集合骨架与一个子变量，确认 tab 红点与保存按钮状态正确联动；确认二进制/目录 tab 下面板 disabled。

## 8. 不在本次范围

| 项 | 原因 |
|---|---|
| FreeMarker 语法高亮 mode、`${` 自动补全 | 需引入 lang mode + completion source，是独立量级的工作（2.7） |
| 真实变量值预览 | 决策 1 已选反射；如需值预览，`ProductGenerator.testDataModel` 已具备能力，可另立需求 |
| `test.vue` 接入面板 | 组件契约已按可复用设计，接入只是换宿主，作为后续增量 |
| 语义分组映射表 | 3.1 已改为结构自动分区；映射表留作实际使用后的可选增强 |
| `TemplateGroupPropertyService` 删除引用检测 | 现有 `buildPropertyAccessPattern` 已覆盖 `xxxTemplatePropertyData.key`，本次新增的插入文本格式与其一致，无需改动 |
| 历史模板中已存在的错误变量引用扫描 | 需要解析全部 `.ftl` 并比对目录，是独立需求 |

## 9. 交付顺序

1. 后端：`@VarDesc` + `VariableNodeVO` + `VariableKindEnum` + `VariableCatalogBuilder` + 单测（1、2、3、5 条先行）；
2. 后端：126 字段加注解（含 12 处 `loopVar`、12 处 `nullable`，清单见 4.1）；
3. 后端：service 编排 + Controller 端点 + `DataModelBuilder` 缺陷修复；
4. 前端：类型 + API + `variable-utils.ts` 及其单测；
5. 前端：面板组件 + hook；
6. 前端：`code-mirror` expose + `tree.vue` 布局接入；
7. vue-tsc 基线差分 + 端到端验证。

第 1～3 步完成后接口即可独立验证，不必等前端。
