package com.yanggu.code.generator.util;

import com.yanggu.code.generator.domain.vo.TreeVO;
import com.yanggu.code.generator.enums.TemplateTypeEnum;
import com.yanggu.code.generator.enums.TreeTypeEnum;
import lombok.experimental.UtilityClass;
import org.dromara.hutool.core.collection.CollUtil;

import java.util.*;
import java.util.stream.Stream;

import static com.yanggu.code.generator.domain.vo.TreeVO.TREE_COMPARATOR;

/**
 * 树形工具类
 */
@UtilityClass
public class TreeUtil {

    /**
     * 构建树形结构
     */
    public static List<TreeVO> buildTree(List<TreeVO> expandTreeList) {
        if (CollUtil.isEmpty(expandTreeList)) {
            return Collections.emptyList();
        }

        Map<String, TreeVO> nodeMap = buildNodeMap(expandTreeList);

        //将平铺的数据，按照树形进行组装
        List<TreeVO> allTreeList = new ArrayList<>(nodeMap.values());
        return allTreeList.stream()
                //找到层级为0的节点
                .filter(treeVO -> treeVO.getLevel() == 0)
                //添加子节点
                .peek(treeVO -> treeVO.setChildren(getChildren(treeVO, allTreeList)))
                //进行排序
                .sorted(TREE_COMPARATOR)
                .toList();
    }

    /**
     * 深度优先遍历获取路径顺序
     */
    public static List<String> getDfsOrder(List<TreeVO> treeList) {
        List<String> orderList = new ArrayList<>();
        for (TreeVO node : treeList) {
            orderList.add(node.getFilePath());
            if (CollUtil.isNotEmpty(node.getChildren())) {
                orderList.addAll(getDfsOrder(node.getChildren()));
            }
        }
        return orderList;
    }

    private static Map<String, TreeVO> buildNodeMap(List<TreeVO> expandTreeList) {
        Map<String, TreeVO> nodeMap = new HashMap<>();

        //将树形数据，平铺成Map
        for (TreeVO treeVO : expandTreeList) {
            String[] pathParts = treeVO.getFilePath().split("/");
            int pathLength = pathParts.length;
            for (int level = 0; level < pathLength; level++) {
                //当前路径
                String currentPath = String.join("/", Arrays.copyOfRange(pathParts, 0, level + 1));
                if (nodeMap.containsKey(currentPath)) {
                    continue;
                }
                TreeVO tempTreeVO = new TreeVO();
                //节点名称
                tempTreeVO.setLabel(pathParts[level]);
                //文件路径
                tempTreeVO.setFilePath(currentPath);
                //层级
                tempTreeVO.setLevel(level);
                //是否为模板
                tempTreeVO.setIsTemplate(level == pathLength - 1);
                if (tempTreeVO.getIsTemplate()) {
                    tempTreeVO.setTemplateId(treeVO.getTemplateId());
                    tempTreeVO.setTemplateType(treeVO.getTemplateType());
                }
                //设置树类型
                boolean anyMatch = Stream.of(TemplateTypeEnum.TEMPLATE_FILE, TemplateTypeEnum.BINARY_FILE)
                        .map(TemplateTypeEnum::getCode)
                        .anyMatch(code -> code.equals(tempTreeVO.getTemplateType()));
                if (anyMatch) {
                    tempTreeVO.setType(TreeTypeEnum.FILE.getCode());
                } else {
                    tempTreeVO.setType(TreeTypeEnum.DIRECTORY.getCode());
                }
                nodeMap.put(currentPath, tempTreeVO);
            }
        }
        return nodeMap;
    }

    private static List<TreeVO> getChildren(TreeVO parent, List<TreeVO> treeList) {
        return treeList.stream()
                //判断是否是直接子节点
                .filter(child -> isDirectChild(parent, child))
                //递归添加子节点
                .peek(child -> child.setChildren(getChildren(child, treeList)))
                //进行排序
                .sorted(TREE_COMPARATOR)
                .toList();
    }

    /**
     * 判断是否是直接子节点
     */
    private static boolean isDirectChild(TreeVO parent, TreeVO child) {
        return child.getLevel() == parent.getLevel() + 1
                && child.getFilePath().startsWith(parent.getFilePath() + "/");
    }

}
