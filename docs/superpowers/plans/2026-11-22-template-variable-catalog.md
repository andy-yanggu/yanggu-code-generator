# 模板变量目录面板 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在模板编辑页 `tree.vue` 右侧增加「模板变量目录」面板，反射 `domain/model` 的 7 个 Model 类生成可插入的 FreeMarker 变量清单，并修复 `DataModelBuilder` 第 182、214 行绕过属性默认值兜底的缺陷。

**Architecture:** 后端三段式 —— `VariableCatalogBuilder`（纯反射、无 Spring 依赖，产出 `List<VariableNodeVO>` 树）→ `VariableCatalogServiceImpl`（按模板组类型选根 Model、把模板组属性填进 `MAP` 节点）→ `TemplateController` 新增 `GET /gen/template/variableCatalog`。前端把该树一次遍历装饰成带 FreeMarker 表达式的显示树，用单棵 `el-tree` 渲染，点击经 `code-mirror` 新暴露的 `insertAtCursor` 写回编辑器，复用 `tree.vue` 既有 watch 自动置 `isEdited`。

**Tech Stack:** Java 21、Spring Boot 3、Lombok、JUnit 5 + Mockito（`spring-boot-starter-test`，无新增依赖）；Vue 3 + Element Plus + vue-codemirror 6（`@codemirror/view` 的 `EditorView`）、vitest、vue-tsc。

**Spec:** [docs/superpowers/specs/2026-11-22-template-variable-catalog-design.md](../specs/2026-11-22-template-variable-catalog-design.md) —— 本计划逐节实现该规格，决策理由与降级矩阵见规格第 3、6 节。

## Global Constraints

以下约束对每个 Task 隐式生效，值直接取自规格：

- 注解 `@VarDesc` 放 `com.yanggu.code.generator.domain.model`，与 7 个 Model 同包（规格决策 3），Model 内使用不加 import。
- `VariableKindEnum` 放 `com.yanggu.code.generator.enums`，**不加 `@JsonValue`**，Jackson 按枚举名序列化，前端契约是 `'SCALAR' | 'SCALAR_LIST' | 'MODEL_LIST' | 'OBJECT' | 'MAP'`（规格 4.2）。
- `VariableCatalogBuilder` 不得加 `@Component`、不得注入任何 bean（规格 4.3）——单测靠 `new` 直接实例化。
- 下钻终止条件：类型全限定名不以 `com.yanggu.code.generator.domain.model.` 开头。
- 缺 `@VarDesc` 的字段照常列出、`desc` 为空串，**绝不抛异常**（规格决策 8）。
- Model 共 126 个字段需标注：`loopVar` 恰好 12 处、`nullable` 恰好 12 处，清单见 Task 4 两张表，不允许自行增减。
- 前端 `.vue` / `.ts` 一律 **tab 缩进**、单引号、无分号（对齐 `.prettierrc.cjs` 与既有文件）；纯函数与 `*.test.ts` 同目录 colocated（对齐 `tree-utils.ts` / `tree-utils.test.ts`）。
- 目录接口必须传 `noErrorMessage: true`，失败提示只由面板承载，不得再调 `ElMessage`（规格 6.2，禁止重复提示）。
- 后端启停只经 IDEA MCP 的 Debug 模式，**禁止命令行杀进程**；命令行只用于 `mvn test` / `pnpm` 校验。
- 本次不引入 FreeMarker 语法高亮 mode、不引入 `${` 自动补全、不接入 `test.vue`（规格第 8 节）。
- **每个 Task 的最后一步 `git commit` 是待办动作，不是授权动作**：执行前必须逐次取得用户明确同意，未同意则跳过该步继续下一 Task。
- 命令一律用 PowerShell 语法，多命令用 `;` 分隔，不用 `&&`。

## 文件结构

**后端新增**（`yanggu-code-generator-backend/src/main/java/com/yanggu/code/generator/`）

| 文件 | 职责 |
|---|---|
| `domain/model/VarDesc.java` | 字段级元数据注解：`value` / `loopVar` / `nullable` |
| `enums/VariableKindEnum.java` | 变量分类，决定前端渲染与插入行为 |
| `domain/vo/VariableNodeVO.java` | 目录树节点（含 children） |
| `service/support/VariableCatalogBuilder.java` | 纯反射把 Model 类转成节点树，无 Spring 依赖 |
| `service/VariableCatalogService.java` + `service/impl/VariableCatalogServiceImpl.java` | 选根 Model、填 `MAP` 节点属性 key；独立 service 以避开规格 4.4 的循环依赖环 |

**后端修改**

| 文件 | 改动 |
|---|---|
| `controller/TemplateController.java` | 注入 `VariableCatalogService`，新增 order 12 端点 |
| `domain/model/{ProjectModel,TableModel,TableFieldModel,EnumModel,EnumItemModel,ModuleModel,BaseClassModel}.java` | 126 字段加 `@VarDesc` |
| `service/support/DataModelBuilder.java` | 182、214 行改走 `templateGroupPropertyDataService.getData(...)` |

**后端测试**：`src/test/java/com/yanggu/code/generator/service/support/VariableCatalogBuilderTest.java`（新增）、`.../service/impl/VariableCatalogServiceTest.java`（新增）、`.../service/support/DataModelBuilderTest.java`（新增）

**前端新增**（`yanggu-code-generator-frontend/src/`）

| 文件 | 职责 |
|---|---|
| `views/gen/template/variable-utils.ts` | 纯函数：装饰表达式、分组、插入文本、kind 标签映射、计数 |
| `views/gen/template/variable-utils.test.ts` | 上述单测 |
| `views/gen/template/variable-panel/use-variable-catalog.ts` | 加载 hook：按 `templateGroupId` 缓存、失败保留上次目录 |
| `views/gen/template/variable-panel/index.vue` | 面板组件（搜索 + 单棵 `el-tree` + 空态/骨架/重试） |

**前端修改**：`types/api/gen/template.ts`（加类型）、`api/gen/template.ts`（加 `variableCatalog`）、`business/code-mirror/index.vue`（`defineExpose`）、`views/gen/template/tree.vue`（内层 splitter + 面板 + 编辑器 ref）

`types/api/gen/index.ts` 第 11 行已 `export * from '@/types/api/gen/template'`，**无需改动**。

---

## Task 1: `@VarDesc` + 目录契约类型 + 标量反射

**Files:**
- Create: `yanggu-code-generator-backend/src/main/java/com/yanggu/code/generator/domain/model/VarDesc.java`
- Create: `yanggu-code-generator-backend/src/main/java/com/yanggu/code/generator/enums/VariableKindEnum.java`
- Create: `yanggu-code-generator-backend/src/main/java/com/yanggu/code/generator/domain/vo/VariableNodeVO.java`
- Create: `yanggu-code-generator-backend/src/main/java/com/yanggu/code/generator/service/support/VariableCatalogBuilder.java`
- Modify: `yanggu-code-generator-backend/src/main/java/com/yanggu/code/generator/domain/model/ModuleModel.java`（3 字段加注解，作为试点类）
- Test: `yanggu-code-generator-backend/src/test/java/com/yanggu/code/generator/service/support/VariableCatalogBuilderTest.java`

**Interfaces:**
- Consumes: 无（本计划首个任务）
- Produces:
  - `@VarDesc(String value, String loopVar() default "", boolean nullable() default false)`，`ElementType.FIELD` / `RUNTIME`
  - `enum VariableKindEnum { SCALAR, SCALAR_LIST, MODEL_LIST, OBJECT, MAP }`
  - `VariableNodeVO`：`String name/path/javaType/desc/loopVar`、`VariableKindEnum kind`、`Boolean nullable`、`List<VariableNodeVO> children`
  - `List<VariableNodeVO> VariableCatalogBuilder.build(Class<?> rootModelClass)`
  - `ModuleModel` 三字段 `@VarDesc`：`moduleName`→「模块名称」、`moduleDesc`→「模块描述」、`modulePath`→「模块路径」

- [ ] **Step 1: 写失败的测试**

新建 `VariableCatalogBuilderTest.java`。沿用 `PreviewBuilderRenderTest` 的「直接 new、不起 Spring」风格：

```java
package com.yanggu.code.generator.service.support;

import com.yanggu.code.generator.domain.model.ModuleModel;
import com.yanggu.code.generator.domain.vo.VariableNodeVO;
import com.yanggu.code.generator.enums.VariableKindEnum;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VariableCatalogBuilderTest {

    private final VariableCatalogBuilder builder = new VariableCatalogBuilder();

    @Test
    void scalarFieldsBecomeLeafNodes() {
        List<VariableNodeVO> nodeList = builder.build(ModuleModel.class);

        assertEquals(3, nodeList.size());
        VariableNodeVO moduleName = nodeList.getFirst();
        assertEquals("moduleName", moduleName.getName());
        // 根级 path 即字段名：FreeMarker 数据模型无对象名前缀
        assertEquals("moduleName", moduleName.getPath());
        assertEquals("String", moduleName.getJavaType());
        assertEquals(VariableKindEnum.SCALAR, moduleName.getKind());
        assertEquals("模块名称", moduleName.getDesc());
        assertEquals(Boolean.FALSE, moduleName.getNullable());
        assertEquals(List.of(), moduleName.getChildren());
    }
}
```

- [ ] **Step 2: 跑测试，确认失败**

```powershell
cd F:\project\idea\self\yanggu-code-generator\yanggu-code-generator-backend; mvn -q test -Dtest=VariableCatalogBuilderTest
```

预期：编译失败，报 `VariableKindEnum`、`VariableNodeVO`、`VariableCatalogBuilder` 找不到符号。

- [ ] **Step 3: 写注解与枚举**

`domain/model/VarDesc.java`：

```java
package com.yanggu.code.generator.domain.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 模板变量说明，标注在 Model 字段上，供前端模板编辑页的变量目录读取
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface VarDesc {

    /**
     * 面向模板作者的中文说明，取自该字段原有的 javadoc 文本
     */
    String value();

    /**
     * 默认循环变量名，只对 List / Set 字段有效，取自既有 .ftl 的 <#list> 约定
     */
    String loopVar() default "";

    /**
     * 渲染时可能为 null，模板需 <#if xxx??> 保护
     */
    boolean nullable() default false;
}
```

`enums/VariableKindEnum.java`（刻意不加 `@JsonValue`，序列化取枚举名）：

```java
package com.yanggu.code.generator.enums;

/**
 * 模板变量分类，前端据此决定标签文案与点击插入的文本形态
 * 注意：不加 @JsonValue，Jackson 按枚举名序列化，前端契约是字符串
 */
public enum VariableKindEnum {

    /**
     * 标量：String / 数值 / Boolean / Object，插入 ${path}
     */
    SCALAR,

    /**
     * 标量集合：List<String>、Set<String>、List<Integer>，插入循环骨架
     */
    SCALAR_LIST,

    /**
     * 模型集合：List<某 Model 类>，插入循环骨架，子变量带循环变量名前缀
     */
    MODEL_LIST,

    /**
     * 嵌套模型对象，子变量直接 ${obj.field} 取，无循环
     */
    OBJECT,

    /**
     * Map<String, Object>，key 由模板组属性配置决定
     */
    MAP
}
```

- [ ] **Step 4: 写节点 VO**

`domain/vo/VariableNodeVO.java`（`@Data` + `@Schema` + javadoc，对齐同目录 `TemplateContentVO` 的写法）：

```java
package com.yanggu.code.generator.domain.vo;

import com.yanggu.code.generator.enums.VariableKindEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 模板变量目录节点VO
 */
@Data
@Schema(description = "模板变量目录节点VO")
public class VariableNodeVO {

    /**
     * 属性名，如 fieldList；MAP 节点的子节点为模板组属性 propKey
     */
    @Schema(description = "属性名")
    private String name;

    /**
     * 从根算起的完整属性路径，如 tableModelList.fieldList
     */
    @Schema(description = "从根算起的完整属性路径")
    private String path;

    /**
     * Java 类型简名，如 List<TableFieldModel>、String、Object
     */
    @Schema(description = "Java 类型简名")
    private String javaType;

    /**
     * 变量分类，前端渲染与插入行为的唯一依据
     */
    @Schema(description = "变量分类（SCALAR/SCALAR_LIST/MODEL_LIST/OBJECT/MAP）")
    private VariableKindEnum kind;

    /**
     * @VarDesc.value()，缺注解时为空串
     */
    @Schema(description = "中文说明，缺注解时为空串")
    private String desc;

    /**
     * 默认循环变量名，kind 为集合类时有值
     */
    @Schema(description = "默认循环变量名")
    private String loopVar;

    /**
     * 渲染时是否可能为 null
     */
    @Schema(description = "渲染时是否可能为 null")
    private Boolean nullable;

    /**
     * 子变量列表
     */
    @Schema(description = "子变量列表")
    private List<VariableNodeVO> children;

}
```

- [ ] **Step 5: 写只支持标量的构建器**

`service/support/VariableCatalogBuilder.java`。此时递归分支尚未铺开，先把「取字段、拼 path、读注解、判标量」跑通：

```java
package com.yanggu.code.generator.service.support;

import com.yanggu.code.generator.domain.model.VarDesc;
import com.yanggu.code.generator.domain.vo.VariableNodeVO;
import com.yanggu.code.generator.enums.VariableKindEnum;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * 模板变量目录构建器：反射 Model 类生成变量树
 * 刻意不注册为 bean、不注入任何依赖，便于纯单测直接 new
 */
public class VariableCatalogBuilder {

    private static final String MODEL_PACKAGE = "com.yanggu.code.generator.domain.model.";

    /**
     * 以 rootModelClass 的声明字段为根生成目录
     */
    public List<VariableNodeVO> build(Class<?> rootModelClass) {
        return buildModel(rootModelClass, "");
    }

    private List<VariableNodeVO> buildModel(Class<?> modelClass, String pathPrefix) {
        List<VariableNodeVO> nodeList = new ArrayList<>();
        for (Field field : modelClass.getDeclaredFields()) {
            if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            nodeList.add(buildNode(field, pathPrefix));
        }
        return nodeList;
    }

    private VariableNodeVO buildNode(Field field, String pathPrefix) {
        VariableNodeVO node = new VariableNodeVO();
        node.setName(field.getName());
        node.setPath(pathPrefix.isEmpty() ? field.getName() : pathPrefix + "." + field.getName());
        node.setJavaType(field.getType().getSimpleName());
        node.setKind(VariableKindEnum.SCALAR);
        node.setChildren(new ArrayList<>());

        VarDesc varDesc = field.getAnnotation(VarDesc.class);
        // 缺注解照常列出，desc 空串：面板挂掉不影响代码生成
        node.setDesc(varDesc == null ? "" : varDesc.value());
        node.setLoopVar(varDesc == null ? "" : varDesc.loopVar());
        node.setNullable(varDesc != null && varDesc.nullable());
        return node;
    }

}
```

- [ ] **Step 6: 给试点类加注解**

`domain/model/ModuleModel.java` 三个字段各加一行 `@VarDesc`，文本取该字段现有 javadoc，位置在 javadoc 之后、`private` 之前：

```java
    /**
     * 模块名称
     */
    @VarDesc("模块名称")
    private String moduleName;
```

`moduleDesc` → `@VarDesc("模块描述")`，`modulePath` → `@VarDesc("模块路径")`。同包无需 import。

- [ ] **Step 7: 跑测试，确认通过**

```powershell
cd F:\project\idea\self\yanggu-code-generator\yanggu-code-generator-backend; mvn -q test -Dtest=VariableCatalogBuilderTest
```

预期：`Tests run: 1, Failures: 0, Errors: 0`。

- [ ] **Step 8: Commit（须先取得用户同意）**

```powershell
cd F:\project\idea\self\yanggu-code-generator; git add yanggu-code-generator-backend/src/main/java/com/yanggu/code/generator/domain/model/VarDesc.java yanggu-code-generator-backend/src/main/java/com/yanggu/code/generator/domain/model/ModuleModel.java yanggu-code-generator-backend/src/main/java/com/yanggu/code/generator/enums/VariableKindEnum.java yanggu-code-generator-backend/src/main/java/com/yanggu/code/generator/domain/vo/VariableNodeVO.java yanggu-code-generator-backend/src/main/java/com/yanggu/code/generator/service/support/VariableCatalogBuilder.java yanggu-code-generator-backend/src/test/java/com/yanggu/code/generator/service/support/VariableCatalogBuilderTest.java; git commit -m "feat(template): 新增 @VarDesc 注解与模板变量目录契约类型"
```

---

## Task 2: 集合与对象下钻（`MODEL_LIST` / `SCALAR_LIST` / `OBJECT`）

**Files:**
- Modify: `.../service/support/VariableCatalogBuilder.java`（`buildNode` 补类型分支，`buildModel` 加 path 前缀与深度约束）
- Modify: `.../domain/model/EnumModel.java`、`TableModel.java`、`TableFieldModel.java`、`BaseClassModel.java`、`EnumItemModel.java` —— **仅**给集合字段与嵌套对象字段加 `@VarDesc`（含 `loopVar`），其余标量字段留给 Task 4
- Test: `.../service/support/VariableCatalogBuilderTest.java`（追加用例）

**Interfaces:**
- Consumes: Task 1 的 `VarDesc`、`VariableKindEnum`、`VariableNodeVO`、`build(Class<?>)`
- Produces:
  - `build(Class<?>)` 现在返回完整下钻的树：`MODEL_LIST` 节点 children 为其元素类型的全部字段，子节点 `path` = `父path.字段名`（**不含**循环变量名，前缀由前端合成，规格 4.3）
  - `SCALAR_LIST` 节点 `children` 恰好 1 个虚拟节点：`name`/`javaType` = 元素类型简名、`kind=SCALAR`、`path` = 父 `path`、`desc` = 空串
  - `OBJECT` 节点 children 为该 Model 的全部字段，继续下钻
  - 本任务落地的 `loopVar` 值：`EnumModel.enumItemList`→`item`、`TableModel.fieldList`/`primaryList`/`gridList`/`queryList`→`field`、`TableModel.formList`→`formField`、`TableModel.importList`→`i`、`TableModel.generatorFunction`→`funcCode`、`BaseClassModel.fieldList`→`fieldName`（Step 4 的 `desc` 已取磁盘 javadoc 原文）

- [ ] **Step 1: 追加失败的测试**

在 `VariableCatalogBuilderTest` 类内追加（import 处补 `com.yanggu.code.generator.domain.model.EnumModel`、`ProjectModel`、`TableModel`、`java.util.Map`、`java.util.function.Function`、`java.util.stream.Collectors`、`static org.junit.jupiter.api.Assertions.assertNotNull` / `assertTrue`）：

```java
    private VariableNodeVO find(List<VariableNodeVO> nodeList, String name) {
        return nodeList.stream().filter(item -> name.equals(item.getName())).findFirst().orElse(null);
    }

    @Test
    void modelListDrillsDownIntoElementTypeFields() {
        List<VariableNodeVO> nodeList = builder.build(EnumModel.class);
        VariableNodeVO itemList = find(nodeList, "enumItemList");

        assertNotNull(itemList);
        assertEquals(VariableKindEnum.MODEL_LIST, itemList.getKind());
        assertEquals("List<EnumItemModel>", itemList.getJavaType());
        assertEquals("item", itemList.getLoopVar());
        // EnumItemModel 声明 4 个字段
        assertEquals(4, itemList.getChildren().size());
        // path 不含循环变量名前缀，前端按 kind 合成 field.xxx
        assertEquals("enumItemList.enumItemName", find(itemList.getChildren(), "enumItemName").getPath());
    }

    @Test
    void scalarListKeepsOneVirtualElementChild() {
        List<VariableNodeVO> nodeList = builder.build(TableModel.class);
        VariableNodeVO importList = find(nodeList, "importList");

        assertEquals(VariableKindEnum.SCALAR_LIST, importList.getKind());
        assertEquals("i", importList.getLoopVar());
        assertEquals(1, importList.getChildren().size());
        assertEquals("String", importList.getChildren().getFirst().getJavaType());
    }

    @Test
    void nestedModelObjectDrillsDownWithoutLoopVar() {
        List<VariableNodeVO> nodeList = builder.build(TableModel.class);
        VariableNodeVO baseClass = find(nodeList, "entityBaseClass");

        assertEquals(VariableKindEnum.OBJECT, baseClass.getKind());
        assertEquals("", baseClass.getLoopVar());
        // BaseClassModel 声明 5 个字段
        assertEquals(5, baseClass.getChildren().size());
        assertEquals("entityBaseClass.fieldList", find(baseClass.getChildren(), "fieldList").getPath());
    }

    @Test
    void fiveTableModelFieldListsExpandIndependently() {
        List<VariableNodeVO> nodeList = builder.build(TableModel.class);
        Map<String, VariableNodeVO> nodeMap = nodeList.stream()
                .collect(Collectors.toMap(VariableNodeVO::getName, Function.identity()));

        // TableModel 声明 44 个字段（规格 7.1 第 1 条）
        assertEquals(44, nodeList.size());
        for (String listName : List.of("fieldList", "primaryList", "formList", "gridList", "queryList")) {
            assertEquals(34, nodeMap.get(listName).getChildren().size(), listName);
        }
        // 内容相同但 path 不同，不做子树共享（TableFieldModel 声明顺序首个字段是 id，故用 find 取 attrName）
        assertEquals("fieldList.attrName", find(nodeMap.get("fieldList").getChildren(), "attrName").getPath());
        assertEquals("formList.attrName", find(nodeMap.get("formList").getChildren(), "attrName").getPath());
    }

    @Test
    void fourLayerPathIsComposedAcrossNestedModelLists() {
        List<VariableNodeVO> nodeList = builder.build(ProjectModel.class);
        VariableNodeVO fieldList = find(find(nodeList, "tableModelList").getChildren(), "fieldList");
        VariableNodeVO attrName = find(fieldList.getChildren(), "attrName");

        // 插入语义的地基：跨两层集合的 path 逐层拼接（规格 7.1 第 2 条）
        assertEquals("tableModelList.fieldList.attrName", attrName.getPath());
    }

    @Test
    void enumModelDoesNotDeclareProjectNameScalars() {
        List<String> nameList = builder.build(EnumModel.class).stream().map(VariableNodeVO::getName).toList();

        // fillProjectCommon(EnumModel) 只填 Dot/Slash，类里确实没有这三个字段（规格 2.3）
        assertTrue(nameList.stream().noneMatch(name -> name.equals("projectName")
                || name.equals("projectNamePascal") || name.equals("projectNameUnderline")));
    }
```

- [ ] **Step 2: 跑测试，确认失败**

```powershell
cd F:\project\idea\self\yanggu-code-generator\yanggu-code-generator-backend; mvn -q test -Dtest=VariableCatalogBuilderTest
```

预期：`modelListDrillsDownIntoElementTypeFields` 失败，`expected: <MODEL_LIST> but was: <SCALAR>`。

- [ ] **Step 3: 补类型分支**

`VariableCatalogBuilder` 增加 import：`java.lang.reflect.ParameterizedType`、`java.lang.reflect.Type`、`java.util.Collection`、`java.util.ArrayDeque`、`java.util.Deque`。把 `buildModel` / `buildNode` 整段替换为：

```java
    private List<VariableNodeVO> buildModel(Class<?> modelClass, String pathPrefix, Deque<Class<?>> visiting) {
        if (visiting.contains(modelClass)) {
            // 循环引用防御：当前模型图无回边，保留兜底
            return new ArrayList<>();
        }
        visiting.addLast(modelClass);
        List<VariableNodeVO> nodeList = new ArrayList<>();
        for (Field field : modelClass.getDeclaredFields()) {
            if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            nodeList.add(buildNode(field, pathPrefix, visiting));
        }
        visiting.removeLast();
        return nodeList;
    }

    private VariableNodeVO buildNode(Field field, String pathPrefix, Deque<Class<?>> visiting) {
        VariableNodeVO node = new VariableNodeVO();
        Class<?> rawType = field.getType();
        node.setName(field.getName());
        node.setPath(pathPrefix.isEmpty() ? field.getName() : pathPrefix + "." + field.getName());
        node.setChildren(new ArrayList<>());

        VarDesc varDesc = field.getAnnotation(VarDesc.class);
        node.setDesc(varDesc == null ? "" : varDesc.value());
        node.setLoopVar(varDesc == null ? "" : varDesc.loopVar());
        node.setNullable(varDesc != null && varDesc.nullable());

        if (Collection.class.isAssignableFrom(rawType)) {
            Class<?> elementType = resolveElementType(field.getGenericType());
            node.setJavaType(describeCollection(rawType, elementType));
            if (elementType != null && isModel(elementType)) {
                node.setKind(VariableKindEnum.MODEL_LIST);
                node.setChildren(buildModel(elementType, node.getPath(), visiting));
            } else {
                node.setKind(VariableKindEnum.SCALAR_LIST);
                node.setChildren(new ArrayList<>(List.of(virtualElementNode(node, elementType))));
            }
            return node;
        }

        node.setJavaType(rawType.getSimpleName());
        if (isModel(rawType)) {
            node.setKind(VariableKindEnum.OBJECT);
            node.setChildren(buildModel(rawType, node.getPath(), visiting));
        } else {
            node.setKind(VariableKindEnum.SCALAR);
        }
        return node;
    }

    /**
     * 标量集合的虚拟元素节点：只用于承载「插入 ${loopVar}」，不展开层级
     */
    private VariableNodeVO virtualElementNode(VariableNodeVO parent, Class<?> elementType) {
        VariableNodeVO node = new VariableNodeVO();
        node.setName(elementType == null ? "" : elementType.getSimpleName());
        node.setPath(parent.getPath());
        node.setJavaType(elementType == null ? "Object" : elementType.getSimpleName());
        node.setKind(VariableKindEnum.SCALAR);
        node.setDesc("");
        node.setLoopVar("");
        node.setNullable(Boolean.FALSE);
        node.setChildren(new ArrayList<>());
        return node;
    }

    private boolean isModel(Class<?> type) {
        return type != null && type.getName().startsWith(MODEL_PACKAGE);
    }

    /**
     * 取集合元素类型；raw type 或通配符返回 null，按标量集合降级
     */
    private Class<?> resolveElementType(Type genericType) {
        if (!(genericType instanceof ParameterizedType parameterizedType)) {
            return null;
        }
        Type[] args = parameterizedType.getActualTypeArguments();
        return args.length == 1 && args[0] instanceof Class<?> elementClass ? elementClass : null;
    }

    private String describeCollection(Class<?> rawType, Class<?> elementType) {
        String simpleName = rawType == java.util.Set.class ? "Set" : "List";
        return simpleName + "<" + (elementType == null ? "Object" : elementType.getSimpleName()) + ">";
    }
```

同步把 `build(Class<?>)` 改为传空 path 前缀与新的访问栈：

```java
    public List<VariableNodeVO> build(Class<?> rootModelClass) {
        return buildModel(rootModelClass, "", new ArrayDeque<>());
    }
```

- [ ] **Step 4: 给集合与嵌套对象字段补注解**

`desc` 文本已逐条核对自下列字段现有的 javadoc（规格决策 8：注解文案 = javadoc 原文），位置在 javadoc 之后、`private` 之前。

`EnumModel.enumItemList`：

```java
    /**
     * 枚举值列表
     */
    @VarDesc(value = "枚举值列表", loopVar = "item")
    private List<EnumItemModel> enumItemList;
```

`TableModel` 九处（javadoc 保持原样，只在下面插注解行）：

```java
    @VarDesc(value = "字段列表", loopVar = "field")
    private List<TableFieldModel> fieldList;

    @VarDesc(value = "主键列表 (支持多主键)", loopVar = "field")
    private List<TableFieldModel> primaryList;

    @VarDesc(value = "表单列表", loopVar = "formField")
    private List<TableFieldModel> formList;

    @VarDesc(value = "网格列表", loopVar = "field")
    private List<TableFieldModel> gridList;

    @VarDesc(value = "查询列表", loopVar = "field")
    private List<TableFieldModel> queryList;

    @VarDesc(value = "导入列表", loopVar = "i")
    private Set<String> importList;

    @VarDesc(value = "生成功能（新增、修改、删除、详情、分页、列表、导入、导出和复制）", loopVar = "funcCode")
    private List<Integer> generatorFunction;

    @VarDesc(value = "Entity继承基类", nullable = true)
    private BaseClassModel entityBaseClass;

    @VarDesc(value = "VO继承基类", nullable = true)
    private BaseClassModel voBaseClass;
```

`BaseClassModel.fieldList`：

```java
    @VarDesc(value = "基类字段", loopVar = "fieldName")
    private List<String> fieldList;
```

`loopVar` / `nullable` 必须与上面一致；若 javadoc 文本与上面不符（说明磁盘文件已变动），**以 javadoc 原文为准**并在此处同步。

- [ ] **Step 5: 跑测试，确认通过**

```powershell
cd F:\project\idea\self\yanggu-code-generator\yanggu-code-generator-backend; mvn -q test -Dtest=VariableCatalogBuilderTest
```

预期：`Tests run: 7, Failures: 0, Errors: 0`（Task 1 的 1 个 + 本任务 6 个）。

- [ ] **Step 6: Commit（须先取得用户同意）**

```powershell
cd F:\project\idea\self\yanggu-code-generator; git add yanggu-code-generator-backend/src; git commit -m "feat(template): 变量目录支持集合与嵌套模型下钻"
```

---

## Task 3: `MAP` 节点与降级防御

**Files:**
- Modify: `.../service/support/VariableCatalogBuilder.java`（`MAP` 分支）
- Modify: `.../domain/model/TableModel.java`、`EnumModel.java`、`ProjectModel.java`（`Map` 字段加 `@VarDesc`）
- Test: `.../service/support/VariableCatalogBuilderTest.java`

**Interfaces:**
- Consumes: Task 2 的 `buildNode` 类型分支
- Produces:
  - `Map<String, Object>` 字段 → `kind=MAP`、`javaType="Map<String, Object>"`、`children` 为**空列表**（属性 key 由 Task 5 的 service 填充）
  - `ProjectModel` 四处注解：`moduleList`→`loopVar="module"`、`tableModelList`→`loopVar="table"`、`enumModelList`→`loopVar="enumModel"`、`projectTemplatePropertyData`→MAP

- [ ] **Step 1: 追加失败的测试**

追加 import：`com.yanggu.code.generator.domain.model.TableFieldModel`、`static org.junit.jupiter.api.Assertions.assertFalse`（`ProjectModel` / `assertTrue` 已在 Task 2 引入）。

```java
    @Test
    void mapFieldExpandsToEmptyPlaceholder() {
        List<VariableNodeVO> nodeList = builder.build(TableModel.class);
        VariableNodeVO propertyData = find(nodeList, "tableTemplatePropertyData");

        // 属性 key 由 service 层按模板组填充，反射层只留空壳
        assertEquals(VariableKindEnum.MAP, propertyData.getKind());
        assertEquals("Map<String, Object>", propertyData.getJavaType());
        assertEquals(0, propertyData.getChildren().size());
    }

    @Test
    void mapNodesInsideNestedModelsAreAlsoDetected() {
        List<VariableNodeVO> nodeList = builder.build(ProjectModel.class);
        VariableNodeVO tableList = find(nodeList, "tableModelList");
        VariableNodeVO nestedTable = find(tableList.getChildren(), "tableTemplatePropertyData");

        assertEquals(VariableKindEnum.MAP, nestedTable.getKind());
        assertEquals("tableModelList.tableTemplatePropertyData", nestedTable.getPath());
    }

    @Test
    void buildingAllModelRootsNeverThrows() {
        // 循环引用与 raw type 防御靠真实模型图验证：7 个类均能构造出非空目录且不抛
        for (Class<?> modelClass : List.of(ProjectModel.class, TableModel.class, EnumModel.class,
                TableFieldModel.class, EnumItemModel.class, BaseClassModel.class, ModuleModel.class)) {
            assertFalse(new VariableCatalogBuilder().build(modelClass).isEmpty(), modelClass.getSimpleName());
        }
    }

    @Test
    void tableFieldModelAsRootHasNoChildren() {
        List<VariableNodeVO> nodeList = builder.build(TableFieldModel.class);

        // 34 个全标量字段，是最浅的一棵目录
        assertEquals(34, nodeList.size());
        assertTrue(nodeList.stream().allMatch(node -> node.getChildren().isEmpty()));
    }
```

- [ ] **Step 2: 跑测试，确认失败**

```powershell
cd F:\project\idea\self\yanggu-code-generator\yanggu-code-generator-backend; mvn -q test -Dtest=VariableCatalogBuilderTest
```

预期：`mapFieldExpandsToEmptyPlaceholder` 失败，`expected: <MAP> but was: <SCALAR>`。

- [ ] **Step 3: 补 `MAP` 分支**

在 `buildNode` 里，`Collection` 分支**之前**插入 Map 判定；并加 import `java.util.Map`：

```java
        if (Map.class.isAssignableFrom(rawType)) {
            node.setJavaType("Map<String, Object>");
            node.setKind(VariableKindEnum.MAP);
            return node;
        }
```

- [ ] **Step 4: 给三个 `Map` 字段与 `ProjectModel` 集合字段加注解**

`desc` 已逐条取磁盘 javadoc 原文（规格决策 8）；「key 来自哪个模板组」的补充说明由 Task 5 的 `fillMapNode` 统一拼接，不写在注解里。

`TableModel` 两处：

```java
    @VarDesc("项目模板属性数据")
    private Map<String, Object> projectTemplatePropertyData;

    @VarDesc("表模板属性数据")
    private Map<String, Object> tableTemplatePropertyData;
```

`EnumModel` 两处：

```java
    @VarDesc("项目模板属性数据")
    private Map<String, Object> projectTemplatePropertyData;

    @VarDesc("枚举模板属性数据")
    private Map<String, Object> enumTemplatePropertyData;
```

`ProjectModel` 一处 `Map` + 三个集合：

```java
    @VarDesc("项目模板属性数据")
    private Map<String, Object> projectTemplatePropertyData;

    @VarDesc(value = "模块列表", loopVar = "module")
    private List<ModuleModel> moduleList;

    @VarDesc(value = "表模型数据列表", loopVar = "table")
    private List<TableModel> tableModelList;

    @VarDesc(value = "枚举模型数据列表", loopVar = "enumModel")
    private List<EnumModel> enumModelList;
```

- [ ] **Step 5: 跑测试，确认通过**

```powershell
cd F:\project\idea\self\yanggu-code-generator\yanggu-code-generator-backend; mvn -q test -Dtest=VariableCatalogBuilderTest
```

预期：`Tests run: 11, Failures: 0, Errors: 0`。

- [ ] **Step 6: Commit（须先取得用户同意）**

```powershell
cd F:\project\idea\self\yanggu-code-generator; git add yanggu-code-generator-backend/src; git commit -m "feat(template): 变量目录支持 MAP 占位与全量构建防御"
```

---

## Task 4: 126 字段注解补齐 + 覆盖闸门测试

**Files:**
- Modify: `.../domain/model/ProjectModel.java`（剩 17 字段）、`TableModel.java`（剩 33）、`TableFieldModel.java`（全 34）、`EnumModel.java`（剩 12）、`EnumItemModel.java`（全 4）、`BaseClassModel.java`（剩 4）
- Test: `.../service/support/VariableCatalogBuilderTest.java`（追加 3 个闸门用例）

**Interfaces:**
- Consumes: Task 1-3 的注解与构建器
- Produces:
  - 7 个类 126 个字段全部带 `@VarDesc`，`value` 非空
  - `loopVar` 恰好 12 处，取值见 Global Constraints 与本任务表
  - `nullable` 恰好 12 处，取值见本任务表

- [ ] **Step 1: 追加闸门测试（先失败）**

```java
    private static final List<Class<?>> ALL_MODEL_CLASSES = List.of(ProjectModel.class, TableModel.class,
            TableFieldModel.class, EnumModel.class, EnumItemModel.class, ModuleModel.class, BaseClassModel.class);

    @Test
    void everyModelFieldCarriesVarDesc() {
        for (Class<?> modelClass : ALL_MODEL_CLASSES) {
            for (Field field : modelClass.getDeclaredFields()) {
                if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                VarDesc varDesc = field.getAnnotation(VarDesc.class);
                assertNotNull(varDesc, modelClass.getSimpleName() + "." + field.getName());
                assertFalse(varDesc.value().isBlank(), modelClass.getSimpleName() + "." + field.getName());
            }
        }
    }

    @Test
    void exactlyTwelveCollectionsCarryLoopVar() {
        List<String> actualList = new ArrayList<>();
        for (Class<?> modelClass : ALL_MODEL_CLASSES) {
            for (Field field : modelClass.getDeclaredFields()) {
                VarDesc varDesc = field.getAnnotation(VarDesc.class);
                boolean isCollection = Collection.class.isAssignableFrom(field.getType());
                // 全部集合字段必须有 loopVar，非集合字段必须没有
                if (isCollection) {
                    assertFalse(varDesc.loopVar().isBlank(), modelClass.getSimpleName() + "." + field.getName());
                    actualList.add(modelClass.getSimpleName() + "." + field.getName() + "=" + varDesc.loopVar());
                } else {
                    assertEquals("", varDesc.loopVar(), modelClass.getSimpleName() + "." + field.getName());
                }
            }
        }
        assertEquals(List.of(
                "ProjectModel.moduleList=module",
                "ProjectModel.tableModelList=table",
                "ProjectModel.enumModelList=enumModel",
                "TableModel.fieldList=field",
                "TableModel.primaryList=field",
                "TableModel.formList=formField",
                "TableModel.gridList=field",
                "TableModel.queryList=field",
                "TableModel.importList=i",
                "TableModel.generatorFunction=funcCode",
                "BaseClassModel.fieldList=fieldName",
                "EnumModel.enumItemList=item"), actualList.stream().sorted().toList());
    }

    @Test
    void onlyConditionallyAssignedFieldsAreNullable() {
        List<String> actualList = new ArrayList<>();
        for (Class<?> modelClass : ALL_MODEL_CLASSES) {
            for (Field field : modelClass.getDeclaredFields()) {
                if (field.getAnnotation(VarDesc.class).nullable()) {
                    actualList.add(modelClass.getSimpleName() + "." + field.getName());
                }
            }
        }
        // 与规格 2.4 表一致：4 个数据源字段 + 2 个基类 + 2 个模块 + 4 个枚举关联字段
        assertEquals(List.of(
                "ProjectModel.databaseDriverClassName",
                "ProjectModel.databaseUrl",
                "ProjectModel.databaseUsername",
                "ProjectModel.databasePassword",
                "TableFieldModel.enumId",
                "TableFieldModel.enumName",
                "TableFieldModel.enumNameAllUpper",
                "TableFieldModel.enumNamePascal",
                "TableModel.moduleDesc",
                "TableModel.modulePath",
                "TableModel.entityBaseClass",
                "TableModel.voBaseClass"), actualList.stream().sorted().toList());
    }
```

追加 import：`java.lang.reflect.Modifier`、`java.util.ArrayList`、`java.util.Collection`、`java.util.Map`、`com.yanggu.code.generator.domain.model.*` 中尚未引入的类，以及 `static org.junit.jupiter.api.Assertions.assertFalse` / `assertTrue`。

- [ ] **Step 2: 跑测试，确认失败**

```powershell
cd F:\project\idea\self\yanggu-code-generator\yanggu-code-generator-backend; mvn -q test -Dtest=VariableCatalogBuilderTest
```

预期：`everyModelFieldCarriesVarDesc` 失败，消息形如 `TableFieldModel.id ==> expected: not <null>`；`exactlyTwelveCollectionsCarryLoopVar` 与 `onlyConditionallyAssignedFieldsAreNullable` 以 `NullPointerException`（`getAnnotation` 返回 null）失败——同属预期，Step 3 补齐注解后三条一起转绿。

- [ ] **Step 3: 批量补注解**

规则（逐字段执行，无例外）：

1. `value` = 该字段现有 javadoc 正文，多行 javadoc 取合并后的完整文本；不重写、不美化、不补充实现细节。
2. `TableFieldModel.enumId` / `enumName` / `enumNamePascal` / `enumNameAllUpper` 加 `nullable = true`（依据：`DataModelBuilder` 第 124-127 行仅在 `enumId != null` 时调 `fillEnumNaming`）。
3. `ProjectModel.databaseDriverClassName` / `databaseUrl` / `databaseUsername` / `databasePassword` 加 `nullable = true`（第 79-84 行整段在 `datasource != null` 内）。
4. `TableModel.modulePath` / `moduleDesc` 加 `nullable = true`（第 141-147 行 `ifPresent` 才赋值）。
5. `entityBaseClass` / `voBaseClass` 的 `nullable = true` 已在 Task 2 写好。
6. `EnumItemModel.enumItemCode` 声明类型是 `Object`：`@VarDesc("枚举项编码，实际类型由 enumCodeType 指示（String 或 Integer）")`，**不加** `nullable`。
7. 其余字段一律 `@VarDesc("javadoc 原文")`，不带其他属性。

- [ ] **Step 4: 跑测试，确认通过**

```powershell
cd F:\project\idea\self\yanggu-code-generator\yanggu-code-generator-backend; mvn -q test -Dtest=VariableCatalogBuilderTest
```

预期：`Tests run: 14, Failures: 0, Errors: 0`。若 `exactlyTwelveCollectionsCarryLoopVar` 报出多出的集合字段，说明 `TableModel` 里还有未识别的 `List` 字段——按实际字段补 `loopVar` 并同步更新规格 4.1 表，不得反向删断言。

- [ ] **Step 5: 跑全量后端测试，确认未影响既有用例**

```powershell
cd F:\project\idea\self\yanggu-code-generator\yanggu-code-generator-backend; mvn -q test
```

预期：现有 `ProductGeneratorTest`、`PreviewBuilderRenderTest` 等全部通过。

- [ ] **Step 6: Commit（须先取得用户同意）**

```powershell
cd F:\project\idea\self\yanggu-code-generator; git add yanggu-code-generator-backend/src; git commit -m "feat(template): Model 全字段补齐 @VarDesc 注解并加覆盖闸门"
```

---

## Task 5: 目录编排 service + Controller 端点

**Files:**
- Create: `.../service/VariableCatalogService.java`
- Create: `.../service/impl/VariableCatalogServiceImpl.java`
- Modify: `.../controller/TemplateController.java`
- Test: `src/test/java/com/yanggu/code/generator/service/impl/VariableCatalogServiceTest.java`

**Interfaces:**
- Consumes: `VariableCatalogBuilder.build(Class<?>)`；`TemplateGroupService.getById(Long, Boolean)`（组不存在时内部 `selectById` 已抛 `BusinessException`）；`TemplateGroupPropertyService.listByGroupId(List<Long>)` 返回按 `propOrder` 升序的 `TemplateGroupPropertyEntity`（字段 `propKey` / `propTitle` / `propDefaultValue`）；`TemplateGroupEntity.getType()`；`TemplateGroupTypeEnum.{PROJECT,TABLE,ENUM}`
- Produces:
  - `List<VariableNodeVO> VariableCatalogService.variableCatalog(Long templateGroupId)`
  - `GET /gen/template/variableCatalog?templateGroupId={id}` → `List<VariableNodeVO>`，`@ApiOperationSupport(order = 12)`

- [ ] **Step 1: 写失败的测试**

```java
package com.yanggu.code.generator.service.impl;

import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.domain.entity.TemplateGroupEntity;
import com.yanggu.code.generator.domain.entity.TemplateGroupPropertyEntity;
import com.yanggu.code.generator.domain.vo.VariableNodeVO;
import com.yanggu.code.generator.enums.VariableKindEnum;
import com.yanggu.code.generator.service.TemplateGroupPropertyService;
import com.yanggu.code.generator.service.TemplateGroupService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VariableCatalogServiceTest {

    @Mock
    private TemplateGroupService templateGroupService;
    @Mock
    private TemplateGroupPropertyService templateGroupPropertyService;
    // builder 在 Impl 里自行实例化（见 Step 3），@InjectMocks 走无参构造时已初始化，无需 mock
    @InjectMocks
    private VariableCatalogServiceImpl variableCatalogService;

    private TemplateGroupEntity group(int type) {
        TemplateGroupEntity entity = new TemplateGroupEntity();
        entity.setId(1L);
        entity.setType(type);
        return entity;
    }

    private TemplateGroupPropertyEntity property(String key, String title, String defaultValue) {
        TemplateGroupPropertyEntity entity = new TemplateGroupPropertyEntity();
        entity.setPropKey(key);
        entity.setPropTitle(title);
        entity.setPropDefaultValue(defaultValue);
        return entity;
    }

    private VariableNodeVO find(List<VariableNodeVO> nodeList, String name) {
        return nodeList.stream().filter(item -> name.equals(item.getName())).findFirst().orElse(null);
    }

    @Test
    void tableGroupExpandsOnlyItsOwnPropertyMap() {
        when(templateGroupService.getById(1L, false)).thenReturn(group(1));
        when(templateGroupPropertyService.listByGroupId(List.of(1L)))
                .thenReturn(List.of(property("useSwagger", "是否启用 swagger", "1"),
                        property("api-prefix", "接口路径前缀", "")));

        List<VariableNodeVO> catalog = variableCatalogService.variableCatalog(1L);

        VariableNodeVO tableMap = find(catalog, "tableTemplatePropertyData");
        // propKey 直接作为子节点 name，path 拼接 .key
        assertEquals(2, tableMap.getChildren().size());
        VariableNodeVO swagger = find(tableMap.getChildren(), "useSwagger");
        assertEquals("tableTemplatePropertyData.useSwagger", swagger.getPath());
        assertEquals(VariableKindEnum.SCALAR, swagger.getKind());
        assertEquals("是否启用 swagger", swagger.getDesc());
        assertEquals(Boolean.FALSE, swagger.getNullable());
        // 无默认值：项目未显式配置时该 key 取不到，标记可空
        assertEquals(Boolean.TRUE, find(tableMap.getChildren(), "api-prefix").getNullable());
        // 决策 4：跨组的 project 属性 Map 不展开，只在说明里点明来源
        assertEquals(0, find(catalog, "projectTemplatePropertyData").getChildren().size());
    }

    @Test
    void nestedMapNodesInsideProjectCatalogAreFilled() {
        when(templateGroupService.getById(2L, false)).thenReturn(group(0));
        when(templateGroupPropertyService.listByGroupId(List.of(2L)))
                .thenReturn(List.of(property("authMode", "鉴权方式", "token")));

        List<VariableNodeVO> catalog = variableCatalogService.variableCatalog(2L);

        assertEquals(1, find(catalog, "projectTemplatePropertyData").getChildren().size());
        // 规格 4.4：项目模板目录下嵌的 TableModel 也要填，不能只填根级
        VariableNodeVO nestedTable = find(find(catalog, "tableModelList").getChildren(), "entityBaseClass");
        assertTrue(nestedTable != null);
        VariableNodeVO tableMap = find(find(catalog, "tableModelList").getChildren(), "projectTemplatePropertyData");
        assertEquals(1, tableMap.getChildren().size());
    }

    @Test
    void unknownGroupTypeThrowsInsteadOfEmptyCatalog() {
        when(templateGroupService.getById(3L, false)).thenReturn(group(9));

        assertThrows(BusinessException.class, () -> variableCatalogService.variableCatalog(3L));
    }
}
```

- [ ] **Step 2: 跑测试，确认失败**

```powershell
cd F:\project\idea\self\yanggu-code-generator\yanggu-code-generator-backend; mvn -q test -Dtest=VariableCatalogServiceTest
```

预期：编译失败，`VariableCatalogService` / `VariableCatalogServiceImpl` 找不到符号。

- [ ] **Step 3: 写接口与实现**

`service/VariableCatalogService.java`：

```java
package com.yanggu.code.generator.service;

import com.yanggu.code.generator.domain.vo.VariableNodeVO;

import java.util.List;

/**
 * 模板变量目录服务层
 * 独立于 TemplateService：TemplateGroupPropertyServiceImpl 已反向注入 TemplateService，
 * 目录编排若塞进 TemplateServiceImpl 会新增一条字段注入环（本项目开启了
 * allow-circular-references，成环不会启动失败，但不应继续累积）
 */
public interface VariableCatalogService {

    /**
     * 按模板组类型返回可插入的变量目录
     *
     * @param templateGroupId 模板组ID
     */
    List<VariableNodeVO> variableCatalog(Long templateGroupId);

}
```

`service/impl/VariableCatalogServiceImpl.java`：

```java
package com.yanggu.code.generator.service.impl;

import cn.hutool.v7.core.text.StrUtil;
import cn.hutool.v7.core.util.EnumUtil;
import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.domain.entity.TemplateGroupEntity;
import com.yanggu.code.generator.domain.entity.TemplateGroupPropertyEntity;
import com.yanggu.code.generator.domain.model.EnumModel;
import com.yanggu.code.generator.domain.model.ProjectModel;
import com.yanggu.code.generator.domain.model.TableModel;
import com.yanggu.code.generator.domain.vo.VariableNodeVO;
import com.yanggu.code.generator.enums.TemplateGroupTypeEnum;
import com.yanggu.code.generator.enums.VariableKindEnum;
import com.yanggu.code.generator.service.TemplateGroupPropertyService;
import com.yanggu.code.generator.service.TemplateGroupService;
import com.yanggu.code.generator.service.VariableCatalogService;
import com.yanggu.code.generator.service.support.VariableCatalogBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 模板变量目录实现
 */
@Slf4j
@Service
public class VariableCatalogServiceImpl implements VariableCatalogService {

    @Autowired
    private TemplateGroupService templateGroupService;
    @Autowired
    private TemplateGroupPropertyService templateGroupPropertyService;
    // 构建器无状态、不起 Spring 依赖，保持规格 4.3 的「非 bean」约束，直接实例化
    private final VariableCatalogBuilder variableCatalogBuilder = new VariableCatalogBuilder();

    @Override
    public List<VariableNodeVO> variableCatalog(Long templateGroupId) {
        // 目录不需要模板内容，isIncludeTemplateList 传 false；组不存在时 getById 内部已抛 BusinessException
        TemplateGroupEntity templateGroup = templateGroupService.getById(templateGroupId, false);
        TemplateGroupTypeEnum groupType = EnumUtil.getBy(TemplateGroupTypeEnum::getCode, templateGroup.getType());
        if (groupType == null) {
            // 不静默返回空列表，否则面板只会显示「无变量」而说不出原因
            throw new BusinessException("模板组类型错误, 模板组id: {}", templateGroupId);
        }
        Class<?> rootModelClass = switch (groupType) {
            case PROJECT -> ProjectModel.class;
            case TABLE -> TableModel.class;
            case ENUM -> EnumModel.class;
        };
        // 决策 4：只展开当前模板组自己的属性 Map
        String matchName = switch (groupType) {
            case PROJECT -> "projectTemplatePropertyData";
            case TABLE -> "tableTemplatePropertyData";
            case ENUM -> "enumTemplatePropertyData";
        };

        List<TemplateGroupPropertyEntity> propertyList = templateGroupPropertyService.listByGroupId(List.of(templateGroupId));
        List<VariableNodeVO> catalog = variableCatalogBuilder.build(rootModelClass);
        fillMapNode(catalog, matchName, propertyList);
        log.debug("模板组 {} 类型 {} 根变量数 {}", templateGroupId, groupType, catalog.size());
        return catalog;
    }

    /**
     * 递归填充 MAP 节点：只展开当前模板组自身属性，其余 MAP 节点留空并说明来源（决策 4）
     */
    private void fillMapNode(List<VariableNodeVO> nodeList, String matchName, List<TemplateGroupPropertyEntity> propertyList) {
        for (VariableNodeVO node : nodeList) {
            if (node.getKind() == VariableKindEnum.MAP) {
                if (matchName.equals(node.getName())) {
                    node.setChildren(buildPropertyNodeList(node, propertyList));
                } else {
                    node.setDesc(StrUtil.isBlank(node.getDesc())
                            ? "key 来自其他模板组的属性配置，此处不展开"
                            : node.getDesc() + "（key 来自其他模板组的属性配置，此处不展开）");
                }
                continue;
            }
            if (node.getKind() == VariableKindEnum.SCALAR_LIST) {
                // 虚拟元素节点无下层，跳过
                continue;
            }
            fillMapNode(node.getChildren(), matchName, propertyList);
        }
    }

    private List<VariableNodeVO> buildPropertyNodeList(VariableNodeVO mapNode, List<TemplateGroupPropertyEntity> propertyList) {
        List<VariableNodeVO> nodeList = new ArrayList<>();
        for (TemplateGroupPropertyEntity property : propertyList) {
            VariableNodeVO node = new VariableNodeVO();
            node.setName(property.getPropKey());
            node.setPath(mapNode.getPath() + "." + property.getPropKey());
            node.setJavaType("Object");
            node.setKind(VariableKindEnum.SCALAR);
            // 等价于 blankToDefault(title, "")：hutool v7 的 StrUtil 没有 blankToDefault
            node.setDesc(StrUtil.isBlank(property.getPropTitle()) ? "" : property.getPropTitle());
            node.setLoopVar("");
            // 没配默认值的属性，getData 兜出来是空串（产品未配置时），模板做判空/拼接时需额外保护
            node.setNullable(StrUtil.isBlank(property.getPropDefaultValue()));
            node.setChildren(new ArrayList<>());
            nodeList.add(node);
        }
        return nodeList;
    }

}
```

- [ ] **Step 4: 跑测试，确认通过**

```powershell
cd F:\project\idea\self\yanggu-code-generator\yanggu-code-generator-backend; mvn -q test -Dtest=VariableCatalogServiceTest
```

预期：`Tests run: 3, Failures: 0, Errors: 0`。若 `unknownGroupTypeThrowsInsteadOfEmptyCatalog` 报 UnnecessaryStubbing，把该用例的两个 `when` 删到只剩 `getById`。

- [ ] **Step 5: 加 Controller 端点**

`TemplateController` 增加字段与方法（沿用该文件的注解顺序：`@*Mapping` → `@ApiOperationSupport` → `@Operation` → `@Parameter`）：

```java
    @Autowired
    private VariableCatalogService variableCatalogService;

    /**
     * 模板变量目录
     *
     * @param templateGroupId 模板组ID
     */
    @GetMapping("/variableCatalog")
    @ApiOperationSupport(order = 12)
    @Operation(summary = "模板变量目录")
    @Parameter(name = "templateGroupId", description = "模板组ID", required = true)
    public List<VariableNodeVO> variableCatalog(@RequestParam("templateGroupId") @NotNull(message = "模板组ID不能为空") Long templateGroupId) {
        return variableCatalogService.variableCatalog(templateGroupId);
    }
```

补 import：`com.yanggu.code.generator.domain.vo.VariableNodeVO`、`com.yanggu.code.generator.service.VariableCatalogService`。

- [ ] **Step 6: 全量后端测试 + 启动自检**

```powershell
cd F:\project\idea\self\yanggu-code-generator\yanggu-code-generator-backend; mvn -q test
```

预期：全部通过。随后经 IDEA MCP 以 Debug 模式启动后端，打开 knife4j 文档确认「模板变量目录」端点出现。（注：本项目 `application.yaml` 开启了 `allow-circular-references: true`，启动成功**不能**证明无注入环；真正的结论靠静态依赖分析——`VariableCatalogService` 的消费者只有 `TemplateController`，而 `VariableCatalogServiceImpl` 只依赖两个既有 bean。）

- [ ] **Step 7: Commit（须先取得用户同意）**

```powershell
cd F:\project\idea\self\yanggu-code-generator; git add yanggu-code-generator-backend/src; git commit -m "feat(template): 新增模板变量目录接口 /gen/template/variableCatalog"
```

---

## Task 6: 修复 `DataModelBuilder` 属性默认值缺陷

**Files:**
- Modify: `.../service/support/DataModelBuilder.java:182`、`:214`
- Test: `src/test/java/com/yanggu/code/generator/service/support/DataModelBuilderTest.java`（新增）

**Interfaces:**
- Consumes: 已注入字段 `templateGroupPropertyDataService.getData(Long templateGroupId, Map<String, Object> productData)`；`ProjectEntity.getProjectTemplateGroupId()`（第 87 行同款用法）
- Produces: `buildTableDataModel` / `buildEnumDataModel` 返回的 `projectTemplatePropertyData` 经 `getData` 兜底，与 `buildProjectDataModel` 行为一致

- [ ] **Step 1: 写失败的测试**

```java
package com.yanggu.code.generator.service.support;

import com.yanggu.code.generator.domain.entity.EnumEntity;
import com.yanggu.code.generator.domain.entity.ModuleEntity;
import com.yanggu.code.generator.domain.entity.ProjectEntity;
import com.yanggu.code.generator.domain.entity.TableEntity;
import com.yanggu.code.generator.domain.model.EnumModel;
import com.yanggu.code.generator.domain.model.TableModel;
import com.yanggu.code.generator.mapstruct.BaseClassMapstruct;
import com.yanggu.code.generator.mapstruct.TableFieldMapstruct;
import com.yanggu.code.generator.service.BaseClassService;
import com.yanggu.code.generator.service.DatasourceService;
import com.yanggu.code.generator.service.EnumService;
import com.yanggu.code.generator.service.FieldTypeService;
import com.yanggu.code.generator.service.TableFieldService;
import com.yanggu.code.generator.service.TableService;
import com.yanggu.code.generator.service.TemplateGroupPropertyDataService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataModelBuilderTest {

    @Mock
    private TableService tableService;
    @Mock
    private TableFieldService tableFieldService;
    @Mock
    private EnumService enumService;
    @Mock
    private FieldTypeService fieldTypeService;
    @Mock
    private BaseClassService baseClassService;
    @Mock
    private TemplateGroupPropertyDataService templateGroupPropertyDataService;
    @Mock
    private TableFieldMapstruct tableFieldMapstruct;
    @Mock
    private BaseClassMapstruct baseClassMapstruct;
    @Mock
    private DatasourceService datasourceService;
    @InjectMocks
    private DataModelBuilder dataModelBuilder;

    private ProjectEntity project() {
        ProjectEntity project = new ProjectEntity();
        project.setId(100L);
        project.setProjectName("demo");
        project.setProjectPackage("com.demo");
        project.setModuleList(List.of());
        project.setProjectTemplateGroupId(11L);
        project.setTableTemplateGroupId(12L);
        project.setEnumTemplateGroupId(13L);
        // 项目自身未显式配置任何属性 key
        project.setTemplateGroupPropertyData(new HashMap<>());
        return project;
    }

    @Test
    void tableProjectPropertyDataGoesThroughGetData() {
        TableEntity table = new TableEntity();
        table.setId(201L);
        table.setModuleName("system");
        table.setFunctionName("user");
        table.setTemplateGroupPropertyData(new HashMap<>());

        when(tableFieldService.getByTableId(201L)).thenReturn(List.of());
        when(tableFieldMapstruct.toModel(any())).thenReturn(new ArrayList<>());
        when(fieldTypeService.getPackageByTableId(201L)).thenReturn(Set.of());
        // getData 会补进模板组属性默认值
        when(templateGroupPropertyDataService.getData(eq(11L), any())).thenReturn(Map.of("useSwagger", "1"));
        when(templateGroupPropertyDataService.getData(eq(12L), any())).thenReturn(Map.of());

        TableModel tableModel = dataModelBuilder.buildTableDataModel(table, project());

        // 修复前取到的是项目的原始空 Map，默认值丢失
        assertEquals(Map.of("useSwagger", "1"), tableModel.getProjectTemplatePropertyData());
    }

    @Test
    void enumProjectPropertyDataGoesThroughGetData() {
        EnumEntity enumEntity = new EnumEntity();
        enumEntity.setId(301L);
        enumEntity.setEnumName("status");
        enumEntity.setTemplateGroupPropertyData(new HashMap<>());

        when(templateGroupPropertyDataService.getData(eq(13L), any())).thenReturn(Map.of("enumStyle", "int"));
        when(templateGroupPropertyDataService.getData(eq(11L), any())).thenReturn(Map.of("useSwagger", "1"));

        EnumModel enumModel = dataModelBuilder.buildEnumDataModel(enumEntity, project());

        assertEquals(Map.of("useSwagger", "1"), enumModel.getProjectTemplatePropertyData());
        assertEquals(Map.of("enumStyle", "int"), enumModel.getEnumTemplatePropertyData());
    }
}
```

- [ ] **Step 2: 跑测试，确认失败**

```powershell
cd F:\project\idea\self\yanggu-code-generator\yanggu-code-generator-backend; mvn -q test -Dtest=DataModelBuilderTest
```

预期：两个用例均失败，`expected: <{useSwagger=1}> but was: <{}>`。

- [ ] **Step 3: 改两行**

第 182 行与第 214 行由直接取实体字段改为走 `getData`，组 id 取 `project.getProjectTemplateGroupId()`：

```java
        tableModel.setProjectTemplatePropertyData(templateGroupPropertyDataService.getData(project.getProjectTemplateGroupId(), project.getTemplateGroupPropertyData()));
```

```java
        enumModel.setProjectTemplatePropertyData(templateGroupPropertyDataService.getData(project.getProjectTemplateGroupId(), project.getTemplateGroupPropertyData()));
```

第 183、215 行的 `tableTemplatePropertyData` / `enumTemplatePropertyData` 保持原样。

- [ ] **Step 4: 跑测试，确认通过**

```powershell
cd F:\project\idea\self\yanggu-code-generator\yanggu-code-generator-backend; mvn -q test -Dtest=DataModelBuilderTest
```

预期：`Tests run: 2, Failures: 0, Errors: 0`。

- [ ] **Step 5: 跑全量后端测试**

```powershell
cd F:\project\idea\self\yanggu-code-generator\yanggu-code-generator-backend; mvn -q test
```

预期：全部通过。`ProductGeneratorTest` 用的是 mock 的 `DataModelBuilder`，不受影响。

- [ ] **Step 6: Commit（须先取得用户同意）**

```powershell
cd F:\project\idea\self\yanggu-code-generator; git add yanggu-code-generator-backend/src; git commit -m "fix(generator): 表与枚举模板的项目属性数据补齐模板组默认值"
```

---

## Task 7: 前端类型 + API + 纯函数层

**Files:**
- Modify: `yanggu-code-generator-frontend/src/types/api/gen/template.ts`（追加类型）
- Modify: `yanggu-code-generator-frontend/src/api/gen/template.ts`（追加 `variableCatalog`）
- Create: `yanggu-code-generator-frontend/src/views/gen/template/variable-utils.ts`
- Test: `yanggu-code-generator-frontend/src/views/gen/template/variable-utils.test.ts`

**Interfaces:**
- Consumes: 后端 `GET /gen/template/variableCatalog` 返回的 `VariableNodeVO[]`
- Produces（`variable-utils.ts` 导出，Task 8 依赖）：
  - `type DisplayKind = VariableKind | 'GROUP'`
  - `interface DisplayVariableNode { name; path; javaType; kind: DisplayKind; desc; loopVar; nullable; selfExpr; expr; uid; children: DisplayVariableNode[] }`
  - `buildVariableTree(nodeList: VariableNode[]): DisplayVariableNode[]`
  - `decorateVariables(nodeList: VariableNode[], parentExpr?: string, uidPrefix?: string): DisplayVariableNode[]`
  - `countVariables(nodeList: VariableNode[]): number`
  - `buildInsertText(node: DisplayVariableNode): { text: string; caret: number } | null`
  - `canInsert(kind: DisplayKind): boolean`
  - `kindTag(kind: DisplayKind): { label: string; type: 'info' | 'primary' | 'warning' | 'success' } | null`
  - `matchKeyword(node: DisplayVariableNode, keyword: string): boolean`

- [ ] **Step 1: 记录 vue-tsc 错误基线**

`views/gen/template` 之外的存量报错必须在 Task 10 做差分，先落基线：

```powershell
cd F:\project\idea\self\yanggu-code-generator\yanggu-code-generator-frontend; pnpm run type-check 2>&1 | Tee-Object -FilePath .\type-check-baseline.txt | Select-Object -Last 5
```

把报错总数与涉及文件列表记在 Task 10 的复选框旁，供最后比对。若基线本身是 0 错，Task 10 要求仍为 0。

- [ ] **Step 2: 追加类型定义**

`src/types/api/gen/template.ts` 末尾追加（tab 缩进，注释风格对齐同文件既有字段）：

```ts
/**
 * 模板变量分类
 */
export type VariableKind = 'SCALAR' | 'SCALAR_LIST' | 'MODEL_LIST' | 'OBJECT' | 'MAP'

/**
 * 模板变量目录节点
 */
export interface VariableNode {
	// 属性名，MAP 节点的子项为模板组属性 propKey
	name: string
	// 从根算起的完整属性路径
	path: string
	// Java 类型简名
	javaType: string
	// 变量分类
	kind: VariableKind
	// 中文说明，字段缺 @VarDesc 时为空串
	desc: string
	// 默认循环变量名，集合类有值
	loopVar: string
	// 渲染时是否可能为 null
	nullable: boolean
	// 子变量列表
	children: VariableNode[]
}
```

`types/api/gen/index.ts` 第 11 行已 re-export，无需改动。

- [ ] **Step 3: 追加 API 方法**

`src/api/gen/template.ts` 的 `CustomApi` 接口加一行签名，并补 import：

```ts
import type { AxiosRequestConfig } from 'axios'
import type { GenTemplateEntity, GenTemplateQuery, VariableNode } from '@/types'
```

```ts
	variableCatalog: (templateGroupId: number) => Promise<VariableNode[]>
```

对象里追加实现（放在 `treeData` 之后）：

```ts
	// 模板变量目录：失败提示与重试由变量面板承载，故关闭全局错误提示
	variableCatalog: (templateGroupId: number, config?: AxiosRequestConfig): Promise<VariableNode[]> => {
		return service.get(baseUrl + '/variableCatalog', { params: { templateGroupId }, noErrorMessage: true, ...config })
	},
```

- [ ] **Step 4: 写失败的纯函数测试**

新建 `variable-utils.test.ts`，沿用 `tree-utils.test.ts` 的「最小节点工厂 + 纯函数断言」写法：

```ts
import { describe, expect, it } from 'vitest'
import { buildVariableTree, canInsert, countVariables, decorateVariables, kindTag, matchKeyword, buildInsertText } from './variable-utils'
import type { VariableNode, VariableKind } from '@/types'

// 最小变量节点工厂：只覆盖被测字段
const makeNode = (name: string, kind: VariableKind, extra: Partial<VariableNode> = {}): VariableNode => ({
	name,
	path: name,
	javaType: kind === 'MAP' ? 'Map<String, Object>' : 'String',
	kind,
	desc: '',
	loopVar: '',
	nullable: false,
	children: [],
	...extra
})

const tableModel: VariableNode = makeNode('tableModelList', 'MODEL_LIST', {
	path: 'tableModelList',
	loopVar: 'table',
	children: [
		makeNode('className', 'SCALAR', { path: 'tableModelList.className' }),
		makeNode('fieldList', 'MODEL_LIST', {
			path: 'tableModelList.fieldList',
			loopVar: 'field',
			children: [makeNode('attrName', 'SCALAR', { path: 'tableModelList.fieldList.attrName' })]
		})
	]
})

describe('decorateVariables', () => {
	it('集合节点的 expr 是循环变量名，selfExpr 是可用路径', () => {
		const [node] = decorateVariables([tableModel])

		expect(node.selfExpr).toBe('tableModelList')
		expect(node.expr).toBe('table')
		// 孙层用父级循环变量名作前缀
		const fieldList = node.children[1]
		expect(fieldList.expr).toBe('field')
		expect(fieldList.children[0].selfExpr).toBe('field.attrName')
	})

	it('标量集合不展开子行', () => {
		const [node] = decorateVariables([
			makeNode('importList', 'SCALAR_LIST', { loopVar: 'i', children: [makeNode('String', 'SCALAR')] })
		])

		expect(node.children).toEqual([])
	})

	it('非法标识符的 MAP key 降级为下标写法', () => {
		const [node] = decorateVariables([
			makeNode('tableTemplatePropertyData', 'MAP', {
				children: [makeNode('api-prefix', 'SCALAR', { path: 'tableTemplatePropertyData.api-prefix' })]
			})
		])

		expect(node.children[0].selfExpr).toBe("tableTemplatePropertyData['api-prefix']")
	})

	it('无属性的 MAP 节点补一条不可插入的说明行', () => {
		const [node] = decorateVariables([makeNode('tableTemplatePropertyData', 'MAP')])

		expect(node.children.length).toBe(1)
		expect(node.children[0].kind).toBe('GROUP')
		expect(node.children[0].name).toBe('无子变量（未配置属性或属于其他模板组）')
		expect(canInsert(node.children[0].kind)).toBe(false)
		// 说明行不计入面板标题的总项数
		expect(countVariables([makeNode('tableTemplatePropertyData', 'MAP')])).toBe(1)
	})
})

describe('buildVariableTree', () => {
	it('顶层标量归入标量变量组，容器各自成组', () => {
		const treeData = buildVariableTree([
			makeNode('projectName', 'SCALAR'),
			makeNode('className', 'SCALAR'),
			tableModel
		])

		expect(treeData[0].kind).toBe('GROUP')
		expect(treeData[0].name).toBe('标量变量')
		expect(treeData[0].children.length).toBe(2)
		expect(treeData[1].name).toBe('tableModelList')
		expect(treeData[1].kind).toBe('MODEL_LIST')
	})
})

describe('buildInsertText', () => {
	it('标量插入 ${}', () => {
		const [node] = decorateVariables([makeNode('projectName', 'SCALAR')])

		expect(buildInsertText(node)).toEqual({ text: '${projectName}', caret: 14 })
	})

	it('集合插入循环骨架且光标落在循环体内', () => {
		const [node] = decorateVariables([makeNode('fieldList', 'MODEL_LIST', { loopVar: 'field' })])
		const result = buildInsertText(node)!

		expect(result.text).toBe('<#list fieldList as field>\n  \n</#list>')
		// 紧跟换行与两空格缩进之后
		expect(result.caret).toBe(result.text.indexOf('\n') + 3)
	})

	it('对象与 MAP 容器行不可插入', () => {
		const [obj] = decorateVariables([makeNode('entityBaseClass', 'OBJECT')])
		const [map] = decorateVariables([makeNode('tableTemplatePropertyData', 'MAP')])

		expect(buildInsertText(obj)).toBeNull()
		expect(buildInsertText(map)).toBeNull()
		expect(canInsert('OBJECT')).toBe(false)
		expect(canInsert('SCALAR_LIST')).toBe(true)
	})
})

describe('辅助函数', () => {
	it('总项数递归统计，不含虚拟分组', () => {
		expect(countVariables([tableModel])).toBe(4)
	})

	it('kind 映射语义色标签', () => {
		expect(kindTag('SCALAR')).toEqual({ label: '标量', type: 'info' })
		expect(kindTag('MODEL_LIST')).toEqual({ label: '列表', type: 'primary' })
		expect(kindTag('OBJECT')).toEqual({ label: '对象', type: 'warning' })
		expect(kindTag('MAP')).toEqual({ label: '属性', type: 'success' })
		expect(kindTag('GROUP')).toBeNull()
	})

	it('搜索命中变量名、说明与表达式', () => {
		const [node] = decorateVariables([makeNode('attrName', 'SCALAR', { desc: '属性名称' })])

		expect(matchKeyword(node, 'attr')).toBe(true)
		expect(matchKeyword(node, '属性名')).toBe(true)
		expect(matchKeyword(node, 'zzz')).toBe(false)
		expect(matchKeyword(node, '')).toBe(true)
	})
})
```

- [ ] **Step 5: 跑测试，确认失败**

```powershell
cd F:\project\idea\self\yanggu-code-generator\yanggu-code-generator-frontend; pnpm exec vitest run src/views/gen/template/variable-utils.test.ts
```

预期：`Cannot find module './variable-utils'` 或全部用例失败。

- [ ] **Step 6: 实现纯函数层**

新建 `variable-utils.ts`：

```ts
// 模板变量目录的纯工具函数：不依赖响应式状态，纯输入 -> 输出，便于复用与单测

import type { VariableKind, VariableNode } from '@/types'

// 面板显示用节点：额外的 GROUP 是前端合成的顶层分组，后端不产出
export type DisplayKind = VariableKind | 'GROUP'

export interface DisplayVariableNode {
	name: string
	path: string
	javaType: string
	kind: DisplayKind
	desc: string
	loopVar: string
	nullable: boolean
	// 自身可用表达式，如 table.field.attrName；即 ${} 内该写的内容
	selfExpr: string
	// 子级表达式前缀：集合用循环变量名，其余同 selfExpr
	expr: string
	// el-tree 的 node-key
	uid: string
	children: DisplayVariableNode[]
}

// 顶层标量归入的虚拟分组名（规格 3.1 结构自动分区）
const SCALAR_GROUP_NAME = '标量变量'

// 合法 FreeMarker 标识符，否则降级为 ['x-y'] 下标写法
const IDENTIFIER_REGEX = /^[A-Za-z_$][A-Za-z0-9_$]*$/

const appendName = (prefix: string, name: string): string => {
	if (!IDENTIFIER_REGEX.test(name)) {
		const quoted = `['${name}']`
		return prefix ? `${prefix}${quoted}` : quoted
	}
	return prefix ? `${prefix}.${name}` : name
}

const isList = (kind: VariableKind): boolean => kind === 'MODEL_LIST' || kind === 'SCALAR_LIST'

// MAP 节点无子变量时的说明行：避免用户以为面板加载不完整（规格 6.3）
// 文案对两种成因都为真：当前组确实未配属性，或是其他模板组的 MAP（后端有意不展开）——见 ledger R16
const makeEmptyMapNode = (uid: string): DisplayVariableNode => ({
	name: '无子变量（未配置属性或属于其他模板组）',
	path: '',
	javaType: '',
	kind: 'GROUP',
	desc: '',
	loopVar: '',
	nullable: false,
	selfExpr: '',
	expr: '',
	uid: `${uid}-empty-map`,
	children: []
})

// 装饰目录：自顶向下合成表达式，集合的子级以循环变量名为前缀
export const decorateVariables = (nodeList: VariableNode[], parentExpr = '', uidPrefix = ''): DisplayVariableNode[] => {
	return nodeList.map((node, index) => {
		const uid = uidPrefix ? `${uidPrefix}-${index}` : `${index}`
		const selfExpr = appendName(parentExpr, node.name)
		const expr = isList(node.kind) ? node.loopVar || node.name : selfExpr
		// 标量集合的骨架文本已含 ${i}，再挂一行「String」无信息量，故不展开
		const children = node.kind === 'SCALAR_LIST' ? [] : decorateVariables(node.children, expr, uid)
		if (node.kind === 'MAP' && children.length === 0) {
			children.push(makeEmptyMapNode(uid))
		}
		return { ...node, selfExpr, expr, uid, children }
	})
}

// 生成面板数据：标量收进一个虚拟分组，容器变量各自成组
export const buildVariableTree = (nodeList: VariableNode[]): DisplayVariableNode[] => {
	const decorated = decorateVariables(nodeList)
	const scalarList: DisplayVariableNode[] = []
	const containerList: DisplayVariableNode[] = []
	decorated.forEach(node => {
		if (node.kind === 'SCALAR') {
			scalarList.push(node)
		} else {
			containerList.push(node)
		}
	})
	const groupList: DisplayVariableNode[] = []
	if (scalarList.length > 0) {
		groupList.push({
			name: SCALAR_GROUP_NAME,
			path: '',
			javaType: '',
			kind: 'GROUP',
			desc: '',
			loopVar: '',
			nullable: false,
			selfExpr: '',
			expr: '',
			uid: 'scalar-group',
			children: scalarList
		})
	}
	return [...groupList, ...containerList]
}

// 递归统计变量条目数（不含虚拟分组），用于面板标题徽标
export const countVariables = (nodeList: VariableNode[]): number => {
	return nodeList.reduce((sum, node) => {
		const childCount = node.kind === 'SCALAR_LIST' ? 0 : countVariables(node.children)
		return sum + 1 + childCount
	}, 0)
}

export const canInsert = (kind: DisplayKind): boolean => kind === 'SCALAR' || isList(kind as VariableKind)

export const kindTag = (kind: DisplayKind): { label: string; type: 'info' | 'primary' | 'warning' | 'success' } | null => {
	switch (kind) {
		case 'SCALAR':
			return { label: '标量', type: 'info' }
		case 'SCALAR_LIST':
		case 'MODEL_LIST':
			return { label: '列表', type: 'primary' }
		case 'OBJECT':
			return { label: '对象', type: 'warning' }
		case 'MAP':
			return { label: '属性', type: 'success' }
		default:
			return null
	}
}

// 搜索命中：变量名、中文说明、可用表达式三者之一
export const matchKeyword = (node: DisplayVariableNode, keyword: string): boolean => {
	if (!keyword) {
		return true
	}
	const lower = keyword.toLowerCase()
	return node.name.toLowerCase().includes(lower) || node.desc.toLowerCase().includes(lower) || node.selfExpr.toLowerCase().includes(lower)
}

// 点击插入的文本与光标偏移；容器行（对象 / MAP / 分组）不可插入
export const buildInsertText = (node: DisplayVariableNode): { text: string; caret: number } | null => {
	if (node.kind === 'SCALAR') {
		const text = `\${${node.selfExpr}}`
		return { text, caret: text.length }
	}
	if (!isList(node.kind as VariableKind)) {
		return null
	}
	const loopVar = node.loopVar || node.name
	const head = `<#list ${node.selfExpr} as ${loopVar}>`
	const tail = `</#list>`
	const text = `${head}\n  \n${tail}`
	// 光标落在循环体首行缩进之后
	return { text, caret: head.length + 3 }
}
```

- [ ] **Step 7: 跑测试，确认通过**

```powershell
cd F:\project\idea\self\yanggu-code-generator\yanggu-code-generator-frontend; pnpm exec vitest run src/views/gen/template/variable-utils.test.ts
```

预期：全部用例通过。若 `countVariables([tableModel])` 报 3，说明标量集合子行未排除，按 Step 6 的 `childCount` 分支修正。

- [ ] **Step 8: Commit（须先取得用户同意）**

```powershell
cd F:\project\idea\self\yanggu-code-generator; git add yanggu-code-generator-frontend/src; git commit -m "feat(template): 前端变量目录类型、API 与纯函数层"
```

---

## Task 8: 加载 hook + 面板组件

**Files:**
- Create: `.../views/gen/template/variable-panel/use-variable-catalog.ts`
- Create: `.../views/gen/template/variable-panel/index.vue`

**Interfaces:**
- Consumes: `genTemplateApi.variableCatalog(templateGroupId)`（Task 7）、`buildVariableTree` / `countVariables` / `kindTag` / `canInsert` / `buildInsertText` / `matchKeyword`（Task 7）
- Produces（Task 9 依赖）：
  - `useVariableCatalog(templateGroupId: Ref<number>): { catalog, loading, error, reload }`
  - `<VariablePanel :template-group-id :disabled :insert />`，无 emit
  - `insert` prop 契约：`(text: string, caret: number) => boolean`

- [ ] **Step 1: 写加载 hook**

```ts
import { ref, watch, type Ref } from 'vue'
import { genTemplateApi } from '@/api'
import type { VariableNode } from '@/types'

// 按模板组缓存：切回看过的组不再请求
const catalogCache = new Map<number, VariableNode[]>()

/**
 * 变量目录加载：失败保留上一次成功目录，提示只落在面板内（规格 6.2）
 */
export const useVariableCatalog = (templateGroupId: Ref<number>) => {
	const catalog = ref<VariableNode[]>([])
	const loading = ref(false)
	// 空串表示无异常
	const error = ref('')

	const load = async (force = false) => {
		const groupId = templateGroupId.value
		if (!groupId) {
			return
		}
		const cached = catalogCache.get(groupId)
		if (cached && !force) {
			catalog.value = cached
			error.value = ''
			return
		}
		loading.value = true
		try {
			const data = await genTemplateApi.variableCatalog(groupId)
			const list = data ?? []
			catalogCache.set(groupId, list)
			catalog.value = list
			error.value = ''
		} catch (e: any) {
			error.value = e?.message || '变量目录加载失败，点击重试'
		} finally {
			loading.value = false
		}
	}

	watch(templateGroupId, () => load(), { immediate: true })

	return { catalog, loading, error, reload: () => load(true) }
}
```

- [ ] **Step 2: 写面板组件**

`defineOptions` 名、hover 显隐、`link` 形态纯图标按钮均对齐项目既有约定；`onErrorCaptured` 用于面板渲染崩时局部降级（规格 6.2 ②层）：

```vue
<template>
	<div class="variable-panel">
		<el-empty v-if="broken" description="变量面板不可用，请刷新页面后重试" :image-size="60"></el-empty>
		<template v-else>
			<div class="panel-header">
				<el-text tag="b" size="small">变量</el-text>
				<el-tag size="small" type="info" effect="plain">{{ totalCount }}</el-tag>
				<el-button link size="small" :icon="Refresh" :loading="loading" title="重新加载" @click="reload()"></el-button>
			</div>
			<el-input v-model="keyword" size="small" clearable :prefix-icon="Search" placeholder="搜索变量名 / 说明" style="margin-bottom: 6px"></el-input>
			<el-skeleton v-if="loading && totalCount === 0" :rows="6" animated></el-skeleton>
			<el-empty v-else-if="error" :description="error" :image-size="60">
				<el-button size="small" @click="reload()">重试</el-button>
			</el-empty>
			<el-empty v-else-if="totalCount === 0" description="该模板组无可用变量" :image-size="60"></el-empty>
			<el-scrollbar v-else class="panel-scrollbar">
				<el-tree
					ref="treeRef"
					:data="treeData"
					node-key="uid"
					:props="treeProps"
					:expand-on-click-node="false"
					:default-expanded-keys="expandedKeyList"
					:filter-node-method="filterNodeMethod"
					class="panel-tree">
					<template #default="{ data }">
						<div class="variable-row">
							<template v-if="data.kind === 'GROUP'">
								<el-text tag="b" size="small">{{ data.name }}（{{ data.children.length }}）</el-text>
							</template>
							<template v-else>
								<el-tooltip :content="data.selfExpr" placement="top" :show-after="400">
									<span class="variable-name">{{ data.name }}</span>
								</el-tooltip>
								<el-tag v-if="kindTag(data.kind)" size="small" :type="kindTag(data.kind)!.type" effect="plain">
									{{ kindTag(data.kind)!.label }}
								</el-tag>
								<el-tooltip v-if="data.nullable" content="该变量渲染时可能为 null，模板需用 <#if xxx??> 保护" placement="left">
									<el-tag size="small" type="warning" effect="plain">可空</el-tag>
								</el-tooltip>
								<el-tooltip v-if="data.desc" :content="data.desc" placement="left" :show-after="200">
									<el-icon class="row-icon"><InfoFilled></InfoFilled></el-icon>
								</el-tooltip>
								<el-tooltip v-if="canInsert(data.kind)" :content="disabled ? '请先打开一个模板文件' : '插入到光标处'" placement="left">
									<el-button class="row-icon insert-button" link size="small" :disabled="disabled" @click.stop="handleInsert(data)">
										{{ data.kind === 'SCALAR' ? '插' : '插循环' }}
									</el-button>
								</el-tooltip>
							</template>
						</div>
					</template>
				</el-tree>
			</el-scrollbar>
		</template>
	</div>
</template>

<script setup lang="ts">
import { InfoFilled, Refresh, Search } from '@element-plus/icons-vue'
import { useVariableCatalog } from './use-variable-catalog'
import { buildInsertText, buildVariableTree, canInsert, countVariables, kindTag, matchKeyword, type DisplayVariableNode } from '../variable-utils'

defineOptions({
	name: 'VariablePanel'
})

const props = defineProps({
	templateGroupId: {
		type: Number,
		required: true
	},
	// 当前 tab 不是模板文件时禁用插入
	disabled: {
		type: Boolean,
		default: false
	},
	// 由宿主提供：把文本写进编辑器光标处，返回是否成功
	insert: {
		type: Function,
		required: true
	}
})

const templateGroupIdRef = toRef(props, 'templateGroupId')
const { catalog, loading, error, reload } = useVariableCatalog(templateGroupIdRef)

const treeRef = ref()
const keyword = ref('')
// 目录结构畸形导致渲染异常时局部降级，不阻断模板编辑
const broken = ref(false)

const treeProps = { children: 'children', label: 'name' }
const treeData = computed(() => buildVariableTree(catalog.value))
const totalCount = computed(() => countVariables(catalog.value))
// 默认只展开顶层分组，268 条全展开反而看不到东西
const expandedKeyList = computed(() => treeData.value.filter(item => item.kind === 'GROUP').map(item => item.uid))

watch(keyword, value => {
	treeRef.value?.filter(value)
})

const filterNodeMethod = (value: string, data: DisplayVariableNode) => matchKeyword(data, value)

const handleInsert = (data: DisplayVariableNode) => {
	const result = buildInsertText(data)
	if (result) {
		props.insert(result.text, result.caret)
	}
}

onErrorCaptured(() => {
	broken.value = true
	// 阻断冒泡，避免抽屉主体被连带卸载
	return false
})
</script>

<style scoped>
.variable-panel {
	display: flex;
	flex-direction: column;
	height: 100%;
	padding-left: 8px;
	border-left: 1px solid var(--el-border-color-lighter);
	overflow: hidden;
}

.panel-header {
	display: flex;
	align-items: center;
	gap: 6px;
	margin-bottom: 6px;
}

.panel-scrollbar {
	flex: 1;
	min-height: 0;
}

.variable-row {
	display: flex;
	align-items: center;
	gap: 4px;
	width: 100%;
	font-size: 12px;
}

.variable-name {
	font-family: var(--el-font-family-mono, Consolas, Monaco, monospace);
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.row-icon {
	font-size: 12px;
	color: var(--el-text-color-secondary);
}

/* 插入按钮 hover 才出现，沿用 tree.vue 的 .tree-node-actions 显隐模式 */
.insert-button {
	visibility: hidden;
	flex-shrink: 0;
}

.variable-row:hover .insert-button {
	visibility: visible;
}

:deep(.el-tree-node__content) {
	height: 26px;
}
</style>
```

- [ ] **Step 3: 类型检查**

```powershell
cd F:\project\idea\self\yanggu-code-generator\yanggu-code-generator-frontend; pnpm run type-check 2>&1 | Select-String -Pattern 'variable-panel|variable-utils'
```

预期：新文件相关报错为 0 条（其余文件报错属基线，Task 10 差分）。若报 `toRef` / `computed` / `onErrorCaptured` 未定义，说明这两个组件未被 `unplugin-auto-import` 覆盖，改为从 `vue` 显式 import（对齐 `tree.vue` 里 `ref`/`computed` 不 import 的现状，先确认再改）。

- [ ] **Step 4: Commit（须先取得用户同意）**

```powershell
cd F:\project\idea\self\yanggu-code-generator; git add yanggu-code-generator-frontend/src; git commit -m "feat(template): 新增模板变量面板组件与加载 hook"
```

---

## Task 9: 编辑器暴露插入能力 + `tree.vue` 接入

**Files:**
- Modify: `.../src/business/code-mirror/index.vue:64-68`
- Modify: `.../src/views/gen/template/tree.vue`（115-215 行布局、195 行 ref、221-233 行 import、新增状态与插入函数）

**Interfaces:**
- Consumes: `VariablePanel`（Task 8）
- Produces: `CodeMirror` 组件 expose `insertAtCursor(text: string, caret?: number): boolean`

- [ ] **Step 1: 给 code-mirror 加插入方法**

在 `handleReady` 之后追加，并暴露出去（`view` ref 已存在，无新增状态）：

```ts
/**
 * 在光标处插入文本并把光标移到 caret 偏移处
 * @returns 是否插入成功，编辑器未 ready 或只读时返回 false
 */
const insertAtCursor = (text: string, caret: number = text.length): boolean => {
	const editorView = view.value as EditorView | undefined
	if (!editorView || props.readOnly) {
		return false
	}
	const from = editorView.state.selection.main.from
	editorView.dispatch({
		changes: { from, insert: text },
		selection: { anchor: from + caret }
	})
	// dispatch 触发 vue-codemirror 的 update 监听 -> v-model 回写 -> 宿主 watch 自动置 isEdited
	editorView.focus()
	return true
}

defineExpose({
	insertAtCursor
})
```

`test.vue` 等既有使用方未传 ref，`defineExpose` 向后兼容，无需改动它们。

- [ ] **Step 2: tree.vue 挂编辑器 ref**

第 195 行改为：

```html
								<code-mirror ref="codeMirrorRef" v-model="activeTabItem.templateContent!" @keydown.ctrl.s.prevent="saveTemplateContent()"></code-mirror>
```

第 116 行的 `<el-container ...>` 去掉 `:class="{ 'full-screen-mode': treeFullScreen }"`（下一步整体上移）。

- [ ] **Step 3: 右侧 panel 内层 splitter 化**

第 115-215 行的 `<el-splitter-panel>` 内容整体包一层内层 splitter，变量面板作为第二个 panel，`full-screen-mode` 挂到内层 splitter 根：

```html
			<el-splitter-panel>
				<el-splitter class="editor-splitter" :class="{ 'full-screen-mode': treeFullScreen }" style="height: 100%">
					<el-splitter-panel :size="variablePanelVisible ? '72%' : '100%'">
						<!-- 原第 116-214 行的 el-container 与空态 el-main 原样搬进来，缩进 +1 层 tab -->
					</el-splitter-panel>
					<el-splitter-panel v-if="variablePanelVisible" size="28%" min="240" max="560" collapsible>
						<variable-panel
							:template-group-id="props.templateGroupId"
							:disabled="!canInsertVariable"
							:insert="insertVariable"></variable-panel>
					</el-splitter-panel>
				</el-splitter>
			</el-splitter-panel>
```

在头部按钮列（原第 123-130 行的 `el-col :span="7"`）末尾追加开关：

```html
								<el-button size="small" :type="variablePanelVisible ? 'primary' : 'default'" plain @click="variablePanelVisible = !variablePanelVisible">
									变量
								</el-button>
```

- [ ] **Step 4: tree.vue 脚本侧接线**

import 区（第 233 行后）追加：

```ts
import VariablePanel from '@/views/gen/template/variable-panel/index.vue'
```

`const treeFullScreen = ref(false)` 附近追加状态与函数：

```ts
// 变量面板默认收起，需要时从头部按钮唤出
const variablePanelVisible = ref(false)
const codeMirrorRef = ref()

// 只有模板文件（templateType 1）有编辑器可插入；目录与二进制文件禁用
// activeTabItem 在第 343 行声明为 `const activeTabItem: Ref<Tree> = computed(...)`，
// 脚本里必须 `.value`（模板里自动解包），写法对齐第 492 行的既有 watch
const canInsertVariable = computed(() => activeTabItem.value.templateType === 1)

// 插入入口：把文本交给 code-mirror 新暴露的 insertAtCursor；
// 返回 false 表示编辑器尚未就绪，面板据此提示而不是静默失败
const insertVariable = (text: string, caret: number): boolean => {
	return codeMirrorRef.value?.insertAtCursor(text, caret) ?? false
}
```

> `activeTabItem.value` 恒有值（computed 在找不到节点时回退为 `{ id: -1, templateContent: '', templateType: -1 }`），因此不需要可选链，`templateType === 1` 直接得到 boolean。

- [ ] **Step 5: 手测插入链路**

后端经 IDEA MCP Debug 启动，前端 `pnpm dev`。打开表模板组的 `Entity.ftl`：

1. 点头部「变量」→ 面板展开，出现变量行；
2. 点 `fieldList` 行的「插循环」→ 编辑器光标处出现 `<#list fieldList as field>` 三行骨架，光标停在中间空行；
3. 展开 `fieldList` → 点 `attrName` 的「插」→ 得到 `${field.attrName}`；
4. 确认 tab 上红点亮起、保存按钮由灰变亮（说明 v-model 回写与既有 watch 生效）；
5. 切到目录节点（`templateType === 0`）→ 插入按钮 disabled，tooltip 为「请先打开一个模板文件」；
6. 全屏 → 退出全屏 → 面板与编辑区宽度分配正常、面板未被留在遮罩外。

- [ ] **Step 6: Commit（须先取得用户同意）**

```powershell
cd F:\project\idea\self\yanggu-code-generator; git add yanggu-code-generator-frontend/src; git commit -m "feat(template): 模板编辑页接入变量面板"
```

---

## Task 10: 回归基线差分校验

**Files:**
- Delete: `yanggu-code-generator-frontend/type-check-baseline.txt`（Task 7 Step 1 临时产物，不入库）
- Modify: 无（仅验证；发现问题回到对应 Task 修）

**Interfaces:**
- Consumes: Task 7 Step 1 记录的 vue-tsc 基线
- Produces: 交付结论（无回归 / 有回归及清单）

- [ ] **Step 1: 前端全量校验**

```powershell
cd F:\project\idea\self\yanggu-code-generator\yanggu-code-generator-frontend; pnpm run type-check; pnpm run lint; pnpm run test
```

预期：vitest 全绿（含 `tree-utils.test.ts` 既有用例）；eslint 对新文件 0 error。

- [ ] **Step 2: vue-tsc 基线差分**

把本次输出与 `type-check-baseline.txt` 比对：

```powershell
cd F:\project\idea\self\yanggu-code-generator\yanggu-code-generator-frontend; pnpm run type-check 2>&1 | Tee-Object -FilePath .\type-check-after.txt | Out-Null; Compare-Object (Get-Content .\type-check-baseline.txt) (Get-Content .\type-check-after.txt)
```

判定：

- 涉及 `variable-panel/`、`variable-utils.ts`、`api/gen/template.ts`、`types/api/gen/template.ts`、`code-mirror/index.vue`、`views/gen/template/tree.vue` 的新增报错 → **回归**，回到对应 Task 修；
- 其余文件报错条数与分布与基线一致 → 未引入回归（只看「有没有报错」会把存量历史报错误判成本次问题）；
- 基线为 0 错时，要求本次仍为 0。

- [ ] **Step 3: 后端全量测试**

```powershell
cd F:\project\idea\self\yanggu-code-generator\yanggu-code-generator-backend; mvn -q test
```

预期：`VariableCatalogBuilderTest`、`VariableCatalogServiceTest`、`DataModelBuilderTest`、`ProductGeneratorTest`、`PreviewBuilderRenderTest` 全通过。

- [ ] **Step 4: 三类模板组端到端**

后端经 IDEA MCP Debug 启动（不得命令行杀进程），前端 `pnpm dev`：

1. 项目模板组开面板 → 确认 `tableModelList` 可下钻到 `table.field.attrName`，插入文本为 `${table.field.attrName}`；
2. 枚举模板组开面板 → 确认**没有** `projectName` / `projectNamePascal` 行（规格 2.3 的差异即面板价值），`enumItemList` 插出 `<#list enumItemList as item>`；
3. 表模板组开面板 → 确认 `tableTemplatePropertyData` 展开出本组属性 key，`projectTemplatePropertyData` 不可展开且说明含「key 来自其他模板组的属性配置」；
4. 断网或停后端后点「重试」→ 面板内出现失败文案且保留上次目录，**全局不弹 error 提示**（规格 6.2 禁止重复提示）；
5. 删掉某个 Model 字段的 `@VarDesc` 跑一次 `mvn -q test` → `everyModelFieldCarriesVarDesc` 失败（闸门确实生效），随后还原。

- [ ] **Step 5: 清理临时文件并汇报**

```powershell
cd F:\project\idea\self\yanggu-code-generator\yanggu-code-generator-frontend; Remove-Item .\type-check-baseline.txt, .\type-check-after.txt
```

向用户交付：变更文件清单、Task 6 的行为变更说明（原本失败的表/枚举模板改为取到属性默认值）、规格 6.4 的两处已知不完美。

- [ ] **Step 6: Commit（须先取得用户同意）**

若前序 Task 的 commit 均已获同意执行，本步无新增内容；否则把本次全部改动按后端 / 前端分两笔提交，提交信息分别用 Task 5 Step 7 与 Task 9 Step 6 的文案。
