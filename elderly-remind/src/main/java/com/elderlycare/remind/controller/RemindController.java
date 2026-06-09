package com.elderlycare.remind.controller;

import com.elderlycare.common.core.result.Result;
import com.elderlycare.remind.dto.RemindSettingUpdateRequest;
import com.elderlycare.remind.dto.RemindTaskCreateRequest;
import com.elderlycare.remind.dto.RemindTaskUpdateRequest;
import com.elderlycare.remind.service.RemindService;
import com.elderlycare.remind.vo.NotificationVO;
import com.elderlycare.remind.vo.RemindTaskVO;
import com.elderlycare.remind.vo.RemindVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 提醒服务控制器
 *
 * @author 郑
 */
@RestController
@RequestMapping("/remind")
@RequiredArgsConstructor
public class RemindController {

    /** 提醒服务 */
    private final RemindService remindService;

    /**
     * 获取当前用户的提醒设置
     */
    @GetMapping("/settings")
    public Result<RemindVO> getSettings() {
        return remindService.getOrCreateSettings();
    }

    /**
     * 更新提醒设置
     */
    @PutMapping("/settings")
    public Result<RemindVO> updateSettings(@RequestBody RemindSettingUpdateRequest request) {
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
     * 删除提醒任务
     */
    @DeleteMapping("/task/delete/{id}")
    public Result<Void> deleteTask(@PathVariable("id") Integer id) {
        return remindService.deleteTask(id);
    }

    /**
     * 根据 ID 获取任务详情
     */
    @GetMapping("/task/{id}")
    public Result<RemindTaskVO> getTaskById(@PathVariable("id") Integer id) {
        return remindService.getTaskById(id);
    }

    /**
     * 分页查询当前用户的任务列表
     */
    @GetMapping("/task/list")
    public Result<List<RemindTaskVO>> listTasks(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return remindService.listTasks(page, pageSize);
    }

    /**
     * 获取当天提醒任务（可选 elderId，不传则按当前用户查询）
     */
    @GetMapping("/task/today")
    public Result<List<RemindTaskVO>> getTodayTasks(@RequestParam(required = false) Integer elderId) {
        return remindService.getTodayTasks(elderId);
    }

    /**
     * 分页查询当前用户的通知列表
     */
    @GetMapping("/notification/list")
    public Result<List<NotificationVO>> listNotifications(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return remindService.getMyNotifications(page, pageSize);
    }

    /**
     * 获取当前用户通知列表（别名，等同 /notification/list）
     */
    @GetMapping("/notification/user/my")
    public Result<List<NotificationVO>> getUserMyNotifications(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return remindService.getMyNotifications(page, pageSize);
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/notification/unread")
    public Result<Integer> countUnread() {
        return remindService.countUnread();
    }

    /**
     * 标记通知为已读
     */
    @PostMapping("/notification/read/{id}")
    public Result<Void> markAsRead(@PathVariable("id") Integer id) {
        return remindService.markAsRead(id);
    }

    /**
     * 标记全部通知为已读
     */
    @PostMapping("/notification/read-all")
    public Result<Void> markAllAsRead() {
        return remindService.markAllAsRead();
    }

    /**
     * 根据老人ID查询提醒任务
     */
    @GetMapping("/task/elder/{elderId}")
    public Result<List<RemindTaskVO>> getTasksByElderId(@PathVariable Integer elderId) {
        return remindService.getTasksByElderId(elderId);
    }
}
