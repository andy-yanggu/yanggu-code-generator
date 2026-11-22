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
