# 代码生成接口统一化重构设计（方案 B）

日期：2026-11-20
状态：已确认（2026-11-20 用户评审通过，含 ProjectFilter 组合修订）
前置：建立在另一会话已完成的 GeneratorServiceImpl 拆分（主类 216 行 + support 包 5 协作类，未提交）基础之上。

## 1. 背景与目标

### 1.1 现状问题

`GeneratorController` 现有 10 个端点，两种分发风格并存：

- **按类型拆方法**（6 个）：`project/table/enum × downloadZip/downloadLocal`，类型放 URL 路径，各配专用 Query DTO；
- **类型字段 + switch**（4 个）：`preview`、`downloadSingle`、`singleLocal`、`templateTest`，类型放请求体字段。

根因：批量生成的三种 Query 参数形状不同（project 是聚合根，带交叉过滤），早期按形状拆端点；单品操作参数天然统一，后期用了新设计。两代风格沉积，操作矩阵（3 产物类型 × 4 操作）未显式建模。

排查中还发现两个实质缺陷：

1. **后端假批量**：`tablePreview`/`enumPreview` 只取 `tableIdList/enumIdList.getFirst()`，前端多选表/枚举批量生成时**静默只生成第一个**。
2. **前端枚举批量传参错误**：`views/gen/enum/index.vue` L235 把 `tableHook.selection.rows`（行对象数组）当 `enumIdList` 传入，后端 `List<Long>` 反序列化必然失败（表页同位置用的是 `selection.ids`，写法不一致）。

### 1.2 目标

- 10 端点收敛为 **6 端点**，统一 Query、统一类型判别符；
- 后端以**产物处理器策略**显式建模操作矩阵，消灭全部 PROJECT/TABLE/ENUM switch；
- 修复假批量与枚举批量传参两个缺陷；
- 前端 API 层同步重写并补齐 TypeScript 类型（现状全 `any`）。

用户已确认：前端可直接修改，大胆重构，**不做旧端点双轨兼容**。

## 2. 现状盘点（已读码验证）

### 2.1 前端调用点清单（共 5 文件 11 处）

| 调用点 | API | 传参 | 备注 |
|---|---|---|---|
| `business/preview/index.vue` L272 | `preview` POST | `{previewProductId, generatorProductType}` | 预览抽屉，project/table/enum 三个列表页打开 |
| `business/preview/index.vue` L450/453 | `downloadSingle` GET / `singleLocal` POST | `{id, templateId, templateGroupType}` | id 按 groupType 取 projectId/tableId/enumId，**类型与产物恒匹配** |
| `views/gen/template/test.vue` L344 | `templateTest` POST | `{projectId, templateGroupId, templateGroupType, templateId, templateContent, testId}` | 错误码 700 = 表达式 false |
| `views/gen/template/test.vue` L449/452 | `downloadSingle` / `singleLocal` | 同上单品形状 | |
| `views/gen/table/template-index.vue` L104/107 | `tableDownloadZip` GET / `tableDownloadLocal` POST | `{templateIdList, tableIdList}` | 批量入口 `table/index.vue` L399 传 `selection.ids`（可多个）→ 命中假批量缺陷 |
| `views/gen/enum/template-index.vue` L95 | `enumDownloadZip` / `enumDownloadLocal` | `{enumIdList, templateIdList}` | 批量入口 `enum/index.vue` L235 传 `selection.rows` → 命中传参缺陷 |
| `views/gen/project/generate-result-v2.vue` L127/129 | `projectDownloadZip` GET / `projectDownloadLocal` POST | GeneratorProjectQuery 6 字段 | 由 `steps-v2.vue` L177-184 向导组装 |

### 2.2 后端影响面

- `GeneratorService` 仅被 `GeneratorController` 引用，**无 MCP 工具或其他调用方**（全仓 grep 验证）；
- 5 个旧 Query DTO 与 `GeneratorProductTypeEnum` 仅在 Controller/Service/Impl 链内使用，可安全删除；
- `GeneratorProductTypeEnum` 与 `TemplateGroupTypeEnum` code 值完全一致（PROJECT=0/TABLE=1/ENUM=2）；前端 `GENERATOR_PRODUCT_TYPES` 与 `TEMPLATE_GROUP_TYPES` 同样一致；
- GET 下载走 `downloadFile`（XHR blob），`qs.stringify(indices:false)` 把列表序列化为重复键（`tableIdList=1&tableIdList=2`），Spring `@ParameterObject` 绑定 `List<Long>` 已被现有 `project/downloadZip` 验证可行；
- MSW handlers 仅 auth 与 _shared，**无生成接口 mock**，无需改动；
- `generatorType`（0=zip/浏览器，1=服务器本地）是项目属性，决定前端调哪个端点，与本次重构正交，不动。

## 3. 目标 API 设计

### 3.1 端点（10 → 6）

基路径 `/gen/generator` 不变：

| 端点 | 方法 | 绑定 | 返回 | 替代 |
|---|---|---|---|---|
| `/preview` | POST | `@RequestBody GeneratorQuery` | `List<PreviewTemplateVO>` | 原 preview |
| `/downloadZip` | POST | `@RequestBody GeneratorQuery` | zip 字节流 | project/table/enum DownloadZip ×3 |
| `/downloadLocal` | POST | `@RequestBody GeneratorQuery` | void | project/table/enum DownloadLocal ×3 |
| `/downloadSingle` | POST | `@RequestBody GeneratorQuery` | 单文件字节流 | 原 downloadSingle |
| `/singleLocal` | POST | `@RequestBody GeneratorQuery` | void | 原 singleLocal |
| `/template/test` | POST | `@RequestBody TemplateTestQuery`（不变） | `TemplateContentVO` | 不变 |

**6 端点全 POST**（2026-11-20 用户决策）：下载走 `downloadFile` 的 XHR blob + `a[download]` 机制，与 HTTP 方法无关；POST body 原生承载嵌套 `projectFilter`（无需 qs allowDots 改造），且无 GET 查询串长度上限风险（Tomcat 请求行约 8KB，向导多选场景可能超限）。GET 的可缓存/可分享优势对生成下载无价值。

旧端点全部删除，不留兼容别名。knife4j `@ApiOperationSupport(order)` 重排为 1-6。

### 3.2 统一 GeneratorQuery + ProjectFilter（组合方案）

PROJECT 专属字段独立成 `ProjectFilter` 类，由 `GeneratorQuery` 持有（组合）。**不采用继承**（`ProjectGeneratorQuery extends GeneratorQuery`）：即便全 POST，继承仍需 Jackson 多态反序列化配置（`@JsonTypeInfo` 把整数判别符映射到子类）、handler 内向下转型，knife4j schema 也变复杂；组合无以上任何成本。

```java
@Data
@Schema(description = "代码生成统一查询")
public class GeneratorQuery {
    @Schema(description = "产物类型：0项目 1表 2枚举")
    private Integer productType;          // 必填

    @Schema(description = "产物ID列表：项目=[projectId]，表=tableIds，枚举=enumIds")
    private List<Long> productIds;        // 必填非空

    @Schema(description = "模板ID列表，空=该产物模板组全部模板；PROJECT 时指项目级模板")
    private List<Long> templateIds;       // 可选

    @Schema(description = "项目子产物过滤，仅 productType=PROJECT 时允许非 null")
    private ProjectFilter projectFilter;  // 可选
}

@Data
@Schema(description = "项目生成的子产物过滤（仅PROJECT）")
public class ProjectFilter {
    @Schema(description = "项目内表ID过滤，空=项目下全部表")
    private List<Long> tableIds;
    @Schema(description = "项目内枚举ID过滤，空=全部枚举")
    private List<Long> enumIds;
    @Schema(description = "表模板ID过滤，空=表模板组全量")
    private List<Long> tableTemplateIds;
    @Schema(description = "枚举模板ID过滤，空=枚举模板组全量")
    private List<Long> enumTemplateIds;
}
```

项目向导完整生成示例：

```json
{ "productType": 0, "productIds": [100], "templateIds": [11, 12],
  "projectFilter": { "tableIds": [201, 202], "enumIds": [301],
                     "tableTemplateIds": [21, 22], "enumTemplateIds": [31] } }
```

`ProjectFilter` 为 null 时项目生成按全量子产物处理；`ProjectGenerator` 直接读 `query.getProjectFilter()`，无转型。

### 3.3 校验规则（service 统一入口执行，违例抛 BusinessException）

1. `productType` 必须能解析为 `TemplateGroupTypeEnum`，否则报"产物类型异常: {value}"（顺带修复现状非法 code → NPE 的问题）；
2. `productIds` 非空，否则报"产物ID列表为空"；
3. `productType=PROJECT` 时 `productIds.size()==1`（项目是单聚合根）；
4. `downloadSingle`/`singleLocal` 额外要求 `productIds.size()==1 && templateIds.size()==1`；
5. 非 PROJECT 且 `projectFilter != null` → 报"产物类型与过滤字段不匹配"（严格模式，暴露误用而非静默忽略）。

校验集中在 `GeneratorQuery.validate(boolean singleMode)` 实例方法（内聚、无额外类），由 `GeneratorServiceImpl` 各入口调用。

## 4. 后端架构

### 4.1 产物处理器策略（操作矩阵显式化）

support 包新增：

```java
public interface ProductGenerator {
    TemplateGroupTypeEnum type();
    List<TemplateContentVO> generate(GeneratorQuery query);
    String artifactName(GeneratorQuery query);   // zip 命名收口
}
```

| 实现 | generate | artifactName |
|---|---|---|
| `ProjectGenerator` | 适配 query → `PreviewBuilder.buildProjectPreview`（表/枚举/项目级三段） | 项目名 |
| `TableGenerator` | **循环 productIds** 逐表调 `PreviewBuilder.tablePreview(tableId, templateIds)` 汇总 | 表名 `_` 拼接 |
| `EnumGenerator` | **循环 productIds** 逐枚举调 `PreviewBuilder.enumPreview(enumId, templateIds)` 汇总 | 枚举名 `_` 拼接 |

Spring 注入 `List<ProductGenerator>`，主类构造 `EnumMap<TemplateGroupTypeEnum, ProductGenerator>`。新增产物类型 = 新增 1 个 handler，零 switch。

### 4.2 GeneratorService 新接口（6 方法）

```java
public interface GeneratorService {
    List<PreviewTemplateVO> preview(GeneratorQuery query);
    ResponseEntity<byte[]> downloadZip(GeneratorQuery query);
    void downloadLocal(GeneratorQuery query);
    ResponseEntity<byte[]> downloadSingle(GeneratorQuery query);
    void singleLocal(GeneratorQuery query);
    TemplateContentVO templateTest(TemplateTestQuery query);
}
```

- 去掉全部 `throws Exception/IOException` 声明（`TemplateUtil.renderTemplate` 已验证不抛受检异常；实施时若发现 `AviatorUtil` 等抛受检异常，在 support 层包装为 BusinessException，不外溢）；
- `GeneratorServiceImpl` 保持薄路由：校验 → 查 handler → generate → 交 `DownloadHandler`/`PreviewTreeBuilder`；`templateTest` 与 `renderForTest` 沿用现有实现。

### 4.3 既有 support 类的调整

- `PreviewBuilder`：
  - `tablePreview(GeneratorTableQuery)` → `tablePreview(Long tableId, List<Long> templateIds)`；`enumPreview` 同理改单产物签名（批量循环上移到 handler）；
  - `buildProjectPreview(GeneratorProjectQuery)` → `buildProjectPreview(GeneratorQuery)`（内部读 `query.getProjectFilter()`，null 视为全量子产物）；
  - `leafTemplates/checkCondition/render/renderGroupTemplates` 渲染原语不动；
  - `resolveDatasource` 从 PreviewBuilder 移入 `DataModelBuilder`（修复归属别扭：它服务于数据模型组装）。
- `DataModelBuilder` / `DownloadHandler` / `PreviewTreeBuilder` / `TemplatePathResolver`：不动。
- 顺手清理（上轮评审 P3 项）：`renderGroupTemplates` 的 peek+no-op 改 map 或无 filler 重载；`DownloadHandler` 类注释"消除三处 switch"改为"集中管理"。

### 4.4 删除清单

- `GeneratorProductTypeEnum`（统一用 `TemplateGroupTypeEnum`，code 已验证一致）；
- `CodePreviewQuery`、`CodeSingleGeneratorQuery`、`GeneratorProjectQuery`、`GeneratorTableQuery`、`GeneratorEnumQuery`；
- Controller/Service/Impl 中全部旧方法与 switch。

## 5. 语义决策（显式非等价项，均已获用户授权）

| # | 决策 | 说明 |
|---|---|---|
| 1 | 表/枚举批量改**真批量** | 修复 getFirst 缺陷；zip 内多表产物按各自 generatorPath 共存，路径冲突时 zip 条目覆盖（与现状单表行为一致，不新增冲突处理） |
| 2 | 枚举批量前端 rows→ids | 修复传参缺陷 |
| 3 | `singleLocal` PROJECT 语义 = 仅项目级模板 | 与 downloadSingle 对齐；已验证前端单品操作类型与产物恒匹配，旧"全项目渲染再过滤"路径不可达 |
| 4 | 非法 productType：NPE → BusinessException | 报错友好化 |
| 5 | 旧端点直接删除 | 前后端同一变更内切换，无双轨 |
| 6 | 空结果行为保持现状 | downloadZip 空列表抛"待写入数据为空"；downloadLocal 空列表 warn 跳过；downloadSingle 空列表 getFirst 抛 NoSuchElementException（本次不改，后续可友好化） |

## 6. 前端改造

### 6.1 API 层与类型

- 新建 `src/types/api/gen/generator.ts` 定义 `GeneratorQuery`（productType/productIds/templateIds/projectFilter?）与 `ProjectFilter`（4 个可选 ID 列表）两个接口，并在同目录 `index.ts` 追加 re-export（与现有 12 个 gen 类型文件组织一致）；项目向导调用点另定义 `interface ProjectGeneratorQuery extends GeneratorQuery { projectFilter: ProjectFilter }`，用 TS 继承获得 projectFilter 必填约束（类型层继承无后端绑定问题）；
- `src/api/gen/generator.ts` 重写为 6 方法，入参全部类型化，去除 `any`；`downloadZip` 保留 `noErrorMessage` 可选参（generate-result-v2 在用）；
- **downloadFile 扩展 POST**：`src/utils/request.ts` 的 `downloadFile` 追加第 4 个可选参 `method: 'get' | 'post' = 'get'`，post 分支走 `service.post(url, params, {responseType:'blob', noErrorMessage})`，blob→a[download] 后续逻辑不变。默认 GET 保住 common export 契约（后端 `TemplateGroupController`/`TemplateGroupPropertyController` 的 `/export` 均为 `@GetMapping`，已验证）；生成 API 的 downloadZip/downloadSingle 显式传 `'post'`。**paramsSerializer 与 allowDots 不再需要任何改动**；`downloadBigFile`（a 标签 URL 导航，仅 GET）本次不动；
- `GENERATOR_PRODUCT_TYPES` / `TEMPLATE_GROUP_TYPES` 两个展示字典保留（label 文案不同），仅请求字段名统一为 `productType`。

### 6.2 调用点改造映射

| 文件 | 改造 |
|---|---|
| `business/preview/index.vue` | preview：`{productType, productIds:[id]}`；downloadTemplateData：`{productType: item.templateGroupType, productIds:[id], templateIds:[item.templateId]}`，id 取值三分支逻辑保留 |
| `views/gen/template/test.vue` | templateTest 不动；generatorHandler 单品传参同上形状 |
| `views/gen/table/template-index.vue` | `{productType:1, productIds: tableIdRef, templateIds}` |
| `views/gen/enum/template-index.vue` | `{productType:2, productIds: enumIdList, templateIds}` |
| `views/gen/enum/index.vue` | **L235 `selection.rows` → `selection.ids`**（缺陷修复） |
| `views/gen/project/steps-v2.vue` | generateCode：`{productType:0, productIds:[projectId], templateIds: 项目级模板ids, projectFilter:{tableIds, enumIds, tableTemplateIds, enumTemplateIds}}` |
| `views/gen/project/generate-result-v2.vue` | 仅 API 名变更（projectDownloadZip→downloadZip 等），init 签名与状态机不动 |

各弹窗组件 `init` 对外签名不变，改动收敛在 dataForm 组装处。

## 7. 测试与验证计划

1. **编译级**：后端 `mvn compile` + IDEA `build_project`；前端 `vue-tsc` 类型检查 + ESLint；
2. **启动**：按既定规范 Debug 模式（`xdebug_start_debugger_session`，configurationName=CodeGeneratorApplication；启动前先向用户确认 IDEA 面板无运行实例；读 `logs/code-generator-info.log` 确认 Started）；
3. **冒烟矩阵**（浏览器 MCP 或手工）：
   - 项目向导：zip 下载 + 本地生成（含表/枚举/模板勾选过滤）；
   - 表：单表生成 + **多选 2 表批量**（zip 与本地各一次，验证两表产物齐全——缺陷修复回归点）；
   - 枚举：单枚举 + **多选批量**（验证 rows→ids 修复后请求体为 ID 数组）；
   - 预览树：project/table/enum 三入口；
   - 预览抽屉单文件下载/本地：项目级、表、枚举三种模板各一；
   - 模板测试页：正常渲染 + 表达式 false（前端提示 700 分支）+ 单品生成；
   - 非法入参：productType=99 → BusinessException 文案；TABLE 携带 projectFilter → 校验报错；
   - **POST 嵌套绑定**：项目向导 zip 下载抓包确认请求体 `projectFilter` 嵌套结构完整、后端过滤生效、响应正常触发浏览器下载。
4. **单元测试（TDD 必做）**：`GeneratorQuery.validate` 7 例 + ProductGenerator 三实现路由/真批量/zip 命名 5 例（项目当前零测试，本次为首批单测，spring-boot-starter-test 已在 pom）。

## 8. 执行步骤

1. 建议先把上一会话的拆分改动提交为一个独立 commit（基线隔离，便于回滚；提交需用户点头）；
2. 后端：GeneratorQuery + 校验 → ProductGenerator×3 → PreviewBuilder 签名调整 → 新 Controller/Service → 删除旧 DTO/枚举/方法 → 编译；
3. 前端：types + api 重写 → 7 处调用点改造 + enum rows 缺陷修复 → 类型检查/lint；
4. Debug 启动 + 冒烟矩阵；
5. 更新记忆（拆分方案状态、接口契约、假批量缺陷修复记录）。

## 9. 风险与回滚

- **下载请求方式**：downloadZip/downloadSingle 改 POST，`downloadFile` 扩展 method 参数（默认 get，common export 不受影响）；blob 下载机制与方法无关，冒烟矩阵含专项抓包验证；
- **knife4j 文档**：端点与 schema 全变，依赖旧 API 文档的外部使用者需知会（本工具为内网自用，影响可控）；
- **回滚**：前后端各自独立 commit，任一侧可单独 revert；旧端点删除后无服务端兼容层，回滚必须前后端成对；
- **未包含项**（明确排除，另行立项）：downloadLocal 路径越界写入防护（P0 安全遗留）、TemplateUtil 模板缓存、N+1 查询优化。