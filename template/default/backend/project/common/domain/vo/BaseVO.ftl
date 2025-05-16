package ${projectPackage}.${projectNameDot}.common.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Controller返回值数据基类
 * <p>自定义VO类继承该类即可</p>
 */
@Data
@Schema(description = "数据VO基类")
public abstract class BaseVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -5814569213714989166L;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createTime;

    /**
     * 修改时间
     */
    @Schema(description = "修改时间")
    private Date updateTime;

    /**
     * 是否删除（0正常, 1删除）
     */
    @Schema(description = "是否删除（0正常, 1删除）")
    private Boolean isDelete;

}