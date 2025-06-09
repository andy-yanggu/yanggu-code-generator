package com.yanggu.code.generator.common.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * Controller返回值数据基类
 * <p>自定义VO类继承该类即可，包含创建人和修改人字段</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "数据VO基类（包含创建人和修改人）")
public abstract class BaseUserVO extends BaseVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 17374763732727L;

    /**
     * 创建人ID
     */
    @Schema(description = "创建人ID")
    private Long createUserId;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private String createUser;

    /**
     * 修改人ID
     */
    @Schema(description = "修改人ID")
    private Long updateUserId;

    /**
     * 修改人
     */
    @Schema(description = "修改人")
    private String updateUser;

}
