# 代码生成接口统一化重构实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 10 个按类型散落的生成端点收敛为 6 个统一端点（GeneratorQuery + ProjectFilter 组合入参 + ProductGenerator 策略路由），修复表/枚举假批量与枚举批量传参两个缺陷，前端 API 层同步类型化重写。

**Architecture:** 后端在已拆分的 support 协作类（PreviewBuilder/DataModelBuilder/DownloadHandler/PreviewTreeBuilder/TemplatePathResolver）之上增加 ProductGenerator 策略层，GeneratorServiceImpl 退化为 EnumMap 薄路由；6 端点全 POST（@RequestBody 原生绑定嵌套 ProjectFilter），前端重写 api/gen/generator.ts 并改造 7 处调用点，downloadFile 扩展 method 参数支持 POST blob 下载（默认 GET 保 common export）。

**Tech Stack:** Spring Boot 4.1 / MyBatis-Plus / hutool v7 / knife4j-next / JUnit5 + Mockito（spring-boot-starter-test 已在 pom L99）；Vue3 + TS + Element Plus + axios/qs。

**Spec:** `docs/superpowers/specs/2026-11-20-generator-api-unification-design.md`（已确认，含 ProjectFilter 组合修订）

## Global Constraints

- 模型类（ProjectModel/TableModel/EnumModel 等）**一字段不动**（DB 中 .ftl 模板正文依赖变量名）。
- `TemplateTestQuery` 与 `/template/test` 端点契约不变；错误码 700（表达式 false）语义不变。
- 所有 commit 步骤执行前必须获得用户明确同意（项目规则：未经同意不改代码、不提交）。
- 后端启动一律 Debug 模式：`xdebug_start_debugger_session(configurationName=CodeGeneratorApplication)`；启动前先向用户确认 IDEA 面板无运行实例；验证读 `logs/code-generator-info.log` 出现 `Started CodeGeneratorApplication`；禁止 netstat/taskkill 绕过。
- 前端 ESLint func-style：一律函数表达式，禁止 function 声明。
- 旧端点直接删除无双轨；前后端必须成对交付/回滚。
- 空结果行为保持现状：zip 空抛"待写入数据为空"、local 空 warn 跳过、downloadSingle 空 getFirst 抛 NoSuchElementException。
- 路径越界防护/模板缓存/N+1 优化明确排除，不得顺手加入。

---

### Task 1: 基线提交（上一会话拆分成果隔离）

**Files:**
- Commit: `yanggu-code-generator-backend/src/main/java/com/yanggu/code/generator/service/impl/GeneratorServiceImpl.java`（已修改）
- Commit: `yanggu-code-generator-backend/src/main/java/com/yanggu/code/generator/service/support/`（5 个新文件）
- Commit: `docs/superpowers/specs/2026-11-20-generator-api-unification-design.md`、`docs/superpowers/plans/2026-11-20-generator-api-unification.md`
- 排除: `yanggu-code-generator-frontend/yanggu-code-generator-frontend.iml`（IDE 产物，不提交）

**Interfaces:**
- Consumes: 无
- Produces: 干净基线 commit，后续任务在其上叠加

- [ ] **Step 1: 向用户确认提交基线**（含"是否提交"明确问句，得到同意才继续）

- [ ] **Step 2: 核对暂存范围**

Run: `git status --porcelain`
Expected: 仅上述后端文件 + 2 个文档 + 1 个 .iml（.iml 不 add）

- [ ] **Step 3: 提交**

```bash
git add yanggu-code-generator-backend/src/main/java/com/yanggu/code/generator/service/impl/GeneratorServiceImpl.java yanggu-code-generator-backend/src/main/java/com/yanggu/code/generator/service/support/ docs/superpowers/specs/2026-11-20-generator-api-unification-design.md docs/superpowers/plans/2026-11-20-generator-api-unification.md
git commit -m "refactor: 拆分 GeneratorServiceImpl 为 support 协作类并归档接口统一化设计"
```

---

### Task 2: GeneratorQuery + ProjectFilter + 校验（TDD）

**Files:**
- Create: `yanggu-code-generator-backend/src/main/java/com/yanggu/code/generator/domain/query/GeneratorQuery.java`
- Create: `yanggu-code-generator-backend/src/main/java/com/yanggu/code/generator/domain/query/ProjectFilter.java`
- Test: `yanggu-code-generator-backend/src/test/java/com/yanggu/code/generator/domain/query/GeneratorQueryTest.java`（src/test 目录首次创建）

**Interfaces:**
- Consumes: `TemplateGroupTypeEnum`（PROJECT=0/TABLE=1/ENUM=2）、`BusinessException(String)`、`EnumUtil.getBy`、`CollUtil`
- Produces: `GeneratorQuery`（getProductType/getProductIds/getTemplateIds/getProjectFilter，Lombok @Data）、`void validate(boolean singleMode)`、`ProjectFilter`（getTableIds/getEnumIds/getTableTemplateIds/getEnumTemplateIds）

- [ ] **Step 1: 写失败测试**

```java
package com.yanggu.code.generator.domain.query;

import com.yanggu.code.generator.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeneratorQueryTest {

    private GeneratorQuery query(Integer type, List<Long> productIds) {
        GeneratorQuery q = new GeneratorQuery();
        q.setProductType(type);
        q.setProductIds(productIds);
        return q;
    }

    @Test
    void invalidProductTypeThrows() {
        assertThrows(BusinessException.class, () -> query(99, List.of(1L)).validate(false));
        assertThrows(BusinessException.class, () -> query(null, List.of(1L)).validate(false));
    }

    @Test
    void emptyProductIdsThrows() {
        assertThrows(BusinessException.class, () -> query(1, List.of()).validate(false));
        assertThrows(BusinessException.class, () -> query(1, null).validate(false));
    }

    @Test
    void projectRequiresExactlyOneProductId() {
        assertThrows(BusinessException.class, () -> query(0, List.of(1L, 2L)).validate(false));
        assertDoesNotThrow(() -> query(0, List.of(1L)).validate(false));
    }

    @Test
    void nonProjectWithProjectFilterThrows() {
        GeneratorQuery q = query(1, List.of(1L));
        q.setProjectFilter(new ProjectFilter());
        assertThrows(BusinessException.class, () -> q.validate(false));
    }

    @Test
    void projectWithProjectFilterPasses() {
        GeneratorQuery q = query(0, List.of(1L));
        ProjectFilter filter = new ProjectFilter();
        filter.setTableIds(List.of(201L));
        q.setProjectFilter(filter);
        assertDoesNotThrow(() -> q.validate(false));
    }

    @Test
    void singleModeRequiresOneProductAndOneTemplate() {
        GeneratorQuery q = query(1, List.of(1L, 2L));
        q.setTemplateIds(List.of(11L));
        assertThrows(BusinessException.class, () -> q.validate(true));
        GeneratorQuery q2 = query(1, List.of(1L));
        assertThrows(BusinessException.class, () -> q2.validate(true));
        GeneratorQuery q3 = query(1, List.of(1L));
        q3.setTemplateIds(List.of(11L));
        assertDoesNotThrow(() -> q3.validate(true));
    }

    @Test
    void batchModeAllowsEmptyTemplateIds() {
        assertDoesNotThrow(() -> query(2, List.of(1L, 2L)).validate(false));
    }
}
```

- [ ] **Step 2: 运行确认失败**

Run: `cd yanggu-code-generator-backend; mvn -q test -Dtest=GeneratorQueryTest`
Expected: 编译失败（GeneratorQuery/ProjectFilter 不存在）

- [ ] **Step 3: 实现两个 DTO**

```java
package com.yanggu.code.generator.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 项目生成的子产物过滤（仅PROJECT）
 */
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

```java
package com.yanggu.code.generator.domain.query;

import cn.hutool.v7.core.collection.CollUtil;
import cn.hutool.v7.core.util.EnumUtil;
import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.enums.TemplateGroupTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 代码生成统一查询
 */
@Data
@Schema(description = "代码生成统一查询")
public class GeneratorQuery {

    @Schema(description = "产物类型：0项目 1表 2枚举")
    private Integer productType;

    @Schema(description = "产物ID列表：项目=[projectId]，表=tableIds，枚举=enumIds")
    private List<Long> productIds;

    @Schema(description = "模板ID列表，空=该产物模板组全部模板；PROJECT时指项目级模板")
    private List<Long> templateIds;

    @Schema(description = "项目子产物过滤，仅productType=PROJECT时允许非null")
    private ProjectFilter projectFilter;

    /**
     * 统一入参校验
     *
     * @param singleMode 单品模式（downloadSingle/singleLocal）要求恰好一个产物与一个模板
     */
    public void validate(boolean singleMode) {
        TemplateGroupTypeEnum type = EnumUtil.getBy(TemplateGroupTypeEnum::getCode, productType);
        if (type == null) {
            throw new BusinessException("产物类型异常: " + productType);
        }
        if (CollUtil.isEmpty(productIds)) {
            throw new BusinessException("产物ID列表为空");
        }
        if (type == TemplateGroupTypeEnum.PROJECT && productIds.size() != 1) {
            throw new BusinessException("项目生成的产物ID必须恰好一个");
        }
        if (type != TemplateGroupTypeEnum.PROJECT && projectFilter != null) {
            throw new BusinessException("产物类型与过滤字段不匹配");
        }
        if (singleMode && (productIds.size() != 1 || CollUtil.size(templateIds) != 1)) {
            throw new BusinessException("单品生成必须指定恰好一个产物与一个模板");
        }
    }
}
```

- [ ] **Step 4: 运行确认通过**

Run: `cd yanggu-code-generator-backend; mvn -q test -Dtest=GeneratorQueryTest`
Expected: 7 个测试 PASS

- [ ] **Step 5: 提交（先征得用户同意）**

```bash
git add yanggu-code-generator-backend/src/main/java/com/yanggu/code/generator/domain/query/GeneratorQuery.java yanggu-code-generator-backend/src/main/java/com/yanggu/code/generator/domain/query/ProjectFilter.java yanggu-code-generator-backend/src/test/java/com/yanggu/code/generator/domain/query/GeneratorQueryTest.java
git commit -m "feat: 新增统一生成查询 GeneratorQuery/ProjectFilter 及校验（含首批单测）"
```

---

### Task 3: PreviewBuilder/DataModelBuilder 签名调整（行为不变，编译绿）

**Files:**
- Modify: `yanggu-code-generator-backend/src/main/java/com/yanggu/code/generator/service/support/PreviewBuilder.java`
- Modify: `yanggu-code-generator-backend/src/main/java/com/yanggu/code/generator/service/support/DataModelBuilder.java`
- Modify: `yanggu-code-generator-backend/src/main/java/com/yanggu/code/generator/service/support/DownloadHandler.java`（仅类注释）
- Modify: `yanggu-code-generator-backend/src/main/java/com/yanggu/code/generator/service/impl/GeneratorServiceImpl.java`（适配新签名，旧端点暂留）

**Interfaces:**
- Consumes: Task 2 的 `GeneratorQuery`/`ProjectFilter`
- Produces: `PreviewBuilder.tablePreview(Long tableId, List<Long> templateIds)`、`PreviewBuilder.enumPreview(Long enumId, List<Long> templateIds)`、`PreviewBuilder.buildProjectPreview(GeneratorQuery query)`（均不再 throws Exception）、`DataModelBuilder.resolveDatasource(ProjectEntity)`；Task 4/5 依赖这些签名

- [ ] **Step 1: PreviewBuilder 改三个方法签名**

`tablePreview` 改为（删除原 GeneratorTableQuery 解包两行，其余逻辑不动）：

```java
public List<TemplateContentVO> tablePreview(Long tableId, List<Long> templateIds) {
    TableEntity table = tableService.getById(tableId);
    ProjectEntity project = projectService.getById(table.getProjectId());
    TableModel tableModel = dataModelBuilder.buildTableDataModel(table, project);
    Long tableTemplateGroupId = tableService.getTableTemplateGroupId(tableId);
    TemplateGroupEntity templateGroup = templateGroupService.getById(tableTemplateGroupId, true);
    return renderGroupTemplates(templateGroup, templateIds, tableModel,
            templateContentVO -> templateContentVO.setTableId(tableId));
}
```

`enumPreview` 改为：

```java
public List<TemplateContentVO> enumPreview(Long enumId, List<Long> templateIds) {
    EnumEntity enumEntity = enumService.getById(enumId);
    ProjectEntity project = projectService.getById(enumEntity.getProjectId());
    EnumModel enumModel = dataModelBuilder.buildEnumDataModel(enumEntity, project);
    TemplateGroupEntity templateGroup = templateGroupService.getById(project.getEnumTemplateGroupId(), true);
    return renderGroupTemplates(templateGroup, templateIds, enumModel,
            templateContentVO -> templateContentVO.setEnumId(enumId));
}
```

`buildProjectPreview(GeneratorQuery query)`：方法体开头改为从 query 解包，后续三段编排逻辑不动：

```java
public List<TemplateContentVO> buildProjectPreview(GeneratorQuery query) {
    Long projectId = query.getProductIds().getFirst();
    ProjectEntity project = projectService.getById(projectId);
    ProjectFilter filter = query.getProjectFilter();
    List<Long> tableIdList = filter == null ? null : filter.getTableIds();
    List<Long> enumIdList = filter == null ? null : filter.getEnumIds();
    List<Long> tableTemplateIdList = filter == null ? null : filter.getTableTemplateIds();
    List<Long> enumTemplateIdList = filter == null ? null : filter.getEnumTemplateIds();
    // ……原有 tableList 查询/过滤、tableListPreview、enumListPreview、projectPreview 三段保持，
    // 其中 projectPreview(project, query.getTemplateIds())
}
```

同步：`tableListPreview` 内部改调 `tablePreview(table.getId(), tableTemplateIdList)`（删除 GeneratorTableQuery 构造）；删除 `resolveDatasource` 方法（移入 DataModelBuilder）；删除三个旧 Query 的 import；`projectPreview` 内 `resolveDatasource(project)` 改调 `dataModelBuilder.resolveDatasource(project)`；去掉 `buildProjectPreview`/`projectPreview` 的 `throws Exception`。

- [ ] **Step 2: renderGroupTemplates 的 peek 改 map**

```java
public List<TemplateContentVO> renderGroupTemplates(TemplateGroupEntity templateGroup, List<Long> templateIdFilter,
                                                    Object dataModel, Consumer<TemplateContentVO> contentFiller) {
    return leafTemplates(templateGroup, templateIdFilter).stream()
            .filter(template -> checkCondition(template.getConditionExpression(), dataModel))
            .map(template -> {
                TemplateContentVO vo = render(templateGroup, template, dataModel);
                if (contentFiller != null) {
                    contentFiller.accept(vo);
                }
                return vo;
            })
            .toList();
}
```

- [ ] **Step 3: DataModelBuilder 接收 resolveDatasource**

注入 `DatasourceService`（@Autowired 字段，与现有风格一致），新增：

```java
/**
 * 解析项目的数据源（无数据源时返回 null）
 */
public DatasourceEntity resolveDatasource(ProjectEntity project) {
    Long datasourceId = project.getDatasourceId();
    return datasourceId == null ? null : datasourceService.getById(datasourceId);
}
```

- [ ] **Step 4: DownloadHandler 类注释修正**

"消除原三处重复 switch" 改为 "DIRECTORY/TEMPLATE_FILE/BINARY_FILE 三种类型的写入分发集中于此管理"。

- [ ] **Step 5: GeneratorServiceImpl 适配（过渡，旧端点保留）**

所有 `previewBuilder.tablePreview(tableQuery)` 改 `previewBuilder.tablePreview(tableQuery.getTableIdList().getFirst(), tableQuery.getTemplateIdList())`；`enumPreview` 同理；`buildProjectPreview(projectQuery)` 处临时构造 GeneratorQuery 适配：

```java
private GeneratorQuery toGeneratorQuery(GeneratorProjectQuery projectQuery) {
    GeneratorQuery query = new GeneratorQuery();
    query.setProductType(TemplateGroupTypeEnum.PROJECT.getCode());
    query.setProductIds(List.of(projectQuery.getProjectId()));
    query.setTemplateIds(projectQuery.getProjectTemplateIdList());
    ProjectFilter filter = new ProjectFilter();
    filter.setTableIds(projectQuery.getTableIdList());
    filter.setEnumIds(projectQuery.getEnumIdList());
    filter.setTableTemplateIds(projectQuery.getTableTemplateIdList());
    filter.setEnumTemplateIds(projectQuery.getEnumTemplateIdList());
    query.setProjectFilter(filter);
    return query;
}
```

`templateTest` 中 `previewBuilder.resolveDatasource(project)` 改 `dataModelBuilder.resolveDatasource(project)`。此过渡方法与旧端点在 Task 5 删除。

- [ ] **Step 6: 编译验证**

Run: `cd yanggu-code-generator-backend; mvn -q compile`
Expected: BUILD SUCCESS（无输出即成功）

- [ ] **Step 7: 提交（先征得用户同意）**

```bash
git add yanggu-code-generator-backend/src/main/java/com/yanggu/code/generator/service/support/ yanggu-code-generator-backend/src/main/java/com/yanggu/code/generator/service/impl/GeneratorServiceImpl.java
git commit -m "refactor: PreviewBuilder 改单产物签名并接入 GeneratorQuery，resolveDatasource 归位 DataModelBuilder"
```

---

### Task 4: ProductGenerator 策略层（TDD）

**Files:**
- Create: `yanggu-code-generator-backend/src/main/java/com/yanggu/code/generator/service/support/ProductGenerator.java`
- Create: `yanggu-code-generator-backend/src/main/java/com/yanggu/code/generator/service/support/ProjectGenerator.java`
- Create: `yanggu-code-generator-backend/src/main/java/com/yanggu/code/generator/service/support/TableGenerator.java`
- Create: `yanggu-code-generator-backend/src/main/java/com/yanggu/code/generator/service/support/EnumGenerator.java`
- Test: `yanggu-code-generator-backend/src/test/java/com/yanggu/code/generator/service/support/ProductGeneratorTest.java`

**Interfaces:**
- Consumes: Task 3 的 `PreviewBuilder.tablePreview(Long, List<Long>)` / `enumPreview(Long, List<Long>)` / `buildProjectPreview(GeneratorQuery)`；`TableService.listByIds`、`EnumService.listByIds`、`ProjectService.getById`
- Produces: `ProductGenerator { TemplateGroupTypeEnum type(); List<TemplateContentVO> generate(GeneratorQuery query); String artifactName(GeneratorQuery query); }`，三个 @Component 实现；Task 5 以 `List<ProductGenerator>` 注入

- [ ] **Step 1: 写失败测试（Mockito 验证真批量循环与命名）**

```java
package com.yanggu.code.generator.service.support;

import com.yanggu.code.generator.domain.entity.EnumEntity;
import com.yanggu.code.generator.domain.entity.ProjectEntity;
import com.yanggu.code.generator.domain.entity.TableEntity;
import com.yanggu.code.generator.domain.query.GeneratorQuery;
import com.yanggu.code.generator.domain.vo.TemplateContentVO;
import com.yanggu.code.generator.enums.TemplateGroupTypeEnum;
import com.yanggu.code.generator.service.EnumService;
import com.yanggu.code.generator.service.ProjectService;
import com.yanggu.code.generator.service.TableService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductGeneratorTest {

    @Mock
    private PreviewBuilder previewBuilder;
    @Mock
    private TableService tableService;
    @Mock
    private EnumService enumService;
    @Mock
    private ProjectService projectService;
    @InjectMocks
    private TableGenerator tableGenerator;
    @InjectMocks
    private EnumGenerator enumGenerator;
    @InjectMocks
    private ProjectGenerator projectGenerator;

    private GeneratorQuery query(int type, List<Long> ids) {
        GeneratorQuery q = new GeneratorQuery();
        q.setProductType(type);
        q.setProductIds(ids);
        q.setTemplateIds(List.of(11L));
        return q;
    }

    private TemplateContentVO vo(String fileName) {
        TemplateContentVO v = new TemplateContentVO();
        v.setFileName(fileName);
        return v;
    }

    @Test
    void tableGenerateLoopsAllProducts() {
        GeneratorQuery q = query(1, List.of(201L, 202L));
        when(previewBuilder.tablePreview(201L, q.getTemplateIds())).thenReturn(List.of(vo("A.java")));
        when(previewBuilder.tablePreview(202L, q.getTemplateIds())).thenReturn(List.of(vo("B.java")));
        List<TemplateContentVO> result = tableGenerator.generate(q);
        assertEquals(List.of("A.java", "B.java"), result.stream().map(TemplateContentVO::getFileName).toList());
        assertEquals(TemplateGroupTypeEnum.TABLE, tableGenerator.type());
    }

    @Test
    void tableArtifactNameJoinsTableNames() {
        TableEntity t1 = new TableEntity();
        t1.setTableName("sys_user");
        TableEntity t2 = new TableEntity();
        t2.setTableName("sys_role");
        when(tableService.listByIds(List.of(201L, 202L))).thenReturn(List.of(t1, t2));
        assertEquals("sys_user_sys_role", tableGenerator.artifactName(query(1, List.of(201L, 202L))));
    }

    @Test
    void enumGenerateLoopsAllProducts() {
        GeneratorQuery q = query(2, List.of(301L, 302L));
        when(previewBuilder.enumPreview(301L, q.getTemplateIds())).thenReturn(List.of(vo("E1.java")));
        when(previewBuilder.enumPreview(302L, q.getTemplateIds())).thenReturn(List.of(vo("E2.java")));
        assertEquals(List.of("E1.java", "E2.java"),
                enumGenerator.generate(q).stream().map(TemplateContentVO::getFileName).toList());
        assertEquals(TemplateGroupTypeEnum.ENUM, enumGenerator.type());
    }

    @Test
    void enumArtifactNameJoinsEnumNames() {
        EnumEntity e1 = new EnumEntity();
        e1.setEnumName("status");
        EnumEntity e2 = new EnumEntity();
        e2.setEnumName("gender");
        when(enumService.listByIds(List.of(301L, 302L))).thenReturn(List.of(e1, e2));
        assertEquals("status_gender", enumGenerator.artifactName(query(2, List.of(301L, 302L))));
    }

    @Test
    void projectDelegatesAndNamesByProject() {
        GeneratorQuery q = query(0, List.of(100L));
        when(previewBuilder.buildProjectPreview(q)).thenReturn(List.of(vo("P.java")));
        assertEquals(1, projectGenerator.generate(q).size());
        ProjectEntity project = new ProjectEntity();
        project.setProjectName("demo");
        when(projectService.getById(100L)).thenReturn(project);
        assertEquals("demo", projectGenerator.artifactName(q));
        assertEquals(TemplateGroupTypeEnum.PROJECT, projectGenerator.type());
    }
}
```

- [ ] **Step 2: 运行确认失败**

Run: `cd yanggu-code-generator-backend; mvn -q test -Dtest=ProductGeneratorTest`
Expected: 编译失败（ProductGenerator 及实现不存在）

- [ ] **Step 3: 实现接口与三个处理器**

```java
package com.yanggu.code.generator.service.support;

import com.yanggu.code.generator.domain.query.GeneratorQuery;
import com.yanggu.code.generator.domain.vo.TemplateContentVO;
import com.yanggu.code.generator.enums.TemplateGroupTypeEnum;

import java.util.List;

/**
 * 产物生成处理器：按产物类型收口生成编排与 zip 命名，新增产物类型只需新增实现
 */
public interface ProductGenerator {

    TemplateGroupTypeEnum type();

    List<TemplateContentVO> generate(GeneratorQuery query);

    String artifactName(GeneratorQuery query);
}
```

```java
package com.yanggu.code.generator.service.support;

import com.yanggu.code.generator.domain.query.GeneratorQuery;
import com.yanggu.code.generator.domain.vo.TemplateContentVO;
import com.yanggu.code.generator.enums.TemplateGroupTypeEnum;
import com.yanggu.code.generator.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 项目产物生成器
 */
@Component
public class ProjectGenerator implements ProductGenerator {

    @Autowired
    private PreviewBuilder previewBuilder;

    @Autowired
    private ProjectService projectService;

    @Override
    public TemplateGroupTypeEnum type() {
        return TemplateGroupTypeEnum.PROJECT;
    }

    @Override
    public List<TemplateContentVO> generate(GeneratorQuery query) {
        return previewBuilder.buildProjectPreview(query);
    }

    @Override
    public String artifactName(GeneratorQuery query) {
        return projectService.getById(query.getProductIds().getFirst()).getProjectName();
    }
}
```

```java
package com.yanggu.code.generator.service.support;

import com.yanggu.code.generator.domain.entity.TableEntity;
import com.yanggu.code.generator.domain.query.GeneratorQuery;
import com.yanggu.code.generator.domain.vo.TemplateContentVO;
import com.yanggu.code.generator.enums.TemplateGroupTypeEnum;
import com.yanggu.code.generator.service.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 表产物生成器（真批量：逐表渲染汇总）
 */
@Component
public class TableGenerator implements ProductGenerator {

    @Autowired
    private PreviewBuilder previewBuilder;

    @Autowired
    private TableService tableService;

    @Override
    public TemplateGroupTypeEnum type() {
        return TemplateGroupTypeEnum.TABLE;
    }

    @Override
    public List<TemplateContentVO> generate(GeneratorQuery query) {
        return query.getProductIds().stream()
                .map(tableId -> previewBuilder.tablePreview(tableId, query.getTemplateIds()))
                .flatMap(Collection::stream)
                .toList();
    }

    @Override
    public String artifactName(GeneratorQuery query) {
        return tableService.listByIds(query.getProductIds()).stream()
                .map(TableEntity::getTableName)
                .collect(Collectors.joining("_"));
    }
}
```

```java
package com.yanggu.code.generator.service.support;

import com.yanggu.code.generator.domain.entity.EnumEntity;
import com.yanggu.code.generator.domain.query.GeneratorQuery;
import com.yanggu.code.generator.domain.vo.TemplateContentVO;
import com.yanggu.code.generator.enums.TemplateGroupTypeEnum;
import com.yanggu.code.generator.service.EnumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 枚举产物生成器（真批量：逐枚举渲染汇总）
 */
@Component
public class EnumGenerator implements ProductGenerator {

    @Autowired
    private PreviewBuilder previewBuilder;

    @Autowired
    private EnumService enumService;

    @Override
    public TemplateGroupTypeEnum type() {
        return TemplateGroupTypeEnum.ENUM;
    }

    @Override
    public List<TemplateContentVO> generate(GeneratorQuery query) {
        return query.getProductIds().stream()
                .map(enumId -> previewBuilder.enumPreview(enumId, query.getTemplateIds()))
                .flatMap(Collection::stream)
                .toList();
    }

    @Override
    public String artifactName(GeneratorQuery query) {
        return enumService.listByIds(query.getProductIds()).stream()
                .map(EnumEntity::getEnumName)
                .collect(Collectors.joining("_"));
    }
}
```

- [ ] **Step 4: 运行确认通过**

Run: `cd yanggu-code-generator-backend; mvn -q test -Dtest=ProductGeneratorTest`
Expected: 5 个测试 PASS

- [ ] **Step 5: 提交（先征得用户同意）**

```bash
git add yanggu-code-generator-backend/src/main/java/com/yanggu/code/generator/service/support/ProductGenerator.java yanggu-code-generator-backend/src/main/java/com/yanggu/code/generator/service/support/ProjectGenerator.java yanggu-code-generator-backend/src/main/java/com/yanggu/code/generator/service/support/TableGenerator.java yanggu-code-generator-backend/src/main/java/com/yanggu/code/generator/service/support/EnumGenerator.java yanggu-code-generator-backend/src/test/java/com/yanggu/code/generator/service/support/ProductGeneratorTest.java
git commit -m "feat: ProductGenerator 策略层，表/枚举真批量生成与 zip 命名收口"
```

---

### Task 5: Service/Controller 统一切换（全 POST）+ 旧类删除

**Files:**
- Modify: `yanggu-code-generator-backend/src/main/java/com/yanggu/code/generator/service/GeneratorService.java`（整体重写）
- Modify: `yanggu-code-generator-backend/src/main/java/com/yanggu/code/generator/service/impl/GeneratorServiceImpl.java`（整体重写）
- Modify: `yanggu-code-generator-backend/src/main/java/com/yanggu/code/generator/controller/GeneratorController.java`（整体重写）
- Delete: `domain/query/CodePreviewQuery.java`、`domain/query/CodeSingleGeneratorQuery.java`、`domain/query/GeneratorProjectQuery.java`、`domain/query/GeneratorTableQuery.java`、`domain/query/GeneratorEnumQuery.java`、`enums/GeneratorProductTypeEnum.java`（均在 `yanggu-code-generator-backend/src/main/java/com/yanggu/code/generator/` 下，用 DeleteFile 工具删除）

**Interfaces:**
- Consumes: Task 2 `GeneratorQuery.validate(boolean)`；Task 4 `ProductGenerator` 三实现
- Produces: 6 个全 POST 端点（前端 Task 6/7 依赖）：`POST /gen/generator/preview`、`POST /downloadZip`、`POST /downloadLocal`、`POST /downloadSingle`、`POST /singleLocal`、`POST /template/test`（不变），入参均 `@RequestBody`

- [ ] **Step 1: 重写 GeneratorService 接口**

```java
package com.yanggu.code.generator.service;

import com.yanggu.code.generator.domain.query.GeneratorQuery;
import com.yanggu.code.generator.domain.query.TemplateTestQuery;
import com.yanggu.code.generator.domain.vo.PreviewTemplateVO;
import com.yanggu.code.generator.domain.vo.TemplateContentVO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface GeneratorService {

    List<PreviewTemplateVO> preview(GeneratorQuery query);

    ResponseEntity<byte[]> downloadZip(GeneratorQuery query);

    void downloadLocal(GeneratorQuery query);

    ResponseEntity<byte[]> downloadSingle(GeneratorQuery query);

    void singleLocal(GeneratorQuery query);

    TemplateContentVO templateTest(TemplateTestQuery templateTestQuery);
}
```

- [ ] **Step 2: 重写 GeneratorServiceImpl**

```java
package com.yanggu.code.generator.service.impl;

import cn.hutool.v7.core.util.EnumUtil;
import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.domain.entity.ProjectEntity;
import com.yanggu.code.generator.domain.entity.TemplateGroupEntity;
import com.yanggu.code.generator.domain.query.GeneratorQuery;
import com.yanggu.code.generator.domain.query.TemplateTestQuery;
import com.yanggu.code.generator.domain.vo.PreviewTemplateVO;
import com.yanggu.code.generator.domain.vo.TemplateContentVO;
import com.yanggu.code.generator.domain.vo.TemplateVO;
import com.yanggu.code.generator.enums.BusinessResultError;
import com.yanggu.code.generator.enums.TemplateGroupTypeEnum;
import com.yanggu.code.generator.service.EnumService;
import com.yanggu.code.generator.service.GeneratorService;
import com.yanggu.code.generator.service.ProjectService;
import com.yanggu.code.generator.service.TableService;
import com.yanggu.code.generator.service.TemplateGroupService;
import com.yanggu.code.generator.service.TemplateService;
import com.yanggu.code.generator.service.support.DataModelBuilder;
import com.yanggu.code.generator.service.support.DownloadHandler;
import com.yanggu.code.generator.service.support.PreviewBuilder;
import com.yanggu.code.generator.service.support.PreviewTreeBuilder;
import com.yanggu.code.generator.service.support.ProductGenerator;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 代码生成服务实现类
 * 仅负责入参校验、产物处理器路由与模板测试；生成编排/数据模型/下载输出委托 support 协作组件。
 */
@Service
public class GeneratorServiceImpl implements GeneratorService {

    @Autowired
    private List<ProductGenerator> productGenerators;

    @Autowired
    private TableService tableService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private EnumService enumService;

    @Autowired
    private TemplateGroupService templateGroupService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PreviewBuilder previewBuilder;

    @Autowired
    private DataModelBuilder dataModelBuilder;

    @Autowired
    private DownloadHandler downloadHandler;

    private Map<TemplateGroupTypeEnum, ProductGenerator> generatorMap;

    @PostConstruct
    void initGeneratorMap() {
        generatorMap = new EnumMap<>(TemplateGroupTypeEnum.class);
        productGenerators.forEach(generator -> generatorMap.put(generator.type(), generator));
    }

    @Override
    public List<PreviewTemplateVO> preview(GeneratorQuery query) {
        query.validate(false);
        return PreviewTreeBuilder.buildTree(generator(query).generate(query));
    }

    @Override
    public ResponseEntity<byte[]> downloadZip(GeneratorQuery query) {
        query.validate(false);
        ProductGenerator generator = generator(query);
        return downloadHandler.downloadZip(generator.artifactName(query), generator.generate(query));
    }

    @Override
    public void downloadLocal(GeneratorQuery query) {
        query.validate(false);
        downloadHandler.downloadLocal(generator(query).generate(query));
    }

    @Override
    public ResponseEntity<byte[]> downloadSingle(GeneratorQuery query) {
        query.validate(true);
        TemplateContentVO content = generator(query).generate(query).getFirst();
        return downloadHandler.buildResponseEntity(content.getFileName(), content.getContent().getBytes());
    }

    @Override
    public void singleLocal(GeneratorQuery query) {
        query.validate(true);
        downloadHandler.downloadLocal(generator(query).generate(query));
    }

    @Override
    public TemplateContentVO templateTest(TemplateTestQuery templateTestQuery) {
        TemplateGroupEntity templateGroup = templateGroupService.getById(templateTestQuery.getTemplateGroupId(), false);
        TemplateVO templateVO = templateService.detail(templateTestQuery.getTemplateId(), true);
        templateVO.setTemplateContent(templateTestQuery.getTemplateContent());
        ProjectEntity project = projectService.getById(templateTestQuery.getProjectId());
        return switch (EnumUtil.getBy(TemplateGroupTypeEnum::getCode, templateTestQuery.getTemplateGroupType())) {
            case PROJECT -> renderForTest(templateGroup, templateVO,
                    dataModelBuilder.buildProjectDataModel(project, dataModelBuilder.resolveDatasource(project)));
            case TABLE -> renderForTest(templateGroup, templateVO,
                    dataModelBuilder.buildTableDataModel(tableService.getById(templateTestQuery.getTestId()), project));
            case ENUM -> renderForTest(templateGroup, templateVO,
                    dataModelBuilder.buildEnumDataModel(enumService.getById(templateTestQuery.getTestId()), project));
            default -> throw new IllegalArgumentException("Invalid template group type: " + templateTestQuery.getTemplateGroupType());
        };
    }

    private ProductGenerator generator(GeneratorQuery query) {
        return generatorMap.get(EnumUtil.getBy(TemplateGroupTypeEnum::getCode, query.getProductType()));
    }

    /**
     * 模板测试渲染：条件表达式不通过则抛业务异常，通过则渲染
     */
    private TemplateContentVO renderForTest(TemplateGroupEntity templateGroup, TemplateVO templateVO, Object dataModel) {
        if (!previewBuilder.checkCondition(templateVO.getConditionExpression(), dataModel)) {
            throw new BusinessException(BusinessResultError.EXECUTION_EXPRESS_ERROR);
        }
        return previewBuilder.render(templateGroup, templateVO, dataModel);
    }
}
```

注意：`templateService.detail` 若声明受检异常，在此包装为 BusinessException（以编译结果为准，不得把 throws 加回接口）。

- [ ] **Step 3: 重写 GeneratorController（6 端点全 POST）**

```java
package com.yanggu.code.generator.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.yanggu.code.generator.domain.query.GeneratorQuery;
import com.yanggu.code.generator.domain.query.TemplateTestQuery;
import com.yanggu.code.generator.domain.vo.PreviewTemplateVO;
import com.yanggu.code.generator.domain.vo.TemplateContentVO;
import com.yanggu.code.generator.service.GeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 代码生成管理
 */
@Validated
@RestController
@Tag(name = "代码生成管理")
@RequestMapping("/gen/generator")
public class GeneratorController {

    @Autowired
    private GeneratorService generatorService;

    @PostMapping("/preview")
    @ApiOperationSupport(order = 1)
    @Operation(summary = "预览数据")
    public List<PreviewTemplateVO> preview(@RequestBody GeneratorQuery query) {
        return generatorService.preview(query);
    }

    @PostMapping("/downloadZip")
    @ApiOperationSupport(order = 2)
    @Operation(summary = "生成代码（zip压缩包）")
    public ResponseEntity<byte[]> downloadZip(@RequestBody GeneratorQuery query) {
        return generatorService.downloadZip(query);
    }

    @PostMapping("/downloadLocal")
    @ApiOperationSupport(order = 3)
    @Operation(summary = "生成代码（服务器本地）")
    public void downloadLocal(@RequestBody GeneratorQuery query) {
        generatorService.downloadLocal(query);
    }

    @PostMapping("/downloadSingle")
    @ApiOperationSupport(order = 4)
    @Operation(summary = "生成代码（下载单个文件）")
    public ResponseEntity<byte[]> downloadSingle(@RequestBody GeneratorQuery query) {
        return generatorService.downloadSingle(query);
    }

    @PostMapping("/singleLocal")
    @ApiOperationSupport(order = 5)
    @Operation(summary = "生成代码（单文件到服务器本地）")
    public void singleLocal(@RequestBody GeneratorQuery query) {
        generatorService.singleLocal(query);
    }

    @PostMapping("/template/test")
    @ApiOperationSupport(order = 6)
    @Operation(summary = "模板测试")
    public TemplateContentVO templateTest(@RequestBody TemplateTestQuery templateTestQuery) {
        return generatorService.templateTest(templateTestQuery);
    }
}
```

- [ ] **Step 4: 删除 6 个旧类**

用 DeleteFile 逐个删除 Files 段所列文件；每个删除前 grep 类名确认无残留引用（Task 3 已清掉 support/impl 引用）。

- [ ] **Step 5: 全量编译 + 全部单测**

Run: `cd yanggu-code-generator-backend; mvn -q test`
Expected: BUILD SUCCESS，GeneratorQueryTest 7 例 + ProductGeneratorTest 5 例全 PASS

- [ ] **Step 6: 提交（先征得用户同意）**

```bash
git add -A yanggu-code-generator-backend/src/main/java/com/yanggu/code/generator/
git commit -m "feat: 生成接口统一为 6 个全 POST 端点，删除按类型散落的旧端点与 Query DTO"
```

---

### Task 6: 前端类型 + API 层重写 + downloadFile 扩展 POST

**Files:**
- Create: `yanggu-code-generator-frontend/src/types/api/gen/generator.ts`
- Modify: `yanggu-code-generator-frontend/src/types/api/gen/index.ts`（追加 re-export）
- Modify: `yanggu-code-generator-frontend/src/api/gen/generator.ts`（整体重写）
- Modify: `yanggu-code-generator-frontend/src/utils/request.ts:207-245`（downloadFile 扩展 method 参数）

**Interfaces:**
- Consumes: Task 5 的 6 个 POST 端点
- Produces: `GeneratorQuery`/`ProjectFilter`/`ProjectGeneratorQuery`/`TemplateTestQuery` TS 类型；`genGeneratorApi.{preview, downloadZip, downloadLocal, downloadSingle, singleLocal, templateTest}`；`downloadFile(url, params?, noErrorMessage?, method?)`（Task 7 依赖）

- [ ] **Step 1: 新建类型文件**

```typescript
export interface ProjectFilter {
	tableIds?: number[]
	enumIds?: number[]
	tableTemplateIds?: number[]
	enumTemplateIds?: number[]
}

export interface GeneratorQuery {
	// 产物类型：0项目 1表 2枚举
	productType: number
	// 产物ID列表：项目=[projectId]，表=tableIds，枚举=enumIds
	productIds: number[]
	// 模板ID列表，空=全量；PROJECT 时指项目级模板
	templateIds?: number[]
	// 项目子产物过滤，仅 productType=0 时允许携带
	projectFilter?: ProjectFilter
}

// 项目向导专用：projectFilter 必填约束
export interface ProjectGeneratorQuery extends GeneratorQuery {
	productType: 0
	projectFilter: ProjectFilter
}

export interface TemplateTestQuery {
	projectId: number | string
	templateGroupId: number
	templateGroupType: number
	templateId: number
	templateContent: string
	testId: number
}
```

`src/types/api/gen/index.ts` 追加一行：`export * from './generator'`

- [ ] **Step 2: downloadFile 扩展 method 参数**

`src/utils/request.ts` 的 downloadFile 签名与请求发起处改为（blob→a[download] 后续逻辑一字不动）：

```typescript
export const downloadFile = (url: string, params?: any, noErrorMessage = false, method: 'get' | 'post' = 'get'): Promise<void> => {
	return new Promise((resolve, reject) => {
		const request =
			method === 'post'
				? service.post(url, params, { responseType: 'blob', noErrorMessage })
				: service.get(url, { responseType: 'blob', params, noErrorMessage })
		request
			.then(response => {
				// ……原有 contentDisposition 解析与 a[download] 逻辑保持不动……
```

默认 'get' 保住 common export（后端 /export 为 @GetMapping）。paramsSerializer 不动。

- [ ] **Step 3: 重写 api/gen/generator.ts**

```typescript
import { downloadFile, service } from '@/utils/request'
import type { AxiosRequestConfig } from 'axios'
import type { GeneratorQuery, TemplateTestQuery } from '@/types'

const baseUrl: string = '/gen/generator'

// 生成代码API（6 端点全 POST，统一 GeneratorQuery 入参）
export const genGeneratorApi = {
	// 预览代码
	preview: (query: GeneratorQuery): Promise<any> => {
		return service.post(baseUrl + '/preview', query)
	},
	// 生成代码（zip压缩包）
	downloadZip: (query: GeneratorQuery, noErrorMessage = false) => {
		return downloadFile(baseUrl + '/downloadZip', query, noErrorMessage, 'post')
	},
	// 生成代码（服务器本地）
	downloadLocal: (query: GeneratorQuery): Promise<any> => {
		return service.post(baseUrl + '/downloadLocal', query)
	},
	// 下载单文件
	downloadSingle: (query: GeneratorQuery) => {
		return downloadFile(baseUrl + '/downloadSingle', query, false, 'post')
	},
	// 生成单文件到服务器本地
	singleLocal: (query: GeneratorQuery): Promise<any> => {
		return service.post(baseUrl + '/singleLocal', query)
	},
	// 模板测试
	templateTest: (query: TemplateTestQuery, config?: AxiosRequestConfig): Promise<any> => {
		return service.post(baseUrl + '/template/test', query, config)
	}
}
```

- [ ] **Step 4: 类型检查（此时调用点未改，预期报错集中在 7 个调用点文件）**

Run: `cd yanggu-code-generator-frontend; npm run type-check`
Expected: 报错仅位于 Task 7 所列调用点文件（旧 API 方法名/旧传参形状）；types/api/utils 本身无错

- [ ] **Step 5: 提交（先征得用户同意）**

```bash
git add yanggu-code-generator-frontend/src/types/api/gen/generator.ts yanggu-code-generator-frontend/src/types/api/gen/index.ts yanggu-code-generator-frontend/src/api/gen/generator.ts yanggu-code-generator-frontend/src/utils/request.ts
git commit -m "feat: 前端生成 API 类型化重写，downloadFile 支持 POST blob 下载"
```

---

### Task 7: 前端 7 处调用点改造 + 枚举批量缺陷修复

**Files:**
- Modify: `yanggu-code-generator-frontend/src/business/preview/index.vue`（L268-271、L444-448）
- Modify: `yanggu-code-generator-frontend/src/views/gen/template/test.vue`（L442-446）
- Modify: `yanggu-code-generator-frontend/src/views/gen/table/template-index.vue`（L98-112）
- Modify: `yanggu-code-generator-frontend/src/views/gen/enum/template-index.vue`（init 签名 + L89-95）
- Modify: `yanggu-code-generator-frontend/src/views/gen/enum/index.vue`（L235、L245）
- Modify: `yanggu-code-generator-frontend/src/views/gen/project/steps-v2.vue`（L175-188）
- Modify: `yanggu-code-generator-frontend/src/views/gen/project/generate-result-v2.vue`（L118-134）

**Interfaces:**
- Consumes: Task 6 的 `genGeneratorApi` 6 方法与 `GeneratorQuery`/`ProjectGeneratorQuery` 类型
- Produces: 全链路可运行的前后端统一契约

- [ ] **Step 1: preview/index.vue 两处**

init 内 previewData 改：

```typescript
const previewData: GeneratorQuery = {
	productType: generatorProductType,
	productIds: [id]
}
```

downloadTemplateData 内 dataForm 改（id 三分支取值逻辑保留）：

```typescript
const dataForm: GeneratorQuery = {
	productType: item.templateGroupType,
	productIds: [id],
	templateIds: [item.templateId]
}
```

submitApi 分支名不变（downloadSingle/singleLocal 方法名未变）。文件头补 `import type { GeneratorQuery } from '@/types'`。

- [ ] **Step 2: template/test.vue generatorHandler**

```typescript
const dataForm: GeneratorQuery = {
	productType: testData.templateGroupType,
	productIds: [id],
	templateIds: [testData.templateId]
}
```

templateTest 调用不动（queryForm 形状未变，可顺手标注 `TemplateTestQuery` 类型）。

- [ ] **Step 3: table/template-index.vue generateCode**

```typescript
const dataForm: GeneratorQuery = {
	productType: 1,
	productIds: tableIdRef.value,
	templateIds: templateIdList
}
const generatorType = generatorTypeRef.value
if (generatorType === 0) {
	submitState.submitApi = genGeneratorApi.downloadZip
	submitState.successMessage = '代码已经下载到浏览器'
} else if (generatorType === 1) {
	submitState.submitApi = genGeneratorApi.downloadLocal
	submitState.successMessage = '代码已经生成到本地'
} else {
	ElMessage.warning('生成类型异常')
	return
}
```

- [ ] **Step 4: enum/template-index.vue + enum/index.vue（含缺陷修复）**

template-index.vue：initData.enumIdList 与 init 签名改 `number[]`；generateCode 的 dataForm 改：

```typescript
const dataForm: GeneratorQuery = {
	productType: 2,
	productIds: initData.enumIdList,
	templateIds: selectedRows.map((row: GenTemplateEntity) => row.id as number)
}
const generatorType = initData.generatorType
const submitApi = generatorType === 0 ? genGeneratorApi.downloadZip : genGeneratorApi.downloadLocal
```

enum/index.vue **缺陷修复**：L235 `templateIndexRef.value.init(enumTemplateGroupId, generatorType, data)` 改传 ID 数组（原误传 selection.rows 行对象）：

```typescript
templateIndexRef.value.init(enumTemplateGroupId, generatorType, tableHook.selection.ids.map(Number))
```

L245 单行入口同步：`templateIndexRef.value.init(row.enumTemplateGroupId, row.generatorType, [Number(row.id)])`。修复后删除 L225 不再使用的 `const data = tableHook.selection.rows`（若 generatorBatchHandler 内无其他消费）。

- [ ] **Step 5: project/steps-v2.vue generateCode**

```typescript
const dataForm: ProjectGeneratorQuery = {
	productType: 0,
	productIds: [projectReactive.id],
	templateIds: templateListRef.value.filter(item => item.templateGroupType === 0).map(item => item.id),
	projectFilter: {
		tableIds: tableListRef.value.map(item => item.id),
		enumIds: enumListRef.value.map(item => item.id),
		tableTemplateIds: templateListRef.value.filter(item => item.templateGroupType === 1).map(item => item.id),
		enumTemplateIds: templateListRef.value.filter(item => item.templateGroupType === 2).map(item => item.id)
	}
}
```

- [ ] **Step 6: project/generate-result-v2.vue**

init 签名 `dataForm: any` 改 `GeneratorQuery`；两个分支改：

```typescript
if (generatorType === 0) {
	submitHandler = genGeneratorApi.downloadZip
} else if (generatorType === 1) {
	submitHandler = genGeneratorApi.downloadLocal
}
```

注意 downloadZip 第二参 noErrorMessage 缺省 false，与原 projectDownloadZip 调用等价；状态机/倒计时逻辑不动。

- [ ] **Step 7: 类型检查 + lint 全绿**

Run: `cd yanggu-code-generator-frontend; npm run type-check; npm run lint`
Expected: 均无错误（lint 带 --fix，提交前 git diff 复核自动修复内容）

- [ ] **Step 8: 提交（先征得用户同意）**

```bash
git add yanggu-code-generator-frontend/src/business/preview/index.vue yanggu-code-generator-frontend/src/views/gen/
git commit -m "feat: 前端调用点切换统一生成接口，修复枚举批量误传行对象缺陷"
```

---

### Task 8: Debug 启动 + 冒烟矩阵验证

**Files:**
- 只读验证，不改代码；发现问题回到对应 Task 修复后重跑

**Interfaces:**
- Consumes: Task 5/7 交付的前后端
- Produces: 冒烟通过证据（日志 + 抓包 + 产物核对）

- [ ] **Step 1: 向用户确认 IDEA 面板无运行实例**（必须等到明确答复）

- [ ] **Step 2: Debug 模式启动后端**

Call: `xdebug_start_debugger_session(configurationName=CodeGeneratorApplication)`（idea MCP）
验证: 读 `logs/code-generator-info.log` 出现 `Started CodeGeneratorApplication`；禁止用 Run 模式/netstat/taskkill

- [ ] **Step 3: 启动前端 dev 并打开浏览器**

Run: `cd yanggu-code-generator-frontend; npm run dev`（后台）
浏览器 MCP 打开对应端口（端口冲突时按既定决策改端口，不占系统端口）

- [ ] **Step 4: 逐项执行冒烟矩阵并记录证据**

1. 项目向导 zip 下载：抓包确认 POST `/gen/generator/downloadZip` 请求体含嵌套 `projectFilter`，浏览器收到 zip，解压核对勾选的表/枚举/项目级产物齐全；
2. 项目向导本地生成：POST downloadLocal，核对落盘文件；
3. 表多选 2 表批量 zip + 本地：**两表产物齐全**（假批量修复回归点）；
4. 枚举多选批量 zip + 本地：抓包确认 productIds 为 ID 数组（rows→ids 修复回归点），产物齐全；
5. 预览树：project/table/enum 三入口正常渲染；
6. 预览抽屉单文件下载/本地：项目级、表、枚举三种模板各一（POST downloadSingle/singleLocal）；
7. 模板测试页：正常渲染 + 表达式 false 走 700 提示分支 + 单品生成；
8. 非法入参（浏览器 MCP evaluate fetch 直发）：productType=99 → "产物类型异常"；TABLE 携带 projectFilter → "产物类型与过滤字段不匹配"；
9. common export 回归：模板组导出仍走 GET 正常下载（downloadFile 默认 method 未破坏）。

- [ ] **Step 5: 停止 Debug 会话**

Call: `xdebug_control_session(STOP)`；前端 dev 进程保留或按用户指示停止

---

### Task 9: 收尾

**Files:**
- 无代码改动

- [ ] **Step 1: 全量复核**

Run: `cd yanggu-code-generator-backend; mvn -q test` 与 `cd yanggu-code-generator-frontend; npm run type-check`
Expected: 双绿；`git status` 无遗漏未提交文件（.iml 除外）

- [ ] **Step 2: 更新记忆**

- 更新"GeneratorServiceImpl拆分方案"记忆：状态改为已执行 + 接口统一化完成；
- 新增项目架构记忆：6 全 POST 端点契约、GeneratorQuery/ProjectFilter、ProductGenerator 策略路由；
- 新增缺陷修复记忆：表/枚举 getFirst 假批量、枚举批量 rows→ids（四要素格式）。

- [ ] **Step 3: 向用户汇报**

汇总：端点 10→6、switch 清零、两个缺陷修复、单测 12 例、冒烟证据清单；提示后续立项项（路径越界防护 P0、模板缓存、N+1）。