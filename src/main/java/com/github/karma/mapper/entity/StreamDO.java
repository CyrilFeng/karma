package com.github.karma.mapper.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@TableName(value = "karma_stream")
public class StreamDO {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String content;

    private String createUser;

    private String updateUser;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
