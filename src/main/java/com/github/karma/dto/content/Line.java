package com.github.karma.dto.content;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Line {

    private String from;

    private String to;

    private List<String> anchor;

    private Integer virtual;
}
