package com.github.karma.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 目录
 * </p>
 *
 * @author junming.ljm
 * @since 2025-01-26
 */
@Data
public class Dir {

    /**
     * 主键
     */
    private Long id;

    /**
     * 目录名称
     */
    private String name;

    /**
     * 目录类型
     */
    private String dirType;

    /**
     * true是文件夹 false是文件
     */
    @JsonProperty("isParent")
    private boolean isParent;

    /**
     * 父目录
     */
    private Long pId;

    /**
     * 归属人
     */
    private String createUser;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改人
     */
    private String updateUser;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;


    private List<Dir> children;

}
