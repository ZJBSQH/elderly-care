package com.elderlycare.auth.entity;

import lombok.Data;

//封装从user调用过来的老人id
@Data
public class Elder {
    Integer id;
    Integer userId;
}
