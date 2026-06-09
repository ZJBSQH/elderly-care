package com.elderlycare.remind.service;

import com.elderlycare.common.core.result.Result;
import com.elderlycare.remind.dto.RemindSettingUpdateRequest;
import com.elderlycare.remind.dto.RemindTaskCreateRequest;
import com.elderlycare.remind.dto.RemindTaskUpdateRequest;
import com.elderlycare.remind.entity.RemindTask;
import com.elderlycare.remind.vo.NotificationVO;
import com.elderlycare.remind.vo.RemindTaskVO;
import com.elderlycare.remind.vo.RemindVO;

import java.util.List;

/**
 * 提醒服务接口
 *
 * @author 郑
 */
public interface RemindService {

    /**
     * 获取或创建当前用户的提醒设置
     */
    Result<RemindVO> getOrCreateSettings();

    /**
     * 更新提醒设置
     */
    Result<RemindVO> updateSettings(RemindSettingUpdateRequest request);

    /**
     * 创建提醒任务
     */
    Result<RemindTaskVO> createTask(RemindTaskCreateRequest request);

    /**
     * 更新提醒任务
     */
    Result<RemindTaskVO> updateTask(RemindTaskUpdateRequest request);

    /**
     * 删除提醒任务
     */
    Result<Void> deleteTask(Integer taskId);

    /**
     * 根据 ID 获取任务详情
     */
    Result<RemindTaskVO> getTaskById(Integer taskId);

    /**
     * 分页查询当前用户的任务列表
     */
    Result<List<RemindTaskVO>> listTasks(Integer page, Integer pageSize);

    /**
     * 获取当天提醒任务（优先按 elderId 查，为空则按当前用户查）
     */
    Result<List<RemindTaskVO>> getTodayTasks(Integer elderId);

    /**
     * 获取所有当天需要执行的任务（供调度器使用，不区分用户）
     */
    List<RemindTask> getAllTodayTasksRaw();

    /**
     * 分页查询当前用户的通知列表
     */
    Result<List<NotificationVO>> getMyNotifications(Integer page, Integer pageSize);

    /**
     * 获取当前用户未读通知数量
     */
    Result<Integer> countUnread();

    /**
     * 标记通知为已读
     */
    Result<Void> markAsRead(Integer notificationId);

    /**
     * 标记当前用户全部通知为已读
     */
    Result<Void> markAllAsRead();

    /**
     * 根据老人ID查询提醒任务列表
     *
     * @param elderId 老人ID
     * @return 提醒任务列表
     */
    Result<List<RemindTaskVO>> getTasksByElderId(Integer elderId);
}
