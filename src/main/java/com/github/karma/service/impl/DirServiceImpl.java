package com.github.karma.service.impl;

import com.github.karma.common.KarmaRuntimeException;
import com.github.karma.dao.StreamDao;
import com.github.karma.dao.DirDao;
import com.github.karma.dto.Dir;
import com.github.karma.service.DirService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 目录 服务实现类
 * </p>
 *
 * @author junming.ljm
 * @since 2025-01-26
 */
@Service
public class DirServiceImpl implements DirService {

    @Autowired
    private DirDao dirDao;
    @Autowired
    private StreamDao streamDao;

    /**
     * 创建目录
     * @param dir
     */
    @Override
    public void saveDir(Dir dir) {
        dir.setDirType("dir");
        dir.setPId(dir.getPId() == null ? 0 : dir.getPId());
        dir.setCreateTime(LocalDateTime.now());
        dirDao.insertDir(dir);
    }

    /**
     * 查询目录树
     * @param account
     * @param dirId
     * @param nameLike
     * @return
     */
    @Override
    public List<Dir> listDir(String account, Long dirId, String nameLike) {
        if (null == dirId) dirId = 0L;
        List<Dir> rootDirs = dirDao.listDir(account, dirId, nameLike);
        if (CollectionUtils.isEmpty(rootDirs)) {
            return null;
        }
        return rootDirs.stream().map(dir -> buildTree(dir, account, nameLike)).collect(Collectors.toList());
    }

    // 构建目录树
    private Dir buildTree(Dir root, String account, String nameLike) {
        if ("dir".equals(root.getDirType())) {
            List<Dir> dirs = dirDao.listDir(account, root.getId(), nameLike);
            if (!CollectionUtils.isEmpty(dirs)) {
                root.setChildren(dirs.stream().map(d -> buildTree(d, account, nameLike)).collect(Collectors.toList()));
            }
        }
        return root;
    }


    /**
     * 删除目录（不能删除非空目录）
     * @param id
     */
    @Override
    public void deleteDir(Long id) {
        Dir rootDir = dirDao.getDirById(id);
//        loopDeleteByParent(rootDir);

        // 文件直接删除
        if ("file".equals(rootDir.getDirType())) {
            dirDao.deleteById(rootDir.getId());
            streamDao.deleteSteam(rootDir.getId());
            return;
        }

        // 目录需要查看是否有子文件或者子目录
        List<Dir> sonDirs = dirDao.listDir(null, rootDir.getId(), null);
        if (CollectionUtils.isEmpty(sonDirs)) {
            dirDao.deleteById(rootDir.getId());
            return;
        }
        throw new KarmaRuntimeException(-99, "当前目录下存在子目录或文件，不可直接删除");
    }

    // 循环遍历删除
    private void loopDeleteByParent(Dir rootDir) {
        if ("dir".equals(rootDir.getDirType())) {
            List<Dir> sonDirs = dirDao.listDir(null, rootDir.getId(), null);
            if (!CollectionUtils.isEmpty(sonDirs)) {
                sonDirs.forEach(this::loopDeleteByParent);
            }
        }
        dirDao.deleteById(rootDir.getId());
    }

    /**
     * 重命名
     * @param id
     * @param name
     */
    public void renameDir(Long id, String name) {
        Dir dir = dirDao.getDirById(id);
        if (dir == null) {
            return;
        }
        dir.setName(name);
        dirDao.updateDir(dir);
    }

}
