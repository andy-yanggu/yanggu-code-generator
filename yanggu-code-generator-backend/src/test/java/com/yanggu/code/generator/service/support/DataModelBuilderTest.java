package com.yanggu.code.generator.service.support;

import com.yanggu.code.generator.domain.entity.EnumEntity;
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
        // TableModel.formLayout / popupType 是原始 int，实体侧为 Integer，不赋值会在拆箱处 NPE
        table.setFormLayout(1);
        table.setPopupType(1);
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
