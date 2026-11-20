package com.yanggu.code.generator.service.support;

import com.yanggu.code.generator.domain.entity.ProjectEntity;
import com.yanggu.code.generator.domain.entity.TableEntity;
import com.yanggu.code.generator.domain.query.GeneratorQuery;
import com.yanggu.code.generator.domain.query.TemplateTestQuery;
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
        return dataModelBuilder.buildTableDataModel(tableService.getById(query.getTestId()), project);
    }
}