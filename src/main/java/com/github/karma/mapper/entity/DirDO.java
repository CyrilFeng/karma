package com.github.karma.mapper.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * 目录
 * </p>
 *
 * @author junming.ljm
 * @since 2025-01-26
 */
@Data
@TableName("karma_dir")
public class DirDO  {

    @TableId(value = "id" ,type = IdType.AUTO)
    private Long id;

    private String dirName;

    private String dirType;

    private Long parentDirId;

    private String createUser;

    private LocalDateTime createTime;

    private String updateUser;

    private LocalDateTime updateTime;

}
