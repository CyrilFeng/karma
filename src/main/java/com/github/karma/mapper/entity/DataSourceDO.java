package com.github.karma.mapper.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * 数据源表
 * </p>
 *
 * @author junming.ljm
 * @since 2025-01-26
 */
@Data
@TableName("karma_data_source")
public class DataSourceDO {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String dsCode;

    private String dsName;

    private String dsType;

    private String dsSql;

    private String dsAuths;

    private String createUser;

    private LocalDateTime createTime;

    private String updateUser;

    private LocalDateTime updateTime;

    private String dsProps;

    private String dsParams;

}
