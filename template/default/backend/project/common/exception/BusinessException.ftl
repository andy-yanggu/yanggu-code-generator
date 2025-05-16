package ${projectPackage}.${projectNameDot}.common.exception;

import ${projectPackage}.${projectNameDot}.common.response.IResultError;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.hutool.core.text.StrUtil;

import java.io.Serial;

/**
 * 自定义业务异常
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends RuntimeException implements IException {

    @Serial
    private static final long serialVersionUID = -2779970368178204678L;

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误信息
     */
    private final String message;

    public BusinessException(IResultError resultError, Object... objects) {
        super(StrUtil.format(resultError.getMessage(), objects));
        this.code = resultError.getCode();
        this.message = StrUtil.format(resultError.getMessage(), objects);
    }

    public BusinessException(IResultError resultError, Throwable cause, Object... objects) {
        super(StrUtil.format(resultError.getMessage(), objects), cause);
        this.code = resultError.getCode();
        this.message = StrUtil.format(resultError.getMessage(), objects);
    }

    public BusinessException(String msg, Object... objects) {
        super(StrUtil.format(msg, objects));
        this.code = 500;
        this.message = StrUtil.format(msg, objects);
    }

    public BusinessException(Integer code, String msg, Object... objects) {
        super(StrUtil.format(msg, objects));
        this.code = code;
        this.message = StrUtil.format(msg, objects);
    }

    public BusinessException(Integer code, String msg, Throwable cause, Object... objects) {
        super(StrUtil.format(msg, objects), cause);
        this.code = code;
        this.message = StrUtil.format(msg, objects);
    }

}
