ALTER TABLE db_elderly_health.health
    ADD COLUMN is_read TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读: 0-未读,
   1-已读';