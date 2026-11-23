package com.yanggu.code.generator.service.support;

import com.yanggu.code.generator.domain.model.EnumModel;
import com.yanggu.code.generator.domain.model.ProjectModel;
import com.yanggu.code.generator.domain.model.TableModel;
import com.yanggu.code.generator.enums.TemplateGroupTypeEnum;

/**
 * 模板组类型到根数据模型及其自身属性 Map 字段名的映射，全项目唯一一份 switch。
 * 新增模板组类型时只需在此补 case，靠 switch 表达式的穷尽性检查在编译期暴露遗漏，
 * 避免变量目录（VariableCatalogServiceImpl）与测试数据模型（GeneratorServiceImpl）两处各漏一次。
 *
 * @param rootModelClass  根数据模型类型，供 VariableCatalogBuilder 反射展开结构目录/带值目录
 * @param propertyMapName 该类型自身属性 Map 在模型里的字段名（决策 4：结构目录只展开这一个 Map）
 */
public record TemplateModelMapping(Class<?> rootModelClass, String propertyMapName) {

    public static TemplateModelMapping of(TemplateGroupTypeEnum groupType) {
        return switch (groupType) {
            case PROJECT -> new TemplateModelMapping(ProjectModel.class, "projectTemplatePropertyData");
            case TABLE -> new TemplateModelMapping(TableModel.class, "tableTemplatePropertyData");
            case ENUM -> new TemplateModelMapping(EnumModel.class, "enumTemplatePropertyData");
        };
    }

}
