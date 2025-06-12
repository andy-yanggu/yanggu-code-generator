package com.yanggu.code.generator.domain.dto;

import com.yanggu.code.generator.common.validation.code.EnumCode;
import com.yanggu.code.generator.common.validation.group.UpdateGroup;
import com.yanggu.code.generator.enums.DbType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 数据源DTO实体类
 */
@Data
@Schema(description = "数据源DTO实体类")
public class DatasourceDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Schema(description = "id")
    @NotNull(message = "id不能为空", groups = {UpdateGroup.class})
    private Long id;

    /**
     * 连接名
     */
    @Schema(description = "连接名")
    @NotBlank(message = "连接名不能为空")
    private String connName;

    /**
     * 数据库类型
     */
    @Schema(description = "数据库类型")
    @NotBlank(message = "数据库类型不能为空")
    @EnumCode(DbType.class)
    private String dbType;

    /**
     * URL
     */
    @Schema(description = "URL")
    @NotBlank(message = "URL不能为空")
    private String connUrl;

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     */
    @Schema(description = "密码")
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 描述
     */
    @Schema(description = "描述")
    private String dataSourceDesc;

}