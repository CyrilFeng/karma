package com.github.karma.service;

import com.github.karma.dto.DataSource;

import java.util.List;
import java.util.Map;

/**
 * DataSourceService
 *
 * @author lujunming
 * @version 2025/02/03 15:17
 **/
public interface DataSourceService {
    /**
     * 保存
     *
     * @param dataSource
     */
    void save(DataSource dataSource);

    /**
     * 删除
     *
     * @param id
     */
    void delete(Long id);

    /**
     * 查询
     */
    DataSource query(Long id);

    /**
     * 根据code查询
     * @param dsCode
     * @return
     */
    DataSource queryByCode(String dsCode);

    /**
     * 查询列表
     *
     * @return
     */
    List<DataSource> list(String queryStr,String account);

    Map<String, DataSource> queryByCodes(List<String> dsCodes);
}
