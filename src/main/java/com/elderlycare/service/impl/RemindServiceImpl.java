package com.elderlycare.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elderlycare.common.util.BeanUtil;
import com.elderlycare.common.util.SecurityUtil;
import com.elderlycare.mapper.NotificationMapper;
import com.elderlycare.mapper.RemindMapper;
import com.elderlycare.mapper.RemindTaskMapper;
import com.elderlycare.mapper.UserMapper;
import com.elderlycare.common.Result;
import com.elderlycare.common.exception.BusinessException;
import com.elderlycare.pojo.dto.remind.RemindQueryRequest;
import com.elderlycare.pojo.dto.remind.RemindSettingUpdateRequest;
import com.elderlycare.pojo.dto.remind.RemindTaskCreateRequest;
import com.elderlycare.pojo.dto.remind.RemindTaskUpdateRequest;
import com.elderlycare.pojo.entity.Notification;
import com.elderlycare.pojo.entity.Remind;
import com.elderlycare.pojo.entity.RemindTask;
import com.elderlycare.pojo.vo.NotificationVO;
import com.elderlycare.pojo.vo.RemindTaskVO;
import com.elderlycare.pojo.vo.RemindVO;
import com.elderlycare.service.RemindService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.elderlycare.common.exception.ErrorCode.*;

/**
 * 提醒服务实现 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RemindServiceImpl implements RemindService {

    private final RemindMapper remindMapper;
    private final UserMapper userMapper;
    private final RemindTaskMapper remindTaskMapper;
    private final NotificationMapper notificationMapper;
    private final SecurityUtil securityUtil;


// ... existing code ...
    /**
     * 获取或创建用户提醒设置
     */
    @Override
    public Result<RemindVO> getOrCreateSettings() {
        Integer userId = securityUtil.getCurrentUserId();
        Remind remind = remindMapper.selectByUserId(userId);

        if (remind == null) {
            remind = new Remind();
            remind.setUserId(userId);
            remind.setRingtone("default");
            remind.setVolume(50);
            remind.setRepeatMode("once");
            remind.setQuietTime("22:00-07:00");
            remindMapper.insert(remind);
            log.info("为用户创建默认提醒设置，userId: {}", userId);
        }

        return Result.success(toRemindVO(remind));
    }

    /**
     * 更新用户提醒设置
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<RemindVO> updateSettings(RemindSettingUpdateRequest request) {
        Integer userId = securityUtil.getCurrentUserId();
        Remind remind = remindMapper.selectByUserId(userId);

        if (remind == null) {
            remind = new Remind();
            remind.setUserId(userId);
        }

        BeanUtil.copyNonNullProperties(request, remind);

        if (remind.getId() == null) {
            remindMapper.insert(remind);
        } else {
            remindMapper.updateById(remind);
        }

        log.info("更新提醒设置成功，userId: {}", userId);
        return Result.success(toRemindVO(remind));
    }

    /**
     * 创建提醒任务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<RemindTaskVO> createTask(RemindTaskCreateRequest request) {
        Integer userId = securityUtil.getCurrentUserId();
        RemindTask task = new RemindTask();
        BeanUtils.copyProperties(request, task);
        task.setUserId(userId);
        task.setStatus(1);

        remindTaskMapper.insert(task);
        log.info("创建提醒任务成功，taskId: {}", task.getId());
        return Result.success(toRemindTaskVO(task));
    }

    /**
     * 更新提醒任务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<RemindTaskVO> updateTask(RemindTaskUpdateRequest request) {
        RemindTask existing = remindTaskMapper.selectById(request.getId());
        if (existing == null) {
            throw new BusinessException("提醒任务不存在");
        }
        BeanUtil.copyNonNullProperties(request, existing);

        remindTaskMapper.updateById(existing);
        log.info("更新提醒任务成功，taskId: {}", request.getId());
        return Result.success(toRemindTaskVO(existing));
    }

    /**
     * 删除提醒任务（软删除）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteTask(Integer id) {
        try {
            RemindTask task = remindTaskMapper.selectById(id);
            if (task == null) {
                throw new BusinessException("提醒任务不存在");
            }

            task.setStatus(0);
            remindTaskMapper.updateById(task);
            log.info("删除提醒任务成功，taskId: {}", id);
            return Result.success(null);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除提醒任务失败", e);
            throw new BusinessException(SYSTEM_ERROR, "删除提醒任务失败");
        }
    }

    /**
     * 根据 ID 查询提醒任务
     */
    @Override
    public Result<RemindTaskVO> getTaskById(Integer id) {
        RemindTask task = remindTaskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException("提醒任务不存在");
        }
        return Result.success(toRemindTaskVO(task));
    }

    /**
     * 查询提醒任务列表
     */
    @Override
    public Result<List<RemindTaskVO>> listTasks(RemindQueryRequest request) {
        Integer userId = securityUtil.getCurrentUserId();
        LambdaQueryWrapper<RemindTask> wrapper = new LambdaQueryWrapper<>();

        if (request.getUserId() != null) {
            wrapper.eq(RemindTask::getUserId, userId);
        }
        if (request.getElderId() != null) {
            wrapper.eq(RemindTask::getElderId, request.getElderId());
        }
        if (request.getRemindType() != null) {
            wrapper.eq(RemindTask::getRemindType, request.getRemindType());
        }
        if (request.getStartDate() != null) {
            wrapper.ge(RemindTask::getRemindDate, request.getStartDate());
        }
        if (request.getEndDate() != null) {
            wrapper.le(RemindTask::getRemindDate, request.getEndDate());
        }
        if (request.getStatus() != null) {
            wrapper.eq(RemindTask::getStatus, request.getStatus());
        } else {
            wrapper.eq(RemindTask::getStatus, 1);
        }

        wrapper.orderByAsc(RemindTask::getRemindTime);
        List<RemindTask> tasks = remindTaskMapper.selectList(wrapper);
        return Result.success(convertTaskList(tasks));
    }

    /**
     * 根据老人 ID 查询提醒任务
     */
    @Override
    public Result<List<RemindTaskVO>> getTasksByElderId(Integer elderId) {
        List<RemindTask> tasks = remindTaskMapper.selectByElderId(elderId, null, null, null);
        return Result.success(convertTaskList(tasks));
    }

    /**
     * 查询当前用户今日的提醒任务
     */
    @Override
    public Result<List<RemindTaskVO>> getTodayTasks() {
        Integer userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(UNAUTHORIZED, "请先登录");
        }
        return getTodayTasksByUserId(userId);
    }


    /**
     * 查询指定用户今日的提醒任务（内部方法）
     */
    private Result<List<RemindTaskVO>> getTodayTasksByUserId(Integer userId) {
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<RemindTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RemindTask::getUserId, userId)
                .eq(RemindTask::getRemindDate, today)
                .eq(RemindTask::getStatus, 1);

        List<RemindTask> tasks = remindTaskMapper.selectList(wrapper);
        return Result.success(convertTaskList(tasks));
    }

    /**
     * 查询今日的提醒任务,全部，定时任务用
     */
    public Result<List<RemindTaskVO>> getAllTodayTasks() {
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<RemindTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RemindTask::getRemindDate, today)
                .eq(RemindTask::getStatus, 1);

        List<RemindTask> tasks = remindTaskMapper.selectList(wrapper);
        return Result.success(convertTaskList(tasks));
    }

    /**
     * 触发提醒任务并创建通知
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<NotificationVO> triggerTask(Integer taskId) {
        RemindTask task = remindTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("提醒任务不存在");
        }

        Notification notification = new Notification();
        notification.setTaskId(taskId);
        notification.setUserId(task.getUserId());
        notification.setElderId(task.getElderId());
        notification.setTitle(task.getTitle());
        notification.setContent(task.getContent());
        notification.setNotifyType(task.getRemindType());
        notification.setSendTime(LocalDateTime.now());
        notification.setReadStatus(0);
        notification.setStatus(1);

        notificationMapper.insert(notification);
        log.info("触发提醒成功，taskId: {}, notificationId: {}", taskId, notification.getId());
        return Result.success(toNotificationVO(notification));
    }

    /**
     * 根据 ID 查询通知记录
     */
    @Override
    public Result<NotificationVO> getNotificationById(Integer id) {
        Integer userId = securityUtil.getCurrentUserId();
        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new BusinessException("通知记录不存在");
        }
        return Result.success(toNotificationVO(notification));
    }

    /**
     * 查询用户的所有通知记录
     */
    @Override
    public Result<List<NotificationVO>> getMyNotifications() {
        Integer userId = securityUtil.getCurrentUserId();
        List<Notification> notifications = notificationMapper.selectByUserId(userId);
        return Result.success(notifications.stream().map(this::toNotificationVO).toList());
    }

    /**
     * 标记通知为已读
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> markAsRead(Integer notificationId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null) {
            throw new BusinessException("通知记录不存在");
        }

        notification.setReadStatus(1);
        notification.setReadTime(LocalDateTime.now());
        notificationMapper.updateById(notification);
        log.info("标记通知为已读，notificationId: {}", notificationId);
        return Result.success();
    }

    /**
     * 标记用户的所有通知为已读
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> markAllAsRead() {
        Integer userId = securityUtil.getCurrentUserId();
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getReadStatus, 0);

        List<Notification> notifications = notificationMapper.selectList(wrapper);
        for (Notification notification : notifications) {
            notification.setReadStatus(1);
            notification.setReadTime(LocalDateTime.now());
            notificationMapper.updateById(notification);
        }
        log.info("标记所有通知为已读，userId: {}", userId);
        return Result.success();
    }

    /**
     * 查询用户的未读通知数量
     */
    @Override
    public Result<Long> countUnread() {
        Integer userId = securityUtil.getCurrentUserId();
        Long count = notificationMapper.countUnread(userId);
        return Result.success(count);
    }


    private RemindVO toRemindVO(Remind remind) {
        RemindVO vo = new RemindVO();
        BeanUtils.copyProperties(remind, vo);
        return vo;
    }

    private RemindTaskVO toRemindTaskVO(RemindTask task) {
        RemindTaskVO vo = new RemindTaskVO();
        BeanUtils.copyProperties(task, vo);
        return vo;
    }

    private List<RemindTaskVO> convertTaskList(List<RemindTask> tasks) {
        return tasks.stream().map(this::toRemindTaskVO).toList();
    }

    private NotificationVO toNotificationVO(Notification notification) {
        NotificationVO vo = new NotificationVO();
        BeanUtils.copyProperties(notification, vo);
        return vo;
    }
}



