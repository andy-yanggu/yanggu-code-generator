package com.yanggu.code.generator.service.support;

import com.yanggu.code.generator.domain.vo.TemplateVO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * 模板生成路径解析器
 *
 * 从模板组模板列表构建生成路径树：根节点路径为文件名，子节点路径为父路径拼接子文件名，
 * 返回叶子节点列表（无子节点的节点）。纯静态算法，不依赖 Spring 容器。
 */
public final class TemplatePathResolver {

    private TemplatePathResolver() {
    }

    /**
     * 为模板 VO 列表分配 generatorPath 并返回叶子节点列表
     *
     * @param allTemplates 已由实体转换的模板 VO 全量列表
     * @return 叶子节点（无子节点）列表
     */
    public static List<TemplateVO> assignGeneratorPaths(List<TemplateVO> allTemplates) {
        // 1. 构建父节点到子节点的映射关系
        Map<Long, List<TemplateVO>> parentChildrenMap = new HashMap<>();
        for (TemplateVO template : allTemplates) {
            Long parentId = template.getParentId();
            parentChildrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(template);
        }

        // 2. 使用队列进行层次遍历，设置 generatorPath
        Queue<TemplateVO> queue = new LinkedList<>();
        for (TemplateVO template : allTemplates) {
            if (template.getParentId() == 0) {
                template.setGeneratorPath(template.getFileName());
                queue.offer(template);
            }
        }

        while (!queue.isEmpty()) {
            TemplateVO current = queue.poll();
            List<TemplateVO> children = parentChildrenMap.getOrDefault(current.getId(), Collections.emptyList());
            for (TemplateVO child : children) {
                child.setGeneratorPath(current.getGeneratorPath() + "/" + child.getFileName());
                queue.offer(child);
            }
        }

        // 3. 筛选叶子节点（无子节点的节点）
        return allTemplates.stream()
                .filter(t -> parentChildrenMap.getOrDefault(t.getId(), Collections.emptyList()).isEmpty())
                .collect(java.util.stream.Collectors.toList());
    }
}
