-- ============================================
-- 数据库: db_elderly_admin
-- 服务: elderly-admin (后台管理服务, 端口 8088)
-- 表: system_config, disease
-- 无跨库外键 (此库完全独立)
-- ============================================

CREATE DATABASE IF NOT EXISTS `db_elderly_admin`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `db_elderly_admin`;

-- ----------------------------
-- Table: system_config (系统配置表)
-- ----------------------------
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config` (
  `id`           INT          NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `config_key`   VARCHAR(100) NOT NULL COMMENT '配置键 (如 site_name)',
  `config_value` TEXT         NULL     COMMENT '配置值',
  `description`  VARCHAR(255) NULL     DEFAULT NULL COMMENT '配置描述',
  `update_time`  DATETIME     NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `config_key` (`config_key`) USING BTREE,
  INDEX `idx_config_key` (`config_key`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- ----------------------------
-- Table: disease (疾病字典表)
-- ----------------------------
DROP TABLE IF EXISTS `disease`;
CREATE TABLE `disease` (
  `id`           INT          NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `disease_name` VARCHAR(100) NOT NULL COMMENT '疾病名称',
  `category`     VARCHAR(50)  NULL     DEFAULT NULL COMMENT '疾病分类',
  `symptoms`     TEXT         NULL     COMMENT '症状描述',
  `treatment`    TEXT         NULL     COMMENT '治疗方法',
  `prevention`   TEXT         NULL     COMMENT '预防建议',
  `status`       TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
  `create_time`  DATETIME     NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`  DATETIME     NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category` (`category`) USING BTREE,
  INDEX `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='疾病字典表';
