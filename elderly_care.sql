/*
 Navicat Premium Dump SQL

 Source Server         : zheng
 Source Server Type    : MySQL
 Source Server Version : 80043 (8.0.43)
 Source Host           : localhost:3306
 Source Schema         : elderly_care

 Target Server Type    : MySQL
 Target Server Version : 80043 (8.0.43)
 File Encoding         : 65001

 Date: 14/05/2026 09:25:54
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for elder
-- ----------------------------
DROP TABLE IF EXISTS `elder`;
CREATE TABLE `elder`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `user_id` int NOT NULL COMMENT '关联用户 ID (老人自己的账号)',
  `medical_history` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '既往病史',
  `allergic_drugs` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '过敏药物',
  `emergency_contact` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '紧急联系人电话',
  `health_file` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '健康档案文件路径',
  `qr_code_token` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '老人专属二维码',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE COMMENT '确保一个用户只有一个健康档案',
  CONSTRAINT `fk_elder_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '老人信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of elder
-- ----------------------------
INSERT INTO `elder` VALUES (1, 1, NULL, NULL, '13800138001', NULL, '7bc98dfe6e57434a8e39065228919516');
INSERT INTO `elder` VALUES (2, 2, NULL, NULL, '17727278889', NULL, NULL);
INSERT INTO `elder` VALUES (3, 3, NULL, NULL, '17727279990', NULL, NULL);

-- ----------------------------
-- Table structure for family
-- ----------------------------
DROP TABLE IF EXISTS `family`;
CREATE TABLE `family`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `family_user_id` int NOT NULL COMMENT '家属用户 ID',
  `elder_id` int NOT NULL COMMENT '老人档案 ID',
  `relation` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '亲属' COMMENT '关系',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '联系方式',
  `bind_status` tinyint NOT NULL DEFAULT 1 COMMENT '状态 (1-已绑定，0-解绑)',
  `bind_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_elder`(`family_user_id` ASC, `elder_id` ASC) USING BTREE,
  INDEX `fk_family_elder`(`elder_id` ASC) USING BTREE,
  CONSTRAINT `fk_family_elder` FOREIGN KEY (`elder_id`) REFERENCES `elder` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_family_user` FOREIGN KEY (`family_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '家属绑定表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of family
-- ----------------------------

-- ----------------------------
-- Table structure for health
-- ----------------------------
DROP TABLE IF EXISTS `health`;
CREATE TABLE `health`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `elder_id` int NOT NULL COMMENT '关联老人 ID',
  `blood_pressure` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '血压 (120/80)',
  `blood_sugar` decimal(5, 2) NULL DEFAULT NULL COMMENT '血糖',
  `heart_rate` tinyint NULL DEFAULT NULL COMMENT '心率',
  `weight` decimal(5, 2) NULL DEFAULT NULL COMMENT '体重',
  `warning_flag` tinyint NOT NULL DEFAULT 0 COMMENT '是否异常 (0-正常，1-异常)',
  `record_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_elder_time`(`elder_id` ASC, `record_time` DESC) USING BTREE,
  CONSTRAINT `fk_health_elder` FOREIGN KEY (`elder_id`) REFERENCES `elder` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '健康数据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of health
-- ----------------------------
INSERT INTO `health` VALUES (1, 1, '135/85', 5.80, 72, 65.50, 0, NULL);
INSERT INTO `health` VALUES (2, 1, '120/80', 5.00, 72, 65.00, 0, '2026-03-10 08:00:00');
INSERT INTO `health` VALUES (3, 1, '122/82', 5.10, 74, 65.10, 0, '2026-03-11 08:00:00');
INSERT INTO `health` VALUES (4, 1, '125/83', 5.20, 75, 65.20, 0, '2026-03-12 08:00:00');
INSERT INTO `health` VALUES (5, 1, '128/85', 5.50, 76, 65.30, 0, '2026-03-13 08:00:00');
INSERT INTO `health` VALUES (6, 1, '130/86', 5.80, 78, 65.40, 0, '2026-03-14 08:00:00');
INSERT INTO `health` VALUES (7, 1, '132/87', 6.00, 79, 65.50, 0, '2026-03-15 08:00:00');
INSERT INTO `health` VALUES (8, 1, '135/88', 6.20, 80, 65.60, 0, '2026-03-16 08:00:00');
INSERT INTO `health` VALUES (9, 1, '138/89', 6.50, 82, 65.70, 0, '2026-03-17 08:00:00');
INSERT INTO `health` VALUES (10, 1, '140/90', 6.80, 84, 65.80, 1, '2026-03-18 08:00:00');
INSERT INTO `health` VALUES (11, 1, '142/92', 7.00, 86, 65.90, 1, '2026-03-19 08:00:00');
INSERT INTO `health` VALUES (12, 1, '145/95', 7.20, 88, 66.00, 1, '2026-03-20 08:00:00');
INSERT INTO `health` VALUES (15, 2, '145/95', 7.50, 100, 55.00, 1, '2026-03-20 15:05:23');
INSERT INTO `health` VALUES (16, 2, '166/95', 7.50, 100, 155.00, 1, '2026-03-20 15:05:39');
INSERT INTO `health` VALUES (17, 1, '92/77', 4.00, 77, 114.00, 0, '2026-04-25 15:39:18');
INSERT INTO `health` VALUES (18, 1, '138/88', 6.20, 85, 65.50, 1, '2026-04-19 08:00:00');
INSERT INTO `health` VALUES (19, 1, '140/90', 6.50, 88, 65.30, 1, '2026-04-20 08:00:00');
INSERT INTO `health` VALUES (20, 1, '142/92', 6.80, 90, 65.20, 1, '2026-04-21 08:00:00');
INSERT INTO `health` VALUES (21, 1, '138/88', 6.30, 82, 65.40, 1, '2026-04-22 08:00:00');
INSERT INTO `health` VALUES (22, 1, '135/85', 5.90, 78, 65.50, 0, '2026-04-23 08:00:00');
INSERT INTO `health` VALUES (23, 1, '132/83', 5.60, 75, 65.60, 0, '2026-04-24 08:00:00');
INSERT INTO `health` VALUES (24, 1, '128/82', 5.40, 72, 65.70, 0, '2026-04-25 08:00:00');
INSERT INTO `health` VALUES (25, 1, '138/88', 6.20, 85, 65.50, 1, '2026-04-19 08:00:00');
INSERT INTO `health` VALUES (26, 1, '140/90', 6.50, 88, 65.30, 1, '2026-04-20 08:00:00');
INSERT INTO `health` VALUES (27, 1, '142/92', 6.80, 90, 65.20, 1, '2026-04-21 08:00:00');
INSERT INTO `health` VALUES (28, 1, '138/88', 6.30, 82, 65.40, 1, '2026-04-22 08:00:00');
INSERT INTO `health` VALUES (29, 1, '135/85', 5.90, 78, 65.50, 0, '2026-04-23 08:00:00');
INSERT INTO `health` VALUES (30, 1, '132/83', 5.60, 75, 65.60, 0, '2026-04-24 08:00:00');
INSERT INTO `health` VALUES (31, 1, '128/82', 5.40, 72, 65.70, 0, '2026-04-25 08:00:00');

-- ----------------------------
-- Table structure for medicine
-- ----------------------------
DROP TABLE IF EXISTS `medicine`;
CREATE TABLE `medicine`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `elder_id` int NOT NULL COMMENT '关联老人 ID',
  `medicine_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '药品名称',
  `dosage` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '单次剂量 (如 2 片)',
  `remind_time` time NULL DEFAULT NULL COMMENT '提醒时间点 (如 08:00:00)',
  `frequency` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '频次 (如 每日 1 次)',
  `start_date` date NOT NULL COMMENT '计划开始日期',
  `end_date` date NOT NULL COMMENT '计划结束日期',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态 (1-进行中，0-停用)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_public` tinyint NOT NULL DEFAULT 0 COMMENT '是否公共药品：0-个人用药，1-公共药品',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_elder_id`(`elder_id` ASC) USING BTREE,
  CONSTRAINT `fk_medicine_elder` FOREIGN KEY (`elder_id`) REFERENCES `elder` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用药计划表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of medicine
-- ----------------------------
INSERT INTO `medicine` VALUES (1, 1, '阿司匹林肠溶片', '100mg', '08:00:00', '每日 1 次', '2026-03-19', '2026-04-19', 1, '2026-03-19 21:20:52', NULL, 0);
INSERT INTO `medicine` VALUES (2, 1, '降压药', '50mg', '12:00:00', '每日 1 次', '2026-03-19', '2026-04-19', 1, '2026-03-19 21:20:52', NULL, 0);
INSERT INTO `medicine` VALUES (3, 2, '感冒药', '1 包', '18:00:00', '每日 3 次', '2026-03-19', '2026-03-25', 1, '2026-03-19 21:20:52', NULL, 0);
INSERT INTO `medicine` VALUES (4, 1, '健康药', '2片', '08:00:00', '每天', '2026-04-18', '2026-05-18', 1, '2026-04-18 18:15:06', NULL, 0);
INSERT INTO `medicine` VALUES (5, 1, '感冒清热颗粒', '1袋', '08:00:00', '每日3次', '2026-04-19', '2026-04-26', 1, '2026-04-25 16:06:03', NULL, 0);
INSERT INTO `medicine` VALUES (6, 1, '感冒清热颗粒', '1袋', '12:00:00', '每日3次', '2026-04-19', '2026-04-26', 1, '2026-04-25 16:06:03', NULL, 0);
INSERT INTO `medicine` VALUES (7, 1, '感冒清热颗粒', '1袋', '18:00:00', '每日3次', '2026-04-19', '2026-04-26', 1, '2026-04-25 16:06:03', NULL, 0);
INSERT INTO `medicine` VALUES (8, 1, '维C银翘片', '2片', '08:00:00', '每日2次', '2026-04-19', '2026-04-26', 1, '2026-04-25 16:06:03', NULL, 0);
INSERT INTO `medicine` VALUES (9, 1, '维C银翘片', '2片', '18:00:00', '每日2次', '2026-04-19', '2026-04-26', 1, '2026-04-25 16:06:03', NULL, 0);
INSERT INTO `medicine` VALUES (10, 1, '板蓝根颗粒', '1袋', '08:00:00', '每日1次', '2026-04-19', '2026-05-19', 1, '2026-04-25 16:06:03', NULL, 0);
INSERT INTO `medicine` VALUES (11, 1, '非布司他', '40ml', '10:10:00', 'custom', '2026-04-25', '2026-06-25', 1, '2026-04-25 16:08:34', NULL, 0);

-- ----------------------------
-- Table structure for news
-- ----------------------------
DROP TABLE IF EXISTS `news`;
CREATE TABLE `news`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '内容',
  `summary` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '摘要',
  `cover_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图片 URL',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类',
  `view_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '阅读量',
  `like_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '点赞数',
  `collect_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '收藏数',
  `is_recommended` tinyint UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否推荐（1-是，0-否）',
  `creator_id` int NULL DEFAULT NULL COMMENT '创建者 ID（关联 user 表）',
  `publish_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `status` tinyint NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE,
  INDEX `idx_is_recommended`(`is_recommended` ASC) USING BTREE,
  INDEX `fk_news_creator`(`creator_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  FULLTEXT INDEX `ft_title_content`(`title`, `content`) WITH PARSER `ngram` COMMENT '全文索引用于搜索',
  CONSTRAINT `fk_news_creator` FOREIGN KEY (`creator_id`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '资讯表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of news
-- ----------------------------
INSERT INTO `news` VALUES (4, '高血压患者的日常用药指南', '高血压是一种常见的慢性疾病，需要长期服药控制。以下是高血压患者的用药注意事项：\n\n1. 按时服药：每天固定时间服用降压药，不要随意停药或更改剂量。\n\n2. 注意药物相互作用：避免与其他药物同时服用，如需合用应咨询医生。\n\n3. 监测血压：定期测量血压，记录血压变化。\n\n4. 生活方式调整：低盐饮食、适量运动、戒烟限酒。\n\n5. 定期复诊：按医生要求定期复查，评估治疗效果。', '详细介绍高血压患者的用药注意事项和日常管理方法', '/images/hypertension_medication.jpg', '慢病知识', 5, 0, 0, 1, NULL, '2026-03-27 23:23:59', 1);
INSERT INTO `news` VALUES (5, '糖尿病饮食控制要点', '糖尿病患者的饮食管理至关重要，以下是饮食控制的要点：\n\n1. 控制总热量摄入，维持理想体重。\n\n2. 合理分配营养素：碳水化合物占 50-60%，蛋白质占 15-20%，脂肪占 25-30%。\n\n3. 少食多餐：每日 5-6 餐，避免血糖大幅波动。\n\n4. 选择低 GI 食物：如全谷物、蔬菜、豆类等。\n\n5. 限制糖分摄入：避免含糖饮料、甜点等高糖食品。\n\n6. 增加膳食纤维：多吃蔬菜、水果（适量）、粗粮。', '糖尿病患者如何科学安排饮食，控制血糖水平', '/images/diabetes_diet.jpg', '慢病知识', 0, 0, 0, 1, NULL, '2026-03-27 23:23:59', 1);
INSERT INTO `news` VALUES (6, '冠心病的运动康复指导', '冠心病患者适当的运动有助于改善心脏功能，但需要注意以下几点：\n\n1. 运动前评估：在开始运动计划前，应进行心脏功能评估。\n\n2. 选择合适的运动：推荐有氧运动，如散步、慢跑、游泳、太极拳等。\n\n3. 控制运动强度：心率控制在（220-年龄）×60%-70% 为宜。\n\n4. 循序渐进：从低强度开始，逐渐增加运动时间和强度。\n\n5. 注意运动时间：每次 30-60 分钟，每周 3-5 次。\n\n6. 避免剧烈运动：避免竞技性运动和突然的剧烈活动。\n\n7. 随身携带急救药物：如硝酸甘油等。', '冠心病患者如何安全有效地进行运动康复', '/images/coronary_exercise.jpg', '运动指导', 1, 0, 0, 1, NULL, '2026-03-27 23:23:59', 1);
INSERT INTO `news` VALUES (7, '老年人用药安全须知', '老年人由于身体机能下降，用药时需要特别注意：\n\n1. 遵医嘱用药：严格按照医生的处方用药，不要自行增减剂量。\n\n2. 注意服药时间：有些药物需要空腹服用，有些需要饭后服用。\n\n3. 避免重复用药：不同药物可能含有相同成分，避免叠加使用。\n\n4. 注意药物副作用：如出现不适，及时就医。\n\n5. 定期复查肝肾功能：老年人代谢能力下降，需定期检查。\n\n6. 妥善保管药物：避免受潮、变质，过期药物及时丢弃。\n\n7. 告知医生过敏史：特别是对某些药物过敏的情况。', '老年人用药的安全注意事项和常见误区', '/images/elderly_medication_safety.jpg', '用药安全', 0, 0, 0, 1, NULL, '2026-03-27 23:23:59', 1);
INSERT INTO `news` VALUES (8, '高血脂的饮食调理', '高血脂患者通过合理的饮食可以有效控制血脂水平：\n\n1. 减少饱和脂肪酸：少吃动物内脏、肥肉、奶油等。\n\n2. 增加不饱和脂肪酸：适量食用深海鱼、坚果、橄榄油等。\n\n3. 控制胆固醇摄入：每日胆固醇摄入量应低于 300mg。\n\n4. 多吃富含膳食纤维的食物：如燕麦、豆类、蔬菜水果等。\n\n5. 适量摄入植物固醇：如豆制品、坚果等。\n\n6. 限制精制碳水化合物：减少白米饭、白面包等的摄入。\n\n7. 保持规律饮食：避免暴饮暴食，控制总热量。', '高血脂患者如何通过饮食调理降低血脂', '/images/lipid_diet.jpg', '饮食指导', 0, 0, 0, 0, NULL, '2026-03-27 23:23:59', 1);
INSERT INTO `news` VALUES (9, '关节炎患者的运动建议', '关节炎患者在保护关节的前提下进行适当运动：\n\n1. 选择低冲击运动：如游泳、骑自行车、椭圆机等。\n\n2. 加强肌肉力量训练：增强关节周围肌肉，减轻关节负担。\n\n3. 进行柔韧性练习：如瑜伽、拉伸运动等。\n\n4. 避免长时间保持同一姿势：定时活动关节。\n\n5. 运动前热身：充分热身可以减少关节损伤。\n\n6. 使用辅助器具：如护膝、手杖等减轻关节压力。\n\n7. 疼痛时休息：急性发作期应减少活动，以休息为主。', '关节炎患者如何在保护关节的前提下进行运动', '/images/arthritis_exercise.jpg', '运动指导', 0, 0, 0, 0, NULL, '2026-03-27 23:23:59', 1);
INSERT INTO `news` VALUES (10, '感冒用药的常见误区', '感冒是常见病，但很多人存在用药误区：\n\n1. 滥用抗生素：普通感冒多为病毒感染，抗生素无效。\n\n2. 多种感冒药混用：可能导致药物过量，加重肝肾负担。\n\n3. 过早停药：症状缓解就停药，可能导致病情反复。\n\n4. 忽视休息：单纯依赖药物，不注意休息和补水。\n\n5. 儿童使用成人药：儿童器官发育不完善，需用儿童专用药。\n\n6. 孕妇随意用药：孕期用药需特别谨慎，应咨询医生。\n\n7. 超剂量服用：认为多吃点好得快，反而有害健康。', '解析感冒用药的七大常见误区', '/images/cold_medication_mistakes.jpg', '用药安全', 1, 0, 0, 0, NULL, '2026-03-27 23:23:59', 1);
INSERT INTO `news` VALUES (11, '脑卒中的预防与康复', '脑卒中是中老年人的常见病，预防和康复很重要：\n\n1. 控制危险因素：高血压、糖尿病、高血脂等。\n\n2. 健康生活方式：戒烟限酒、合理饮食、适量运动。\n\n3. 定期体检：早期发现和治疗潜在疾病。\n\n4. 识别早期症状：如突发头痛、肢体无力、言语不清等。\n\n5. 及时就医：发病后 3-4.5 小时内是溶栓黄金时间。\n\n6. 康复训练：包括肢体功能、语言、认知等方面的训练。\n\n7. 心理支持：患者和家属都需要心理调适和支持。', '脑卒中的预防措施和康复治疗要点', '/images/stroke_prevention.jpg', '慢病知识', 0, 0, 0, 1, NULL, '2026-03-27 23:23:59', 1);

-- ----------------------------
-- Table structure for news_collect
-- ----------------------------
DROP TABLE IF EXISTS `news_collect`;
CREATE TABLE `news_collect`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `user_id` int NOT NULL COMMENT '用户 ID',
  `news_id` int NOT NULL COMMENT '资讯 ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_news`(`user_id` ASC, `news_id` ASC) USING BTREE COMMENT '防止重复收藏',
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_news_id`(`news_id` ASC) USING BTREE,
  CONSTRAINT `fk_collect_news` FOREIGN KEY (`news_id`) REFERENCES `news` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_collect_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '资讯收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of news_collect
-- ----------------------------

-- ----------------------------
-- Table structure for news_like
-- ----------------------------
DROP TABLE IF EXISTS `news_like`;
CREATE TABLE `news_like`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `user_id` int NOT NULL COMMENT '用户 ID',
  `news_id` int NOT NULL COMMENT '资讯 ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_news_like`(`user_id` ASC, `news_id` ASC) USING BTREE COMMENT '防止重复点赞',
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_news_id`(`news_id` ASC) USING BTREE,
  CONSTRAINT `fk_like_news` FOREIGN KEY (`news_id`) REFERENCES `news` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_like_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '资讯点赞表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of news_like
-- ----------------------------

-- ----------------------------
-- Table structure for notification
-- ----------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `task_id` int NULL DEFAULT NULL COMMENT '任务 ID',
  `user_id` int NOT NULL COMMENT '用户 ID',
  `elder_id` int NOT NULL COMMENT '老人 ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '通知内容',
  `notify_type` int NOT NULL COMMENT '通知类型：1-用药，2-体检，3-活动，4-其他',
  `send_time` datetime NOT NULL COMMENT '发送时间',
  `read_status` int NULL DEFAULT 0 COMMENT '阅读状态：0-未读，1-已读',
  `read_time` datetime NULL DEFAULT NULL COMMENT '阅读时间',
  `status` int NULL DEFAULT 1 COMMENT '状态：0-无效，1-有效',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_read_status`(`read_status` ASC) USING BTREE,
  INDEX `idx_send_time`(`send_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '通知记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notification
-- ----------------------------
INSERT INTO `notification` VALUES (1, 1, 1, 1, '早晨服药提醒', '请服用阿司匹林 100mg', 1, '2026-03-19 08:00:00', 1, '2026-03-20 14:42:37', 1, '2026-03-19 21:20:52');
INSERT INTO `notification` VALUES (2, 2, 1, 1, '中午服药提醒', '请服用降压药 50mg', 1, '2026-03-19 12:00:00', 0, NULL, 1, '2026-03-19 21:20:52');
INSERT INTO `notification` VALUES (3, 3, 2, 2, '晚上服药提醒', '请服用感冒药', 1, '2026-03-19 18:00:00', 0, NULL, 1, '2026-03-19 21:20:52');
INSERT INTO `notification` VALUES (4, NULL, 2, 2, '健康预警：血压异常', '检测到异常：血压异常（收缩压145mmHg，舒张压95mmHg）。正常范围：90-140/60-90 mmHg', 2, '2026-03-20 14:38:59', 1, '2026-03-20 15:06:44', 1, NULL);
INSERT INTO `notification` VALUES (5, NULL, 2, 2, '健康预警：血糖偏高', '检测到异常：血糖偏高（血糖值7.5 mmol/L）。正常范围：3.9-6.1 mmol/L', 2, '2026-03-20 14:38:59', 1, '2026-03-20 15:06:44', 1, NULL);
INSERT INTO `notification` VALUES (6, NULL, 2, 2, '健康预警：心率过快', '检测到异常：心率过快（心率105次/分）。正常范围：60-100 次/分', 2, '2026-03-20 14:38:59', 1, '2026-03-20 15:06:44', 1, NULL);
INSERT INTO `notification` VALUES (7, NULL, 2, 2, '健康预警：体重超标', '检测到异常：体重超标（体重195.0kg，超过正常范围）。正常范围：40-150 kg', 2, '2026-03-20 14:38:59', 1, '2026-03-20 15:06:44', 1, NULL);
INSERT INTO `notification` VALUES (8, NULL, 2, 2, '健康预警：血压异常', '检测到异常：血压异常（收缩压145mmHg，舒张压95mmHg）。正常范围：90-140/60-90 mmHg', 2, '2026-03-20 15:05:23', 1, '2026-03-20 15:06:44', 1, NULL);
INSERT INTO `notification` VALUES (9, NULL, 2, 2, '健康预警：血糖偏高', '检测到异常：血糖偏高（血糖值7.5 mmol/L）。正常范围：3.9-6.1 mmol/L', 2, '2026-03-20 15:05:23', 1, '2026-03-20 15:06:44', 1, NULL);
INSERT INTO `notification` VALUES (10, NULL, 2, 2, '健康预警：血压异常', '检测到异常：血压异常（收缩压166mmHg，舒张压95mmHg）。正常范围：90-140/60-90 mmHg', 2, '2026-03-20 15:05:39', 1, '2026-03-20 15:06:44', 1, NULL);
INSERT INTO `notification` VALUES (11, NULL, 2, 2, '健康预警：血糖偏高', '检测到异常：血糖偏高（血糖值7.5 mmol/L）。正常范围：3.9-6.1 mmol/L', 2, '2026-03-20 15:05:39', 1, '2026-03-20 15:06:44', 1, NULL);
INSERT INTO `notification` VALUES (12, NULL, 2, 2, '健康预警：体重超标', '检测到异常：体重超标（体重155.0kg，超过正常范围）。正常范围：40-150 kg', 2, '2026-03-20 15:05:39', 1, '2026-03-20 15:06:44', 1, NULL);
INSERT INTO `notification` VALUES (13, 7, 1, 1, '钙片', '每次2片', 1, '2026-04-18 20:00:00', 0, NULL, 1, NULL);

-- ----------------------------
-- Table structure for record
-- ----------------------------
DROP TABLE IF EXISTS `record`;
CREATE TABLE `record`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `task_id` int NOT NULL COMMENT '关联用药计划 ID',
  `elder_id` int NOT NULL COMMENT '冗余字段：方便查询老人所有记录',
  `remind_date` date NOT NULL COMMENT '计划服药日期',
  `record_time` datetime NULL DEFAULT NULL COMMENT '实际服药时间',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态 (0-待服，1-已服，2-漏服，3-跳过)',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_plan_date`(`remind_date` ASC) USING BTREE,
  INDEX `idx_elder_id`(`elder_id` ASC) USING BTREE,
  INDEX `fk_record_task`(`task_id` ASC) USING BTREE,
  CONSTRAINT `fk_record_task` FOREIGN KEY (`task_id`) REFERENCES `medicine` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '服药记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of record
-- ----------------------------
INSERT INTO `record` VALUES (1, 1, 1, '2026-03-19', '2026-03-19 21:25:23', 1, '已服药');
INSERT INTO `record` VALUES (2, 2, 1, '2026-03-19', '2026-03-19 21:26:42', 0, '超时未服');

-- ----------------------------
-- Table structure for remind
-- ----------------------------
DROP TABLE IF EXISTS `remind`;
CREATE TABLE `remind`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `user_id` int NOT NULL COMMENT '关联用户 ID',
  `ringtone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '铃声',
  `volume` tinyint UNSIGNED NULL DEFAULT 50 COMMENT '音量',
  `quiet_time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '勿扰时间段',
  `repeat_mode` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_remind_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '提醒设置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of remind
-- ----------------------------
INSERT INTO `remind` VALUES (1, 1, 'default', 50, '22:00-07:00', 'once');
INSERT INTO `remind` VALUES (2, 2, 'default', 50, '22:00-07:00', 'once');
INSERT INTO `remind` VALUES (3, 3, 'default', 50, '22:00-07:00', 'once');

-- ----------------------------
-- Table structure for remind_task
-- ----------------------------
DROP TABLE IF EXISTS `remind_task`;
CREATE TABLE `remind_task`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `user_id` int NOT NULL COMMENT '用户 ID',
  `elder_id` int NOT NULL COMMENT '老人 ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '提醒标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '提醒内容',
  `remind_time` time NOT NULL COMMENT '提醒时间',
  `remind_date` date NULL DEFAULT NULL COMMENT '提醒日期',
  `remind_type` int NOT NULL COMMENT '提醒类型：1-用药，2-体检，3-活动，4-其他',
  `need_voice` tinyint NULL DEFAULT 1 COMMENT '是否需要语音播报',
  `need_popup` tinyint NULL DEFAULT 1 COMMENT '是否需要弹窗提醒',
  `voice_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '语音播报文本',
  `repeat_cycle` int NULL DEFAULT NULL COMMENT '重复周期：0-不重复，1-每天，2-每周，3-每月',
  `end_date` date NULL DEFAULT NULL COMMENT '结束日期',
  `status` int NULL DEFAULT 1 COMMENT '状态：0-停用，1-启用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `medicine_id` bigint NULL DEFAULT NULL COMMENT '关联的药品ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_elder_id`(`elder_id` ASC) USING BTREE,
  INDEX `idx_remind_date`(`remind_date` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '提醒任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of remind_task
-- ----------------------------
INSERT INTO `remind_task` VALUES (1, 1, 1, '早晨服药提醒', '请服用阿司匹林 100mg', '08:00:00', '2026-04-25', 1, 1, 1, '请按时服用阿司匹林', NULL, NULL, 1, NULL, '2026-03-19 21:20:52', '2026-04-29 19:28:16', 1);
INSERT INTO `remind_task` VALUES (2, 1, 1, '中午服药提醒', '请服用降压药 50mg', '12:00:00', '2026-03-19', 1, 1, 1, '请按时服用降压药', NULL, NULL, 1, NULL, '2026-03-19 21:20:52', '2026-04-23 19:28:16', 2);
INSERT INTO `remind_task` VALUES (3, 2, 2, '晚上服药提醒', '请服用感冒药', '18:00:00', '2026-03-19', 1, 1, 1, '请按时服用感冒药', NULL, NULL, 1, NULL, '2026-03-19 21:20:52', '2026-04-23 19:28:16', 3);
INSERT INTO `remind_task` VALUES (4, 1, 1, '阿司匹林肠溶片', '每次1片，饭后服用', '08:00:00', '2026-04-18', 1, 1, 1, '请服用阿司匹林肠溶片1片', 1, NULL, 1, '心血管药物', '2026-04-18 19:45:44', '2026-04-23 19:28:16', 4);
INSERT INTO `remind_task` VALUES (5, 1, 1, '降压药', '每次1片', '12:00:00', '2026-04-18', 1, 1, 1, '请服用降压药1片', 1, NULL, 1, '控制血压', '2026-04-18 19:45:44', '2026-04-23 19:28:16', 5);
INSERT INTO `remind_task` VALUES (6, 1, 1, '降糖药', '每次1片，餐前服用', '18:00:00', '2026-04-18', 1, 1, 1, '请服用降糖药1片', 1, NULL, 1, '控制血糖', '2026-04-18 19:45:44', '2026-04-23 19:28:16', 6);
INSERT INTO `remind_task` VALUES (7, 1, 1, '钙片', '每次2片', '20:00:00', '2026-04-18', 1, 1, 1, '请服用钙片2片', 1, NULL, 1, '补钙', '2026-04-18 19:45:44', '2026-04-23 19:28:16', 7);

-- ----------------------------
-- Table structure for system_config
-- ----------------------------
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置键（如：site_name）',
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '配置值',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置描述',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `config_key`(`config_key` ASC) USING BTREE,
  INDEX `idx_config_key`(`config_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of system_config
-- ----------------------------
INSERT INTO `system_config` VALUES (1, 'site_name', '养老服务平台', '网站名称', '2026-03-28 14:49:06');
INSERT INTO `system_config` VALUES (2, 'sms_app_key', 'your-sms-key', '短信 API Key', '2026-03-28 14:49:06');
INSERT INTO `system_config` VALUES (3, 'sms_app_secret', 'your-sms-secret', '短信 API Secret', '2026-03-28 14:49:06');
INSERT INTO `system_config` VALUES (4, 'health_warning_threshold', '{\"bloodPressure\":{\"min\":\"90/60\",\"max\":\"140/90\"},\"heartRate\":{\"min\":60,\"max\":100}}', '健康警告阈值', '2026-03-28 14:49:06');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '姓名',
  `sex` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '性别',
  `age` int NOT NULL COMMENT '年龄',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '手机号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码 (BCrypt 加密)',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像 URL',
  `user_type` tinyint NOT NULL DEFAULT 0 COMMENT '角色 (0-老人，1-家属，2-管理员)',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态 (1-正常，0-禁用)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, '张三', '男', 67, '13800138000', '$2a$10$X1HnterlStD8vl5S4FRn3egLjNBJKbmNAUvUCva5oGwJr4vXW/ohq', NULL, 0, 1, '2026-03-19 21:20:52');
INSERT INTO `user` VALUES (2, '郑六', '男', 108, '17727278888', '$2a$10$xDu8eVWXQzgoAm.K/IN6DuXca69k2q4XqHR8Jv3bQsGxgeccGY1PC', NULL, 0, 1, '2026-03-19 21:20:52');
INSERT INTO `user` VALUES (3, '王五', '男', 35, '17727279999', '$2a$10$84cuLovCpnZHy44mqDCh7urCdB4zz4EDrXQHFb5eHtY4rOW7wp8rm', NULL, 2, 1, '2026-03-19 21:20:52');
INSERT INTO `user` VALUES (4, '王小五', '女', 13, '17727271999', '$2a$10$kG8rgYUR4SX2/dFNrsKXx.8P1KPzib3l5DVfgXAlcL/6jFWsES8Ay', NULL, 1, 1, '2026-03-19 21:20:52');
INSERT INTO `user` VALUES (5, '张小三', '男', 33, '17727271888', '$2a$10$zWZ9fcv34V7Y8TZbUIKkOOuAU4FB4mYzCIdWN3.Rqj/dkLFKvM0SK', NULL, 1, 1, '2026-03-19 21:20:52');
INSERT INTO `user` VALUES (6, '李四', '男', 78, '17727277777', '$2a$10$jA1TxlzcnRxCetoX1oxig.GeJL4IRyK0TMXSSiEFRVuf32xNeSvs.', NULL, 0, 1, '2026-03-19 21:20:52');
INSERT INTO `user` VALUES (7, '郑王', '男', 18, '13588888888', '$2a$10$aZISWMssdxr8oreT31h2geUgUL/TZzB.5wP2jVkEgl30TjLmiJsIG', NULL, 1, 1, '2026-04-17 16:21:28');

SET FOREIGN_KEY_CHECKS = 1;
