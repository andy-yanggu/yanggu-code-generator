package com.yanggu.code.generator.domain.vo;

import com.yanggu.code.generator.common.domain.vo.BaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 表VO实体类
 */
@Data
@Schema(description = "表VO实体类")
@EqualsAndHashCode(callSuper = true)
public class TableVO extends BaseVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
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
    private String className;

    /**
     * 注释
     */
    @Schema(description = "注释")
    private String tableComment;

    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    private Long projectId;

    /**
     * 项目名称
     */
    @Schema(description = "项目名称")
    private String projectName;

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
     * 表单布局  1：一列   2：两列
     */
    @Schema(description = "表单布局  1：一列   2：两列")
    private Integer formLayout;

    /**
     * 弹窗方式（0-对话框，1-抽屉）
     */
    @Schema(description = "弹窗方式（0-对话框，1-抽屉）")
    private Integer popupType;

    /**
     * 表模板组ID
     */
    @Schema(description = "表模板组ID")
    private Long tableTemplateGroupId;

    /**
     * 生成方式（0-zip压缩包，1-服务器本地）
     */
    @Schema(description = "生成方式（0-zip压缩包，1-服务器本地）")
    private Integer generatorType;

    /**
     * 生成功能（新增、修改、删除、详情、分页、列表、导入、导出和复制）
     */
    @Schema(description = "生成功能")
    private List<Integer> generatorFunction = List.of();

}