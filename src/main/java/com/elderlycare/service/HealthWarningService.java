package com.elderlycare.service;

import com.elderlycare.common.Result;
import com.elderlycare.pojo.dto.health.HealthDTO;
import com.elderlycare.pojo.vo.HealthVO;
import com.elderlycare.pojo.vo.HealthWarningVO;

import java.util.List;

/**
 * 健康预警服务接口
 */
public interface HealthWarningService {
    
    /**
     * 录入健康数据并自动预警
     */
    Result<HealthVO> saveHealthWithWarning(HealthDTO request);
    
    /**
     * 查询用户的预警列表
     */
    Result<List<HealthWarningVO>> getUserWarnings(Integer userId);
    
    /**
     * 查询未读预警数量
     */
    Result<Integer> countUnreadWarnings(Integer userId);
    
    /**
     * 标记预警为已读
     */
    Result<Void> markWarningAsRead(Integer notificationId);
    
    /**
     * 标记所有预警为已读
     */
    Result<Void> markAllWarningsAsRead(Integer userId);
}
