package com.elderlycare.service.impl;

import com.elderlycare.mapper.ElderMapper;
import com.elderlycare.mapper.HealthMapper;
import com.elderlycare.mapper.NotificationMapper;
import com.elderlycare.common.Result;
import com.elderlycare.common.exception.BusinessException;
import com.elderlycare.common.util.HealthCheckUtil;
import com.elderlycare.pojo.dto.health.HealthDTO;
import com.elderlycare.pojo.entity.Elder;
import com.elderlycare.pojo.entity.Health;
import com.elderlycare.pojo.entity.Notification;
import com.elderlycare.pojo.vo.HealthVO;
import com.elderlycare.pojo.vo.HealthWarningVO;
import com.elderlycare.service.HealthWarningService;
import com.elderlycare.common.websocket.NotifyWebSocket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 健康预警服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthWarningServiceImpl implements HealthWarningService {
    
    private final HealthMapper healthMapper;
    private final NotificationMapper notificationMapper;
    private final ElderMapper elderMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<HealthVO> saveHealthWithWarning(HealthDTO request) {
        // 保存健康记录并检测异常指标，如有异常则创建预警通知

            Health health = new Health();
            BeanUtils.copyProperties(request, health);
            
            // 2. 检查异常并生成预警信息
            List<AbnormalInfo> abnormalInfos = checkAbnormal(
                request.getBloodPressure(),
                request.getBloodSugar(),
                request.getHeartRate(),
                request.getWeight()
            );
            
            // 3. 设置异常标记
            health.setWarningFlag(abnormalInfos.isEmpty() ? 0 : 1);
            
            // 4. 保存健康记录
            healthMapper.insert(health);
            
            // 5. 如果有异常，创建预警通知
            if (!abnormalInfos.isEmpty()) {
                createWarningNotifications(health, abnormalInfos);
            }
            
            log.info("健康数据录入成功，elderId: {}, 异常数量：{}",
                     request.getElderId(), abnormalInfos.size());

            return Result.success(toHealthVO(health));



    }
    
    @Override
    public Result<List<HealthWarningVO>> getUserWarnings(Integer userId) {
        // 查询指定用户关联的所有老人的健康预警列表

            // 1. 查询用户关联的老人 ID 列表
            List<Integer> elderIds = getElderIdsByUserId(userId);
            
            if (elderIds.isEmpty()) {
                return Result.success(new ArrayList<>());
            }
            
            // 2. 查询预警通知
            List<Notification> notifications = notificationMapper.selectUserWarnings(userId, elderIds);
            
            // 3. 转换为 VO
            List<HealthWarningVO> warnings = notifications.stream()
                    .map(this::convertToWarningVO)
                    .collect(Collectors.toList());
            
            return Result.success(warnings);
    }
    
    @Override
    public Result<Integer> countUnreadWarnings(Integer userId) {
        // 统计指定用户的未读健康预警数量

            List<Integer> elderIds = getElderIdsByUserId(userId);
            if (elderIds.isEmpty()) {
                return Result.success(0);
            }
            
            int count = notificationMapper.countUnreadWarnings(userId, elderIds);
            return Result.success(count);

    }
    
    @Override
    public Result<Void> markWarningAsRead(Integer notificationId) {
        // 将指定的健康预警标记为已读

            Notification notification = notificationMapper.selectById(notificationId);
            if (notification == null) {
                throw new BusinessException("预警不存在");
            }
            
            notification.setReadStatus(1);
            notification.setReadTime(LocalDateTime.now());
            notificationMapper.updateById(notification);
            
            return Result.success(null);
    }
    
    @Override
    public Result<Void> markAllWarningsAsRead(Integer userId) {
        // 将指定用户的所有健康预警批量标记为已读

            List<Integer> elderIds = getElderIdsByUserId(userId);
            if (elderIds.isEmpty()) {
                return Result.success(null);
            }
            
            notificationMapper.markAllWarningsAsRead(userId, elderIds);
            return Result.success(null);

    }
    
    /**
     * 检查异常指标（阈值统一由 HealthCheckUtil 管理）
     */
    private List<AbnormalInfo> checkAbnormal(String bloodPressure, BigDecimal bloodSugar,
                                             Integer heartRate, BigDecimal weight) {
        List<AbnormalInfo> abnormalInfos = new ArrayList<>();

        int[] bp = HealthCheckUtil.parseBloodPressure(bloodPressure);
        if (bp != null && HealthCheckUtil.isBloodPressureAbnormal(bp[0], bp[1])) {
            String currentValue = String.format("%d/%d mmHg", bp[0], bp[1]);
            String detail = String.format("收缩压%dmmHg，舒张压%dmmHg", bp[0], bp[1]);
            abnormalInfos.add(new AbnormalInfo(
                    "BLOOD_PRESSURE", "血压异常", detail, currentValue, "90-140/60-90 mmHg"));
        }

        if (HealthCheckUtil.isBloodSugarAbnormal(bloodSugar)) {
            boolean high = bloodSugar.compareTo(HealthCheckUtil.BLOOD_SUGAR_MAX) > 0;
            abnormalInfos.add(new AbnormalInfo(
                    "BLOOD_SUGAR", high ? "血糖偏高" : "血糖偏低",
                    String.format("血糖值%.1f mmol/L", bloodSugar),
                    String.format("%.1f mmol/L", bloodSugar),
                    "3.9-6.1 mmol/L"));
        }

        if (HealthCheckUtil.isHeartRateAbnormal(heartRate)) {
            boolean high = heartRate > HealthCheckUtil.HEART_RATE_MAX;
            abnormalInfos.add(new AbnormalInfo(
                    "HEART_RATE", high ? "心率过快" : "心率过缓",
                    String.format("心率%d次/分", heartRate),
                    String.format("%d 次/分", heartRate),
                    "60-100 次/分"));
        }

        if (HealthCheckUtil.isWeightAbnormal(weight)) {
            boolean high = weight.compareTo(HealthCheckUtil.WEIGHT_MAX) > 0;
            abnormalInfos.add(new AbnormalInfo(
                    "WEIGHT", high ? "体重超标" : "体重过轻",
                    String.format("体重%.1fkg，超过正常范围", weight),
                    String.format("%.1f kg", weight),
                    "40-150 kg"));
        }

        return abnormalInfos;
    }
    
    /**
     * 创建预警通知
     */
    private void createWarningNotifications(Health health, List<AbnormalInfo> abnormalInfos) {
        // 为异常指标创建数据库通知记录并通过WebSocket推送给对应用户
        // 1. 查询老人信息
        Elder elder = elderMapper.selectById(health.getElderId());
        if (elder == null) {
            return;
        }
        
        // 2. 获取老人关联的用户 ID（老人自己）
        Integer userId = elder.getUserId();
        
        // 3. 为每个异常创建通知
        for (AbnormalInfo abnormal : abnormalInfos) {
            Notification notification = new Notification();
            notification.setTaskId(null);
            notification.setUserId(userId);
            notification.setElderId(health.getElderId());
            notification.setTitle("健康预警：" + abnormal.getWarningType());
            notification.setContent(String.format(
                "检测到异常：%s（%s）。正常范围：%s",
                abnormal.getWarningType(),
                abnormal.getDetail(),
                abnormal.getNormalRange()
            ));
            notification.setNotifyType(2); // 2-健康预警
            notification.setSendTime(LocalDateTime.now());
            notification.setReadStatus(0);
            notification.setStatus(1);
            
            notificationMapper.insert(notification);
            
            // 4. WebSocket 推送
            NotifyWebSocket.NotifyMessage message = new NotifyWebSocket.NotifyMessage(
                200,
                "健康预警",
                notification
            );
            NotifyWebSocket.sendToUser(String.valueOf(userId), message);
            
            log.info("创建健康预警通知，userId: {}, type: {}", userId, abnormal.getAbnormalType());
        }
    }
    
    /**
     * 获取用户关联的老人 ID 列表
     */
    private List<Integer> getElderIdsByUserId(Integer userId) {
        // 根据用户ID查询其关联的老人ID列表
        // 简单实现：查询用户自己
        Elder elder = elderMapper.selectByUserId(userId);
        List<Integer> elderIds = new ArrayList<>();
        if (elder != null) {
            elderIds.add(elder.getId());
        }
        return elderIds;
    }
    
    /**
     * 转换为 VO
     */
    private HealthVO toHealthVO(Health health) {
        HealthVO vo = new HealthVO();
        BeanUtils.copyProperties(health, vo);
        return vo;
    }

    private HealthWarningVO convertToWarningVO(Notification notification) {
        // 将通知实体转换为健康预警VO对象
        HealthWarningVO vo = new HealthWarningVO();
        vo.setId(notification.getId());
        vo.setElderId(notification.getElderId());
        vo.setWarningTime(notification.getSendTime());
        vo.setIsRead(notification.getReadStatus() == 1);
        
        // 从内容中提取信息
        String content = notification.getContent();
        vo.setAbnormalDetail(content);
        vo.setCurrentValue("");
        vo.setNormalRange("");
        
        return vo;
    }
    
    /**
     * 异常信息内部类
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class AbnormalInfo {
        private String abnormalType;
        private String warningType;
        private String detail;
        private String currentValue;
        private String normalRange;
    }
}
