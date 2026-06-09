-- ============================================
-- 老年护理云平台 — 完整建库 + 测试数据脚本
-- 密码均为: 123456
-- 执行: docker-compose exec mysql mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" < init-sql/all.sql
-- ============================================
SET FOREIGN_KEY_CHECKS = 0;

-- ==================== db_elderly_auth ====================
CREATE DATABASE IF NOT EXISTS `db_elderly_auth` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `db_elderly_auth`;
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
  `update_time` DATETIME     NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_phone` (`phone`) USING BTREE,
  INDEX `idx_user_type` (`user_type`) USING BTREE,
  INDEX `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

DELETE FROM `user`;
INSERT INTO `user` (`id`, `name`, `sex`, `age`, `phone`, `password`, `user_type`, `status`) VALUES
(1, '张三',   '男', 72, '13800000001', '$2a$10$jicyr1PtKMT29WI7XI/QOeT5yw7Lz0NyBg24uIgJ9tvlMAQ2qEMsa', 0, 1),
(2, '李四',   '男', 68, '13800000002', '$2a$10$jicyr1PtKMT29WI7XI/QOeT5yw7Lz0NyBg24uIgJ9tvlMAQ2qEMsa', 0, 1),
(3, '王五',   '男', 75, '13800000003', '$2a$10$jicyr1PtKMT29WI7XI/QOeT5yw7Lz0NyBg24uIgJ9tvlMAQ2qEMsa', 0, 1),
(4, '赵六',   '女', 70, '13800000004', '$2a$10$jicyr1PtKMT29WI7XI/QOeT5yw7Lz0NyBg24uIgJ9tvlMAQ2qEMsa', 0, 1),
(5, '帐小三', '男', 42, '13800000005', '$2a$10$jicyr1PtKMT29WI7XI/QOeT5yw7Lz0NyBg24uIgJ9tvlMAQ2qEMsa', 1, 1),
(6, '李筱思', '女', 38, '13800000006', '$2a$10$jicyr1PtKMT29WI7XI/QOeT5yw7Lz0NyBg24uIgJ9tvlMAQ2qEMsa', 1, 1),
(7, '王舞',   '女', 45, '13800000007', '$2a$10$jicyr1PtKMT29WI7XI/QOeT5yw7Lz0NyBg24uIgJ9tvlMAQ2qEMsa', 1, 1),
(8, '管理员', '男', 35, '13800000008', '$2a$10$jicyr1PtKMT29WI7XI/QOeT5yw7Lz0NyBg24uIgJ9tvlMAQ2qEMsa', 2, 1);

-- ==================== db_elderly_user ====================
CREATE DATABASE IF NOT EXISTS `db_elderly_user` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `db_elderly_user`;
DROP TABLE IF EXISTS `family`;
DROP TABLE IF EXISTS `elder`;
CREATE TABLE `elder` (
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
  UNIQUE INDEX `uk_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='老人信息表';
CREATE TABLE `family` (
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
  CONSTRAINT `fk_family_elder` FOREIGN KEY (`elder_id`) REFERENCES `elder` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='家属绑定表';

INSERT INTO `elder` (`id`, `user_id`, `medical_history`, `allergic_drugs`, `emergency_contact`) VALUES
(1, 1, '高血压 10 年史，偶发头晕',  '青霉素', '13800000005'),
(2, 2, '2 型糖尿病 5 年，空腹血糖偏高', '无', '13800000006'),
(3, 3, '冠心病，2019 年行支架手术',  '阿司匹林过敏', '13800000007'),
(4, 4, '骨质疏松，腰椎退行性病变',   '磺胺类', '13900000001');

INSERT INTO `family` (`family_user_id`, `elder_id`, `relation`, `phone`) VALUES
(5, 1, '儿子', '13800000005'),
(6, 2, '女儿', '13800000006'),
(7, 3, '女儿', '13800000007');

-- ==================== db_elderly_health ====================
CREATE DATABASE IF NOT EXISTS `db_elderly_health` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `db_elderly_health`;
DROP TABLE IF EXISTS `health`;
CREATE TABLE `health` (
  `id`             INT           NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `elder_id`       INT           NOT NULL COMMENT '关联老人 ID (软引用 db_elderly_user.elder.id)',
  `blood_pressure` VARCHAR(20)   NULL     DEFAULT NULL COMMENT '血压',
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

INSERT INTO `health` (`elder_id`, `blood_pressure`, `blood_sugar`, `heart_rate`, `weight`, `warning_flag`, `record_time`) VALUES
(1, '148/92', 5.8, 78, 72.0, 1, '2026-05-28 07:30:00'),
(1, '142/88', 5.6, 75, 71.5, 0, '2026-05-27 07:30:00'),
(1, '150/95', 6.2, 82, 72.0, 1, '2026-05-26 07:30:00'),
(2, '125/80', 8.5, 72, 65.0, 1, '2026-05-28 06:45:00'),
(2, '120/78', 7.8, 70, 64.5, 1, '2026-05-27 06:45:00'),
(2, '118/75', 7.2, 68, 64.8, 0, '2026-05-26 06:45:00'),
(3, '155/98', 5.2, 88, 75.0, 1, '2026-05-28 08:00:00'),
(3, '148/92', 5.0, 85, 74.5, 1, '2026-05-27 08:00:00'),
(3, '140/85', 4.8, 80, 74.8, 0, '2026-05-26 08:00:00'),
(4, '118/75', 5.0, 68, 55.0, 0, '2026-05-28 09:00:00'),
(4, '115/72', 4.9, 65, 54.5, 0, '2026-05-27 09:00:00'),
(4, '120/78', 5.1, 70, 55.2, 0, '2026-05-26 09:00:00');

-- ==================== db_elderly_medicine ====================
CREATE DATABASE IF NOT EXISTS `db_elderly_medicine` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `db_elderly_medicine`;
DROP TABLE IF EXISTS `record`;
DROP TABLE IF EXISTS `medicine`;
CREATE TABLE `medicine` (
  `id`            INT          NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `elder_id`      INT          NOT NULL COMMENT '关联老人 ID',
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
CREATE TABLE `record` (
  `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `task_id`     INT          NOT NULL COMMENT '关联用药计划 ID',
  `elder_id`    INT          NOT NULL COMMENT '老人 ID',
  `remind_date` DATE         NOT NULL COMMENT '计划服药日期',
  `record_time` DATETIME     NULL     DEFAULT NULL COMMENT '实际服药时间',
  `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态: 0-待服, 1-已服, 2-漏服, 3-跳过',
  `remark`      VARCHAR(255) NULL     DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_plan_date` (`remind_date`) USING BTREE,
  INDEX `idx_elder_id` (`elder_id`) USING BTREE,
  INDEX `fk_record_task` (`task_id`) USING BTREE,
  CONSTRAINT `fk_record_task` FOREIGN KEY (`task_id`) REFERENCES `medicine` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服药记录表';

INSERT INTO `medicine` (`id`, `elder_id`, `medicine_name`, `dosage`, `remind_time`, `frequency`, `start_date`, `end_date`) VALUES
(1, 1, '硝苯地平缓释片', '30mg 每日一次', '08:00:00', '每日一次', '2026-05-01', '2026-12-31'),
(2, 1, '阿司匹林肠溶片', '100mg 每日一次', '08:00:00', '每日一次', '2026-05-01', '2026-12-31'),
(3, 1, '缬沙坦胶囊',   '80mg 每日一次', '20:00:00', '每日一次', '2026-05-01', '2026-12-31'),
(4, 2, '二甲双胍', '500mg 每日二次', '08:00:00', '每日二次', '2026-05-01', '2026-12-31'),
(5, 2, '格列美脲', '2mg 每日一次',  '08:00:00', '每日一次', '2026-05-01', '2026-12-31'),
(6, 3, '阿托伐他汀钙片', '20mg 每日一次', '20:00:00', '每日一次', '2026-05-01', '2026-12-31'),
(7, 3, '单硝酸异山梨酯', '40mg 每日一次', '08:00:00', '每日一次', '2026-05-01', '2026-12-31'),
(8, 4, '碳酸钙D3片', '600mg 每日一次', '08:00:00', '每日一次', '2026-05-01', '2026-12-31');

INSERT INTO `record` (`task_id`, `elder_id`, `remind_date`, `record_time`, `status`) VALUES
(1, 1, '2026-05-28', '2026-05-28 08:05:00', 1),
(2, 1, '2026-05-28', '2026-05-28 08:06:00', 1),
(1, 1, '2026-05-27', '2026-05-27 08:10:00', 1),
(2, 1, '2026-05-27', NULL, 2),
(4, 2, '2026-05-28', '2026-05-28 08:15:00', 1);

-- ==================== db_elderly_remind ====================
CREATE DATABASE IF NOT EXISTS `db_elderly_remind` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `db_elderly_remind`;
DROP TABLE IF EXISTS `notification`;
DROP TABLE IF EXISTS `remind_task`;
DROP TABLE IF EXISTS `remind`;
CREATE TABLE `remind` (
  `id`         INT          NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `user_id`    INT          NOT NULL COMMENT '关联用户 ID',
  `ringtone`   VARCHAR(255) NULL     DEFAULT NULL COMMENT '铃声',
  `volume`     TINYINT      UNSIGNED NULL DEFAULT 50 COMMENT '音量',
  `quiet_time` VARCHAR(50)  NULL     DEFAULT NULL COMMENT '勿扰时间段',
  `repeat_mode` VARCHAR(50) NULL     DEFAULT NULL COMMENT '重复模式',
  `create_time` DATETIME    NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME    NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提醒设置表';
CREATE TABLE `remind_task` (
  `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `user_id`     INT          NOT NULL COMMENT '用户 ID',
  `elder_id`    INT          NOT NULL COMMENT '老人 ID',
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
  `medicine_id` BIGINT       NULL     DEFAULT NULL COMMENT '关联药品 ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id` (`user_id`) USING BTREE,
  INDEX `idx_elder_id` (`elder_id`) USING BTREE,
  INDEX `idx_remind_date` (`remind_date`) USING BTREE,
  INDEX `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提醒任务表';
CREATE TABLE `notification` (
  `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `task_id`     INT          NULL     DEFAULT NULL COMMENT '任务 ID',
  `user_id`     INT          NOT NULL COMMENT '用户 ID',
  `elder_id`    INT          NOT NULL COMMENT '老人 ID',
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
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知记录表';

INSERT INTO `remind` (`user_id`, `volume`, `quiet_time`) VALUES
(1, 70, '22:00-07:00'), (2, 60, '22:00-06:00'), (3, 80, '21:00-07:00'), (4, 50, '22:00-07:00');

INSERT INTO `remind_task` (`user_id`, `elder_id`, `medicine_id`, `title`, `content`, `remind_time`, `remind_date`, `remind_type`, `repeat_cycle`, `status`) VALUES
(1, 1, 1, '硝苯地平缓释片', '30mg 每日一次', '08:00:00', '2026-05-01', 1, 1, 1),
(1, 1, 2, '阿司匹林肠溶片', '100mg 每日一次', '08:00:00', '2026-05-01', 1, 1, 1),
(1, 1, 3, '缬沙坦胶囊', '80mg 每日一次', '20:00:00', '2026-05-01', 1, 1, 1),
(2, 2, 4, '二甲双胍', '500mg 每日二次', '08:00:00', '2026-05-01', 1, 1, 1),
(2, 2, 5, '格列美脲', '2mg 每日一次', '08:00:00', '2026-05-01', 1, 1, 1),
(3, 3, 6, '阿托伐他汀钙片', '20mg 每日一次', '20:00:00', '2026-05-01', 1, 1, 1),
(3, 3, 7, '单硝酸异山梨酯', '40mg 每日一次', '08:00:00', '2026-05-01', 1, 1, 1),
(4, 4, 8, '碳酸钙D3片', '600mg 每日一次', '08:00:00', '2026-05-01', 1, 1, 1);

-- ==================== db_elderly_news ====================
CREATE DATABASE IF NOT EXISTS `db_elderly_news` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `db_elderly_news`;
DROP TABLE IF EXISTS `news_like`;
DROP TABLE IF EXISTS `news_collect`;
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
  `creator_id`     INT          NULL     DEFAULT NULL COMMENT '创建者 ID',
  `publish_time`   DATETIME     NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `status`         TINYINT      NOT NULL DEFAULT 1 COMMENT '状态',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category` (`category`) USING BTREE,
  INDEX `idx_is_recommended` (`is_recommended`) USING BTREE,
  INDEX `idx_creator` (`creator_id`) USING BTREE,
  INDEX `idx_status` (`status`) USING BTREE,
  FULLTEXT INDEX `ft_title_content` (`title`, `content`) WITH PARSER ngram
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资讯表';
CREATE TABLE `news_collect` (
  `id`          INT      NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `user_id`     INT      NOT NULL COMMENT '用户 ID',
  `news_id`     INT      NOT NULL COMMENT '资讯 ID',
  `create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_news` (`user_id`, `news_id`) USING BTREE,
  INDEX `idx_user_id` (`user_id`) USING BTREE,
  INDEX `idx_news_id` (`news_id`) USING BTREE,
  CONSTRAINT `fk_collect_news` FOREIGN KEY (`news_id`) REFERENCES `news` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资讯收藏表';
CREATE TABLE `news_like` (
  `id`          INT      NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `user_id`     INT      NOT NULL COMMENT '用户 ID',
  `news_id`     INT      NOT NULL COMMENT '资讯 ID',
  `create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_news_like` (`user_id`, `news_id`) USING BTREE,
  INDEX `idx_user_id` (`user_id`) USING BTREE,
  INDEX `idx_news_id` (`news_id`) USING BTREE,
  CONSTRAINT `fk_like_news` FOREIGN KEY (`news_id`) REFERENCES `news` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资讯点赞表';

INSERT INTO `news` (`id`, `title`, `content`, `summary`, `category`, `view_count`, `like_count`, `collect_count`, `is_recommended`, `creator_id`) VALUES
(1, '老年人高血压日常管理指南',
   '高血压是老年人常见的慢性病之一。日常管理包括：1. 低盐饮食，每日食盐摄入不超过5克；2. 规律服药，不可随意停药；3. 定期监测血压，建议每天早晚各测一次；4. 适度运动，如散步、太极拳等；5. 保持情绪稳定，避免大喜大悲。',
   '高血压老年患者的饮食、用药和生活方式建议', '慢性病管理', 1280, 56, 23, 1, 8),
(2, '糖尿病饮食控制的五个要点',
   '对于糖尿病患者来说，饮食控制是治疗的基础。要点一：控制总热量摄入；要点二：定时定量进餐；要点三：粗细搭配，增加膳食纤维；要点四：选择低升糖指数食物；要点五：限制高糖、高脂食物。同时要配合适量运动，定期监测血糖，遵医嘱用药。',
   '糖尿病患者的科学饮食控制方法', '营养健康', 980, 42, 18, 1, 8),
(3, '春季老年人养生保健小常识',
   '春季是万物复苏的季节，也是养生的好时机。老年人春季养生要注意：1. 早睡早起，顺应自然规律；2. 适当春捂，避免受凉；3. 多到户外活动，晒太阳补充维生素D；4. 饮食清淡，多吃新鲜蔬菜；5. 保持心情愉悦，多与家人交流。',
   '适合老年人的春季养生知识和建议', '养生保健', 1560, 78, 35, 1, 8),
(4, '冠心病患者用药安全须知',
   '冠心病患者长期用药需特别注意：定期复查肝功能、肾功能；不要擅自增减药量或停药；随身携带急救药物如硝酸甘油；了解药物的常见副作用；出现胸痛、胸闷等不适症状应立即就医。',
   '冠心病用药注意事项和安全指导', '用药安全', 750, 31, 12, 0, 8),
(5, '老年人如何预防跌倒',
   '跌倒是老年人最常见的意外伤害之一。预防措施包括：保持家中光线充足、地面干燥防滑；浴室安装扶手和防滑垫；穿合适的防滑鞋；进行平衡训练和肌力锻炼；定期检查视力和听力；必要时使用辅助器具如拐杖。',
   '老年人防跌倒的居家安全和锻炼建议', '安全防护', 1120, 65, 28, 1, 8);

-- ==================== db_elderly_admin ====================
CREATE DATABASE IF NOT EXISTS `db_elderly_admin` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `db_elderly_admin`;
DROP TABLE IF EXISTS `disease`;
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config` (
  `id`           INT          NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `config_key`   VARCHAR(100) NOT NULL COMMENT '配置键',
  `config_value` TEXT         NULL     COMMENT '配置值',
  `description`  VARCHAR(255) NULL     DEFAULT NULL COMMENT '配置描述',
  `create_time`  DATETIME     NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`  DATETIME     NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config_key` (`config_key`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';
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

INSERT INTO `system_config` (`config_key`, `config_value`, `description`) VALUES
('site_name', '老年护理云平台', '平台名称'),
('contact_phone', '400-123-4567', '客服电话'),
('max_alert_count', '5', '每日最大预警推送数量');

INSERT INTO `disease` (`disease_name`, `category`, `symptoms`, `treatment`, `prevention`) VALUES
('高血压', '心血管', '头晕、头痛、心悸、耳鸣', '降压药物治疗、生活方式干预', '低盐饮食、规律运动、戒烟限酒'),
('糖尿病', '内分泌', '多饮、多尿、多食、体重下降', '降糖药物、胰岛素治疗、饮食控制', '控制体重、均衡饮食、定期体检'),
('冠心病', '心血管', '胸痛、胸闷、心悸、气短', '药物治疗、介入治疗、搭桥手术', '控制血压血脂、戒烟、适度运动'),
('骨质疏松', '骨骼', '腰背疼痛、身高缩短、易骨折', '补钙、维生素D、双膦酸盐药物', '适量运动、充足日照、均衡营养');

SET FOREIGN_KEY_CHECKS = 1;
