package com.github.karma.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import com.github.karma.common.ExceptionEnum;
import com.github.karma.common.KarmaRuntimeException;
import com.github.karma.dto.TreeNode;
import com.github.karma.dto.content.Line;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class LineUtil {
    /**
     * 检验节点id在相同路径上
     *
     * @param paths
     * @param nodeIds
     * @return
     */
    public static boolean checkSamePath(Map<String, List<String>> paths,List<String> nodeIds){
        if(CollectionUtils.isEmpty(nodeIds) || nodeIds.size() < 2){
            return false;
        }
        String checkRoot = null;
        List<String> maxDepthPath = null;
        for(String nodeId: nodeIds){
            List<String> path = paths.get(nodeId);
            if(null == path){
                return false;
            }
            if(null == checkRoot){
                checkRoot = path.get(0);
                maxDepthPath = path;
            }else {
                if(!StringUtils.equals(checkRoot,path.get(0))){
                    return false;
                }
                maxDepthPath = maxDepthPath.size() > path.size() ?maxDepthPath : path;
            }
        }
        Set<String> pathNodeIds = maxDepthPath.stream().collect(Collectors.toSet());
        for(String nodeId: nodeIds){
            if(!pathNodeIds.contains(nodeId)){
                return false;
            }
        }
        return true;
    }

    /**
     * 检验节点id在相同层级上
     *
     * @param paths
     * @param nodeIds
     * @return
     */
    public static boolean checkSameLevel(Map<String, List<String>> paths,List<String> nodeIds){
        if(CollectionUtils.isEmpty(nodeIds) || nodeIds.size() < 2){
            return false;
        }
        String checkRoot = null;
        int checkLevel = 0;
        for(String nodeId: nodeIds){
            List<String> path = paths.get(nodeId);
            if(null == path){
                return false;
            }
            if(null == checkRoot){
                checkRoot = path.get(0);
                checkLevel = path.size();
            }else {
                if(!StringUtils.equals(checkRoot,path.get(0))){
                    return false;
                }
                if(checkLevel != path.size()){
                    return false;
                }
            }
        }
        return true;
    }

    public static List<TreeNode> buildTrees(List<Line> lines,Set<String> nodes) {
        if (CollectionUtils.isEmpty(lines)) {
            return nodes.stream().map(TreeNode::new).collect(Collectors.toList());
        }
        Set<String> nodeSet = new HashSet<>(nodes);
        // 使用HashMap存储节点，方便根据id快速查找
        Map<String, TreeNode> nodeMap = new HashMap<>();
        // 使用HashSet存储所有子节点id，用于后续判断根节点
        Set<String> childIds = new HashSet<>();
        List<TreeNode> rootNodes = new ArrayList<>();

        // 遍历关系数组，构建节点和子节点集合
        for (Line line : lines) {
            String fromId = line.getFrom();
            String toId = line.getTo();

            // 如果父节点不存在，则创建并加入map
            nodeMap.putIfAbsent(fromId, new TreeNode(fromId));
            // 如果子节点不存在，则创建并加入map
            nodeMap.putIfAbsent(toId, new TreeNode(toId));

            // 建立父子关系
            nodeMap.get(fromId).getChildren().add(nodeMap.get(toId));
            // 将子节点id加入集合
            childIds.add(toId);
            nodeSet.remove(fromId);
            nodeSet.remove(toId);
        }
        // 查找根节点，根节点id不在子节点集合中
        for (String id : nodeMap.keySet()) {
            if (!childIds.contains(id)) {
                rootNodes.add(nodeMap.get(id));
            }
        }
        if(!CollectionUtils.isEmpty(nodeSet)){
            for(String node : nodeSet){
                rootNodes.add(new TreeNode(node));
            }
        }

        // 如果没有找到根节点，说明关系数组有误
        if (CollectionUtils.isEmpty(rootNodes)) {
            throw new KarmaRuntimeException(ExceptionEnum.PROCESS_FAILED.getCode(),"找不到根节点，关系信息有误");
        }

        // 返回根节点，即整棵树
        return rootNodes;
    }

    public static Map<String,List<String>> buildPaths(List<TreeNode> rootNodes){
        Map<String,List<String>> paths = new HashMap<>();
        if(CollectionUtils.isEmpty(rootNodes)){
            return paths;
        }
        List<String> currentPath = new ArrayList<>();
        for(TreeNode root : rootNodes){
            // 添加当前节点的 id
            currentPath.add(root.getId());
            // 添加当前路径到结果列表
            paths.put(root.getId(), new ArrayList<>(currentPath));
            dfsPath(root,currentPath,paths);
            // 去除当前节点的 id
            currentPath.remove(root.getId());
        }
        return paths;
    }

    private static void dfsPath(TreeNode node, List<String> currentPath, Map<String, List<String>> paths) {
        if (null == node) {
            return;
        }

        for (TreeNode child : node.getChildren()) {
            // 添加当前节点的 id
            currentPath.add(child.getId());
            // 添加当前路径到结果列表
            paths.put(child.getId(), new ArrayList<>(currentPath));
            dfsPath(child, currentPath, paths);
            // 去除当前节点的 id
            currentPath.remove(child.getId());
        }
    }

    public static String templateReplace(String template, Map<String, String> props) {
        return templateReplace(template, props, "${", "}");
    }

    public static String templateReplace(String template, Map<String, String> props, String prefix, String suffix) {
        int startIndex = template.indexOf(prefix);
        if (startIndex == -1) {
            return template;
        }

        StringBuilder result = new StringBuilder(template);
        while (startIndex != -1) {
            int endIndex = findPlaceholderEndIndex(result, startIndex, prefix, suffix);
            if (endIndex != -1) {
                String propKey = result.substring(startIndex + prefix.length(), endIndex);
                if (props == null) {
                    throw new KarmaRuntimeException(ExceptionEnum.PROCESS_FAILED.getCode(),"参数配置不能为空");
                }
                String propVal = props.get(propKey);
                if (propVal != null) {
                    result.replace(startIndex, endIndex + suffix.length(), propVal);
                    startIndex = result.indexOf(prefix, startIndex + propVal.length());
                } else {
                    throw new KarmaRuntimeException(ExceptionEnum.PROCESS_FAILED.getCode(), "未知变量结果" +
                            propKey + "=" + propVal);
                }
            } else {
                startIndex = -1;
            }
        }
        return result.toString();
    }

    private static int findPlaceholderEndIndex(CharSequence buf, int startIndex, String prefix, String suffix) {
        int index = startIndex + prefix.length();
        int withinNestedPlaceholder = 0;
        while (index < buf.length()) {
            if (substringMatch(buf, index, suffix)) {
                if (withinNestedPlaceholder > 0) {
                    withinNestedPlaceholder--;
                    index = index + suffix.length();
                } else {
                    return index;
                }
            } else if (substringMatch(buf, index, prefix)) {
                withinNestedPlaceholder++;
                index = index + prefix.length();
            } else {
                index++;
            }
        }
        return -1;
    }

    private static boolean substringMatch(CharSequence str, int index, CharSequence substring) {
        for (int j = 0; j < substring.length(); j++) {
            int i = index + j;
            if (i >= str.length() || str.charAt(i) != substring.charAt(j)) {
                return false;
            }
        }
        return true;
    }
}
