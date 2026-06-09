-- ============================================
-- 数据库: db_elderly_news
-- 服务: elderly-news (新闻服务, 端口 8086)
-- 表: news, news_collect, news_like
-- 跨库软引用: news.creator_id → db_elderly_auth.user.id
--             news_collect.user_id → db_elderly_auth.user.id
--             news_like.user_id → db_elderly_auth.user.id
-- 本地FK保留:  news_collect.news_id → news.id
--             news_like.news_id → news.id
-- ============================================

CREATE DATABASE IF NOT EXISTS `db_elderly_news`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `db_elderly_news`;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table: news (资讯表)
-- ----------------------------
DROP TABLE IF EXISTS `news`;
CREATE TABLE `news` (
  `id`             INT          NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `title`          VARCHAR(255) NOT NULL COMMENT '标题',
  `content`        TEXT         NULL     COMMENT '内容',
  `summary`        VARCHAR(500) NULL     DEFAULT NULL COMMENT '摘要',
  `cover_image`    VARCHAR(500) NULL     DEFAULT NULL COMMENT '封面图片 URL',
  `category`       VARCHAR(50)  NULL     DEFAULT NULL COMMENT '分类',
  `view_count`     INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '阅读量',
  `like_count`     INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '点赞数',
  `collect_count`  INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '收藏数',
  `is_recommended` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否推荐: 1-是, 0-否',
  `creator_id`     INT          NULL     DEFAULT NULL COMMENT '创建者 ID (软引用 db_elderly_auth.user.id)',
  `publish_time`   DATETIME     NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `status`         TINYINT      NOT NULL DEFAULT 1 COMMENT '状态',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category` (`category`) USING BTREE,
  INDEX `idx_is_recommended` (`is_recommended`) USING BTREE,
  INDEX `idx_creator` (`creator_id`) USING BTREE,
  INDEX `idx_status` (`status`) USING BTREE,
  FULLTEXT INDEX `ft_title_content` (`title`, `content`) WITH PARSER ngram
  -- 原外键 fk_news_creator 已移除 (跨库)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资讯表';

-- ----------------------------
-- Table: news_collect (资讯收藏表)
-- ----------------------------
DROP TABLE IF EXISTS `news_collect`;
CREATE TABLE `news_collect` (
  `id`          INT      NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `user_id`     INT      NOT NULL COMMENT '用户 ID (软引用 db_elderly_auth.user.id)',
  `news_id`     INT      NOT NULL COMMENT '资讯 ID',
  `create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_news` (`user_id`, `news_id`) USING BTREE COMMENT '防止重复收藏',
  INDEX `idx_user_id` (`user_id`) USING BTREE,
  INDEX `idx_news_id` (`news_id`) USING BTREE,
  -- 本地外键保留
  CONSTRAINT `fk_collect_news` FOREIGN KEY (`news_id`) REFERENCES `news` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
  -- 原外键 fk_collect_user 已移除 (跨库)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资讯收藏表';

-- ----------------------------
-- Table: news_like (资讯点赞表)
-- ----------------------------
DROP TABLE IF EXISTS `news_like`;
CREATE TABLE `news_like` (
  `id`          INT      NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `user_id`     INT      NOT NULL COMMENT '用户 ID (软引用 db_elderly_auth.user.id)',
  `news_id`     INT      NOT NULL COMMENT '资讯 ID',
  `create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_news_like` (`user_id`, `news_id`) USING BTREE COMMENT '防止重复点赞',
  INDEX `idx_user_id` (`user_id`) USING BTREE,
  INDEX `idx_news_id` (`news_id`) USING BTREE,
  -- 本地外键保留
  CONSTRAINT `fk_like_news` FOREIGN KEY (`news_id`) REFERENCES `news` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
  -- 原外键 fk_like_user 已移除 (跨库)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资讯点赞表';
