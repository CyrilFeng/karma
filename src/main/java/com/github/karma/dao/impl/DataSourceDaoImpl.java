package com.github.karma.dao.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.karma.dao.DataSourceDao;
import com.github.karma.dto.DataSource;
import com.github.karma.mapper.DataSourceMapper;
import com.github.karma.mapper.entity.DataSourceDO;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DataSourceDaoImpl
 *
 * @author lujunming
 * @version 2025/01/26 21:12
 **/
@Service
public class DataSourceDaoImpl implements DataSourceDao {

    @Autowired
    private DataSourceMapper dataSourceMapper;

    @Override
    public void insert(DataSource model) {
        DataSourceDO data = convert(model);
        dataSourceMapper.insert(data);
    }

    @Override
    public void update(DataSource model) {
        DataSourceDO data = convert(model);
        dataSourceMapper.updateById(data);
    }

    @Override
    public void delete(Long id) {
        dataSourceMapper.deleteById(id);
    }

    @Override
    public DataSource query(Long id) {
        DataSourceDO data = dataSourceMapper.selectById(id);
        if (null == data) {
            return null;
        }
        return convert(data);
    }

    @Override
    public DataSource queryByCode(String dsCode) {
        DataSourceDO data = dataSourceMapper.selectOne(new QueryWrapper<DataSourceDO>().eq("ds_code", dsCode));
        if (null == data) {
            return null;
        }
        return convert(data);
    }

    @Override
    public List<DataSource> list(String queryStr, String userName) {
        QueryWrapper<DataSourceDO> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(userName)) {
            wrapper.like("ds_auths", userName);
        }
        if (StringUtils.isNotBlank(queryStr)) {
            wrapper.like("ds_name", queryStr).or().like("ds_code",queryStr);
        }
        List<DataSourceDO> dataList = dataSourceMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(dataList)) {
            return null;
        }
        return dataList.stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public Map<String, DataSource> queryDataSourceMap(List<String> dsCodes) {
        if (CollectionUtils.isEmpty(dsCodes)){
            return null;
        }
        QueryWrapper<DataSourceDO> wrapper = new QueryWrapper<>();
        wrapper.in("ds_code", dsCodes);
        List<DataSourceDO> dataList = dataSourceMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(dataList)) {
            return null;
        }
        Map<String, DataSource> result = dataList.stream().map(this::convert).collect(Collectors.toMap(DataSource::getDsCode, dataSource -> dataSource));
        return result;
    }

    /**
     * DTO2DO
     *
     * @param model
     * @return
     */
    private DataSourceDO convert(DataSource model) {
        DataSourceDO data = new DataSourceDO();
        BeanUtils.copyProperties(model, data);
        return data;
    }

    /**
     * DO2DTO
     *
     * @param data
     * @return
     */
    private DataSource convert(DataSourceDO data) {
        DataSource model = new DataSource();
        BeanUtils.copyProperties(data, model);
        return model;
    }
}
