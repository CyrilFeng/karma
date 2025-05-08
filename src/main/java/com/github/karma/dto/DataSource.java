package com.github.karma.dto;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * <p>
 * 数据源表
 * </p>
 *
 * @author junming.ljm
 * @since 2025-01-26
 */
@Data
public class DataSource {

    /**
     * 主键
     */
    private Long id;

    /**
     * 数据源编码
     */
    private String dsCode;

    /**
     * 数据源名称
     */
    private String dsName;

    /**
     * 数据源类型
     */
    private String dsType;

    /**
     * 原子SQL
     */
    private String dsSql;

    /**
     * 变量列表
     */
    private String dsProps;

    /**
     * 字段列表
     */
    private String dsParams;

    /**
     * 权限
     */
    private String dsAuths;

    /**
     * 创建人
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

}
