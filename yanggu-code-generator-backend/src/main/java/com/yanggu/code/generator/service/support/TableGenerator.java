package com.yanggu.code.generator.service.support;

import cn.hutool.v7.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yanggu.code.generator.domain.entity.ProjectEntity;
import com.yanggu.code.generator.domain.entity.TableEntity;
import com.yanggu.code.generator.domain.query.GeneratorQuery;
import com.yanggu.code.generator.domain.query.TemplateTestQuery;
import com.yanggu.code.generator.domain.vo.CascaderDataVO;
import com.yanggu.code.generator.domain.vo.TemplateContentVO;
import com.yanggu.code.generator.enums.TemplateGroupTypeEnum;
import com.yanggu.code.generator.service.ProjectService;
import com.yanggu.code.generator.service.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private ProjectService projectService;

    @Autowired
    private DataModelBuilder dataModelBuilder;

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

    @Override
    public Object testDataModel(TemplateTestQuery query, ProjectEntity project) {
        TableEntity tableEntity = tableService.getById(query.getTestId());
        return dataModelBuilder.buildTableDataModel(tableEntity, project);
    }

    /**
     * 候选产物级联数据：一级为绑定了本表模板组的项目，二级为项目下的表，无表的项目不作为候选
     */
    @Override
    public List<CascaderDataVO> cascaderData(Long templateGroupId) {
        List<ProjectEntity> projectList = projectService.listByTableTemplateGroupId(templateGroupId);
        if (CollUtil.isEmpty(projectList)) {
            return List.of();
        }
        List<Long> projectIdList = projectList.stream()
                .map(ProjectEntity::getId)
                .toList();
        // 一次 in 查询取回全部候选表再按项目分组，避免逐项目查询的 N+1
        LambdaQueryWrapper<TableEntity> tableQueryWrapper = Wrappers.lambdaQuery(TableEntity.class)
                .in(TableEntity::getProjectId, projectIdList);
        List<TableEntity> tableList = tableService.list(tableQueryWrapper);
        Map<Long, List<TableEntity>> tableGroupMap = tableList.stream()
                .collect(Collectors.groupingBy(TableEntity::getProjectId));

        List<CascaderDataVO> nodeList = new ArrayList<>();
        for (ProjectEntity project : projectList) {
            List<TableEntity> children = tableGroupMap.get(project.getId());
            if (CollUtil.isEmpty(children)) {
                // 该项目下一张表也没有，候选无意义
                continue;
            }
            CascaderDataVO projectNode = projectNode(project);
            projectNode.setChildren(children.stream()
                    .map(table -> tableNode(table, project))
                    .toList());
            nodeList.add(projectNode);
        }
        return nodeList;
    }

    // ==================== private：候选产物装配 ====================

    /**
     * 项目节点（一级）：value 与 type 的同源规则在 CascaderDataVO.of
     */
    private CascaderDataVO projectNode(ProjectEntity project) {
        return CascaderDataVO.of(project.getId(), project.getProjectName(), project.getProjectDesc(),
                "project", project.getGeneratorType());
    }

    /**
     * 表节点（二级）：generatorType 恒取所属项目
     */
    private CascaderDataVO tableNode(TableEntity table, ProjectEntity project) {
        return CascaderDataVO.of(table.getId(), table.getTableName(), table.getTableComment(),
                "table", project.getGeneratorType());
    }
}