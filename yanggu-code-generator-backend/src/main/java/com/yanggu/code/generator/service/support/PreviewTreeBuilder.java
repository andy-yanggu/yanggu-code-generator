package com.yanggu.code.generator.service.support;

import com.yanggu.code.generator.domain.vo.PreviewTemplateVO;
import com.yanggu.code.generator.domain.vo.TemplateContentVO;
import com.yanggu.code.generator.enums.TemplateTypeEnum;
import cn.hutool.v7.core.collection.CollUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预览树构建器
 *
 * 将平铺的模板渲染内容按文件路径层级组装为树形结构。纯静态算法，不依赖 Spring 容器。
 */
public final class PreviewTreeBuilder {

    private PreviewTreeBuilder() {
    }

    /**
     * 构建树形结构
     *
     * @param expandTreeList 平铺的模板渲染内容列表
     * @return 层级为 0 的根节点树列表
     */
    public static List<PreviewTemplateVO> buildTree(List<TemplateContentVO> expandTreeList) {
        if (CollUtil.isEmpty(expandTreeList)) {
            return Collections.emptyList();
        }

        Map<String, PreviewTemplateVO> nodeMap = buildNodeMap(expandTreeList);

        //将平铺的数据，按照树形进行组装
        List<PreviewTemplateVO> allTreeList = new ArrayList<>(nodeMap.values());
        return allTreeList.stream()
                //找到层级为0的节点
                .filter(treeVO -> treeVO.getLevel() == 0)
                //添加子节点
                .peek(treeVO -> treeVO.setChildren(getChildren(treeVO, allTreeList)))
                //进行排序
                .sorted(PreviewTemplateVO.TREE_COMPARATOR)
                .toList();
    }

    private static Map<String, PreviewTemplateVO> buildNodeMap(List<TemplateContentVO> expandTreeList) {
        Map<String, PreviewTemplateVO> nodeMap = new HashMap<>();

        //将树形数据，平铺成Map
        for (TemplateContentVO treeVO : expandTreeList) {
            String[] pathParts = treeVO.getFilePath().split("/");
            int pathLength = pathParts.length;
            for (int level = 0; level < pathLength; level++) {
                //当前路径
                String currentPath = String.join("/", Arrays.copyOfRange(pathParts, 0, level + 1));
                if (nodeMap.containsKey(currentPath)) {
                    continue;
                }
                PreviewTemplateVO tempTreeVO = new PreviewTemplateVO();
                //节点名称
                tempTreeVO.setFileName(pathParts[level]);
                //文件路径
                tempTreeVO.setFilePath(currentPath);
                //层级
                tempTreeVO.setLevel(level);
                //是否为模板
                if (level == pathLength - 1) {
                    //模板ID
                    tempTreeVO.setTemplateId(treeVO.getTemplateId());
                    //枚举ID
                    tempTreeVO.setEnumId(treeVO.getEnumId());
                    //表ID
                    tempTreeVO.setTableId(treeVO.getTableId());
                    //模板类型
                    tempTreeVO.setTemplateType(treeVO.getTemplateType());
                    //模板组类型
                    tempTreeVO.setTemplateGroupType(treeVO.getTemplateGroupType());
                    //内容
                    tempTreeVO.setTemplateContent(treeVO.getContent());
                } else {
                    tempTreeVO.setTemplateType(TemplateTypeEnum.DIRECTORY.getCode());
                }
                nodeMap.put(currentPath, tempTreeVO);
            }
        }
        return nodeMap;
    }

    private static List<PreviewTemplateVO> getChildren(PreviewTemplateVO parent, List<PreviewTemplateVO> treeList) {
        return treeList.stream()
                //判断是否是直接子节点
                .filter(child -> isDirectChild(parent, child))
                //递归添加子节点
                .peek(child -> child.setChildren(getChildren(child, treeList)))
                //进行排序
                .sorted(PreviewTemplateVO.TREE_COMPARATOR)
                .toList();
    }

    /**
     * 判断是否是直接子节点
     */
    private static boolean isDirectChild(PreviewTemplateVO parent, PreviewTemplateVO child) {
        return child.getLevel() == parent.getLevel() + 1
                && child.getFilePath().startsWith(parent.getFilePath() + "/");
    }
}
