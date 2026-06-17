package com.elderlycare.service;

import com.elderlycare.pojo.vo.RemindTaskVO;
import com.elderlycare.pojo.vo.NotificationVO;

/**
 * 提醒推送服务接口
 */
public interface RemindPushService {

    /**
     * 推送提醒通知到前端
     */
    void pushNotification(Integer userId, RemindTaskVO task, NotificationVO notification);

    /**
     * 触发语音播报
     */
    void triggerVoiceBroadcast(RemindTaskVO task);

    /**
     * 触发弹窗提醒
     */
    void triggerPopupAlert(RemindTaskVO task);
}
