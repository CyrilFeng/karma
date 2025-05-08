package com.github.karma.service.impl;

import com.github.karma.utils.GlobleParamsUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.github.karma.common.ExceptionEnum;
import com.github.karma.common.KarmaRuntimeException;
import com.github.karma.dao.StreamDao;
import com.github.karma.dao.DataSourceDao;
import com.github.karma.dao.DirDao;
import com.github.karma.dto.Stream;
import com.github.karma.dto.DataSource;
import com.github.karma.dto.Dir;
import com.github.karma.dto.TreeNode;
import com.github.karma.dto.content.Content;
import com.github.karma.dto.content.Node;
import com.github.karma.service.StreamService;
import com.github.karma.trino.TrinoClient;
import com.github.karma.utils.LineUtil;
import com.github.karma.utils.TaskSqlUtil;
import com.google.common.collect.Lists;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StreamServiceImpl implements StreamService {

    @Autowired
    private DirDao dirDao;
    @Autowired
    private DataSourceDao dataSourceDao;
    @Autowired
    private StreamDao streamDao;

    @Autowired
    private TrinoClient trinoClient;

    /**
     * 保存（创建/更新）分析流
     * 
     * @param stream
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAnalyseStream(Stream stream) {
        if (null == stream.getId()) {
            Long id = createDir(stream);
            stream.setId(id);
            streamDao.insertSteam(stream);
        } else {
            updateDir(stream);
            updateAnalyseStream(stream);
        }
    }

    private void updateAnalyseStream(Stream stream) {
        stream.setUpdateUser(stream.getUpdateUser());
        stream.setUpdateTime(LocalDateTime.now());
        streamDao.updateSteam(stream);
    }

    private void updateDir(Stream stream) {
        Dir file = dirDao.getDirById(stream.getId());
        file.setName(stream.getName());
        file.setPId(stream.getDirId());
        file.setUpdateUser(stream.getUpdateUser());
        file.setUpdateTime(LocalDateTime.now());
        dirDao.updateDir(file);
    }

    private Long createDir(Stream stream) {
        Dir dir = new Dir();
        dir.setName(stream.getName());
        dir.setDirType("file");
        dir.setPId(stream.getDirId());
        dir.setCreateUser(stream.getCreateUser());
        dir.setCreateTime(LocalDateTime.now());
        return dirDao.insertDir(dir);
    }

    /**
     * 删除分析流
     * 
     * @param streamId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAnalyseStream(Long streamId) {
        dirDao.deleteById(streamId);
        streamDao.deleteSteam(streamId);
    }

    /**
     * 根据id获取单个分析流
     * 
     * @param streamId
     * @return
     */
    @Override
    public Stream getAnalyseStream(Long streamId) {
        Dir file = dirDao.getDirById(streamId);
        Stream stream = streamDao.getSteam(streamId);
        stream.setDirId(file.getPId());
        stream.setName(file.getName());
        return stream;
    }

    /**
     * 执行分析流
     * 
     * @param content
     * @param nodeCode
     * @param account
     * @return
     */
    @Override
    public Map<String, JSONObject> execute(Content content, String nodeCode, String account) {

        // 1、校验数据源权限
        checkDsAuth(content, nodeCode, account);

        // 2、对分析流的内容进行解析，生成任务sql
        Map<String, String> sqls = generateTaskSql(content, nodeCode);

        // 3、调用trino，执行sql，返回当前结果，不一定是最终结果
        Map<String, JSONObject> result = new HashMap<>(sqls.size());
        sqls.forEach((nCode, sql) -> {
            JSONObject queryResults = trinoClient.runSQL(sql);
            result.put(nCode, queryResults);
        });
        return result;
    }

    // 校验数据源权限
    private void checkDsAuth(Content content, String nodeCode, String account) {
        // nodeId为空，说明需要执行整个树，所有节点都需要进行校验
        Set<String> needCheckAuthNodeCodes = new HashSet<>();

        if (StringUtils.isBlank(nodeCode)) {
            needCheckAuthNodeCodes = content.getNodes().keySet();
        } else {
            List<TreeNode> rootTreeNodes = LineUtil.buildTrees(content.getLines(), content.getNodes().keySet());
            Map<String, List<String>> rootLinkedTreeNodes = LineUtil.buildPaths(rootTreeNodes);
            List<String> linkedTreeNodes = rootLinkedTreeNodes.get(nodeCode);
            needCheckAuthNodeCodes.addAll(linkedTreeNodes);
        }

        // 循环进行校验每个ds的数据源权限
        for (String nCode : needCheckAuthNodeCodes) {
            String dsCode = content.getNodes().get(nCode).getDs();
            DataSource dataSource = dataSourceDao.queryByCode(dsCode);
            if (dataSource == null) {
                throw new KarmaRuntimeException(ExceptionEnum.DATASOURCE_NOT_EXIST.getCode(), "数据源不存在，code=" + dsCode);
            }
            String dsAuths = dataSource.getDsAuths();
            if (StringUtils.isBlank(dsAuths)
                || !Arrays.stream(dsAuths.split(",")).collect(Collectors.toSet()).contains(account)) {
                throw new KarmaRuntimeException(ExceptionEnum.NO_PERMISSION.getCode(),
                    "您没有权限，数据源：" + dataSource.getDsName());
            }
        }
    }

    /**
     * 获取分析流结果
     * 
     * @param nextUri
     * @return
     */
    @Override
    public JSONObject queryResults(String nextUri) {
        if (StringUtils.isBlank(nextUri)) {
            throw new KarmaRuntimeException(ExceptionEnum.PARAMS_MISSING.getCode(), "nextUri参数缺失");
        }
        return trinoClient.queryData(nextUri);
    }

    /**
     * 终止任务
     * 
     * @param nextUri
     * @return
     */
    @Override
    public JSONObject terminate(String nextUri) {
        if (StringUtils.isBlank(nextUri)) {
            throw new KarmaRuntimeException(ExceptionEnum.PARAMS_MISSING.getCode(), "partialCancelUri参数缺失");
        }
        return trinoClient.terminate(nextUri);
    }

    @Override
    public Map<String, String> generateTaskSql(Content content, String nodeId) {

        // 遍历生成节点关系
        if (CollectionUtils.isEmpty(content.getNodes())) {
            throw new KarmaRuntimeException(ExceptionEnum.PROCESS_FAILED.getCode(), "节点配置为空，无法执行");
        }
        List<TreeNode> rootNodes = LineUtil.buildTrees(content.getLines(), content.getNodes().keySet());

        if (StringUtils.isBlank(nodeId)) {
            // 不指定节点，生成所有sql

            // 查询原始sql，生成待执行sql
            List<String> dataSourceCodes =
                content.getNodes().values().stream().map(Node::getDs).collect(Collectors.toList());
            Map<String, DataSource> dataSourceMap = dataSourceDao.queryDataSourceMap(dataSourceCodes);

            List<String> checkMsg = TaskSqlUtil.check(rootNodes, content.getNodes(), dataSourceMap, content.getVars());
            if (!CollectionUtils.isEmpty(checkMsg)) {
                throw new KarmaRuntimeException(ExceptionEnum.PROCESS_FAILED.getCode(), JSONUtil.toJsonStr(checkMsg));
            }

            return TaskSqlUtil.genAllTaskSql(rootNodes, content.getNodes(), dataSourceMap, content.getVars());
        } else {
            // 指定节点，生成指定节点sql及相关依赖的sql
            Map<String, List<String>> paths = LineUtil.buildPaths(rootNodes);
            List<String> path = paths.get(nodeId);

            // 查询原始sql，生成待执行sql
            List<String> dataSourceCodes = path.stream().map(nodeCode -> {
                return content.getNodes().get(nodeCode);
            }).filter(Objects::nonNull).map(Node::getDs).collect(Collectors.toList());
            Map<String, DataSource> dataSourceMap = dataSourceDao.queryDataSourceMap(dataSourceCodes);

            List<String> checkMsg = TaskSqlUtil.checkPath(path, content.getNodes(), dataSourceMap);
            if (!CollectionUtils.isEmpty(checkMsg)) {
                throw new KarmaRuntimeException(ExceptionEnum.PROCESS_FAILED.getCode(), JSONUtil.toJsonStr(checkMsg));
            }

            return TaskSqlUtil.genTaskSql(path, content.getNodes(), dataSourceMap, content.getVars());
        }
    }

    @Override
    public List<JSONObject> genParams(Content content, String nodeId) {
        // 遍历生成节点关系
        if (CollectionUtils.isEmpty(content.getNodes())) {
            throw new KarmaRuntimeException(ExceptionEnum.PROCESS_FAILED.getCode(), "节点配置为空，无法执行");
        }
        Node node = content.getNodes().get(nodeId);
        if (null != node && StringUtils.isBlank(node.getDs())) {
            return new ArrayList<>();
        }
        List<TreeNode> rootNodes = LineUtil.buildTrees(content.getLines(), content.getNodes().keySet());

        // 指定节点，生成指定节点sql及相关依赖的sql
        Map<String, List<String>> paths = LineUtil.buildPaths(rootNodes);
        List<String> path = paths.get(nodeId);

        // 查询原始sql，生成待执行sql
        List<String> dataSourceCodes = path.stream().map(nodeCode -> {
            return content.getNodes().get(nodeCode);
        }).filter(Objects::nonNull).map(Node::getDs).collect(Collectors.toList());
        Map<String, DataSource> dataSourceMap = dataSourceDao.queryDataSourceMap(dataSourceCodes);

        return TaskSqlUtil.genParams(path, content.getNodes(), dataSourceMap);
    }

    @Override
    public List<String> check(Content content) {
        List<String> result = Lists.newArrayList();
        if (CollectionUtils.isEmpty(content.getNodes())) {
            result.add("请包涵至少一个节点");
            return result;
        }
        List<TreeNode> rootNodes = LineUtil.buildTrees(content.getLines(), content.getNodes().keySet());

        // 查询原始sql，生成待执行sql
        List<String> dataSourceCodes =
            content.getNodes().values().stream().map(Node::getDs).collect(Collectors.toList());
        Map<String, DataSource> dataSourceMap = dataSourceDao.queryDataSourceMap(dataSourceCodes);
        GlobleParamsUtil.setGlobalParams(content.getVars());
        return TaskSqlUtil.check(rootNodes, content.getNodes(), dataSourceMap, content.getVars());
    }

    @Override
    public String checkRelations(Content content, List<String> nodeIds) {
        // 遍历生成节点关系
        if (CollectionUtils.isEmpty(content.getNodes())) {
            throw new KarmaRuntimeException(ExceptionEnum.PROCESS_FAILED.getCode(), "节点配置为空，无法执行");
        }
        List<TreeNode> rootNodes = LineUtil.buildTrees(content.getLines(), content.getNodes().keySet());

        // 指定节点，生成指定节点sql及相关依赖的sql
        Map<String, List<String>> paths = LineUtil.buildPaths(rootNodes);

        if (LineUtil.checkSamePath(paths, nodeIds)) {
            return "SAME_PATH";
        }
        if (LineUtil.checkSameLevel(paths, nodeIds)) {
            return "SAME_LEVEL";
        }
        return null;
    }
}
