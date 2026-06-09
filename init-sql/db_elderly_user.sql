-- ============================================
-- 数据库: db_elderly_user
-- 服务: elderly-user (用户服务, 端口 8082)
-- 表: elder, family
-- 跨库软引用: elder.user_id → db_elderly_auth.user.id
--             family.family_user_id → db_elderly_auth.user.id
-- ============================================

CREATE DATABASE IF NOT EXISTS `db_elderly_user`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `db_elderly_user`;

-- ----------------------------
-- Table: elder (老人信息表)
-- ----------------------------
CREATE TABLE IF NOT EXISTS `elder` (
  `id`                INT          NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `user_id`           INT          NOT NULL COMMENT '关联用户 ID (软引用 db_elderly_auth.user.id)',
  `medical_history`   TEXT         NULL     COMMENT '既往病史',
  `allergic_drugs`    VARCHAR(255) NULL     DEFAULT NULL COMMENT '过敏药物',
  `emergency_contact` VARCHAR(100) NULL     DEFAULT NULL COMMENT '紧急联系人电话',
  `health_file`       VARCHAR(255) NULL     DEFAULT NULL COMMENT '健康档案文件路径',
  `qr_code_token`     VARCHAR(255) NULL     DEFAULT NULL COMMENT '老人专属二维码',
  `create_time`       DATETIME     NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`       DATETIME     NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id` (`user_id`) USING BTREE COMMENT '确保一个用户只有一个健康档案'
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='老人信息表';

-- ----------------------------
-- Table: family (家属绑定表)
-- ----------------------------
CREATE TABLE IF NOT EXISTS `family` (
  `id`             INT         NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `family_user_id` INT         NOT NULL COMMENT '家属用户 ID (软引用 db_elderly_auth.user.id)',
  `elder_id`       INT         NOT NULL COMMENT '老人档案 ID',
  `relation`       VARCHAR(20) NOT NULL DEFAULT '亲属' COMMENT '关系',
  `phone`          VARCHAR(20) NOT NULL COMMENT '联系方式',
  `bind_status`    TINYINT     NOT NULL DEFAULT 1 COMMENT '状态: 1-已绑定, 0-解绑',
  `bind_time`      DATETIME    NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_elder` (`family_user_id`, `elder_id`) USING BTREE,
  INDEX `fk_family_elder` (`elder_id`) USING BTREE,
  -- 本地外键保留 (elder 在同一库)
  CONSTRAINT `fk_family_elder` FOREIGN KEY (`elder_id`) REFERENCES `elder` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
  -- 原外键 fk_family_user 已移除 (跨库)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='家属绑定表';
