package ${projectPackage}.${projectNameDot}.common.util;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.dromara.hutool.core.array.ArrayUtil;
import org.dromara.hutool.core.map.MapUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AOP数据处理工具类
 */
@Slf4j
@UtilityClass
public class AopDataUtil {

    private static final List<Class<?>> REQUEST_PARAM_FILTER_CLASS_LIST = List.of(
            MultipartFile.class,
            MultipartFile[].class,
            HttpServletRequest.class,
            HttpServletResponse.class
    );

    private static final List<Class<?>> RESPONSE_FILTER_CLASS_LIST = List.of(
            ResponseEntity.class
    );

    /**
     * 预编译正则表达式提升解析性能
     */
    private static final Pattern PATH_PATTERN = Pattern.compile("^([a-zA-Z_$][\\w$]*)(?:\\[(\\d+)])?$");

    /**
     * 构建请求参数数据
     */
    public static String buildRequestData(JoinPoint joinPoint,
                                          String[] excludeRequestParamNames,
                                          ObjectMapper objectMapper) throws Exception {
        Signature signature = joinPoint.getSignature();
        Class<?>[] args = ((MethodSignature) signature).getParameterTypes();
        Method method = joinPoint.getTarget().getClass().getMethod(signature.getName(), args);

        Object[] params = joinPoint.getArgs();
        //key是参数名, value是参数值
        Map<String, Object> paramMap = new HashMap<>();
        if (ArrayUtil.isEmpty(params)) {
            return objectMapper.writeValueAsString(paramMap);
        }
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            //判断参数是否需要过滤
            boolean anyMatch = REQUEST_PARAM_FILTER_CLASS_LIST.stream().anyMatch(type -> type.isInstance(param));
            if (anyMatch) {
                continue;
            }
            paramMap.put(method.getParameters()[i].getName(), param);
        }
        if (MapUtil.isEmpty(paramMap)) {
            return objectMapper.writeValueAsString(paramMap);
        } else {
            //去除指定的参数
            //转换为JsonNode树结构
            JsonNode rootNode = objectMapper.valueToTree(paramMap);
            //遍历所有需要排除的路径
            for (String path : excludeRequestParamNames) {
                AopDataUtil.deleteByBeanPath(rootNode, path);
            }
            return objectMapper.writeValueAsString(rootNode);
        }
    }

    /**
     * 构建响应参数数据
     */
    public static String buildResponseData(Object result, String[] excludeResponseFieldNames, ObjectMapper objectMapper) throws Exception {
        //判断响应参数是否需要过滤
        boolean anyMatch = RESPONSE_FILTER_CLASS_LIST.stream().anyMatch(type -> type.isInstance(result));
        if (anyMatch) {
            return "响应参数已过滤";
        }
        String responseJsonString;
        //过滤部分字段
        if (ArrayUtil.isNotEmpty(excludeResponseFieldNames)) {
            //去除指定的参数
            //转换为JsonNode树结构
            JsonNode rootNode = objectMapper.valueToTree(result);
            //遍历所有需要排除的路径
            for (String path : excludeResponseFieldNames) {
                AopDataUtil.deleteByBeanPath(rootNode, path);
            }
            responseJsonString = objectMapper.writeValueAsString(rootNode);
        } else {
            responseJsonString = objectMapper.writeValueAsString(result);
        }
        return responseJsonString;
    }

    /**
     * 支持bean-path方式删除字段数据
     */
    public static void deleteByBeanPath(JsonNode rootNode, String beanPath) {
        try {
            List<PathSegment> pathSegments = parseBeanPath(beanPath);
            if (pathSegments.isEmpty()) {
                return;
            }

            JsonNode currentNode = rootNode;
            for (int i = 0; i < pathSegments.size() - 1; i++) {
                PathSegment segment = pathSegments.get(i);
                currentNode = traverseToChild(currentNode, segment);
                if (currentNode == null || currentNode.isNull()) {
                    return;
                }
            }

            PathSegment lastSegment = pathSegments.get(pathSegments.size() - 1);
            performSafeDelete(currentNode, lastSegment);
        } catch (Exception e) {
            log.error("Error processing path: {}", beanPath, e);
        }
    }

    /**
     * 增强版路径解析（带格式校验）
     */
    private static List<PathSegment> parseBeanPath(String beanPath) {
        List<PathSegment> segments = new ArrayList<>();
        if (beanPath == null || beanPath.isEmpty()) {
            return segments;
        }

        String[] parts = beanPath.split("\\.");
        for (String part : parts) {
            Matcher matcher = PATH_PATTERN.matcher(part);
            if (!matcher.matches()) {
                log.warn("Invalid path segment: {}", part);
                return Collections.emptyList();
            }

            String field = matcher.group(1);
            String indexStr = matcher.group(2);
            // 新增索引校验方法
            int index = parseIndex(indexStr);

            segments.add(new PathSegment(field, index));
        }
        return segments;
    }

    // 新增索引校验逻辑
    private static int parseIndex(String indexStr) {
        try {
            return indexStr != null ? Integer.parseInt(indexStr) : -1;
        } catch (NumberFormatException e) {
            log.warn("Invalid array index format: {}", indexStr);
            return -1;
        }
    }

    /**
     * 增强型节点遍历方法（支持对象和数组类型）
     */
    private static JsonNode traverseToChild(JsonNode currentNode, PathSegment segment) {
        if (currentNode.isObject()) {
            return handleObjectTraversal(currentNode, segment);
        } else if (currentNode.isArray()) {
            return handleArrayTraversal(currentNode, segment);
        }
        throw new IllegalStateException("Unsupported node type at path segment: " + segment.field());
    }

    /**
     * 处理对象类型节点遍历
     */
    private static JsonNode handleObjectTraversal(JsonNode node, PathSegment segment) {
        JsonNode child = node.get(segment.field());
        if (child == null) {
            return null;
        }

        // 检查数组索引需求
        if (segment.index() >= 0) {
            return getArrayElement(child, segment.index());
        }
        return child;
    }

    /**
     * 处理数组类型节点遍历
     */
    private static JsonNode handleArrayTraversal(JsonNode node, PathSegment segment) {
        if (segment.index() >= 0 && segment.index() < node.size()) {
            return node.get(segment.index());
        }
        return null;
    }

    /**
     * 安全获取数组元素（带边界检查）
     */
    private static JsonNode getArrayElement(JsonNode node, int index) {
        if (!node.isArray() || index < 0 || index >= node.size()) {
            return null;
        }
        return node.get(index);
    }

    /**
     * 安全删除操作
     */
    private static void performSafeDelete(JsonNode node, PathSegment segment) {
        try {
            if (node.isObject()) {
                ObjectNode objectNode = (ObjectNode) node;
                if (objectNode.has(segment.field())) {
                    JsonNode jsonNode = objectNode.get(segment.field());
                    if (jsonNode.isArray() && segment.index() >= 0) {
                        ArrayNode arrayNode = (ArrayNode) jsonNode;
                        if (segment.index() < arrayNode.size()) {
                            arrayNode.remove(segment.index());
                        }
                    } else {
                        objectNode.remove(segment.field());
                    }
                }
            } else if (node.isArray() && segment.index() >= 0) {
                ArrayNode arrayNode = (ArrayNode) node;
                if (segment.index() < arrayNode.size()) {
                    arrayNode.remove(segment.index());
                }
            }
        } catch (ClassCastException e) {
            log.warn("Type mismatch at path segment: {}", segment);
        }
    }

    /**
     * 更新 PathSegment 记录类
     *
     * @param field
     * @param index
     */
    private record PathSegment(String field, int index) {

        @Override
        public String toString() {
            return index >= 0 ? field + "[" + index + "]" : field;
        }

    }

}
