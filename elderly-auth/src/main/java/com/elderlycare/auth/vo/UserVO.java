package com.elderlycare.auth.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVO {
    private Integer id;
    private String phone;
    private String name;
    private String avatar;
    private Integer age;
    private Integer userType;
    private Integer status;
    private String sex;
    private LocalDateTime createTime;
}
