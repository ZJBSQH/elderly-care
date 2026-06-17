package com.elderlycare.pojo.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemindVO {
    private Integer id;
    private Integer userId;
    private String ringtone;
    private Integer volume;
    private String repeatMode;
    private String quietTime;
}
