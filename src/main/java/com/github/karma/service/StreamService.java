package com.github.karma.service;

import cn.hutool.json.JSONObject;
import com.github.karma.dto.Stream;
import com.github.karma.dto.content.Content;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

public interface StreamService {

    /**
     * 保存（创建/更新）分析流
     * @param stream
     */
    void saveAnalyseStream(Stream stream);

    /**
     * 删除分析流
     * @param streamId
     */
    void deleteAnalyseStream(Long streamId);

    /**
     * 根据id获取单个分析流
     * @param streamId
     * @return
     */
    Stream getAnalyseStream(Long streamId);

    /**
     * 生成任务sql
     * @param content
     * @param nodeId
     * @return
     */
    Map<String,String> generateTaskSql(Content content, String nodeId);

    /**
     * 执行分析流
     * @param content
     * @param nodeCode
     * @param account
     * @return
     */
    Map<String, JSONObject> execute(Content content, String nodeCode, String account);

    /**
     * 获取分析流结果
     * @param nextUri
     * @return
     */
    JSONObject queryResults(String nextUri);

    /**
     * 终止任务
     * @param nextUri
     * @return
     */
    JSONObject terminate(String nextUri);

    /**
     * 生成参数
     *
     * @param content
     * @param nodeId
     * @return
     */
    List<JSONObject> genParams(Content content,String nodeId);


    /**
     * 检查
     *
     * @param content
     * @return
     */
    List<String> check(Content content);

    /**
     * 检查节点关系
     *
     * @param content
     * @param nodeIds
     * @return
     */
    String checkRelations(@RequestBody Content content, @RequestParam List<String> nodeIds);
}
