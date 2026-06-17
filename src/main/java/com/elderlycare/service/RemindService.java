package com.elderlycare.service;

import com.elderlycare.common.Result;
import com.elderlycare.pojo.dto.remind.RemindQueryRequest;
import com.elderlycare.pojo.dto.remind.RemindSettingUpdateRequest;
import com.elderlycare.pojo.dto.remind.RemindTaskCreateRequest;
import com.elderlycare.pojo.dto.remind.RemindTaskUpdateRequest;
import com.elderlycare.pojo.vo.NotificationVO;
import com.elderlycare.pojo.vo.RemindVO;
import com.elderlycare.pojo.vo.RemindTaskVO;

import java.util.List;

/**
 * 提醒服务接口
 */
public interface RemindService {

    Result<RemindVO> getOrCreateSettings();

    Result<RemindVO> updateSettings(RemindSettingUpdateRequest request);

    Result<RemindTaskVO> createTask(RemindTaskCreateRequest request);

    Result<RemindTaskVO> updateTask(RemindTaskUpdateRequest request);

    Result<Void> deleteTask(Integer id);

    Result<RemindTaskVO> getTaskById(Integer id);

    Result<List<RemindTaskVO>> listTasks(RemindQueryRequest request);

    Result<List<RemindTaskVO>> getTasksByElderId(Integer elderId);

    Result<List<RemindTaskVO>> getTodayTasks();

    Result<NotificationVO> triggerTask(Integer taskId);

    Result<NotificationVO> getNotificationById(Integer id);


    Result<List<NotificationVO>> getMyNotifications();

    Result<Void> markAsRead(Integer notificationId);

    Result<Void> markAllAsRead();

    Result<Long> countUnread();

   Result<List<RemindTaskVO>> getAllTodayTasks();


}
