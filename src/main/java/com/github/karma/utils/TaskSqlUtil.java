package com.github.karma.utils;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import com.github.karma.common.ExceptionEnum;
import com.github.karma.common.KarmaRuntimeException;
import com.github.karma.dto.DataSource;
import com.github.karma.dto.TreeNode;
import com.github.karma.dto.content.Node;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * TaskSqlUtil
 *
 * @author lujunming
 * @version 2025/02/07 18:14
 **/
public class TaskSqlUtil {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");

    /**
     * 检查分析流内容
     *
     * @param path
     * @param nodeMap
     * @param dataSourceMap
     * @return
     */
    public static List<String> checkPath(List<String> path, Map<String, Node> nodeMap,
        Map<String, DataSource> dataSourceMap) {
        List<String> result = Lists.newArrayList();
        if (CollectionUtils.isEmpty(nodeMap)) {
            return result;
        }
        if (CollectionUtils.isEmpty(dataSourceMap)) {
            throw new KarmaRuntimeException(ExceptionEnum.DATASOURCE_NOT_EXIST.getCode(), "请配置至少一个数据源");
        }
        dfsCheck(result, path, 0, nodeMap, dataSourceMap);
        return result;
    }

    private static void dfsCheck(List<String> result, List<String> path, int level, Map<String, Node> nodeMap,
        Map<String, DataSource> dataSourceMap) {
        if (CollectionUtils.isEmpty(path)) {
            return;
        }
        if (level >= path.size()) {
            return;
        }

        // 获取信息
        Node node = nodeMap.get(path.get(level));
        DataSource dataSource = dataSourceMap.get(node.getDs());

        // 校验变量
        checkProps(result, dataSource, node);

        if (null == dataSource) {
            throw new KarmaRuntimeException(ExceptionEnum.DATASOURCE_NOT_EXIST.getCode(), node.getName() + "对应的数据源不存在");
        }

        dfsCheck(result, path, level + 1, nodeMap, dataSourceMap);
    }

    /**
     * 检查分析流内容
     * 
     * @param rootNodes
     * @param nodeMap
     * @param dataSourceMap
     * @return
     */
    public static List<String> check(List<TreeNode> rootNodes, Map<String, Node> nodeMap,
        Map<String, DataSource> dataSourceMap, Map<String, String> globalParams) {
        try {
            GlobleParamsUtil.setGlobalParams(globalParams);
            return doCheck(rootNodes, nodeMap, dataSourceMap);
        } finally {
            GlobleParamsUtil.clear();
        }
    }

    private static List<String> doCheck(List<TreeNode> rootNodes, Map<String, Node> nodeMap,
        Map<String, DataSource> dataSourceMap) {
        List<String> result = Lists.newArrayList();
        if (CollectionUtils.isEmpty(nodeMap)) {
            result.add("请包涵至少一个节点");
            return result;
        }
        if (CollectionUtils.isEmpty(dataSourceMap)) {
            result.add("请配置至少一个数据源");
            return result;
        }
        for (TreeNode root : rootNodes) {
            dfsCheck(result, root, nodeMap, dataSourceMap);
        }
        return result;
    }

    private static void dfsCheck(List<String> result, TreeNode treeNode, Map<String, Node> nodeMap,
        Map<String, DataSource> dataSourceMap) {
        if (null == treeNode) {
            return;
        }
        // 获取信息
        Node node = nodeMap.get(treeNode.getId());
        DataSource dataSource = dataSourceMap.get(node.getDs());

        // 校验变量
        checkProps(result, dataSource, node);

        for (TreeNode child : treeNode.getChildren()) {
            dfsCheck(result, child, nodeMap, dataSourceMap);
        }
    }

    private static void checkProps(List<String> result, DataSource dataSource, Node node) {
        if (null == dataSource) {
            result.add(node.getName() + "节点未设置数据源");
            return;
        }
        String dataSourcePropsJson = dataSource.getDsProps();
        if (StringUtils.isBlank(dataSourcePropsJson)) {
            return;
        }
        List<JSONObject> dsProps = JSONUtil.toList(dataSourcePropsJson, JSONObject.class);
        Map<String, String> props = node.getProps();
        Map<String, String> realProps = getRealPropsMap(dataSource, props);
        for (JSONObject dsProp : dsProps) {
            String name = dsProp.getStr("name");
            if (StringUtils.isBlank(realProps.get(name))) {
                result.add(node.getName() + "节点缺少属性" + name);
                continue;
            }
            if ("text".equals(dsProp.getStr("type"))) {
                continue;
            } else if ("number".equals(dsProp.getStr("type"))) {
                if (!isNumeric(realProps.get(name))) {
                    result.add(node.getName() + "节点属性" + name + "不是数字");
                }
            } else if ("date".equals(dsProp.getStr("type")) || "datetime".equals(dsProp.getStr("type"))) {
                String fmt = dsProp.getStr("fmt");
                if (StringUtils.isBlank(fmt)) {
                    result.add(node.getName() + "节点属性" + name + "未设置格式");
                    continue;
                }
                try {
                    DateTimeFormatter.ofPattern(fmt).parse(realProps.get(name));
                } catch (Exception e) {
                    result.add(node.getName() + "节点属性" + name + "格式不正确，应为" + fmt);
                }
            } else {
                result.add(node.getName() + "节点属性" + name + "未知格式");
            }
        }
    }

    public static boolean isNumeric(String str) {
        return str != null && NUMBER_PATTERN.matcher(str).matches();
    }

    /**
     * 生成参数列表
     *
     * @param path
     * @param nodeMap
     * @param dataSourceMap
     * @return
     */
    public static List<JSONObject> genParams(List<String> path, Map<String, Node> nodeMap,
        Map<String, DataSource> dataSourceMap) {
        if (CollectionUtils.isEmpty(nodeMap) || CollectionUtils.isEmpty(dataSourceMap)) {
            return new ArrayList<>();
        }
        Map<String, JSONObject> resultMap = new LinkedHashMap<>();

        dfs(resultMap, path, nodeMap, dataSourceMap);
        return new ArrayList<>(resultMap.values());
    }

    private static void dfs(Map<String, JSONObject> resultMap, List<String> path, Map<String, Node> nodeMap,
        Map<String, DataSource> dataSourceMap) {
        if (CollectionUtils.isEmpty(nodeMap)) {
            return;
        }
        for (String nodeId : path) {
            Node node = nodeMap.get(nodeId);
            if (null == node) {
                throw new KarmaRuntimeException(ExceptionEnum.TARGET_NOT_EXIST.getCode(), "节点不存在:" + nodeId);
            }
            DataSource dataSource = dataSourceMap.get(node.getDs());
            if (null == dataSource) {
                throw new KarmaRuntimeException(ExceptionEnum.TARGET_NOT_EXIST.getCode(),
                    "dsCode无法找到对应数据源,nodeId:" + nodeId);
            }
            String dsParamsJson = dataSource.getDsParams();
            List<JSONObject> dsParams = JSONUtil.toList(dsParamsJson, JSONObject.class);
            resultMap.putAll(dsParams.stream().filter(obj -> !"uid".equals(obj.getStr("name")))
                .collect(Collectors.toMap(obj -> obj.getStr("name"), obj -> obj)));
        }
    }

    /**
     * 生成任务sql
     *
     * @param path
     * @param nodeMap
     * @param dataSourceMap
     * @return
     */
    public static Map<String, String> genTaskSql(List<String> path, Map<String, Node> nodeMap,
        Map<String, DataSource> dataSourceMap, Map<String, String> globalParams) {
        try {
            GlobleParamsUtil.setGlobalParams(globalParams);
            return doGenTaskSql(path, nodeMap, dataSourceMap);
        } finally {
            GlobleParamsUtil.clear();
        }
    }

    private static Map<String, String> doGenTaskSql(List<String> path, Map<String, Node> nodeMap,
        Map<String, DataSource> dataSourceMap) {
        Map<String, String> result = Maps.newHashMap();
        if (CollectionUtils.isEmpty(nodeMap)) {
            return result;
        }
        if (CollectionUtils.isEmpty(dataSourceMap)) {
            throw new KarmaRuntimeException(ExceptionEnum.DATASOURCE_NOT_EXIST.getCode(), "请配置至少一个数据源");
        }
        Set<String> parentSelect = new HashSet<>();
        dfs(result, parentSelect, path, 0, null, nodeMap, dataSourceMap);
        return result;
    }

    private static void dfs(Map<String, String> result, Set<String> parentSelect, List<String> path, int level,
        String parentSql, Map<String, Node> nodeMap, Map<String, DataSource> dataSourceMap) {
        if (CollectionUtils.isEmpty(path)) {
            return;
        }
        if (level >= path.size()) {
            return;
        }

        // 获取信息
        Node node = nodeMap.get(path.get(level));
        DataSource dataSource = dataSourceMap.get(node.getDs());
        if (null == dataSource) {
            throw new KarmaRuntimeException(ExceptionEnum.DATASOURCE_NOT_EXIST.getCode(), node.getName() + "对应的数据源不存在");
        }

        // 变量替换
        String originSql = buildOriginSql(dataSource, node.getProps());

        // 生成sql
        String sql = buildTaskSql(parentSql, originSql, level,
            buildExtSelect(parentSelect, dataSource.getDsParams(), level), node.getRelation());
        result.put(path.get(level), buildAggrsSql(sql, level, node.getAggrs(), parentSelect, node.getName()));

        dfs(result, parentSelect, path, level + 1, sql, nodeMap, dataSourceMap);
    }

    /**
     * 生成所有任务sql
     *
     * @param rootNodes
     * @param nodeMap
     * @param dataSourceMap
     * @return
     */
    public static Map<String, String> genAllTaskSql(List<TreeNode> rootNodes, Map<String, Node> nodeMap,
        Map<String, DataSource> dataSourceMap, Map<String, String> globalParams) {
        try {
            GlobleParamsUtil.setGlobalParams(globalParams);
            return doGenAllTaskSql(rootNodes, nodeMap, dataSourceMap);
        } finally {
            GlobleParamsUtil.clear();
        }
    }

    private static Map<String, String> doGenAllTaskSql(List<TreeNode> rootNodes, Map<String, Node> nodeMap,
        Map<String, DataSource> dataSourceMap) {
        Map<String, String> result = Maps.newHashMap();
        if (CollectionUtils.isEmpty(nodeMap)) {
            return result;
        }
        if (CollectionUtils.isEmpty(dataSourceMap)) {
            throw new KarmaRuntimeException(ExceptionEnum.DATASOURCE_NOT_EXIST.getCode(), "请配置至少一个数据源");
        }
        for (TreeNode root : rootNodes) {
            dfs(result, new HashSet<>(), root, 0, null, nodeMap, dataSourceMap);
        }
        return result;
    }

    private static void dfs(Map<String, String> result, Set<String> parentSelect, TreeNode treeNode, int level,
        String parentSql, Map<String, Node> nodeMap, Map<String, DataSource> dataSourceMap) {
        if (null == treeNode) {
            return;
        }
        // 获取信息
        Node node = nodeMap.get(treeNode.getId());
        DataSource dataSource = dataSourceMap.get(node.getDs());
        if (null == dataSource) {
            throw new KarmaRuntimeException(ExceptionEnum.DATASOURCE_NOT_EXIST.getCode(), node.getName() + "对应的数据源不存在");
        }

        // 变量替换
        String originSql = buildOriginSql(dataSource, node.getProps());

        // 生成sql
        String sql = buildTaskSql(parentSql, originSql, level,
            buildExtSelect(parentSelect, dataSource.getDsParams(), level), node.getRelation());
        result.put(treeNode.getId(), buildAggrsSql(sql, level, node.getAggrs(), parentSelect, node.getName()));

        for (TreeNode child : treeNode.getChildren()) {
            dfs(result, new HashSet<>(parentSelect), child, level + 1, sql, nodeMap, dataSourceMap);
        }
    }

    private static final Set<String> COMMON_AGGRS = Sets.newHashSet("count", "sum", "avg", "max", "min");

    private static String buildAggrsSql(String sql, int level, Map<String, String> aggrsmap, Set<String> parentSelect,
        String nodeName) {
        StringBuilder builder = new StringBuilder();
        Map<String, String> aggrs = new LinkedHashMap<>();
        aggrs.put("uid", "count_distinct");
        if (!CollectionUtils.isEmpty(aggrsmap)) {
            aggrs.putAll(aggrsmap);
        }
        builder.append("select ");
        for (Map.Entry<String, String> entry : aggrs.entrySet()) {
            if (!parentSelect.contains(entry.getKey()) && !"uid".equals(entry.getKey())) {
                throw new KarmaRuntimeException(ExceptionEnum.PROCESS_FAILED.getCode(),
                    entry.getKey() + "不存在，请检查 " + nodeName + " 聚合参数配置");
            }
            String func = null == entry.getValue() ? "none" : entry.getValue().intern();
            if (COMMON_AGGRS.contains(func)) {
                // min max avg count
                builder.append(func).append("(l").append(level).append(".").append(entry.getKey()).append(") as a_")
                    .append(entry.getKey()).append(",");
            } else if ("count_distinct".equals(func)) {
                // count_distinct
                builder.append("count(distinct ").append("l").append(level).append(".").append(entry.getKey())
                    .append(") as a_").append(entry.getKey()).append(",");
            } else {
                // none 和 不填 不做任何处理
                continue;
            }
        }
        if (builder.length() < 8) {
            // 所有聚合都为none，不处理
            return sql;
        }
        builder.setLength(builder.length() - 1);
        builder.append(" from (").append(sql).append(") l").append(level);
        return builder.toString();
    }

    private static String buildOriginSql(DataSource dataSource, Map<String, String> props) {
        String dsSql = stdOriginSql(dataSource);

        Map<String, String> propsMap = getRealPropsMap(dataSource, props);

        dsSql = StringUtils.replace(dsSql, "\n", " ");
        dsSql = RegExUtils.replaceAll(dsSql, "\\s+", " ");
        return LineUtil.templateReplace(dsSql, propsMap);
    }

    private static Map<String, String> getRealPropsMap(DataSource dataSource, Map<String, String> props) {
        if(CollectionUtils.isEmpty(props)){
            return props;
        }
        String dataSourcePropsJson = dataSource.getDsProps();
        Map<String, JSONObject> dsPropMap = new HashMap<>();
        if (!StringUtils.isBlank(dataSourcePropsJson)) {
            dsPropMap = JSONUtil.toList(dataSourcePropsJson, JSONObject.class).stream()
                .collect(Collectors.toMap(obj -> obj.getStr("name"), obj -> obj));
        }
        Map<String,String> realPropsMap = new HashMap<>();
        for (Map.Entry<String, String> entry : props.entrySet()) {
            if (entry.getValue().startsWith("#")) {
                String realValue = GlobleParamsUtil.getValue(entry.getValue());
                JSONObject prop = dsPropMap.get(entry.getKey());
                if ("date".equals(prop.getStr("type"))) {
                    realPropsMap.put(entry.getKey(), getReadDate(realValue, prop.getStr("fmt")));
                } else if ("datetime".equals(prop.getStr("type"))) {
                    realPropsMap.put(entry.getKey(), getRealDateTime(realValue, prop.getStr("fmt")));
                } else {
                    realPropsMap.put(entry.getKey(), realValue);
                }
            }else {
                realPropsMap.put(entry.getKey(), entry.getValue());
            }
        }
        return realPropsMap;
    }

    private static String getReadDate(String dateExp, String format) {
        if (StringUtils.isBlank(dateExp)) {
            return dateExp;
        }
        try {
            if (dateExp.startsWith("yyyyMMdd")) {
                Long offset = 8 == dateExp.length() ? 0L : Long.parseLong(dateExp.substring(8));
                return LocalDate.now().plusDays(offset).format(DateTimeFormatter.ofPattern(format));
            } else {
                return dateExp;
            }
        } catch (Exception e) {
            return dateExp;
        }
    }

    private static String getRealDateTime(String dateExp, String format) {
        if (StringUtils.isBlank(dateExp)) {
            return dateExp;
        }
        try {
            if (dateExp.startsWith("yyyyMMddHH")) {
                Long offset = 10 == dateExp.length() ? 0L : Long.parseLong(dateExp.substring(10));
                return LocalDateTime.now().plusHours(offset).format(DateTimeFormatter.ofPattern(format));
            } else if (dateExp.startsWith("yyyyMMdd")) {
                Long offset = 8 == dateExp.length() ? 0L : Long.parseLong(dateExp.substring(8));
                return LocalDateTime.now().plusDays(offset).format(DateTimeFormatter.ofPattern(format));
            } else {
                return dateExp;
            }
        } catch (Exception e) {
            return dateExp;
        }
    }

    private static String stdOriginSql(DataSource dataSource) {
        String dataSourcePropsJson = dataSource.getDsProps();
        if (StringUtils.isBlank(dataSourcePropsJson)) {
            return dataSource.getDsSql();
        }
        String sql = dataSource.getDsSql();
        List<JSONObject> dsProps = JSONUtil.toList(dataSourcePropsJson, JSONObject.class);
        for (JSONObject dsProp : dsProps) {
            if ("number".equals(dsProp.getStr("type"))) {
                String name = dsProp.getStr("name");
                sql = StringUtils.replace(sql, "\"${" + name + "}\"", "${" + name + "}");
                sql = StringUtils.replace(sql, "'${" + name + "}'", "${" + name + "}");
            }
        }
        return sql;
    }

    private static String buildExtSelect(Set<String> leftSelect, String dsParams, int level) {
        if (CollectionUtils.isEmpty(leftSelect) && StringUtils.isBlank(dsParams)) {
            return "";
        }
        int curLevel = level - 1;
        StringBuilder builder = new StringBuilder();
        if (!CollectionUtils.isEmpty(leftSelect)) {
            for (String select : leftSelect) {
                builder.append(" ,l").append(curLevel).append(".").append(select);
            }
        }
        if (StringUtils.isBlank(dsParams)) {
            return builder.toString();
        }
        List<JSONObject> list = JSONUtil.toList(dsParams, JSONObject.class);
        Set<String> rightSelect = list.stream().map(jsonObject -> jsonObject.getStr("name"))
            .filter(name -> !"uid".equals(name)).collect(Collectors.toSet());
        if (!CollectionUtils.isEmpty(rightSelect)) {
            for (String select : rightSelect) {
                if (leftSelect.contains(select)) {
                    // 过滤重复字段，避免下一层报错
                    continue;
                }
                builder.append(" ,r").append(curLevel).append(".").append(select);
            }
        }

        leftSelect.addAll(rightSelect);
        return builder.toString();
    }

    private static String buildTaskSql(String leftSql, String rightSql, int level, String extSelect, String relation) {
        if (0 == level) {
            // 第一层 直接返回目标sql
            return rightSql;
        }
        Map<String, String> params = new HashMap<>();
        String template =
            "select l${level}.uid${extSelect} from (${leftSql}) l${level} left join (${rightSql}) r${level} on l${level}.uid = r${level}.uid where r${level}.uid ${relation}";
        params.put("leftSql", leftSql);
        params.put("rightSql", rightSql);
        params.put("level", String.valueOf(level - 1));
        params.put("extSelect", extSelect);
        if ("intersection".equals(relation)) {
            params.put("relation", "is not null");
        } else if ("difference".equals(relation)) {
            params.put("relation", "is null");
        }
        return LineUtil.templateReplace(template, params);
    }
}
