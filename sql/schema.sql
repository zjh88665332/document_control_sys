CREATE DATABASE IF NOT EXISTS document_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE document_system;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS `b_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `salt` VARCHAR(50) DEFAULT NULL COMMENT '盐值',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `id_card` VARCHAR(18) DEFAULT NULL COMMENT '身份证号',
    `gender` TINYINT DEFAULT 0 COMMENT '性别（0：未知，1：男，2：女）',
    `birthday` DATE DEFAULT NULL COMMENT '生日',
    `education` VARCHAR(20) DEFAULT NULL COMMENT '学历',
    `phone` VARCHAR(11) NOT NULL COMMENT '手机号',
    `identity` VARCHAR(20) DEFAULT NULL COMMENT '身份',
    `role` VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT '角色（user/admin/super）',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（0：禁用，1：启用）',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `last_notice_read_time` DATETIME DEFAULT NULL COMMENT '上次阅读公告时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_role` (`role`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 文件表
CREATE TABLE IF NOT EXISTS `b_file` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `file_uuid` VARCHAR(100) NOT NULL COMMENT '文件唯一标识',
    `name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `format` VARCHAR(50) DEFAULT NULL COMMENT '文件扩展名',
    `size` BIGINT NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
    `path` VARCHAR(500) NOT NULL COMMENT '服务器存储路径',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态（0：待审核，1：通过，2：拒绝）',
    `is_audit_read` TINYINT NOT NULL DEFAULT 1 COMMENT '上传人是否已查看审核结果（0：未查看，1：已查看）',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `tags` VARCHAR(500) DEFAULT NULL COMMENT '智能标签',
    `search_content` TEXT DEFAULT NULL COMMENT '可检索文本内容',
    `uploader_id` BIGINT NOT NULL COMMENT '上传人ID',
    `folder_id` BIGINT DEFAULT NULL COMMENT '所属文件夹ID',
    `upload_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `audit_by` BIGINT DEFAULT NULL COMMENT '审核人ID',
    `audit_reject_reason` VARCHAR(500) DEFAULT NULL COMMENT '审核驳回原因',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已删除（0：未删除，1：已删除）',
    `delete_time` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_file_uuid` (`file_uuid`),
    KEY `idx_uploader_id` (`uploader_id`),
    KEY `idx_status` (`status`),
    KEY `idx_upload_time` (`upload_time`),
    KEY `idx_folder_id` (`folder_id`),
    FULLTEXT KEY `ft_search` (`name`, `tags`, `remark`, `search_content`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件表';

-- 7. 用户文件夹表
CREATE TABLE IF NOT EXISTS `b_folder` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
    `name` VARCHAR(100) NOT NULL COMMENT '文件夹名称',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父文件夹ID（0为根）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户文件夹表';

-- 8. 操作日志表
CREATE TABLE IF NOT EXISTS `b_operation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
    `username` VARCHAR(50) DEFAULT NULL COMMENT '操作人用户名',
    `module` VARCHAR(50) NOT NULL COMMENT '模块',
    `action` VARCHAR(50) NOT NULL COMMENT '操作',
    `target_id` BIGINT DEFAULT NULL COMMENT '目标ID',
    `target_name` VARCHAR(255) DEFAULT NULL COMMENT '目标名称',
    `detail` VARCHAR(500) DEFAULT NULL COMMENT '详情',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_module` (`module`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 3. 好友关系表
CREATE TABLE IF NOT EXISTS `b_friend` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '发起人ID',
    `friend_id` BIGINT NOT NULL COMMENT '目标人ID',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态（0：待验证，1：通过，2：拒绝）',
    `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '接收方是否已查看（0：未查看，1：已查看）',
    `apply_message` VARCHAR(255) DEFAULT NULL COMMENT '申请验证信息',
    `reply_message` VARCHAR(255) DEFAULT NULL COMMENT '回复信息',
    `apply_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
    `is_del` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已删除（0：否，1：是）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_friend` (`user_id`, `friend_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_friend_id` (`friend_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友关系表';

-- 4. 分享记录表
CREATE TABLE IF NOT EXISTS `b_share` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `file_id` BIGINT NOT NULL COMMENT '文件ID',
    `sharer_id` BIGINT NOT NULL COMMENT '分享人ID',
    `receiver_id` BIGINT NOT NULL COMMENT '接收人ID',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '分享备注',
    `share_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分享时间',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（0：失效，1：有效）',
    `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已查看（0：未查看，1：已查看）',
    PRIMARY KEY (`id`),
    KEY `idx_file_id` (`file_id`),
    KEY `idx_sharer_id` (`sharer_id`),
    KEY `idx_receiver_id` (`receiver_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分享记录表';

-- 5. 公告表
CREATE TABLE IF NOT EXISTS `b_notice` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title` VARCHAR(200) NOT NULL COMMENT '公告主题',
    `content` TEXT NOT NULL COMMENT '公告内容',
    `publisher_id` BIGINT NOT NULL COMMENT '发布人ID',
    `publisher_name` VARCHAR(50) DEFAULT NULL COMMENT '发布人名称',
    `is_top` TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶（0：否，1：是）',
    `view_count` INT NOT NULL DEFAULT 0 COMMENT '查看次数',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（0：草稿，1：已发布）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `publish_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    PRIMARY KEY (`id`),
    KEY `idx_publisher_id` (`publisher_id`),
    KEY `idx_is_top` (`is_top`),
    KEY `idx_status` (`status`),
    KEY `idx_publish_time` (`publish_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- 6. 反馈表
CREATE TABLE IF NOT EXISTS `b_feedback` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `subject` VARCHAR(200) NOT NULL COMMENT '反馈主题',
    `content` TEXT NOT NULL COMMENT '反馈内容',
    `submitter_id` BIGINT NOT NULL COMMENT '提交人ID',
    `submitter_name` VARCHAR(50) DEFAULT NULL COMMENT '提交人名称',
    `submit_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    `reply` TEXT DEFAULT NULL COMMENT '管理员回复内容',
    `reply_by` BIGINT DEFAULT NULL COMMENT '回复人ID',
    `reply_time` DATETIME DEFAULT NULL COMMENT '回复时间',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态（0：待回复，1：已回复）',
    `is_reply_read` TINYINT NOT NULL DEFAULT 1 COMMENT '提交人是否已查看回复（0：未查看，1：已查看）',
    PRIMARY KEY (`id`),
    KEY `idx_submitter_id` (`submitter_id`),
    KEY `idx_status` (`status`),
    KEY `idx_submit_time` (`submit_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='反馈表';
