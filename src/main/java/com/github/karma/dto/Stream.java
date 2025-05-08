package com.github.karma.dto;

import com.github.karma.dto.content.Content;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Stream {

    private Long id;

    // 所在目录的id
    private Long dirId;

    private String name;

    private Content content;

    private String createUser;

    private String updateUser;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
