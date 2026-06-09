USE document_system;

ALTER TABLE `b_share` ADD COLUMN `remark` VARCHAR(255) DEFAULT NULL COMMENT '分享备注' AFTER `receiver_id`;
ALTER TABLE `b_friend` ADD COLUMN `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '接收方是否已查看（0：未查看，1：已查看）' AFTER `status`;
ALTER TABLE `b_feedback` ADD COLUMN `is_reply_read` TINYINT NOT NULL DEFAULT 1 COMMENT '提交人是否已查看回复（0：未查看，1：已查看）' AFTER `status`;
ALTER TABLE `b_file` ADD COLUMN `is_audit_read` TINYINT NOT NULL DEFAULT 1 COMMENT '上传人是否已查看审核结果（0：未查看，1：已查看）' AFTER `status`;
ALTER TABLE `b_user` ADD COLUMN `last_notice_read_time` DATETIME DEFAULT NULL COMMENT '上次阅读公告时间' AFTER `avatar`;
