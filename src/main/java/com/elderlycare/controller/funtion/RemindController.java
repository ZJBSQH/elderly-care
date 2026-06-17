package com.elderlycare.controller.funtion;

import com.elderlycare.common.Result;
import com.elderlycare.pojo.dto.remind.RemindQueryRequest;
import com.elderlycare.pojo.dto.remind.RemindSettingUpdateRequest;
import com.elderlycare.pojo.dto.remind.RemindTaskCreateRequest;
import com.elderlycare.pojo.dto.remind.RemindTaskUpdateRequest;
import com.elderlycare.pojo.vo.NotificationVO;
import com.elderlycare.pojo.vo.RemindTaskVO;
import com.elderlycare.pojo.vo.RemindVO;
import com.elderlycare.service.RemindService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 提醒管理控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/remind")
public class RemindController {

    private final RemindService remindService;



    /**
     * 获取或创建用户提醒设置
     */
    @GetMapping("/settings")
    public Result<RemindVO> getOrCreateSettings() {
        return remindService.getOrCreateSettings();
    }



    /**
     * 更新用户提醒设置
     */
    @PutMapping("/settings")
    public Result<RemindVO> updateSettings(@Valid @RequestBody RemindSettingUpdateRequest request) {
        return remindService.updateSettings(request);
    }

    /**
     * 创建提醒任务
     */
    @PostMapping("/task/create")
    public Result<RemindTaskVO> createTask(@Valid @RequestBody RemindTaskCreateRequest request) {
        return remindService.createTask(request);
    }

    /**
     * 更新提醒任务
     */
    @PutMapping("/task/update")
    public Result<RemindTaskVO> updateTask(@Valid @RequestBody RemindTaskUpdateRequest request) {
        return remindService.updateTask(request);
    }

    /**
     * 删除提醒任务（软删除）
     */
    @DeleteMapping("/task/delete/{id}")
    public Result<Void> deleteTask(@PathVariable Integer id) {
        return remindService.deleteTask(id);
    }

    /**
     * 根据 ID 查询提醒任务
     */
    @GetMapping("/task/{id}")
    public Result<RemindTaskVO> getTaskById(@PathVariable Integer id) {
        return remindService.getTaskById(id);
    }

    /**
     * 查询提醒任务列表（支持条件筛选）
     */
    @GetMapping("/task/list")
    public Result<List<RemindTaskVO>> listTasks(RemindQueryRequest request) {
        return remindService.listTasks(request);
    }

    /**
     * 根据老人 ID 查询提醒任务
     */
    @GetMapping("/task/elder/{elderId}")
    public Result<List<RemindTaskVO>> getTasksByElderId(@PathVariable Integer elderId) {
        return remindService.getTasksByElderId(elderId);
    }

    /**
     * 查询今日的提醒任务
     */
    @GetMapping("/task/today")
    public Result<List<RemindTaskVO>> getTodayTasks() {
        return remindService.getTodayTasks();
    }

    /**
     * 手动触发提醒任务（测试用）
     */
    @PostMapping("/task/trigger/{taskId}")
    public Result<Void> triggerTask(@PathVariable Integer taskId) {
        return remindService.triggerTask(taskId).map(data -> null);
    }

    /**
     * 根据 ID 查询通知记录
     */
    @GetMapping("/notification/{id}")
    public Result<NotificationVO> getNotificationById(@PathVariable Integer id) {
        return remindService.getNotificationById(id);
    }

    /**
     * 查询用户的所有通知记录
     */
    @GetMapping("/notification/user/my")
    public Result<List<NotificationVO>> getUserNotifications() {
        return remindService.getMyNotifications();
    }

    /**
     * 标记通知为已读
     */
    @PostMapping("/notification/read/{notificationId}")
    public Result<Void> markAsRead(@PathVariable Integer notificationId) {
        return remindService.markAsRead(notificationId);
    }

    /**
     * 标记用户的所有通知为已读
     */
    @PostMapping("/notification/read-all")
    public Result<Void> markAllAsRead() {
        return remindService.markAllAsRead();
    }

    /**
     * 查询用户的未读通知数量
     */
    @GetMapping("/notification/unread")
    public Result<Long> countUnread() {
        return remindService.countUnread();
    }
}
