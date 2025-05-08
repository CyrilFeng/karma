package com.github.karma.service.impl;

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
            // ds的auth字段如果为空，直接把createUser添加进去，不为空做走下面逻辑
            if (!StringUtils.isBlank(ds.getDsAuths())) {
                // auth字段不为空，按逗号分隔，如果包含createUser什么也不做，不包含则追加到auth字段的后面
                if (!Arrays.stream(ds.getDsAuths().split(",")).collect(Collectors.toSet()).contains(dataSource.getCreateUser())) {
                    dataSource.setDsAuths(ds.getDsAuths() + "," + dataSource.getCreateUser());
                }
            } else {
                dataSource.setDsAuths(dataSource.getCreateUser());
            }
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
