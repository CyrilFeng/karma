package com.github.karma.dao;

import com.github.karma.dto.Dir;

import java.util.List;

/**
 * <p>
 * 目录
 * </p>
 *
 * @author junming.ljm
 * @since 2025-01-26
 */
public interface DirDao {

    // 返回主键ID
    Long insertDir(Dir dir);

    void updateDir(Dir dir);

    void deleteById(Long id);

    Dir getDirById(Long id);

    List<Dir> listDir(String createUser, Long parentId, String nameLike);

}
