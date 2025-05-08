package com.github.karma.dao;

import com.github.karma.dto.DataSource;

import java.util.List;
import java.util.Map;

/**
 * DataSourceDao
 *
 * @author lujunming
 * @version 2025/01/26 21:23
 **/
public interface DataSourceDao {
    void insert(DataSource model);

    void update(DataSource model);

    void delete(Long id);

    DataSource query(Long id);

    DataSource queryByCode(String dsCode);

    List<DataSource> list(String dsNameLike,String userName);

    Map<String ,DataSource> queryDataSourceMap(List<String> dsCodes);
}
