package com.github.karma.trino;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.github.karma.common.ExceptionEnum;
import com.github.karma.common.KarmaRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Component
public class TrinoClient {

    @Autowired
    private TrinoProperties trinoProperties;

    private String encodeAuth;

    /**
     * 执行 sql
     * @param sql
     * @return
     */
    public JSONObject runSQL(String sql) {
        String restApiUrl = trinoProperties.getServer() + "/v1/statement";
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic " + getEncodeAuth());
        headers.put("X-Trino-Catalog", trinoProperties.getCatalog());
        try {
            String response = TrinoPoolingHttpClientUtil.post(restApiUrl, headers, sql);
            log.info("RUNSQL ===> catalog={}, schema={}, sql={}, response={}", trinoProperties.getCatalog(), trinoProperties.getSchema(), sql, response);
            return JSONUtil.parseObj(response);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new KarmaRuntimeException(ExceptionEnum.PROCESS_FAILED.getCode(), e.getMessage());
        }
    }

    /**
     * 获取sql执行结果
     * @param nextUri
     * @return
     */
    public JSONObject queryData(String nextUri) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic " + getEncodeAuth());
        try {
            String response = TrinoPoolingHttpClientUtil.get(nextUri, headers);
            log.info("QUERY DATA ===> nextUri={}, response={}", nextUri, response);
            return JSONUtil.parseObj(response);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new KarmaRuntimeException(ExceptionEnum.PROCESS_FAILED.getCode(), e.getMessage());
        }
    }

    /**
     * 终止任务
     * @param nextUri
     * @return
     */
    public JSONObject terminate(String nextUri) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic " + getEncodeAuth());
        try {
            String response = TrinoPoolingHttpClientUtil.delete(nextUri, headers);
            log.info("TERMINATE TASK ===> nextUri={}, response={}", nextUri, response);
            return JSONUtil.parseObj(response);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new KarmaRuntimeException(ExceptionEnum.PROCESS_FAILED.getCode(), e.getMessage());
        }
    }

    /**
     * 获取 auth
     * @return
     */
    private String getEncodeAuth() {
        if (StringUtils.isNotBlank(encodeAuth)) {
            return encodeAuth;
        }
        String auth = String.format("%s:%s", trinoProperties.getUsername(), trinoProperties.getPassword());
        encodeAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        return encodeAuth;
    }

}
