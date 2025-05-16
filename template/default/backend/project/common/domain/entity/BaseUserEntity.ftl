package ${projectPackage}.${projectNameDot}.common.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 数据库实体类基类，适用于用户操作的数据表、增删改查
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class BaseUserEntity extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -3534460034686573788L;

    /**
     * 创建人
     */
    @TableField(value = "create_user_id", fill = FieldFill.INSERT)
    private Long createUserId;

    /**
     * 修改人
     */
    @TableField(value = "update_user_id", fill = FieldFill.INSERT_UPDATE)
    private Long updateUserId;

}
