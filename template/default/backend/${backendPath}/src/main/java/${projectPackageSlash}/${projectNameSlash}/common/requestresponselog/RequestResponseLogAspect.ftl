package ${projectPackage}.${projectNameDot}.common.requestresponselog;

import com.fasterxml.jackson.databind.ObjectMapper;
import ${projectPackage}.${projectNameDot}.common.util.AopDataUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 请求和响应日志切面
 */
@Slf4j
@Aspect
@Component
@ConditionalOnProperty(name = "web.log.enable", havingValue = "true", matchIfMissing = true)
public class RequestResponseLogAspect {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 对RestController进行增强, 打印方法的入参、出参和执行时间
     */
    @Around("execution("
            + "public * *..controller..*(..))"
            + " && @within(org.springframework.web.bind.annotation.RestController)"
            + " && (@annotation(org.springframework.web.bind.annotation.RequestMapping)"
            + " || @annotation(org.springframework.web.bind.annotation.GetMapping)"
            + " || @annotation(org.springframework.web.bind.annotation.PostMapping)"
            + " || @annotation(org.springframework.web.bind.annotation.PutMapping)"
            + " || @annotation(org.springframework.web.bind.annotation.DeleteMapping))"
    )
    public Object around(final ProceedingJoinPoint joinPoint) throws Throwable {
        String url = request.getRequestURI();
        Signature signature = joinPoint.getSignature();

        //获取日志参数，默认请求日志和响应日志都日志记录
        LogParam logParam = getLogParam(joinPoint);

        //记录请求日志
        if (logParam.isLogRequestData()) {
            //获取请求参数
            String requestData = AopDataUtil.buildRequestData(joinPoint, logParam.excludeRequestParamNames(), objectMapper);
            //打印请求参数
            log.info("[请求日志记录] 请求的url: {}, 执行的方法名: {}, 请求的参数: {}", url, signature, requestData);
        }

        long current = System.currentTimeMillis();
        //执行方法, 这里不进行异常处理, 在通用异常处理统一进行处理
        Object result = joinPoint.proceed();

        //记录响应日志
        if (logParam.isLogResponseData()) {
            //打印响应参数
            long executeTime = System.currentTimeMillis() - current;
            String responseData = AopDataUtil.buildResponseData(result, logParam.excludeResponseFieldNames(), objectMapper);
            log.info("[响应日志记录] 请求的url: {}, 执行的方法名: {}, 执行时间: {}毫秒, 响应的参数: {}", url, signature, executeTime, responseData);
        }
        return result;
    }

    /**
     * 获取日志参数
     */
    private LogParam getLogParam(ProceedingJoinPoint joinPoint) {
        boolean isLogRequestData = true;
        boolean isLogResponseData = true;
        String[] excludeRequestParamNames = {};
        String[] excludeResponseFieldNames = {};
        //优先从方法上尝试获取
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        RequestResponseLog annotation = method.getAnnotation(RequestResponseLog.class);
        //获取不到从类上尝试获取
        if (annotation == null) {
            annotation = joinPoint.getTarget().getClass().getAnnotation(RequestResponseLog.class);
        }
        //如果有注解才获取
        if (annotation != null) {
            excludeRequestParamNames = annotation.excludeRequestParamNames();
            excludeResponseFieldNames = annotation.excludeResponseFieldNames();
            //如果不记录日志
            if (annotation.isIgnoreLog()) {
                isLogRequestData = false;
                isLogResponseData = false;
            } else {
                isLogRequestData = annotation.isLogRequestData();
                isLogResponseData = annotation.isLogResponseData();
            }
        }
        return new LogParam(isLogRequestData, isLogResponseData, excludeRequestParamNames, excludeResponseFieldNames);
    }

    private record LogParam(boolean isLogRequestData,
                            boolean isLogResponseData,
                            String[] excludeRequestParamNames,
                            String[] excludeResponseFieldNames) {
    }

}