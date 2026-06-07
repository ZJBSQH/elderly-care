package com.elderlycare.ai.service;

/**
 * 医疗上下文构建器
 * 把药物服务,提醒服务的数据聚合,构建ai可理解的自然语言上下文
 */

public interface MedicalContextBuilder {

    String buildAndFormat(Integer elderId);
}
