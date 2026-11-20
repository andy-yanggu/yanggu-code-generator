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