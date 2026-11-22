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
