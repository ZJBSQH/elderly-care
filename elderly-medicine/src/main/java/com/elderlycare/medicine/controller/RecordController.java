package com.elderlycare.medicine.controller;

import com.elderlycare.common.core.result.Result;
import com.elderlycare.medicine.service.RecordService;
import com.elderlycare.medicine.vo.RecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    /**
     * 服药确认
     *
     * @param taskId  用药计划 ID
     * @param elderId 老人 ID
     * @return 操作结果
     */
    @PostMapping("/take")
    public Result<Void> takeMedicine(@RequestParam Integer taskId,
                                     @RequestParam Integer elderId) {
        return recordService.takeMedicine(taskId, elderId);
    }

    /**
     * 获取今日服药记录
     *
     * @param elderId 老人 ID
     * @return 今日服药记录列表
     */
    @GetMapping("/today")
    public Result<List<RecordVO>> getTodayRecords(@RequestParam Integer elderId) {
        return recordService.getTodayRecords(elderId);
    }

    /**
     * 获取历史服药记录
     *
     * @param elderId 老人 ID
     * @param page    页码
     * @param size    每页大小
     * @return 历史服药记录列表
     */
    @GetMapping("/history")
    public Result<List<RecordVO>> getHistoryRecords(@RequestParam Integer elderId,
                                                     @RequestParam Integer page,
                                                     @RequestParam Integer size) {
        return recordService.getHistoryRecords(elderId, page, size);
    }

    /**
     * 检查今日服药状态
     *
     * @param taskId 用药计划 ID
     * @return true-已服
     */
    @GetMapping("/check")
    public Result<Boolean> checkRecord(@RequestParam Integer taskId) {
        return recordService.checkRecord(taskId);
    }

    /**
     * 标记漏服
     *
     * @param taskId  用药计划 ID
     * @param elderId 老人 ID
     * @return 操作结果
     */
    @PostMapping("/missed")
    public Result<Void> markMissed(@RequestParam Integer taskId, @RequestParam Integer elderId) {
        return recordService.markMissed(taskId, elderId);
    }
}
