package com.github.karma.controller.request;

import com.github.karma.dto.content.Content;
import lombok.Data;


@Data
public class StreamExecuteRequest {

    // 分析流
    private Content content;

    // 分析流中的节点code，可以为空，为空就是查询整个分析流
    private String nodeCode;

    // 当前用户
    private String account;

}
