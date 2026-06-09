-- ============================================
-- 数据库: db_elderly_health
-- 服务: elderly-health (健康服务, 端口 8083)
-- 表: health
-- 跨库软引用: health.elder_id → db_elderly_user.elder.id
-- ============================================

CREATE DATABASE IF NOT EXISTS `db_elderly_health`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `db_elderly_health`;

-- ----------------------------
-- Table: health (健康数据表)
-- ----------------------------
CREATE TABLE IF NOT EXISTS `health` (
  `id`             INT           NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `elder_id`       INT           NOT NULL COMMENT '关联老人 ID (软引用 db_elderly_user.elder.id)',
  `blood_pressure` VARCHAR(20)   NULL     DEFAULT NULL COMMENT '血压 (如 120/80)',
  `blood_sugar`    DECIMAL(5,2)  NULL     DEFAULT NULL COMMENT '血糖',
  `heart_rate`     SMALLINT      NULL     DEFAULT NULL COMMENT '心率',
  `weight`         DECIMAL(5,2)  NULL     DEFAULT NULL COMMENT '体重',
  `warning_flag`   TINYINT       NOT NULL DEFAULT 0 COMMENT '是否异常: 0-正常, 1-异常',
  `is_read`        TINYINT       NOT NULL DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
  `record_time`    DATETIME      NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
  `create_time`    DATETIME      NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`    DATETIME      NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_elder_time` (`elder_id`, `record_time` DESC) USING BTREE,
  INDEX `idx_warning_flag` (`warning_flag`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='健康数据表';
