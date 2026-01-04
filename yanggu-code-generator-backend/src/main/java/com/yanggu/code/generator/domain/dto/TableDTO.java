package com.yanggu.code.generator.domain.dto;

import com.yanggu.code.generator.common.validation.code.EnumCode;
import com.yanggu.code.generator.common.validation.group.UpdateGroup;
import com.yanggu.code.generator.enums.FormLayoutEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 表DTO实体类
 */
@Data
@Schema(description = "表DTO实体类")
public class TableDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    @NotNull(message = "主键ID不能为空", groups = {UpdateGroup.class})
    private Long id;

    /**
     * 表名
     */
    @Schema(description = "表名")
    private String tableName;

    /**
     * 数据库名
     */
    @Schema(description = "数据库名")
    private String databaseName;

    /**
     * 类名
     */
    @Schema(description = "类名")
    @NotBlank(message = "类名不能为空")
    private String className;

    /**
     * 注释
     */
    @Schema(description = "注释")
    @NotBlank(message = "注释不能为空")
    private String tableComment;

    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    private Long projectId;

    /**
     * 作者
     */
    @Schema(description = "作者")
    private String author;

    /**
     * 版本
     */
    @Schema(description = "版本")
    private String version;

    /**
     * 功能名
     */
    @Schema(description = "功能名")
    @NotBlank(message = "功能名不能为空")
    private String functionName;

    /**
     * 模块名
     */
    @Schema(description = "模块名")
    private String moduleName;

    /**
     * 权限标识
     */
    @Schema(description = "权限标识")
    private String permissionFlag;

    /**
     * 生成功能
     */
    @Schema(description = "生成功能")
    private List<Integer> generatorFunction;

    /**
     * 弹窗方式（0-对话框，1-抽屉）
     */
    @Schema(description = "弹窗方式（0-对话框，1-抽屉）")
    private Integer popupType;

    /**
     * 表单布局  1：一列   2：两列
     */
    @Schema(description = "表单布局  1：一列   2：两列")
    @NotNull(message = "表单布局不能为空")
    @EnumCode(FormLayoutEnum.class)
    private Integer formLayout;

    /**
     * Entity基类ID
     */
    @Schema(description = "Entity基类ID")
    private Long entityBaseClassId;

    /**
     * VO基类ID
     */
    @Schema(description = "VO基类ID")
    private Long voBaseClassId;

    /**
     * 模板组数据
     */
    @Schema(description = "模板组数据")
    private Map<String, Object> templateGroupPropertyData;

}