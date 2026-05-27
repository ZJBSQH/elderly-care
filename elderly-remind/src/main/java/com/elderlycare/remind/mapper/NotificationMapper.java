package com.elderlycare.remind.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.remind.entity.Notification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知记录 Mapper
 *
 * @author 郑
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    /**
     * 查询用户未读通知数量
     *
     * @param userId 用户 ID
     * @return 未读通知数量
     */
    default Integer countUnread(Integer userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getReadStatus, 0)
                .eq(Notification::getStatus, 1);
        return Math.toIntExact(this.selectCount(wrapper));
    }
}
