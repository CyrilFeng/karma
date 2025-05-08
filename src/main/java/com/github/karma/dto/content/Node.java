package com.github.karma.dto.content;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import lombok.Data;

@Data
public class Node {
    /**
     * eg.
     * 用户分析节点1
     */
    private String name;

    /**
     * eg.
     * user
     */
    private String type;

    private String desc;
    private String relation;
    private String ds;
    @JsonProperty("ds_cn_name")
    private String dsCnName;
    private String top;
    private String left;

    /**
     * eg.
     * {"bizDate":"20250101"},
     */
    private Map<String,String> props;

    /**
     * eg.
     * {"x":"sum"}
     */
    private Map<String,String> aggrs;
}
