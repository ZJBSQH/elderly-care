package com.elderlycare.remind.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.common.core.exception.BusinessException;
import com.elderlycare.common.core.result.Result;
import com.elderlycare.common.security.util.SecurityUtil;
import com.elderlycare.remind.dto.RemindSettingUpdateRequest;
import com.elderlycare.remind.dto.RemindTaskCreateRequest;
import com.elderlycare.remind.dto.RemindTaskUpdateRequest;
import com.elderlycare.remind.entity.Notification;
import com.elderlycare.remind.entity.Remind;
import com.elderlycare.remind.entity.RemindTask;
import com.elderlycare.remind.mapper.NotificationMapper;
import com.elderlycare.remind.mapper.RemindMapper;
import com.elderlycare.remind.mapper.RemindTaskMapper;
import com.elderlycare.remind.service.RemindService;
import com.elderlycare.remind.vo.NotificationVO;
import com.elderlycare.remind.vo.RemindTaskVO;
import com.elderlycare.remind.vo.RemindVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.elderlycare.common.core.util.BeanUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.elderlycare.remind.dto.RemindErrorCode.*;

/**
 * 提醒服务实现
 *
 * @author 郑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RemindServiceImpl implements RemindService {

    /** 默认音量 */
    private static final Integer DEFAULT_VOLUME = 50;
    /** 默认需要语音播报 */
    private static final Integer DEFAULT_NEED_VOICE = 1;
    /** 默认需要弹窗提醒 */
    private static final Integer DEFAULT_NEED_POPUP = 1;
    /** 默认状态（启用） */
    private static final Integer DEFAULT_STATUS = 1;
    /** 默认读取状态（未读） */
    private static final Integer DEFAULT_READ_STATUS = 0;

    /** 提醒设置 Mapper */
    private final RemindMapper remindMapper;
    /** 提醒任务 Mapper */
    private final RemindTaskMapper remindTaskMapper;
    /** 通知记录 Mapper */
    private final NotificationMapper notificationMapper;
    /** 安全工具类 */
    private final SecurityUtil securityUtil;

    /**
     * 获取当前登录用户 ID
     */
    private Integer getCurrentUserId() {
        Integer userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        return userId;
    }

    @Override
    public Result<RemindVO> getOrCreateSettings() {
        Integer userId = getCurrentUserId();
        LambdaQueryWrapper<Remind> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Remind::getUserId, userId);
        Remind remind = remindMapper.selectOne(wrapper);

        if (remind == null) {
            remind = new Remind();
            remind.setUserId(userId);
            remind.setVolume(DEFAULT_VOLUME);
            remindMapper.insert(remind);
            log.info("为用户 {} 创建默认提醒设置", userId);
        }

        RemindVO vo = new RemindVO();
        BeanUtil.copyProperties(remind, vo);
        return Result.success(vo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<RemindVO> updateSettings(RemindSettingUpdateRequest request) {
        Integer userId = getCurrentUserId();
        LambdaQueryWrapper<Remind> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Remind::getUserId, userId);
        Remind remind = remindMapper.selectOne(wrapper);

        if (remind == null) {
            throw new BusinessException(REMIND_NOT_EXIST);
        }

        BeanUtil.copyNonNullProperties(request, remind);
        remindMapper.updateById(remind);

        RemindVO vo = new RemindVO();
        BeanUtil.copyProperties(remind, vo);
        return Result.success(vo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<RemindTaskVO> createTask(RemindTaskCreateRequest request) {
        Integer userId = getCurrentUserId();

        RemindTask task = new RemindTask();
        task.setUserId(userId);
        task.setElderId(request.getElderId());
        task.setMedicineId(request.getMedicineId());
        task.setTitle(request.getTitle());
        task.setContent(request.getContent());
        task.setRemindTime(request.getRemindTime() != null ? request.getRemindTime() : LocalTime.of(8, 0));
        task.setRemindDate(request.getRemindDate() != null ? request.getRemindDate() : LocalDate.now());
        task.setRemindType(request.getRemindType());
        task.setNeedVoice(request.getNeedVoice() != null ? request.getNeedVoice() : DEFAULT_NEED_VOICE);
        task.setNeedPopup(request.getNeedPopup() != null ? request.getNeedPopup() : DEFAULT_NEED_POPUP);
        task.setVoiceText(request.getVoiceText());
        task.setRepeatCycle(request.getRepeatCycle());
        task.setEndDate(request.getEndDate());
        task.setRemark(request.getRemark());
        task.setStatus(DEFAULT_STATUS);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        remindTaskMapper.insert(task);
        log.info("用户 {} 创建提醒任务成功, 任务 ID: {}", userId, task.getId());

        RemindTaskVO vo = new RemindTaskVO();
        BeanUtil.copyProperties(task, vo);
        return Result.success(vo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<RemindTaskVO> updateTask(RemindTaskUpdateRequest request) {
        Integer userId = getCurrentUserId();
        RemindTask task = remindTaskMapper.selectById(request.getId());

        if (task == null) {
            throw new BusinessException(TASK_NOT_EXIST);
        }
        if (!task.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权修改他人的提醒任务");
        }

        BeanUtil.copyNonNullProperties(request, task);
        task.setUpdateTime(LocalDateTime.now());
        remindTaskMapper.updateById(task);
        log.info("用户 {} 更新提醒任务成功, 任务 ID: {}", userId, task.getId());

        RemindTaskVO vo = new RemindTaskVO();
        BeanUtil.copyProperties(task, vo);
        return Result.success(vo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteTask(Integer taskId) {
        Integer userId = getCurrentUserId();
        RemindTask task = remindTaskMapper.selectById(taskId);

        if (task == null) {
            throw new BusinessException(TASK_NOT_EXIST);
        }
        if (!task.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除他人的提醒任务");
        }

        remindTaskMapper.deleteById(taskId);
        log.info("用户 {} 删除提醒任务成功, 任务 ID: {}", userId, taskId);
        return Result.success();
    }

    @Override
    public Result<RemindTaskVO> getTaskById(Integer taskId) {
        Integer userId = getCurrentUserId();
        RemindTask task = remindTaskMapper.selectById(taskId);

        if (task == null) {
            throw new BusinessException(TASK_NOT_EXIST);
        }
        if (!task.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权查看他人的提醒任务");
        }

        RemindTaskVO vo = new RemindTaskVO();
        BeanUtil.copyProperties(task, vo);
        return Result.success(vo);
    }

    @Override
    public Result<List<RemindTaskVO>> listTasks(Integer page, Integer pageSize) {
        Integer userId = getCurrentUserId();
        Page<RemindTask> pageParam = new Page<>(page != null ? page : 1, pageSize != null ? pageSize : 10);

        LambdaQueryWrapper<RemindTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RemindTask::getUserId, userId)
                .orderByDesc(RemindTask::getCreateTime);
        Page<RemindTask> resultPage = remindTaskMapper.selectPage(pageParam, wrapper);

        List<RemindTaskVO> voList = resultPage.getRecords().stream().map(task -> {
            RemindTaskVO vo = new RemindTaskVO();
            BeanUtil.copyProperties(task, vo);
            return vo;
        }).collect(Collectors.toList());

        return Result.success(voList);
    }

    @Override
    public Result<List<RemindTaskVO>> getTodayTasks(Integer elderId) {
        List<RemindTask> todayTasks;
        if (elderId != null) {
            todayTasks = remindTaskMapper.selectTodayTasksByElderId(elderId);
        } else {
            Integer userId = getCurrentUserId();
            todayTasks = remindTaskMapper.selectTodayTasksByUserId(userId);
        }
        List<RemindTaskVO> voList = todayTasks.stream()
                .map(task -> {
                    RemindTaskVO vo = new RemindTaskVO();
                    BeanUtil.copyProperties(task, vo);
                    return vo;
                }).collect(Collectors.toList());
        return Result.success(voList);
    }

    @Override
    public List<RemindTask> getAllTodayTasksRaw() {
        return remindTaskMapper.selectTodayTasks();
    }

    @Override
    public Result<List<NotificationVO>> getMyNotifications(Integer page, Integer pageSize) {
        Integer userId = getCurrentUserId();
        Page<Notification> pageParam = new Page<>(page != null ? page : 1, pageSize != null ? pageSize : 10);

        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getStatus, 1)
                .orderByDesc(Notification::getSendTime);
        Page<Notification> resultPage = notificationMapper.selectPage(pageParam, wrapper);

        List<NotificationVO> voList = resultPage.getRecords().stream().map(notification -> {
            NotificationVO vo = new NotificationVO();
            BeanUtil.copyProperties(notification, vo);
            return vo;
        }).collect(Collectors.toList());

        return Result.success(voList);
    }

    @Override
    public Result<Integer> countUnread() {
        Integer userId = getCurrentUserId();
        Integer count = notificationMapper.countUnread(userId);
        return Result.success(count);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> markAsRead(Integer notificationId) {
        Notification notification = notificationMapper.selectById(notificationId);

        if (notification == null) {
            throw new BusinessException(NOTIFICATION_NOT_EXIST);
        }

        notification.setReadStatus(1);
        notification.setReadTime(LocalDateTime.now());
        notificationMapper.updateById(notification);
        log.info("通知 {} 已标记为已读", notificationId);
        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> markAllAsRead() {
        Integer userId = getCurrentUserId();
        notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getReadStatus, 0)
                .set(Notification::getReadStatus, 1)
                .set(Notification::getReadTime, LocalDateTime.now()));
        log.info("用户 {} 全部通知已标记为已读", userId);
        return Result.success();
    }

    @Override
    public Result<List<RemindTaskVO>> getTasksByElderId(Integer elderId) {
        LambdaQueryWrapper<RemindTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RemindTask::getElderId, elderId)
                .orderByDesc(RemindTask::getCreateTime);
        List<RemindTask> tasks = remindTaskMapper.selectList(wrapper);
        List<RemindTaskVO> voList = tasks.stream().map(task -> {
            RemindTaskVO vo = new RemindTaskVO();
            BeanUtil.copyProperties(task, vo);
            return vo;
        }).collect(Collectors.toList());
        return Result.success(voList);
    }
}
