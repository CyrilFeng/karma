package com.github.karma.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.github.karma.mapper.entity.DataSourceDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 数据源表 Mapper 接口
 * </p>
 *
 * @author junming.ljm
 * @since 2025-01-26
 */
@Mapper
public interface DataSourceMapper extends BaseMapper<DataSourceDO> {

}
