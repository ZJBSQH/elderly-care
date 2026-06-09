-- ============================================
-- 数据库: db_elderly_medicine
-- 服务: elderly-medicine (用药服务, 端口 8084)
-- 表: medicine, record
-- 跨库软引用: medicine.elder_id → db_elderly_user.elder.id
-- 本地外键:  record.task_id → medicine.id (同库保留)
-- ============================================

CREATE DATABASE IF NOT EXISTS `db_elderly_medicine`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `db_elderly_medicine`;

-- ----------------------------
-- Table: medicine (用药计划表)
-- ----------------------------
CREATE TABLE IF NOT EXISTS `medicine` (
  `id`            INT          NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `elder_id`      INT          NOT NULL COMMENT '关联老人 ID (软引用 db_elderly_user.elder.id)',
  `medicine_name` VARCHAR(100) NOT NULL COMMENT '药品名称',
  `dosage`        VARCHAR(50)  NULL     DEFAULT NULL COMMENT '单次剂量',
  `remind_time`   TIME         NULL     DEFAULT NULL COMMENT '提醒时间点',
  `frequency`     VARCHAR(50)  NULL     DEFAULT NULL COMMENT '频次',
  `start_date`    DATE         NOT NULL COMMENT '计划开始日期',
  `end_date`      DATE         NOT NULL COMMENT '计划结束日期',
  `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 1-进行中, 0-停用',
  `create_time`   DATETIME     NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME     NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_public`     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否公共药品: 0-个人, 1-公共',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_elder_id` (`elder_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用药计划表';

-- ----------------------------
-- Table: record (服药记录表)
-- ----------------------------
CREATE TABLE IF NOT EXISTS `record` (
  `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `task_id`     INT          NOT NULL COMMENT '关联用药计划 ID',
  `elder_id`    INT          NOT NULL COMMENT '冗余字段: 老人 ID (软引用 db_elderly_user.elder.id)',
  `remind_date` DATE         NOT NULL COMMENT '计划服药日期',
  `record_time` DATETIME     NULL     DEFAULT NULL COMMENT '实际服药时间',
  `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态: 0-待服, 1-已服, 2-漏服, 3-跳过',
  `remark`      VARCHAR(255) NULL     DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_plan_date` (`remind_date`) USING BTREE,
  INDEX `idx_elder_id` (`elder_id`) USING BTREE,
  INDEX `fk_record_task` (`task_id`) USING BTREE,
  -- 本地外键保留 (medicine 在同一库)
  CONSTRAINT `fk_record_task` FOREIGN KEY (`task_id`) REFERENCES `medicine` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服药记录表';
