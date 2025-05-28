package com.github.karma.service.impl;

import com.google.common.collect.Sets;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.karma.dao.DataSourceDao;
import com.github.karma.dto.DataSource;
import com.github.karma.service.DataSourceService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DataSourceServiceImpl
 *
 * @author lujunming
 * @version 2025/01/26 21:24
 **/
@Service
public class DataSourceServiceImpl implements DataSourceService {

    @Autowired
    private DataSourceDao dataSourceDao;

    @Override
    public void save(DataSource dataSource) {
        // 新增
        if (Objects.isNull(dataSource.getId())) {
            dataSource.setDsAuths(dataSource.getCreateUser());
            dataSourceDao.insert(dataSource);
        }
        // 更新
        else {
            DataSource ds = dataSourceDao.query(dataSource.getId());
            Set<String> auths = Sets.newHashSet(dataSource.getCreateUser());
            if (StringUtils.isNotBlank(ds.getDsAuths())) {
                auths.addAll(Arrays.asList(ds.getDsAuths().split(",")));
            }
            if (StringUtils.isNotBlank(dataSource.getDsAuths())) {
                auths.addAll(Arrays.asList(dataSource.getDsAuths().split(",")));
            }
            dataSource.setDsAuths(String.join(",",auths));
            dataSourceDao.update(dataSource);
        }
    }

    @Override
    public void delete(Long id){
        dataSourceDao.delete(id);
    }

    @Override
    public DataSource query(Long id){
        return dataSourceDao.query(id);
    }

    @Override
    public DataSource queryByCode(String dsCode) {
        return dataSourceDao.queryByCode(dsCode);
    }

    @Override
    public Map<String, DataSource> queryByCodes(List<String> dsCodes) {
        return dataSourceDao.queryDataSourceMap(dsCodes);
    }


    @Override
    public List<DataSource> list(String queryStr,String account){
        return dataSourceDao.list(queryStr,account);
    }
}
