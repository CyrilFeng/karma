package com.github.karma.controller;

import cn.hutool.json.JSONObject;
import com.github.karma.common.Result;
import com.github.karma.controller.request.StreamExecuteRequest;
import com.github.karma.dto.Stream;
import com.github.karma.dto.content.Content;
import com.github.karma.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/analyseStream")
public class StreamController {

    @Autowired
    private StreamService streamService;

    /**
     * 保存（创建/更新）分析流
     * 
     * @param stream
     * @return
     */
    @PostMapping("/save")
    public Result<Long> saveAnalyseStream(@RequestBody Stream stream) {
        streamService.saveAnalyseStream(stream);
        return Result.success(stream.getId());
    }

    /**
     * 删除分析流
     * 
     * @param request
     * @return
     */
    @DeleteMapping("/delete")
    public Result<Void> deleteAnalyseStream(@RequestBody Stream request) {
        streamService.deleteAnalyseStream(request.getId());
        return Result.success();
    }

    /**
     * 根据id获取单个分析流
     * 
     * @param id
     * @return
     */
    @GetMapping("/get")
    public Result<Stream> getAnalyseStream(@RequestParam("id") Long id) {
        Stream data = streamService.getAnalyseStream(id);
        return Result.success(data);
    }

    /**
     * 生成任务sql
     * 
     * @param content
     * @param nodeId
     * @return
     */
    @PostMapping("/generateTaskSql")
    public Result<Map<String, String>> generateTaskSql(@RequestBody Content content,
        @RequestParam(value = "nodeId", required = false) String nodeId) {
        Map<String, String> result = streamService.generateTaskSql(content, nodeId);
        return Result.success(result);
    }

    /**
     * 执行分析流
     * 
     * @return
     */
    @PostMapping("/execute")
    public Result<Map<String, JSONObject>> executeAnalyseStream(@RequestBody StreamExecuteRequest request) {
        Map<String, JSONObject> data =
            streamService.execute(request.getContent(), request.getNodeCode(), request.getAccount());
        return Result.success(data);
    }

    /**
     * 终止任务
     * 
     * @param nextUri
     * @return
     */
    @PostMapping("/terminate")
    public Result<JSONObject> terminate(String nextUri) {
        JSONObject data = streamService.terminate(nextUri);
        return Result.success(data);
    }

    /**
     * 查询分析流结果
     * 
     * @param nextUri
     * @return
     */
    @GetMapping("/result/query")
    public Result<JSONObject> queryResults(@RequestParam("nextUri") String nextUri) {
        JSONObject data = streamService.queryResults(nextUri);
        return Result.success(data);
    }

    /**
     * 生成任务参数
     * 
     * @param content
     * @param nodeId
     * @return
     */
    @PostMapping("/genParams")
    public Result<List<JSONObject>> genParams(@RequestBody Content content, @RequestParam String nodeId) {
        List<JSONObject> result = streamService.genParams(content, nodeId);
        return Result.success(result);
    }

    @PostMapping("/check")
    public Result<List<String>> check(@RequestBody Content content) {
        List<String> result = streamService.check(content);
        return Result.success(result);
    }

    @PostMapping("/checkRelations")
    public Result<String> checkRelations(@RequestBody Content content, @RequestParam String nodeIds) {
        String result = streamService.checkRelations(content, Arrays.asList(nodeIds.split(",")));
        return Result.success(result);
    }

}
