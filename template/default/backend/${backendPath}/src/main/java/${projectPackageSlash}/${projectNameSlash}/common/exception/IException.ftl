package ${projectPackage}.${projectNameDot}.common.exception;

/**
 * 异常信息接口，设置CODE和异常信息
 */
public interface IException {

    /**
     * 获取异常码
     */
    Integer getCode();

    /**
     * 获取异常信息
     */
    String getMessage();

}
