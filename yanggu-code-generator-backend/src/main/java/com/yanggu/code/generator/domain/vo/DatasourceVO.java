package com.yanggu.code.generator.domain.vo;

import com.yanggu.code.generator.common.domain.vo.BaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 数据源VO实体类
 */
@Data
@Schema(description = "数据源VO实体类")
@EqualsAndHashCode(callSuper = true)
public class DatasourceVO extends BaseVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 数据库类型
     */
    @Schema(description = "数据库类型")
    private String dbType;

    /**
     * 连接名称
     */
    @Schema(description = "连接名称")
    private String connName;

    /**
     * URL
     */
    @Schema(description = "URL")
    private String connUrl;

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String username;

    /**
     * 密码
     */
    @Schema(description = "密码")
    private String password;

    /**
     * 描述
     */
    @Schema(description = "描述")
    private String datasourceDesc;

}