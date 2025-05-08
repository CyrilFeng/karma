package com.github.karma.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.karma.dao.DirDao;
import com.github.karma.dto.Dir;
import com.github.karma.mapper.DirMapper;
import com.github.karma.mapper.entity.DirDO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 目录
 * </p>
 *
 * @author junming.ljm
 * @since 2025-01-26
 */
@Service
public class DirDaoImpl implements DirDao {

    @Autowired
    private DirMapper dirMapper;

    @Override
    public Long insertDir(Dir dir) {
        DirDO dirDO = convert(dir);
        dirMapper.insert(dirDO);
        return dirDO.getId();
    }

    @Override
    public void updateDir(Dir dir) {
        DirDO dirDO = convert(dir);
        dirMapper.updateById(dirDO);
    }

    @Override
    public void deleteById(Long id) {
        dirMapper.deleteById(id);
    }

    @Override
    public Dir getDirById(Long id) {
        DirDO dirDO = dirMapper.selectById(id);
        if (dirDO == null) {
            return null;
        }
        return convert(dirDO);
    }

    @Override
    public List<Dir> listDir(String createUser, Long parentId, String nameLike) {
        List<DirDO> dirDOS = dirMapper.selectList(new LambdaQueryWrapper<DirDO>()
                .eq(StringUtils.isNotBlank(createUser), DirDO::getCreateUser, createUser)
                .eq(DirDO::getParentDirId, parentId)
                .like(StringUtils.isNotBlank(nameLike), DirDO::getDirName, nameLike));

        if (CollectionUtils.isEmpty(dirDOS)) {
            return null;
        }
        return dirDOS.stream().map(this::convert).collect(Collectors.toList());
    }


    private DirDO convert(Dir dir) {
        DirDO dirDO = new DirDO();
        dirDO.setId(dir.getId());
        dirDO.setDirName(dir.getName());
        dirDO.setDirType(dir.getDirType());
        dirDO.setParentDirId(dir.getPId());
        dirDO.setCreateUser(dir.getCreateUser());
        dirDO.setCreateTime(dir.getCreateTime());
        dirDO.setUpdateUser(dir.getUpdateUser());
        dirDO.setUpdateTime(dir.getUpdateTime());
        return dirDO;
    }

    private Dir convert(DirDO dirDO) {
        Dir dir = new Dir();
        dir.setId(dirDO.getId());
        dir.setName(dirDO.getDirName());
        dir.setDirType(dirDO.getDirType());
        dir.setParent("dir".equals(dirDO.getDirType()));
        dir.setPId(dirDO.getParentDirId());
        dir.setCreateUser(dirDO.getCreateUser());
        dir.setCreateTime(dirDO.getCreateTime());
        dir.setUpdateUser(dirDO.getUpdateUser());
        dir.setUpdateTime(dirDO.getUpdateTime());
        return dir;
    }
}
