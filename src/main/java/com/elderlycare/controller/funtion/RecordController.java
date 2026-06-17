package com.elderlycare.controller.funtion;

import com.elderlycare.common.Result;
import com.elderlycare.pojo.dto.record.RecordHistoryQueryRequest;
import com.elderlycare.pojo.vo.MedicineExportVO;
import com.elderlycare.pojo.vo.RecordVO;
import com.elderlycare.service.RecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 服药记录控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/record")
public class RecordController {

    private final RecordService recordService;

    /**
     * 记录服药（一键确认）
     */
    @PostMapping("/take")
    public Result<RecordVO> take(@RequestBody Map<String, Integer> request) {
        return recordService.takeMedicine(request.get("taskId"));
    }

    /**
     * 查询今日记录
     */
    @GetMapping("/today")
    public Result<List<RecordVO>> today(@RequestParam Integer elderId) {
        return recordService.getTodayRecords(elderId);
    }

    /**
     * 检查是否已服药
     */
    @GetMapping("/check")
    public Result<Map<String, Object>> check(@RequestParam Integer taskId) {
        return recordService.checkRecord(taskId);
    }

    /**
     * 标记漏服
     */
    @PostMapping("/missed")
    public Result<RecordVO> missed(@RequestBody Map<String, Integer> request) {
        return recordService.markAsMissed(request.get("taskId"));
    }


    /**
     * 查询用药历史
     */
    @GetMapping("/history")
    public Result<List<RecordVO>> history(RecordHistoryQueryRequest request) {
        return recordService.getHistoryRecords(request);
    }

    /**
     * 导出用药清单
     */
    @GetMapping("/export")
    public Result<List<MedicineExportVO>> export(RecordHistoryQueryRequest request) {
        return recordService.exportMedicineList(request);
    }

    /**
     * 家属查看绑定的老人列表
     */
    @GetMapping("/elders")
    public Result<List<Map<String, Object>>> getBoundElders() {
        return recordService.getBoundEldersForFamily();
    }

    /**
     * 查看指定老人的今日服药记录（需验证权限）
     */
    @GetMapping("/today/{elderId}")
    public Result<List<RecordVO>> todayWithAuth(@PathVariable Integer elderId) {
        return recordService.getTodayRecordsWithAuth(elderId);
    }
}
