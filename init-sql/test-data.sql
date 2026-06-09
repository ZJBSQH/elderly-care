-- ============================================
-- 测试数据脚本 (密码均为: 123456)
-- 执行方式: 在各数据库客户端中分别执行对应部分
-- ============================================

-- ==================== db_elderly_auth ====================
USE `db_elderly_auth`;
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
USE `db_elderly_user`;
SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM `family`;
DELETE FROM `elder`;
SET FOREIGN_KEY_CHECKS = 1;

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
USE `db_elderly_health`;
DELETE FROM `health`;

-- 张三: 血压偏高
INSERT INTO `health` (`elder_id`, `blood_pressure`, `blood_sugar`, `heart_rate`, `weight`, `warning_flag`, `record_time`) VALUES
(1, '148/92', 5.8, 78, 72.0, 1, '2026-05-28 07:30:00'),
(1, '142/88', 5.6, 75, 71.5, 0, '2026-05-27 07:30:00'),
(1, '150/95', 6.2, 82, 72.0, 1, '2026-05-26 07:30:00');

-- 李四: 血糖偏高
INSERT INTO `health` (`elder_id`, `blood_pressure`, `blood_sugar`, `heart_rate`, `weight`, `warning_flag`, `record_time`) VALUES
(2, '125/80', 8.5, 72, 65.0, 1, '2026-05-28 06:45:00'),
(2, '120/78', 7.8, 70, 64.5, 1, '2026-05-27 06:45:00'),
(2, '118/75', 7.2, 68, 64.8, 0, '2026-05-26 06:45:00');

-- 王五: 血压和心率偏高
INSERT INTO `health` (`elder_id`, `blood_pressure`, `blood_sugar`, `heart_rate`, `weight`, `warning_flag`, `record_time`) VALUES
(3, '155/98', 5.2, 88, 75.0, 1, '2026-05-28 08:00:00'),
(3, '148/92', 5.0, 85, 74.5, 1, '2026-05-27 08:00:00'),
(3, '140/85', 4.8, 80, 74.8, 0, '2026-05-26 08:00:00');

-- 赵六: 正常
INSERT INTO `health` (`elder_id`, `blood_pressure`, `blood_sugar`, `heart_rate`, `weight`, `warning_flag`, `record_time`) VALUES
(4, '118/75', 5.0, 68, 55.0, 0, '2026-05-28 09:00:00'),
(4, '115/72', 4.9, 65, 54.5, 0, '2026-05-27 09:00:00'),
(4, '120/78', 5.1, 70, 55.2, 0, '2026-05-26 09:00:00');

-- ==================== db_elderly_medicine ====================
USE `db_elderly_medicine`;
SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM `record`;
DELETE FROM `medicine`;
SET FOREIGN_KEY_CHECKS = 1;

-- 张三的用药
INSERT INTO `medicine` (`id`, `elder_id`, `medicine_name`, `dosage`, `remind_time`, `frequency`, `start_date`, `end_date`) VALUES
(1, 1, '硝苯地平缓释片', '30mg 每日一次', '08:00:00', '每日一次', '2026-05-01', '2026-12-31'),
(2, 1, '阿司匹林肠溶片', '100mg 每日一次', '08:00:00', '每日一次', '2026-05-01', '2026-12-31'),
(3, 1, '缬沙坦胶囊',   '80mg 每日一次', '20:00:00', '每日一次', '2026-05-01', '2026-12-31');

-- 李四的用药
INSERT INTO `medicine` (`id`, `elder_id`, `medicine_name`, `dosage`, `remind_time`, `frequency`, `start_date`, `end_date`) VALUES
(4, 2, '二甲双胍', '500mg 每日二次', '08:00:00', '每日二次', '2026-05-01', '2026-12-31'),
(5, 2, '格列美脲', '2mg 每日一次',  '08:00:00', '每日一次', '2026-05-01', '2026-12-31');

-- 王五的用药
INSERT INTO `medicine` (`id`, `elder_id`, `medicine_name`, `dosage`, `remind_time`, `frequency`, `start_date`, `end_date`) VALUES
(6, 3, '阿托伐他汀钙片', '20mg 每日一次', '20:00:00', '每日一次', '2026-05-01', '2026-12-31'),
(7, 3, '单硝酸异山梨酯', '40mg 每日一次', '08:00:00', '每日一次', '2026-05-01', '2026-12-31');

-- 赵六的用药
INSERT INTO `medicine` (`id`, `elder_id`, `medicine_name`, `dosage`, `remind_time`, `frequency`, `start_date`, `end_date`) VALUES
(8, 4, '碳酸钙D3片', '600mg 每日一次', '08:00:00', '每日一次', '2026-05-01', '2026-12-31');

-- 服药记录
INSERT INTO `record` (`task_id`, `elder_id`, `remind_date`, `record_time`, `status`) VALUES
(1, 1, '2026-05-28', '2026-05-28 08:05:00', 1),
(2, 1, '2026-05-28', '2026-05-28 08:06:00', 1),
(1, 1, '2026-05-27', '2026-05-27 08:10:00', 1),
(2, 1, '2026-05-27', NULL, 2),
(4, 2, '2026-05-28', '2026-05-28 08:15:00', 1);

-- ==================== db_elderly_remind ====================
USE `db_elderly_remind`;
SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM `notification`;
DELETE FROM `remind_task`;
DELETE FROM `remind`;
SET FOREIGN_KEY_CHECKS = 1;

-- 张三的提醒任务 (userId=1, elderId=1)
INSERT INTO `remind_task` (`user_id`, `elder_id`, `medicine_id`, `title`, `content`, `remind_time`, `remind_date`, `remind_type`, `repeat_cycle`, `status`) VALUES
(1, 1, 1, '硝苯地平缓释片', '30mg 每日一次', '08:00:00', '2026-05-01', 1, 1, 1),
(1, 1, 2, '阿司匹林肠溶片', '100mg 每日一次', '08:00:00', '2026-05-01', 1, 1, 1),
(1, 1, 3, '缬沙坦胶囊', '80mg 每日一次', '20:00:00', '2026-05-01', 1, 1, 1);

-- 李四的提醒任务 (userId=2, elderId=2)
INSERT INTO `remind_task` (`user_id`, `elder_id`, `medicine_id`, `title`, `content`, `remind_time`, `remind_date`, `remind_type`, `repeat_cycle`, `status`) VALUES
(2, 2, 4, '二甲双胍', '500mg 每日二次', '08:00:00', '2026-05-01', 1, 1, 1),
(2, 2, 5, '格列美脲', '2mg 每日一次', '08:00:00', '2026-05-01', 1, 1, 1);

-- 王五的提醒任务 (userId=3, elderId=3)
INSERT INTO `remind_task` (`user_id`, `elder_id`, `medicine_id`, `title`, `content`, `remind_time`, `remind_date`, `remind_type`, `repeat_cycle`, `status`) VALUES
(3, 3, 6, '阿托伐他汀钙片', '20mg 每日一次', '20:00:00', '2026-05-01', 1, 1, 1),
(3, 3, 7, '单硝酸异山梨酯', '40mg 每日一次', '08:00:00', '2026-05-01', 1, 1, 1);

-- 赵六的提醒任务 (userId=4, elderId=4)
INSERT INTO `remind_task` (`user_id`, `elder_id`, `medicine_id`, `title`, `content`, `remind_time`, `remind_date`, `remind_type`, `repeat_cycle`, `status`) VALUES
(4, 4, 8, '碳酸钙D3片', '600mg 每日一次', '08:00:00', '2026-05-01', 1, 1, 1);

-- 提醒设置
INSERT INTO `remind` (`user_id`, `volume`, `quiet_time`) VALUES
(1, 70, '22:00-07:00'),
(2, 60, '22:00-06:00'),
(3, 80, '21:00-07:00'),
(4, 50, '22:00-07:00');

-- ==================== db_elderly_news ====================
USE `db_elderly_news`;
SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM `news_like`;
DELETE FROM `news_collect`;
DELETE FROM `news`;
SET FOREIGN_KEY_CHECKS = 1;

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
USE `db_elderly_admin`;
DELETE FROM `disease`;
DELETE FROM `system_config`;

INSERT INTO `system_config` (`config_key`, `config_value`, `description`) VALUES
('site_name', '老年护理云平台', '平台名称'),
('contact_phone', '400-123-4567', '客服电话'),
('max_alert_count', '5', '每日最大预警推送数量');

INSERT INTO `disease` (`disease_name`, `category`, `symptoms`, `treatment`, `prevention`) VALUES
('高血压', '心血管', '头晕、头痛、心悸、耳鸣', '降压药物治疗、生活方式干预', '低盐饮食、规律运动、戒烟限酒'),
('糖尿病', '内分泌', '多饮、多尿、多食、体重下降', '降糖药物、胰岛素治疗、饮食控制', '控制体重、均衡饮食、定期体检'),
('冠心病', '心血管', '胸痛、胸闷、心悸、气短', '药物治疗、介入治疗、搭桥手术', '控制血压血脂、戒烟、适度运动'),
('骨质疏松', '骨骼', '腰背疼痛、身高缩短、易骨折', '补钙、维生素D、双膦酸盐药物', '适量运动、充足日照、均衡营养');
