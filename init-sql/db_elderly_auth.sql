-- ============================================
-- 数据库: db_elderly_auth
-- 服务: elderly-auth (认证授权服务, 端口 8081)
-- 表: user (用户账户)
-- ============================================

CREATE DATABASE IF NOT EXISTS `db_elderly_auth`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `db_elderly_auth`;

-- ----------------------------
-- Table: user (用户表)
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `name`        VARCHAR(50)  NULL     DEFAULT NULL COMMENT '姓名',
  `sex`         VARCHAR(10)  NOT NULL COMMENT '性别',
  `age`         INT          NOT NULL COMMENT '年龄',
  `phone`       VARCHAR(20)  NOT NULL COMMENT '手机号',
  `password`    VARCHAR(255) NOT NULL COMMENT '密码 (BCrypt)',
  `avatar`      VARCHAR(255) NULL     DEFAULT NULL COMMENT '头像 URL',
  `user_type`   TINYINT      NOT NULL DEFAULT 0  COMMENT '角色: 0-老人, 1-家属, 2-管理员',
  `status`      TINYINT      NOT NULL DEFAULT 1  COMMENT '状态: 1-正常, 0-禁用',
  `create_time` DATETIME     NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_phone` (`phone`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
