-- 方案B功能迁移脚本
USE document_system;

-- 文件表扩展：文件夹、智能标签、全文索引、驳回原因
ALTER TABLE `b_file`
    ADD COLUMN `folder_id` BIGINT DEFAULT NULL COMMENT '所属文件夹ID' AFTER `uploader_id`,
    ADD COLUMN `tags` VARCHAR(500) DEFAULT NULL COMMENT '智能标签（逗号分隔）' AFTER `remark`,
    ADD COLUMN `search_content` TEXT DEFAULT NULL COMMENT '可检索文本内容' AFTER `tags`,
    ADD COLUMN `audit_reject_reason` VARCHAR(500) DEFAULT NULL COMMENT '审核驳回原因' AFTER `audit_by`,
    ADD KEY `idx_folder_id` (`folder_id`),
    ADD FULLTEXT KEY `ft_search` (`name`, `tags`, `remark`, `search_content`);

-- 用户文件夹表
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

-- 操作日志表
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
