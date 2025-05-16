package com.yanggu.code.generator.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 预览数据VO实体类
 */
@Data
@Schema(description = "预览数据VO实体类")
public class PreviewDataVO {

    /**
     * 模板内容列表
     */
    @Schema(description = "模板内容列表")
    private List<TemplateContentVO> templateContentList;

    /**
     * 树形数据列表
     */
    @Schema(description = "树形数据列表")
    private List<TreeVO> treeList;

}
