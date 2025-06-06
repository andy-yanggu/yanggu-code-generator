package com.yanggu.code.generator.common.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.dromara.hutool.core.collection.CollUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 对RestController返回值统一处理
 */
@RestControllerAdvice(annotations = RestController.class)
@ConditionalOnProperty(name = "web.response.enable", havingValue = "true", matchIfMissing = true)
public class ResultAdvice implements ResponseBodyAdvice<Object> {

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 不需要增强的类名，有默认值，主要是swagger等的类
     */
    @Value("#{'${web.response.exclude-list:swagger,springfox,springdoc}'.split(',')}")
    private List<String> excludeNameList;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        //类上有IgnoreResult注解的，不增强
        IgnoreResult annotation = AnnotationUtils.findAnnotation(returnType.getDeclaringClass(), IgnoreResult.class);
        if (annotation != null) {
            return false;
        }

        //方法上有IgnoreResult注解的，不增强
        annotation = AnnotationUtils.findAnnotation(returnType.getAnnotatedElement(), IgnoreResult.class);
        if (annotation != null) {
            return false;
        }
        Method method = returnType.getMethod();
        //如果类名包含swagger等，不处理
        if (method != null && CollUtil.isNotEmpty(excludeNameList)) {
            String name = method.getDeclaringClass().getName();
            for (String excludeName : excludeNameList) {
                if (name.contains(excludeName)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    @SneakyThrows
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        //如果返回类型已经是Result则直接返回，否则需要用Result进行返回值包装
        if (body instanceof Result) {
            return body;
        } else if (body instanceof byte[]) {
            return body;
        } else {
            Result<Object> result = Result.success(body);
            //如果返回值是字符串, 需要特殊处理, 手动变成JSON字符串
            if (returnType.getParameterType().equals(String.class)) {
                return objectMapper.writeValueAsString(result);
            }
            return result;
        }
    }

}
