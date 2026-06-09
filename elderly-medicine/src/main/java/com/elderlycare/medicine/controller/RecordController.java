package com.elderlycare.medicine.controller;

import com.elderlycare.common.core.exception.BusinessException;
import com.elderlycare.common.core.result.Result;
import com.elderlycare.common.security.util.SecurityUtil;
import com.elderlycare.medicine.dto.RecordQuery;
import com.elderlycare.medicine.service.RecordService;
import com.elderlycare.medicine.vo.RecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 服药记录控制器
 *
 * @author 郑
 */
@Slf4j
@RestController
@RequestMapping("/record")
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;
    private final SecurityUtil securityUtil;

    /**
     * 解析 elderId：优先使用前端传参，为空则从 JWT 安全上下文自动获取
     */
    private Integer resolveElderId(Integer requestElderId) {
        if (requestElderId != null) {
            return requestElderId;
        }
        Integer userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(400, "无法识别当前用户身份");
        }
        return userId;
    }

    /**
     * 服药确认
     */
    @PostMapping("/take")
    public Result<Void> takeMedicine(@RequestBody Map<String, Object> body) {
        Integer taskId = parseTaskId(body);
        Integer elderId = resolveElderId(null);
        return recordService.takeMedicine(taskId, elderId);
    }

    /**
     * 标记漏服
     */
    @PostMapping("/missed")
    public Result<Void> markMissed(@RequestBody Map<String, Object> body) {
        Integer taskId = parseTaskId(body);
        Integer elderId = resolveElderId(null);
        return recordService.markMissed(taskId, elderId);
    }

    /**
     * 获取今日服药记录
     */
    @GetMapping("/today")
    public Result<List<RecordVO>> getTodayRecords(@RequestParam(required = false) Integer elderId) {
        elderId = resolveElderId(elderId);
        return recordService.getTodayRecords(elderId);
    }

    /**
     * 获取历史服药记录 / 导出记录
     */
    @GetMapping({"/history", "/export"})
    public Result<List<RecordVO>> getHistoryRecords(RecordQuery query) {
        query.setElderId(resolveElderId(query.getElderId()));
        return recordService.getHistoryRecords(query);
    }

    /**
     * 检查今日服药状态
     */
    @GetMapping("/check")
    public Result<java.util.Map<String, Integer>> checkRecord(@RequestParam Integer taskId) {
        return recordService.checkRecord(taskId);
    }

    /** 从请求体中提取 taskId */
    private Integer parseTaskId(Map<String, Object> body) {
        Object raw = body.get("taskId");
        if (raw == null) {
            throw new BusinessException(400, "缺少必填参数 taskId");
        }
        return raw instanceof Integer ? (Integer) raw : Integer.parseInt(raw.toString());
    }
}
