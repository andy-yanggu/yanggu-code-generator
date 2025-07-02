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

/**
 * 表DTO实体类
 */
@Data
@Schema(description = "表DTO实体类")
public class TableDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Schema(description = "id")
    @NotNull(message = "id不能为空", groups = {UpdateGroup.class})
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
     * 表单布局  1：一列   2：两列
     */
    @Schema(description = "表单布局  1：一列   2：两列")
    @NotNull(message = "表单布局不能为空")
    @EnumCode(FormLayoutEnum.class)
    private Integer formLayout;

}