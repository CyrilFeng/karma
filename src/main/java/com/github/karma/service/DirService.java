package com.github.karma.service;

import com.github.karma.dto.Dir;

import java.util.List;

/**
 * <p>
 * 目录 服务类
 * </p>
 *
 * @author junming.ljm
 * @since 2025-01-26
 */
public interface DirService {

    /**
     * 创建目录
     */
    void saveDir(Dir dir);

    /**
     * 查询目录树
     * @param account
     * @param dirId
     * @param nameLike
     * @return
     */
    List<Dir> listDir(String account, Long dirId, String nameLike);

    /**
     * 删除目录（不能删除非空目录）
     * @param id
     */
    void deleteDir(Long id);

    /**
     * 重命名
     * @param id
     * @param name
     * @return
     */
    void renameDir(Long id, String name);
}
