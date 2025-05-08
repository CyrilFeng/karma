package com.github.karma.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class TreeNode {
    private String id;
    private List<TreeNode> children;

    public TreeNode(String id){
        this.id = id;
        this.children = new ArrayList<>();
    }
}
