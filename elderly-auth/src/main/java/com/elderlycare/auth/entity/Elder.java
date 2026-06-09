package com.elderlycare.auth.entity;

import lombok.Data;

/**
 * 老人实体，封装从user服务调用返回的老人信息
 */
@Data
public class Elder {
    Integer id;
    Integer userId;
}
