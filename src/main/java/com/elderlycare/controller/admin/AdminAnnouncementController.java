package com.elderlycare.controller.admin;

import com.elderlycare.common.Result;
import com.elderlycare.pojo.entity.Notification;
import com.elderlycare.pojo.vo.NotificationVO;
import com.elderlycare.mapper.NotificationMapper;
import org.springframework.beans.BeanUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 公告管理控制器
 */
@RestController
@RequestMapping("/admin/announcements")
@RequiredArgsConstructor
public class AdminAnnouncementController {

    private final NotificationMapper notificationMapper;

    /**
     * 发布公告
     */
    @PostMapping
    public Result<Integer> publishAnnouncement(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(defaultValue = "2") Integer notifyType,
            @RequestParam(required = false) Integer targetUserId
    ) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setNotifyType(notifyType);
        notification.setUserId(targetUserId);
        notification.setSendTime(LocalDateTime.now());
        notification.setStatus(1);
        
        notificationMapper.insert(notification);
        return Result.success(notification.getId());
    }

    /**
     * 查询公告列表
     */
    @GetMapping
    public Result<List<NotificationVO>> getAnnouncements(
            @RequestParam(required = false) Integer notifyType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        // TODO: 实现分页查询
        Map<String, Object> result = new HashMap<>();
        List<Notification> entities = notificationMapper.selectList(null);
        return Result.success(entities.stream().map(this::toNotificationVO).toList());
    }

    private NotificationVO toNotificationVO(Notification entity) {
        NotificationVO vo = new NotificationVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteAnnouncement(@PathVariable Integer id) {
        notificationMapper.deleteById(id);
        return Result.success();
    }
}
