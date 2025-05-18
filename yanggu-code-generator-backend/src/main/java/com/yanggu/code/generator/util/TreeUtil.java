package com.yanggu.code.generator.util;

import com.yanggu.code.generator.domain.vo.TreeVO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.yanggu.code.generator.domain.vo.TreeVO.TREE_COMPARATOR;

public class TreeUtil {

    public static List<TreeVO> buildTree(List<TreeVO> expandTreeList) {
        Map<String, TreeVO> nodeMap = getStringTreeVOMap(expandTreeList);

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

    private static Map<String, TreeVO> getStringTreeVOMap(List<TreeVO> expandTreeList) {
        Map<String, TreeVO> nodeMap = new HashMap<>();

        for (TreeVO treeVO : expandTreeList) {
            String[] pathParts = treeVO.getFilePath().split("/");
            for (int i = 0; i < pathParts.length; i++) {
                StringBuilder pathBuilder = new StringBuilder();
                for (int j = 0; j <= i; j++) {
                    pathBuilder.append(pathParts[j]);
                    if (j < i) {
                        pathBuilder.append("/");
                    }
                }
                String path = pathBuilder.toString();
                if (nodeMap.containsKey(path)) {
                    continue;
                }
                TreeVO tempTreeVO = new TreeVO();
                tempTreeVO.setLabel(pathParts[i]);
                tempTreeVO.setFilePath(path);
                tempTreeVO.setLevel(i);
                if (i == pathParts.length - 1) {
                    tempTreeVO.setTemplateId(treeVO.getTemplateId());
                    tempTreeVO.setIsFile(true);
                } else {
                    tempTreeVO.setIsFile(false);
                }
                nodeMap.put(path, tempTreeVO);
            }
        }
        return nodeMap;
    }

    private static List<TreeVO> getChildren(TreeVO node, List<TreeVO> treeList) {
        return treeList.stream()
                //判断是否是子节点
                .filter(treeVO -> isChild(node, treeVO))
                //递归添加子节点
                .peek(treeVO -> treeVO.setChildren(getChildren(treeVO, treeList)))
                //进行排序
                .sorted(TREE_COMPARATOR)
                .toList();
    }

    private static boolean isChild(TreeVO node, TreeVO treeVO) {
        return treeVO.getFilePath().startsWith(node.getFilePath()) && node.getLevel() + 1 == treeVO.getLevel();
    }

}
