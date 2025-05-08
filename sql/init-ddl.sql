CREATE TABLE `karma_stream` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `content` mediumtext NOT NULL DEFAULT '' COMMENT '内容（json格式）',
  `create_user` varchar(60) NOT NULL DEFAULT '' COMMENT '创建人',
  `update_user` varchar(60) NOT NULL DEFAULT '' COMMENT '变更人',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='分析流'


CREATE TABLE `karma_dir` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `dir_name` varchar(50) NOT NULL DEFAULT ''  COMMENT '目录/文件名称',
  `dir_type` varchar(10) NOT NULL DEFAULT ''  COMMENT '目录类型（dir/file）',
  `parent_dir_id` bigint NOT NULL DEFAULT 0 COMMENT '父id（树型结构）',
  `create_user` varchar(60) NOT NULL DEFAULT '' COMMENT '创建人',
  `update_user` varchar(60) NOT NULL DEFAULT '' COMMENT '变更人',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='目录'


CREATE TABLE `karma_data_source` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `ds_code` varchar(50) NOT NULL DEFAULT '' COMMENT '数据源编码',
  `ds_name` varchar(50) NOT NULL DEFAULT '' COMMENT '数据源名称',
  `ds_type` varchar(20) NOT NULL DEFAULT '' COMMENT '数据源类型',
  `ds_sql` varchar(2000) NOT NULL DEFAULT '' COMMENT '原子SQL',
  `ds_props` varchar(2000) NOT NULL DEFAULT '[]' COMMENT '变量列表',
  `ds_params` varchar(2000) NOT NULL DEFAULT '[]' COMMENT '字段列表',
  `ds_auths` varchar(2000) NOT NULL DEFAULT '' COMMENT '权限',
  `create_user` varchar(60) NOT NULL DEFAULT '' COMMENT '创建人',
  `update_user` varchar(60) NOT NULL DEFAULT '' COMMENT '变更人',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ds_code` (`ds_code`)
) ENGINE=InnoDB COMMENT='数据源表';