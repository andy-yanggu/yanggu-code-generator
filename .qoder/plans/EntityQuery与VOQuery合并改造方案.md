# EntityQuery + VOQuery 合并改造方案（方案 A）

> 范围：**仅 Java 代码**。`.ftl` 生成模板与 `gen_template` 数据表的同步列为后续待办（见第十一章）。
> 本轮改造不改变对外 HTTP 契约的请求体字段与响应体结构，前端 0 改动。

---

## 一、目标与决策依据

### 1.1 要解决的问题

11 张业务表各有 `XxxEntityQuery` + `XxxVOQuery` 两个查询类，字段**逐字段相同**，唯一差异是 `PageQuery<T>` 的泛型实参（`T=Entity` vs `T=VO`）。其中 5 个 VOQuery 是零业务字段的空壳。

### 1.2 泛型约束的真实来源（实测）

| 约束点 | 是否真正依赖泛型 | 取证 |
|---|---|---|
| `BaseMapper.selectPage` | **是（唯一硬约束）** | `javap` 实测 MP 3.5.14：`public default <P extends IPage<T>> P selectPage(P, Wrapper<T>)`；`T` 被 `BaseMapperPlus<ProjectEntity>` 钉死 |
| XML 分页判定 | 否 | `ParameterUtils.findPage(Object)` 返回**裸类型** `Optional<IPage>`，按 `instanceof IPage` 扫描参数，泛型已擦除且不参与判定 |
| XML 排序 | 否 | `BaseMapperPlus.xml` 的 `prefixOrderBySQL` 直接 OGNL 遍历 `query.orderItemList`，不读 `IPage.orders()` |
| Wrapper 排序 | 否 | `MybatisUtil.orderBy(wrapper, query.getOrderItemList())` |
| `PageQuery.orders()` | 反向 | 显式 `return List.of()`，注释「返回空数组，让框架不做排序处理」 |

**结论**：泛型 `T` 只为满足 `selectPage` 的编译期约束而存在。合并的正解不是调和两个泛型，而是**把泛型从入参对象挪到分页载体上**。

### 1.3 核心手法

```java
// PageQuery 不再 implements IPage，改为"产出一个分页载体"
public <T> Page<T> toPage() { return Page.of(pageNum, pageSize); }
```

`toPage()` 的 `<T>` 由调用点目标类型推断 → 同一个 Query 既能产出 `Page<Entity>` 也能产出 `Page<VO>`，泛型冲突就地消失。

---

## 二、影响面总览（实测计数）

| 层 | 文件数 | 动作 |
|---|---|---|
| 底座（common） | 4 | 修改 |
| Query | 11 | 修改（保留 EntityQuery 改名）|
| Query | 11 | **删除**（全部 VOQuery）|
| Mapper 接口 | 11 | 修改（每文件 2 个方法签名）|
| Service 接口 | 11 | 修改（类型引用改名）|
| ServiceImpl | 11 | 修改（每文件 4 个方法体）|
| Controller | 11 | 修改（62 处 Query 引用）|
| Mapper.xml | **0** | **不改**（理由见 5.4）|
| Mapstruct 业务接口 | 0 | 不改（default 方法在底座）|
| Entity / VO / DTO | 0 | 不改 |
| 前端 | 0 | 不改（`types/api/common` 的 `PageQuery` 本就只含条件字段）|

**合计**：修改 59 个 java 文件、删除 11 个、XML 与前端零改动。

关键调用点计数（改造时必须逐一覆盖）：

- `selectPage(query, buildQueryWrapper(query))` → **10 处**（TableField 无 entityPage）
- `entityToPageVO(query)` → **10 处**
- `voToPageVO(query)` → **10 处**
- `mapper.voPage(query)` → **10 处**
- `mapper.voList(query)` → **10 处**
- `query.setPageSize(-1L)` → **22 处**（分布见第七章）

---

## 三、底座改造 diff（4 个文件）

### 3.1 `common/domain/query/PageQuery.java`

**Before（118 行）**：`class PageQuery<T> implements IPage<T>, Serializable`，含 `records / total / size / current / pages` 5 个输出字段与 5 个 `@Schema(hidden = true)`，并覆写 `getSize/setSize/getCurrent/setCurrent/getRecords/setRecords/getTotal/setTotal/orders` 共 9 个方法。

**After（约 45 行）**：

```java
package com.yanggu.code.generator.common.domain.query;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 分页查询参数基类（仅承载查询条件与分页参数，不承载分页结果）
 *
 * <p>分页结果容器请使用 {@link #toPage()} 产出的 Page，返回值统一封装为 PageVO</p>
 */
@Data
@Schema(description = "分页查询参数基类")
public class PageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 2101820826443930585L;

    /**
     * 当前页码
     */
    @Schema(description = "当前页码", defaultValue = "1")
    private Long pageNum = 1L;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小", defaultValue = "10")
    private Long pageSize = 10L;

    /**
     * 排序字段列表，默认按updateTime和id降序
     */
    @Schema(description = "排序字段列表，默认按updateTime和id降序")
    private List<OrderItemQuery> orderItemList = OrderItemQuery.descs("updateTime", "id");

    /**
     * 产出 mybatis-plus 分页载体，泛型由调用点目标类型推断
     *
     * <p>调用规约：必须先赋值给显式声明类型的局部变量，再传入 mapper 方法，
     * 不做链式内联（嵌套泛型方法推导在 selectPage 上易出歧义）</p>
     */
    public <T> Page<T> toPage() {
        return Page.of(this.pageNum, this.pageSize);
    }

    /**
     * 产出 mybatis-plus 分页载体并指定是否执行 count 查询
     */
    public <T> Page<T> toPage(boolean searchCount) {
        return Page.of(this.pageNum, this.pageSize, searchCount);
    }

}
```

**删除项**：`records / total / size / current / pages` 字段、9 个 `IPage` 覆写方法、`implements IPage<T>`、类上的 `<T>`、`IPage`/`OrderItem` import。

**依赖确认**：`Page` 位于 `mybatis-plus-extension`，该 artifact 已在编译期使用（`BaseMapperPlus.java:5` import `...extension.toolkit.Db`），无需新增依赖。

### 3.2 `common/domain/vo/PageVO.java`

把 `PageVO(PageQuery<T>)` 构造器替换为 `PageVO(IPage<T>)`，其余 2 个构造器保持：

```java
    /**
     * 由 mybatis-plus 分页载体构造 PageVO（records 已是最终类型）
     */
    public PageVO(IPage<T> page) {
        this.pageNum = page.getCurrent();
        this.pageSize = page.getSize();
        this.records = page.getRecords();
        this.total = page.getTotal();
    }
```

- `IPage` 的 `getCurrent()/getSize()/getTotal()` 均返回 `long`（已 javap 确认），自动装箱进 `Long` 成立。
- 新增 import `com.baomidou.mybatisplus.core.metadata.IPage`；移除对 `PageQuery` 的 import。

### 3.3 `common/mapstruct/PageEntityMapstruct.java`

```java
    /**
     * 将Entity分页数据转换为VO分页数据
     */
    default PageVO<V> entityToPageVO(IPage<E> page) {
        return new PageVO<>(page.getCurrent(), page.getSize(), page.getTotal(),
                this.entityToVO(page.getRecords()));
    }
```

- 这里**不能**用 `new PageVO<>(page)`，因为 `records` 是 `List<E>` 需转 `List<V>`，走 4 参构造器。
- import 由 `PageQuery` 换为 `IPage`；`java.util.List` import 可移除。

### 3.4 `common/mapstruct/PageVOMapstruct.java`

```java
    /**
     * 将VO分页数据转换成PageVO分页数据
     */
    default PageVO<V> voToPageVO(IPage<V> page) {
        return new PageVO<>(page);
    }
```

- `records` 已是 `List<V>`，直接用 `IPage` 构造器。

### 3.5 `OrderItemQuery.java`

**不改动**。排序数据仍挂在 Query 上，wrapper 与 XML 两条路都直接读它。

---

## 四、Query 类合并规则

11 组：保留原 `XxxEntityQuery` 文件与字段，改名 `XxxQuery`；删除 `XxxVOQuery`。

改名映射表：

| 删除 | 保留并改名 | 字段来源 |
|---|---|---|
| `BaseClassVOQuery` | `BaseClassEntityQuery`→`BaseClassQuery` | 保留 EntityQuery 的字段 |
| `DatasourceVOQuery` | `DatasourceEntityQuery`→`DatasourceQuery` | 同上 |
| `EnumVOQuery` | `EnumEntityQuery`→`EnumQuery` | 同上 |
| `EnumItemVOQuery` | `EnumItemEntityQuery`→`EnumItemQuery` | 同上 |
| `FieldTypeVOQuery` | `FieldTypeEntityQuery`→`FieldTypeQuery` | 同上 |
| `ProjectVOQuery` | `ProjectEntityQuery`→`ProjectQuery` | 同上 |
| `TableFieldVOQuery` | `TableFieldEntityQuery`→`TableFieldQuery` | 同上 |
| `TableVOQuery` | `TableEntityQuery`→`TableQuery` | 同上 |
| `TemplateVOQuery` | `TemplateEntityQuery`→`TemplateQuery` | 同上 |
| `TemplateGroupVOQuery` | `TemplateGroupEntityQuery`→`TemplateGroupQuery` | 同上 |
| `TemplateGroupPropertyVOQuery` | `TemplateGroupPropertyEntityQuery`→`TemplateGroupPropertyQuery` | 同上 |

改造后的类形态（以 Project 为例，字段照抄现状，仅父类与泛型变化）：

```java
package com.yanggu.code.generator.domain.query;

import com.yanggu.code.generator.common.domain.query.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 项目查询实体类
 */
@Data
@Schema(description = "项目查询实体类")
@EqualsAndHashCode(callSuper = true)
public class ProjectQuery extends PageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "生成方式")
    private Integer generatorType;

    @Schema(description = "开始时间")
    private Date startTime;

    @Schema(description = "结束时间")
    private Date endTime;

}
```

**注意**：去掉泛型后 `extends PageQuery implements Serializable` 成立（`PageQuery` 本身已 Serializable，此处保留显式 implements 以维持各表既有写法一致性，不加 `@EqualsAndHashCode` 之外的改动）。

**8 个专项 Query 不动**：`CodePreviewQuery`、`CodeSingleGeneratorQuery`、`GeneratorEnumQuery`、`GeneratorProjectQuery`、`GeneratorTableQuery`、`ProjectTableQuery`、`TemplateGroupDetailQuery`、`TemplateTestQuery` —— 它们不继承 `PageQuery`，不在本次范围。

---

## 五、统一改造模式（试点模块 BaseClass 全量前后对比）

选 BaseClass 作为试点：字段最少（4 个），且 `entityPage` + `voPage` 双通道都被前端实际调用。

### 5.1 Mapper 接口

```java
// Before
IPage<BaseClassEntity> entityPage(@Param("query") BaseClassEntityQuery query);
List<BaseClassEntity> entityList(@Param("query") BaseClassEntityQuery query);
IPage<BaseClassVO>     voPage(@Param("query") BaseClassVOQuery query);
List<BaseClassVO>      voList(@Param("query") BaseClassVOQuery query);

// After（page 与 query 双参数；list 通道不传 page）
IPage<BaseClassEntity> entityPage(IPage<BaseClassEntity> page, @Param("query") BaseClassQuery query);
List<BaseClassEntity>  entityList(@Param("query") BaseClassQuery query);
IPage<BaseClassVO>     voPage(IPage<BaseClassVO> page, @Param("query") BaseClassQuery query);
List<BaseClassVO>      voList(@Param("query") BaseClassQuery query);
```

### 5.2 Service 接口

```java
// Before
PageVO<BaseClassVO> entityPage(BaseClassEntityQuery query);
List<BaseClassVO>   entityList(BaseClassEntityQuery query);
PageVO<BaseClassVO> voPage(BaseClassVOQuery query);
List<BaseClassVO>   voList(BaseClassVOQuery query);

// After
PageVO<BaseClassVO> entityPage(BaseClassQuery query);
List<BaseClassVO>   entityList(BaseClassQuery query);
PageVO<BaseClassVO> voPage(BaseClassQuery query);
List<BaseClassVO>   voList(BaseClassQuery query);
```

### 5.3 ServiceImpl

```java
    /**
     * 简单分页
     */
    @Override
    public PageVO<BaseClassVO> entityPage(BaseClassQuery query) {
        //简单sql使用QueryWrapper
        Page<BaseClassEntity> page = query.toPage();
        baseClassMapper.selectPage(page, buildQueryWrapper(query));
        //较复杂sql，使用xml
        //Page<BaseClassEntity> page = query.toPage();
        //baseClassMapper.entityPage(page, query);
        return baseClassMapstruct.entityToPageVO(page);
    }

    /**
     * 简单列表
     */
    @Override
    public List<BaseClassVO> entityList(BaseClassQuery query) {
        //不分页：不传 page 即不触发分页插件，替代原 query.setPageSize(-1L)
        List<BaseClassEntity> entityList = baseClassMapper.selectList(buildQueryWrapper(query));
        return baseClassMapstruct.entityToVO(entityList);
    }

    /**
     * 复杂分页
     */
    @Override
    public PageVO<BaseClassVO> voPage(BaseClassQuery query) {
        Page<BaseClassVO> page = query.toPage();
        baseClassMapper.voPage(page, query);
        return baseClassMapstruct.voToPageVO(page);
    }

    /**
     * 复杂列表
     */
    @Override
    public List<BaseClassVO> voList(BaseClassQuery query) {
        //查询全部数据：不传 page
        return baseClassMapper.voList(query);
    }
```

新增 import：`com.baomidou.mybatisplus.extension.plugins.pagination.Page`。

### 5.4 Mapper.xml —— **本次不改**（重要）

`BaseClassMapper.xml` 中的 `entityPage / entityList / voPage / voList` 四条语句**保持原样**，理由：

- SQL 内引用条件是 `#{query.xxx}` 与 `query.orderItemList`（`whereSQL`、`prefixOrderBySQL`），参数名 `query` 未变；
- 多出的 `page` 形参由 `PaginationInnerInterceptor` 通过 `ParameterUtils.findPage` 从参数表按 `instanceof IPage` 识别，**不需要 XML 声明它**；
- 排序仍走 `prefixOrderBySQL`，行为不变。

因此 XML 层零改动，这是本方案的主要降风险点。**已实测确认**：12 个 mapper.xml（11 业务 + `BaseMapperPlus.xml`）中不存在任何对 `records` / `.total` 的引用，故不存在需要调整的语句。

### 5.5 Controller

```java
// Before
public PageVO<BaseClassVO> entityPage(@RequestBody BaseClassEntityQuery query)
public List<BaseClassVO>   entityList(@RequestBody BaseClassEntityQuery query)
public PageVO<BaseClassVO> voPage(@RequestBody BaseClassVOQuery query)
public List<BaseClassVO>   voList(@RequestBody BaseClassVOQuery query)

// After
public PageVO<BaseClassVO> entityPage(@RequestBody BaseClassQuery query)
public List<BaseClassVO>   entityList(@RequestBody BaseClassQuery query)
public PageVO<BaseClassVO> voPage(@RequestBody BaseClassQuery query)
public List<BaseClassVO>   voList(@RequestBody BaseClassQuery query)
```

**端点 URL、请求体字段、响应体结构全部不变** → 前端零改动。

---

## 六、11 张表逐表影响清单

每表需改 5 个 java 文件（Query、Mapper、Service、ServiceImpl、Controller）+ 删 1 个 VOQuery。

| # | 模块 | Query 合并 | 需改方法数 | `selectPage` | `voPage` | `voList` | `setPageSize(-1L)` | 备注 |
|---|---|---|---|---|---|---|---|---|
| 1 | BaseClass | `BaseClassQuery` | 4 | 114 | 138 | 149 | 125 / 148 | 试点 |
| 2 | Datasource | `DatasourceQuery` | 4 | 119 | 143 | 154 | 130 / 153 | VOQuery 为空壳 |
| 3 | Enum | `EnumQuery` | 4 | 134 | 158 | 169 | 145 / 168 | |
| 4 | EnumItem | `EnumItemQuery` | 4 + `selectByEnumId` | 107 | 131 | 142 | 118 / 141 / **149** | 第 3 处 `-1` 在 `selectByEnumId`，它走 **XML `entityList`**，见 7.2 |
| 5 | FieldType | `FieldTypeQuery` | 4 | 112 | 136 | 147 | 123 / 146 | VOQuery 为空壳 |
| 6 | Project | `ProjectQuery` | 4 | 151 | 175 | 186 | 162 / 185 | `voPage` 含 JOIN，回归重点 |
| 7 | Table | `TableQuery` | 4 | 135 | 159 | 170 | 146 / 169 | |
| 8 | TableField | `TableFieldQuery` | **仅 entityList** | 无 | 无 | 无 | 47 | Mapper 声明的 4 个 XML 方法**全部无调用方**（`entityList` 仅出现在注释行），见 7.3 |
| 9 | Template | `TemplateQuery` | 4 | 144 | 168 | 179 | 155 / 178 | |
| 10 | TemplateGroup | `TemplateGroupQuery` | 4 | 164 | 188 | 199 | 175 / 198 | `voList` 前端有调用 |
| 11 | TemplateGroupProperty | `TemplateGroupPropertyQuery` | 4 | 125 | 149 | 160 | 136 / 159 | VOQuery 字段极少 |

行号为改造前定位参考，实际以方法名匹配为准（连续编辑后行号会漂移）。

---

## 七、连带清理项

### 7.1 `setPageSize(-1L)` 22 处 → 全部删除

原语义：`query` 自身是 `IPage`，靠 `pageSize=-1` 让分页插件跳过 limit。改造后 list 通道**不传 page**，插件 `findPage` 返回 `Optional.empty()`，天然不分页 —— 语义等价且不再依赖魔法值。

- 20 处位于 `entityList` / `voList`（11 张表，TableField 仅 1 处）
- 2 处需单独确认语义：`EnumItemServiceImpl:149`、`TableFieldServiceImpl:47`

### 7.2 `EnumItemServiceImpl:149`（已读方法体确认）

```java
public List<EnumItemEntity> selectByEnumId(Long enumId) {
    EnumItemEntityQuery enumItemEntityQuery = new EnumItemEntityQuery();
    enumItemEntityQuery.setEnumId(enumId);
    enumItemEntityQuery.setPageSize(-1L);
    return enumItemMapper.entityList(enumItemEntityQuery);   // 走 XML entityList，返回 List
}
```

确认结论：它调用的是 **XML `entityList`**（返回 `List`，非分页语句）。改造后该语句不需要 `IPage` 参数，`setPageSize(-1L)` **直接删除**即可，行为等价。

顺带注意：这是全项目**唯一**一处真正使用 XML `entityPage/entityList` 语句的地方（其余模块的 XML Entity 语句调用点都是注释），说明 `entityList` XML 通道并非完全死代码，改造时不要顺手删掉它的调用。

### 7.3 `TableFieldMapper` 的 4 个死方法（已确认）

`TableFieldMapper` 声明 `entityPage / entityList / voPage / voList`，但 `TableFieldService` 只有 `entityList`，且其实现走 `selectList(buildQueryWrapper(query))`；`tableFieldMapper.entityList(query)` 只出现在注释行（`TableFieldServiceImpl:51`）。因此这 4 个 XML 方法**当前全部无调用方**（`TableFieldVOQuery` 仅 3 处引用即由此而来）。

**本轮只改名引用、不删方法**，避免与本次改造混在一个 diff 里影响 review 与回滚。

### 7.4 建议顺手加的可观测性（可选，不阻塞）

`PageQuery` 去掉 `records/total` 后，误用 `query.getRecords()` 会编译失败 —— 这本身就是想要的效果（错误立刻暴露）。若想更早拦截，可给 `pageNum/pageSize` 加 `@Min(1)` 并在 `toPage()` 中断言 `pageNum >= 1`，但会改变现有边界行为，**默认不做**，留作后续。

---

## 八、实施顺序（4 个阶段，每阶段可编译）

### 阶段 1：底座 + 试点（1 个 commit）

1. 改 `PageQuery` / `PageVO` / `PageEntityMapstruct` / `PageVOMapstruct`
2. 立刻改 `BaseClass` 全链路（5 文件）+ 删 `BaseClassVOQuery`
3. `mvn -q compile` + 启动 + 按第九章验证 BaseClass 的 4 个端点

> 底座与试点必须在同一 commit，否则编译不过。

### 阶段 2：双通道表批量（9 张表）

顺序：`Datasource` → `FieldType` → `Project` → `Enum` → `EnumItem` → `Table` → `Template` → `TemplateGroup` → `TemplateGroupProperty`。

每张表一个独立可回滚的小步，改完即编译。**Project 优先**（唯一 `voPage` 带 JOIN，验证派生字段是否仍填充）。

### 阶段 3：单通道表

`TableField`（只改 `entityList` 引用与 Query 改名）。

### 阶段 4：清理

删除 22 处 `setPageSize(-1L)`（含 7.2 的两处确认后处理）、清理 import、全局 grep 残留校验（见 9.6）。

---

## 九、验证步骤

### 9.1 编译期校验

```
mvn -q clean compile
```

期望：无 `incompatible types` / `cannot find symbol`。底座去泛型后，任何遗漏点会以编译错误暴露（这是设计意图，不要通过加回泛型来消错）。

### 9.2 分页正确性（核心）

对 BaseClass / Project 各做一次：

| 用例 | 断言 |
|---|---|
| 第 1 页 `pageSize=2` | `records.length==2`、`total` 与 `select count(*)` 一致、`pageNum==1` |
| 第 2 页 `pageSize=2` | 数据与第 1 页不重叠、顺序连续 |
| 末页溢出 `pageNum=9999` | `records` 为空数组、`total` 仍正确、无异常 |
| 不传 `pageNum/pageSize` | 走默认 `1/10`（`PageQuery` 字段默认值） |

**count 行为一致性**（必须实测，不能靠推断）：改造前 `PageQuery` 未覆写 `searchCount()`、走 `IPage` 默认；改造后走 `Page.of(current,size)` 默认。用 SQL 日志（`log-impl: Slf4jImpl` 已开）确认两条通道**各执行一次 count SQL**，与改造前一致。若不一致，用 `toPage(boolean)` 显式对齐。

### 9.3 排序回归（最易出错点）

1. 默认序：不传 `orderItemList`，断言结果按 `update_time DESC, id DESC`（来自 `OrderItemQuery.descs("updateTime","id")` 默认值）。
2. 自定义序：传 `orderItemList=[{column:"createName",asc:true}]`，wrapper 通道（`MybatisUtil.orderBy`）与 XML 通道（`prefixOrderBySQL`）都要覆盖。
3. 非法列名：传不存在的 column，确认 wrapper 通道抛 `IllegalArgumentException`（`MybatisUtil.buildGetter`）且 XML 通道行为与改造前一致 —— **不新增校验，保持原行为**。

### 9.4 双通道字段完整度（顺带验证既有问题是否变化）

`Project` 的 `voPage` 走 JOIN 填 `projectTemplateGroupName`，`entityPage` 不填。改造后断言：`voPage` 该字段仍非空、`entityPage` 仍为 null —— **本次改造不修复也不恶化该隐式契约**，它属于上一轮评估的必修项，另案处理。

### 9.5 list 通道全量语义

对 `entityList` / `voList` 各调用一次，断言返回条数 == 全表未删除行数（改造前靠 `pageSize=-1`，改造后靠不传 page）。重点看 `TemplateGroupApi.voList`（前端 `project/form.vue`、`table/form.vue` 的下拉数据源，调用面最大）。

### 9.6 残留校验（收尾必查）

```
# 应全部为 0 命中
grep -rn "VOQuery"            --include=*.java
grep -rn "EntityQuery"         --include=*.java
grep -rn "setPageSize(-1L)"    --include=*.java
grep -rn "entityToPageVO(query)" --include=*.java
grep -rn "voToPageVO(query)"     --include=*.java
grep -rn "PageQuery<"           --include=*.java
# SQL 种子文件不作为本轮门禁
```

### 9.7 Swagger 契约校验

对比改造前后 `/v3/api-docs`：

- **必须不变**：所有端点路径、请求体已有字段名（`pageNum/pageSize/orderItemList` + 各业务字段）、响应体结构。
- **预期变化（正向收益）**：请求体 schema 不再含 `records/total/size/current/pages`（原先靠 `@Schema(hidden)` 屏蔽，现已物理消失）。
- 前端 `types/api/common` 的 `PageQuery` 本就只声明条件字段，与改造后契约一致 → 前端 0 改动。

---

## 十、风险与回滚

| 风险 | 等级 | 处置 |
|---|---|---|
| `toPage()` 泛型内联推断歧义 | 中 | 强制规约：必须先赋值给显式类型局部变量（5.3 写法），禁止 `mapper.selectPage(query.toPage(), ...)` 内联 |
| count SQL 执行次数变化 | 中 | 9.2 实测门禁；必要时 `toPage(false)` 显式控制 |
| XML 多参数后 OGNL 解析 | 低 | 参数名 `query` 未变；`findPage` 按类型扫描，已 javap 确认。9.3 排序用例覆盖 |
| 漏改某张表 | 低 | 编译期即报错（类型不存在），第二阶段逐表 `mvn compile` |
| 与业务修复混在同一 diff | 中 | 7.3 的死方法清理、9.4 的隐式契约修复**均不并入本 diff**，各自另案 |

**回滚**：阶段 1 单独成 commit，底座 + 试点可整体 revert 而不影响其余 10 张表；阶段 2 每表一个 commit，支持单表回滚。HTTP 契约不变，回滚不需要前端配合。

---

## 十一、明确不在本轮范围（后续待办）

1. **`.ftl` 模板与 `gen_template` 数据表同步**（用户指定暂缓）。需知悉的后果：
   - 模板正文存于 **DB `gen_template.template_content`**，文件系统 `template/default/**.ftl` 只是导入源 —— 同步时两者都要动，且需 SQL 脚本。
   - 涉及行（实测）：`EntityQuery.ftl` + `VOQuery.ftl` 位于模板组 1（id 12、13，父目录节点 349）以及模板组 22–27；公共基类 `PageQuery.ftl` / `OrderItemQuery.ftl` / `PageVO.ftl` / `PageEntityMapstruct.ftl` / `PageVOMapstruct.ftl` 位于**在用的**模板组 29（id 191、224、194、204、205）与 80（id 2139、2140、2144、2156、2157）。
   - 在同步前，**生成器产出的项目仍是旧的「双 Query + PageQuery implements IPage」形态**，与本仓库形态分叉。这是本轮暂缓的既定代价，需在方案评审时确认接受。
   - 另有 6 个孤儿模板组（22–27，`gen_template` 有行但 `gen_template_group` 无记录）与软删模板组 66、以及源码中不存在却仍会下发给生成项目的 `PageVOOtherMapstruct.ftl`（id 206、2158），一并留到模板同步轮处理。
2. **前端 `GenXxxQuery` 类型合并**：后端 Query 合并后，前端每表两个 query 类型可合为一个；纯收益、非阻塞。
3. **`entityPage` 与 `voPage` 的 `whereSQL` / `buildQueryWrapper` 双写收敛**：已确认两处条件各写一遍且已漂移（XML 用 `>=/<=`，wrapper 用 `between`）。
4. **`TableFieldMapper` 死方法清理**（7.3）。

---

## 十二、实施记录（2026-11-12，已落地）

### 12.1 执行环境与本方案的偏差（先说明，避免后文被误读）

1. **IDEA MCP 本会话未注册**（可用 server 仅 `browser-use` / `chrome-devtools` / `context7`）。编译、重启、接口验证改用 CLI `mvn` + `Invoke-RestMethod` + MySQL 客户端完成，验证结论不受影响。
2. **第八章「阶段 1 只改底座 + 试点即可编译」不成立**：`PageQuery` 去掉 `<T>` 的瞬间，其余 10 张表的 `extends PageQuery<Entity>` 与 `selectPage(query, wrapper)` 必然编译失败，不存在可编译的中间态。实际执行路径为：底座 + BaseClass 试点 → 编译暴露全部遗漏点（20 个泛型错误，精确等于 10 组 × 2）→ 一次性推平 11 张表。若要按阶段拆 commit，需事后用 `git add` 按文件分组，不能靠分阶段提交保证每步可编译。

### 12.2 实际改动面（git 复核数据）

| 类别 | 文件数 | +行 | −行 |
|---|---|---|---|
| 修改（底座 4 + Mapper 11 + Service 11 + ServiceImpl 11 + Controller 11） | 48 | 336 | 432 |
| 新增合并 Query | 11 | 461 | 0 |
| 删除旧 Query（11 `*EntityQuery` + 11 `*VOQuery`） | 22 | 0 | 805 |
| **合计** | **81** | **797** | **1237**（净 **−440**） |

- `git diff HEAD -- .../src/main/resources` 与 `-- yanggu-code-generator-frontend` 均**无输出** → mapper XML、Entity、VO、DTO、业务 Mapstruct 接口、前端**全部零改动**，与 5.4 / 9.7 预期一致。
- 统计口径：`git diff HEAD --no-renames --numstat --ignore-cr-at-eol`。仓库工作树原为 CRLF，部分文件被改写为 LF，不加 `--ignore-cr-at-eol` 会把整文件计为改动（虚增为 6667/7107）；不加 `--no-renames` 则 git 的改名检测会把「删旧 Query + 新增合并 Query」折叠成 R，只报 70 文件。

### 12.3 验证结果（每条都有一手证据）

| 项 | 结果 |
|---|---|
| `mvn compile` | BUILD SUCCESS ×2（改造后、12.4-1 字段补全后） |
| 41 个 `entityPage/voPage/entityList/voList` 端点 | 40 通过；1 个失败为存量缺陷，见 12.5 |
| count 语义（9.2） | 每个 `*Page` 请求恰好 1 次 `*_mpCount`；两个 `list` 请求 **0 次 count 且 SQL 无 LIMIT** → 不传 page 替代 `setPageSize(-1L)` 语义等价 |
| 双通道一致性 | 同参数 `pageNum=2,pageSize=5`：`entityPage` 与 `voPage` 逐条相同（5 条、`total=11`、firstId=11）；`pageNum=3` 返回 1 条 = 11−10 |
| 末页溢出 | `pageNum=999` 只执行 count、`records` 为空数组、无异常（MP 越界短路） |
| 排序（9.3） | wrapper 通道与 XML `prefixOrderBySQL` 通道 ORDER BY 均生效（自定义 `className asc` 命中） |
| 唯一实时 XML Entity 通道（7.2） | `EnumItemMapper.xml` 的 `entityList`：allColumns 无表别名 → 安全；完整 SQL 直接在 MySQL 执行返回数据行 ✓ |
| list 全量语义（9.5） | `entityList` / `voList` 均返回全量未删除行（BaseClass 11 条 == `count(*)`） |
| 生成主链路 | 只读 preview（刻意避开 `downloadLocal` / `singleLocal` 写盘端点）：project / table / enum 三种产物共 6 次调用全 200 ✓ |
| Swagger 契约（9.7） | 端点路径与请求体字段名不变；请求体 schema 已物理不含 `records/total/size/current/pages` |

### 12.4 实施中我自身引入的两处偏差（已修，记录以防复犯）

1. **合并 Query 只取了 Entity 侧字段，违反第五章「取并集」规则**。批量脚本走了快路径，丢掉 VO 侧独有的条件字段。逐模块对旧字段集做对称差后确认只有 2 个模块不对称：`TableQuery` 丢 `projectIdList` / `databaseName`，`TemplateQuery` 丢 `templateGroupIdList` / `templateGroupName` / `fileName` / `templateGroupType`。这些字段既被 XML `whereSQL` 引用又被前端类型声明，丢失的表现是**过滤条件静默失效（不报错、返回全量）**，比编译失败危险得多。补齐后实测：`template/voPage templateGroupType=0` 315→295、`table/voList databaseName` 50→34、`table/entityPage className=Base` 50→2。
   → 教训：同类字段合并必须跑「新旧字段集对称差」校验，不能只从单一侧取。
2. **PowerShell `-replace` 默认大小写不敏感**，把局部变量 `enumItemEntityQuery` 连带改成了与类名同名的 `EnumItemQuery` —— 语法合法、编译通过，但变量遮蔽了类型名。定向扫描全量可疑点（仅 1 处真实受害）后对照 `git show` 原文修复。
   → 教训：批量改名一律用 `-creplace`，事后按首字母大小写变体各 grep 一遍。

补充一条排查教训：第一次编译附带出现的一批 `ResultEnum` / `DbType` / `BusinessException` / `getId()` 报错，是 **Lombok 注解处理轮次被真实错误中断后的级联假错误**（pom 无 `annotationProcessorPaths`，lombok 由 Boot 3.3.1 管理）。全量改完即全部消失，不要被它带偏去查依赖配置。

### 12.5 一条与本次改造无关的存量缺陷（已消除，处置方式见 12.10）

`/gen/template/voList` 返回 500，异常 `SQLSyntaxErrorException: Unknown column 't.id' in 'field list'`。

证据链：① `git diff HEAD -- resources` 为空，本次未改任何 mapper XML；② `TemplateMapper.xml` 的 `voList` 写成 `FROM gen_template`（缺 `AS t`），而它 `<include>` 的 `allColumns` 片段每一项都带 `t.` 前缀（同文件的 `voPage` 走 `(...) AS t` 子查询所以正常）；③ 把该形态 SQL 直接在 MySQL 执行，复现 `ERROR 1054`。

补充：该端点**当前前端无人调用**（只调 `genTemplateGroupApi.voList()`），属潜伏缺陷；且它的 `whereSQL` 含 `g.` 前缀条件，`voList` 的单表 FROM 同样不支持 —— 修时两件事一起看。因其属 XML/模板面（本轮指定只改 java），未擅自动手。

### 12.6 双通道字段完整度新增实例（同 9.4，另案）

`BaseClassVO.fieldList` 在 `entityPage` 有值、在 `voPage` 为 null。本次改造不修复也不恶化该隐式契约。

### 12.7 交接须知（运行环境状态）

- 验证实例：`mvn spring-boot:run` 起在 **8899**（context-path `/code-generator`）。日志实际落在 `yanggu-code-generator-backend/logs/`（mvn 工作目录 ≠ 仓库根，仓库根 `logs/` 看不到本次运行日志）。
- 原 8888 上的 IDEA 调试实例当前**未在监听**。我没有终止它（只停了自己起的进程），需在 IDEA 里重新启动才能继续用 8888 验证。

### 12.8 浏览器 UI 端到端复测（后端改由 IDEA 启动）

后端改由 IDEA MCP 接管：`build_project` → `isSuccess=true`（仅存量 MapStruct unmapped 警告，零 error）；`execute_run_configuration(CodeGeneratorApplication)` → Tomcat 8888 + context-path `/code-generator`。命令行实例已先停掉以腾出端口。前端 `pnpm dev` 在 5000，`.env.development` 的 `VITE_API_URL` 指向 8888；`src/mocks/handlers/index.ts` 只注册 `authHandlers`，业务接口 passthrough，故 UI 请求确实打到改造后的后端。

| # | 用例（UI 真实交互） | 结果 |
|---|---|---|
| 1 | 基类管理翻页 | 第 1 页 10 条 / `total=11`；第 2 页 1 条（id=19），UI 序号 11、下一页禁用 |
| 2 | 基类类名 = `BaseVO` 查询 | `total` 11→4，4 条 `className` 全命中，`pageNum` 自动回 1 |
| 3 | 点「创建时间」列头排序 | 请求体 `{"baseClassName":"","packageName":"","className":"BaseVO","pageNum":1,"pageSize":10,"orderItemList":[{"column":"createTime","asc":true}]}` —— **条件 + 分页 + 排序三者共存于同一 Query**，响应严格升序 |
| 4 | `project/voPage`（XML JOIN 通道） | `total=7`，派生字段 `projectTemplateGroupName` 7 条全部非空（9.4 契约未恶化） |
| 5 | `project/entityList`（下拉数据源） | 请求体 `{}`，返回 7 条全量、无 LIMIT → 取代 `setPageSize(-1L)` 的 UI 侧证据 |
| 6 | 表管理「数据库名称」= `code_generator` | `total` 50→34，**12.4-1 补回的 `databaseName` 字段确认生效** |
| 7 | `enumItem/entityPage`（枚举项弹窗） | 请求体带 `enumId=21`（Entity 侧字段在合并类中正常携带），3 条按 `updateTime` 降序 → `EnumItemQuery` 保留的自定义默认排序未丢失 |
| 8 | `templateGroup` list vs page | `voList` 9 条 == `voPage.total` 9，且 page 记录是 list 的子集 |
| 9 | 双通道同参对比 | `entityPage` 与 `voPage` 第 1 页 id 序列完全相同 `[55,4]`；`fieldList` entity 通道有值、vo 通道 null（即 12.6） |
| 10 | 控制台 | 全程零 error / 零 warn |

两条**非本次引入**的存量观察，记录备查：

- 用例 6 的结果中混有 `databaseName=yanggu_code_generator` 的行 —— `TableMapper.xml:40` 用的是 `LIKE CONCAT('%', #{query.databaseName}, '%')` 模糊匹配，`code_generator` 是其子串，属预期语义，不是过滤失效。
- 前端排序只传单项 `orderItemList` 时会覆盖掉默认的多列兜底，`updateTime` 相同的行顺序不稳定（无唯一列 tie-breaker）。wrapper 与 XML 两条通道同样如此，与本次改造无关。

工具侧限制（影响后续取证方式）：chrome-devtools MCP 的 `filePath` / `requestFilePath` / `responseFilePath` 均受 workspace roots 白名单限制，写入项目内 `logs/` 也被拒（`Access denied`），因此响应体只能内联读取；页面内 `window.fetch` 被 MSW 猴补丁替换后返回值不是 Promise，重放请求需改用 `XMLHttpRequest`。

### 12.9 Query 参数一致性全量核查（应"检查是否少字段"要求）

核查目标：合并后的 `XxxQuery` 是否相对 `XxxEntityQuery` + `XxxVOQuery` 丢了参数。方法是从 `git show HEAD:` 取旧类逐字段比对，而不是靠记忆或抽查。共 6 轮静态 + 2 轮运行时。

| 轮次 | 核查维度 | 判据 | 结果 |
| --- | --- | --- | --- |
| 1 | 字段名集合（旧两侧并集 vs 新） | 对称差为空 | 11 模块 lost=0 / gained=0 |
| 2 | 字段类型与初始化表达式 | 逐字段字符串比对 | drift=0；两侧同名字段无类型冲突 |
| 2 | 校验注解（@NotNull/@Min/@Size…） | 旧有新无即为丢失 | 0 处丢失（旧类本就无校验注解） |
| 3 | `@Schema(description)` 文本 | 与旧同名字段完全一致 | no-description=0，desc-changed=0 |
| 4 | Mapper XML 的 `query.*` 引用数与 `<if>` 数 | HEAD vs 工作树相等 | 12 个 XML 全部相等（含 TableMapper 4/4、TemplateMapper 7/7） |
| 4 | ServiceImpl 的 `query.getXxx()` 集合与 wrapper 条件调用数 | HEAD vs 工作树相等 | 12 个 impl 全部相等，无 COND-COUNT-DECREASED |
| 5 | 前端 `GenXxxQuery` 属性 → 后端可接收 | 前端每个属性都能解析 | 11 模块全 OK；后端独有仅 orderItemList / startTime,endTime / projectIdList（均为服务端内部使用） |
| 6 | `PageQuery<` 泛型残留、类声明形态 | 0 残留 | 全后端 0 处；11 个业务 Query 均 `extends PageQuery implements Serializable` |
| 7 | 结构性增删（Controller 端点 / Service 方法 / Mapper 声明） | HEAD vs 工作树 | 0 增 0 删 |
| 8 | 运行时判别性探针（传不存在的值必须使结果归零） | total 变化 | 见下表，与旧行为逐条吻合 |

运行时探针结论 —— **两个通道的条件消费面没有被合并改变**，仍是"entity 通道认 Entity 侧条件、vo 通道认 VO 侧条件"：

| 探针 | 结果 | 判读 |
| --- | --- | --- |
| `table/entityPage` + `databaseName=zzzNOPE` | 50（不变） | entityPage 不消费 databaseName；旧 `TableEntityQuery` 亦无此字段，一致 |
| `table/entityPage` + `className=zzzNOPE` | **0** | Entity 侧字段在 Entity 通道生效 |
| `table/voPage` + `databaseName=zzzNOPE` / `projectIdList=[999]` | **0** / **0** | VO 侧字段（含本轮补回的 databaseName、projectIdList）生效 |
| `table/voPage` + `className=zzzNOPE` | 50（不变） | XML 无该条件，与旧 VOQuery 时代一致 |
| `template/voPage` + `fileName` / `templateGroupName` / `templateGroupIdList` | **0** / **0** / **0** | 本轮补回的 4 个 VO 侧字段全部真实生效（改造中途曾失效） |
| `template/entityPage` + `fileName` / `templateGroupIdList` | 779（不变） | entityPage 走 wrapper，本就不认这些条件，一致 |
| `project/{entityPage,voPage}` + 未来时间区间 | **0** / **0** | `startTime/endTime` 两侧通道均生效 |
| `baseClass/entityPage` + `className=zzzNOPE` | **0** | 生效 |
| `tableField/entityList` + `tableId=156/164/999999` | 10 / 7 / **0** | 生效，返回行 tableId 单一 |
| `enumItem/entityPage` + `orderItemList=[{id,asc}]` | 按 id 升序 | 覆写的默认值被显式传参正确覆盖 |

核查过程中纠正/排掉的三条误判（都不是缺陷，但必须留痕）：

- `templateGroupType` 传字符串 `'backend'` 返回 400 —— 该字段新旧**都是 `Integer`**（`@Schema` 描述为 0-项目/1-表/2-枚举），是探针参数写错，非契约变化。
- **推翻我此前的一条口头结论**：曾说 `ProjectServiceImpl` 用 `isNotEmpty(query,"beginTime")` 导致时间条件静默失效。实测 `beginTime` 在当前与 HEAD 均无任何引用，代码实际是 `query.getStartTime()` / `getEndTime()`（`ProjectServiceImpl:239`、`ProjectMapper.xml:39,42`），时间过滤一直正常工作。该说法作废。
- `tableField/entityPage` 返回 404 —— `TableFieldController` 自始至终只有 `/entityList` 与 `/submitList` 两个端点（HEAD 比对一致），是我调用了不存在的路径。

附带确认：`PageQuery` 删除的 `records/total/size/current/pages` 5 个字段，在 HEAD 全仓**从未被任何代码读取**（`git grep "query\.(getRecords|getTotal|getSize|getCurrent|getPages)\(\)" HEAD` 零命中），属纯死字段，删除无影响面。`domain/query` 下另有 8 个不继承 `PageQuery` 的类（`Generator*Query`、`ProjectTableQuery`、`TemplateGroupDetailQuery`、`TemplateTestQuery`、`CodePreviewQuery`、`CodeSingleGeneratorQuery`）是代码生成参数载体，不在合并范围；`AuthController.java` 是工作树里的新增文件，与本改造无关（本轮新增文件恰为 11 个 `XxxQuery.java`）。

`TableFieldMapper.xml` 的 `whereSQL` 引用 `columnType`/`attrType` 而 `TableFieldQuery` 只有 `tableId`，属**存量不一致**（HEAD 同为 2 处引用、旧 `TableFieldEntityQuery` 同样无此字段），且该 XML 的 4 个语句无任何 Java 调用方；`MybatisUtil.isNotEmpty` 在 `FieldUtil.hasField` 为 false 时直接 `return false`，因此既不报错也不拼条件——这也正是"字段丢失只静默失效、不抛异常"的根因，说明字段级比对是唯一可靠的检查手段。

### 12.10 存量缺陷 12.5 的处置：删除 Template 无人使用的 list/分页全链路

决策变更：不修 SQL，改为**整条链路删除**（用户指令"如果 list 和分页没用的话，从 controller 一直到下面直接删除"）。

删除前的取证（决定能不能删）：

| 取证面 | 方法 | 结果 |
| --- | --- | --- |
| 前端调用方 | 全前端 `genTemplateApi.\w+` 扫描 | 仅 `voPage` / `detailData` / `treeData` / `submit` / `updateParent` / `updateContentList`；`entityPage`/`entityList`/`voList` **零调用** |
| 动态拼接调用 | 全前端 `entityPage\|entityList\|voList` 文本扫描 | 命中的全是**其他模块**（`genProjectApi.entityList` 等），Template 无关 |
| 后端 Service 内部引用 | `templateService.\w+` 全仓扫描 | 三个方法除 Controller 外零引用 |
| **后端自调用（关键）** | `TemplateServiceImpl` 内 `this.\w+(` 扫描 | `tree()` 第 170 行调用 `this.entityList(query)`，而 `tree` 是前端在用的接口 |
| 私有方法归属 | `buildQueryWrapper` 调用点 | 仅被 `entityPage`/`entityList` 使用 |
| XML 片段归属 | `resultMap templateMap` 引用点 | 仅被 `entityPage`/`entityList` 使用（`voPage` 用 resultType） |

落地内容（自上而下五层）：

- `TemplateController`：删 `/entityPage`、`/entityList`、`/voList` 三个端点，`@ApiOperationSupport(order)` 由 1–14 重排为 1–11（保持连续，无跳号）
- `TemplateService`：删 `entityPage` / `entityList` / `voList` 三个声明，保留 `voPage`
- `TemplateServiceImpl`：删 `entityPage`、`voList`；**`entityList` 不删实现，降为 `private`**（去掉 `@Override`），因为 `tree()` 依赖它，这样 `tree` 行为零变化；随之清理指向已删 Mapper 方法的死注释行
- `TemplateMapper`：删 `entityPage` / `entityList` / `voList` 三个声明，只剩 `voPage`；孤儿 `import java.util.List` 一并删
- `TemplateMapper.xml`：删 `entityPage`、`entityList`、`voList` 三段 select + 孤儿 `resultMap templateMap`，206 行 → 150 行

保留判定（没被顺手删掉的）：`allColumns` / `whereSQL` / `leafNodeTemplate` 三个片段仍被 `voPage` 消费；`buildQueryWrapper` 仍被 private `entityList` 消费。**TemplateGroup、TemplateGroupProperty 一律未动**（`genTemplateGroupApi.voList()` 前端在用）。

验证：

- 孤儿 import 扫描（按简单类名在 import 之外的正文出现判定）：4 个文件共 65 个 import，**0 孤儿**
- IDEA `build_project`：**`isSuccess=true, problems=[]`**（改动后跑两次，均零 error）
- `TemplateMapper.xml` XML 良构校验 OK；剩余 4 个 refid 引用全部指向存在的片段
- IDEA 重启后端（8888）实测：

| 端点 | 期望 | 实测 |
| --- | --- | --- |
| `POST /gen/template/entityPage` | 已删 | 业务 `code=404`（HTTP 200，全局异常处理器对无 mapping 的形态） |
| `POST /gen/template/entityList` | 已删 | `code=404` |
| `POST /gen/template/voList` | 已删 | `code=404` |
| `POST /gen/template/voPage` | 正常 | `code=200 total=315`（与删除前一致） |
| `GET /gen/template/tree?templateGroupId=1` | 正常（依赖 private `entityList`） | `code=200`，树数据 55543 字符 |
| `POST /gen/template/detailList`、`GET /detail` | 正常 | 均 `code=200` |
| `POST /gen/templateGroup/voList`（对照组） | 未动 | `code=200` |

一处遗留（无需处理，留档说明）：前端 `api/common/index.ts` 的 CRUD 工厂对**所有**模块统一注册 `entityPage`/`entityList`/`voList`，所以 `genTemplateApi.entityPage` 这类函数仍然存在、调用会拿到 `code=404`。它是跨模块共享工厂，为 Template 单独摘除会波及其他模块（其他模块这三个端点都还在用），故保持原样。
