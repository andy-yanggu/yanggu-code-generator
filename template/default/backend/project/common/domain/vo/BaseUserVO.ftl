package ${projectPackage}.${projectNameDot}.common.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Controller返回值数据基类（包含用户相关信息）
 * <p>自定义VO类继承该类即可</p>
 */
@Data
@Schema(description = "数据VO基类（包含用户相关信息）")
public abstract class BaseUserVO extends BaseVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -5814569213714989166L;

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