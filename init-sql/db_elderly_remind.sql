-- ============================================
-- 数据库: db_elderly_remind
-- 服务: elderly-remind (提醒服务, 端口 8085)
-- 表: remind, remind_task, notification
-- 跨库软引用: remind.user_id → db_elderly_auth.user.id
--             remind_task.user_id → db_elderly_auth.user.id
--             remind_task.elder_id → db_elderly_user.elder.id
--             remind_task.medicine_id → db_elderly_medicine.medicine.id
--             notification.user_id → db_elderly_auth.user.id
--             notification.elder_id → db_elderly_user.elder.id
--  本地FK保留: notification.task_id → remind_task.id (同库)
-- ============================================

CREATE DATABASE IF NOT EXISTS `db_elderly_remind`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `db_elderly_remind`;

-- ----------------------------
-- Table: remind (提醒设置表)
-- ----------------------------
DROP TABLE IF EXISTS `remind`;
CREATE TABLE `remind` (
  `id`         INT          NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `user_id`    INT          NOT NULL COMMENT '关联用户 ID (软引用 db_elderly_auth.user.id)',
  `ringtone`   VARCHAR(255) NULL     DEFAULT NULL COMMENT '铃声',
  `volume`     TINYINT      UNSIGNED NULL DEFAULT 50 COMMENT '音量',
  `quiet_time` VARCHAR(50)  NULL     DEFAULT NULL COMMENT '勿扰时间段',
  `repeat_mode` VARCHAR(50) NULL     DEFAULT NULL COMMENT '重复模式',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id` (`user_id`) USING BTREE
  -- 原外键 fk_remind_user 已移除 (跨库)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提醒设置表';

-- ----------------------------
-- Table: remind_task (提醒任务表)
-- ----------------------------
DROP TABLE IF EXISTS `remind_task`;
CREATE TABLE `remind_task` (
  `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `user_id`     INT          NOT NULL COMMENT '用户 ID (软引用 db_elderly_auth.user.id)',
  `elder_id`    INT          NOT NULL COMMENT '老人 ID (软引用 db_elderly_user.elder.id)',
  `title`       VARCHAR(100) NOT NULL COMMENT '提醒标题',
  `content`     TEXT         NULL     COMMENT '提醒内容',
  `remind_time` TIME         NOT NULL COMMENT '提醒时间',
  `remind_date` DATE         NULL     DEFAULT NULL COMMENT '提醒日期',
  `remind_type` INT          NOT NULL COMMENT '提醒类型: 1-用药, 2-体检, 3-活动, 4-其他',
  `need_voice`  TINYINT      NULL     DEFAULT 1 COMMENT '是否需要语音播报',
  `need_popup`  TINYINT      NULL     DEFAULT 1 COMMENT '是否需要弹窗提醒',
  `voice_text`  TEXT         NULL     COMMENT '语音播报文本',
  `repeat_cycle` INT         NULL     DEFAULT NULL COMMENT '重复周期: 0-不重复, 1-每天, 2-每周, 3-每月',
  `end_date`    DATE         NULL     DEFAULT NULL COMMENT '结束日期',
  `status`      INT          NULL     DEFAULT 1 COMMENT '状态: 0-停用, 1-启用',
  `remark`      VARCHAR(500) NULL     DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME     NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `medicine_id` BIGINT       NULL     DEFAULT NULL COMMENT '关联药品 ID (软引用 db_elderly_medicine.medicine.id)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id` (`user_id`) USING BTREE,
  INDEX `idx_elder_id` (`elder_id`) USING BTREE,
  INDEX `idx_remind_date` (`remind_date`) USING BTREE,
  INDEX `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提醒任务表';

-- ----------------------------
-- Table: notification (通知记录表)
-- ----------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
  `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `task_id`     INT          NULL     DEFAULT NULL COMMENT '任务 ID',
  `user_id`     INT          NOT NULL COMMENT '用户 ID (软引用 db_elderly_auth.user.id)',
  `elder_id`    INT          NOT NULL COMMENT '老人 ID (软引用 db_elderly_user.elder.id)',
  `title`       VARCHAR(100) NOT NULL COMMENT '通知标题',
  `content`     TEXT         NULL     COMMENT '通知内容',
  `notify_type` INT          NOT NULL COMMENT '通知类型: 1-用药, 2-体检, 3-活动, 4-其他',
  `send_time`   DATETIME     NOT NULL COMMENT '发送时间',
  `read_status` INT          NULL     DEFAULT 0 COMMENT '阅读状态: 0-未读, 1-已读',
  `read_time`   DATETIME     NULL     DEFAULT NULL COMMENT '阅读时间',
  `status`      INT          NULL     DEFAULT 1 COMMENT '状态: 0-无效, 1-有效',
  `create_time` DATETIME     NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id` (`user_id`) USING BTREE,
  INDEX `idx_read_status` (`read_status`) USING BTREE,
  INDEX `idx_send_time` (`send_time`) USING BTREE
  -- 原无外键，保持索引即可
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知记录表';
