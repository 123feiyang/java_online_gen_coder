CREATE TABLE `sys_online_gen_entity` (
                                  `id` VARCHAR(255) NOT NULL COMMENT '主键ID',
                                  `entity_name` VARCHAR(255) NOT NULL COMMENT '实体名称',
                                  `table_name` VARCHAR(255) NOT NULL COMMENT '对应数据库表名',
                                  `description` TEXT COMMENT '描述',
                                  `status` INT DEFAULT 0 COMMENT '状态: 0-草稿, 1-已发布',
                                  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='在线生成表单的表配置';


CREATE TABLE `sys_online_gen_field` (
                                        `id` VARCHAR(255) NOT NULL COMMENT '主键ID',
                                        `field_name` VARCHAR(255) NOT NULL COMMENT '字段名',
                                        `field_label` VARCHAR(255) NOT NULL COMMENT '字段标签',
                                        `field_type` VARCHAR(50) NOT NULL COMMENT '字段类型',
                                        `length` INT DEFAULT NULL COMMENT '字段长度',
                                        `nullable` TINYINT(1) DEFAULT 1 COMMENT '是否可为空: 0-否, 1-是',
                                        `primary_key` TINYINT(1) DEFAULT 0 COMMENT '是否主键: 0-否, 1-是',
                                        `in_list` TINYINT(1) DEFAULT 1 COMMENT '是否在列表中显示: 0-否, 1-是',
                                        `in_form` TINYINT(1) DEFAULT 1 COMMENT '是否在表单中显示: 0-否, 1-是',
                                        `in_search` TINYINT(1) DEFAULT 1 COMMENT '是否在搜索条件中显示: 0-否, 1-是',
                                        `entity_id` VARCHAR(255) NOT NULL COMMENT '所属实体ID',
                                        PRIMARY KEY (`id`),
                                        KEY `idx_entity_id` (`entity_id`),
                                        CONSTRAINT `fk_field_entity` FOREIGN KEY (`entity_id`) REFERENCES `sys_online_gen_entity` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='在线生成表单的字段配置';


CREATE TABLE `dynamic_config` (
    `id` VARCHAR(255) NOT NULL COMMENT '主键ID',
    `entity_id` VARCHAR(255) NOT NULL COMMENT '实体ID',
    `form_config` TEXT COMMENT '表单配置',
    `list_config` TEXT COMMENT '列表配置',
    `search_config` TEXT COMMENT '搜索配置',
    `status` INT DEFAULT 1 COMMENT '状态',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_entity_id` (`entity_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态配置表';


