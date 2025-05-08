package com.github.karma.dto.content;

import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * Content
 *
 * @author lujunming
 * @version 2025/02/03 15:43
 **/
@Data
public class Content {

    /**
     * 节点
     */
    private Map<String ,Node> nodes;

    /**
     * 连线
     */
    private List<Line> lines;

    /**
     * 全局变量
     */
    private Map<String,String> vars;
}
