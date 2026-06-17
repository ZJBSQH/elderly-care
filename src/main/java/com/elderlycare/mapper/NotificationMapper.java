package com.elderlycare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.pojo.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知记录 Mapper
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    List<Notification> selectByUserId(@Param("userId") Integer userId);

    Long countUnread(@Param("userId") Integer userId);
    
    /**
     * 查询用户的预警通知
     */
    List<Notification> selectUserWarnings(
        @Param("userId") Integer userId,
        @Param("elderIds") List<Integer> elderIds
    );
    
    /**
     * 统计未读预警数量
     */
    int countUnreadWarnings(
        @Param("userId") Integer userId,
        @Param("elderIds") List<Integer> elderIds
    );
    
    /**
     * 标记所有预警为已读
     */
    void markAllWarningsAsRead(
        @Param("userId") Integer userId,
        @Param("elderIds") List<Integer> elderIds
    );
}
