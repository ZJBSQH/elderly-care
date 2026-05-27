package com.elderlycare.medicine.controller;

import com.elderlycare.common.core.result.Result;
import com.elderlycare.medicine.dto.MedicineAddRequest;
import com.elderlycare.medicine.dto.MedicineUpdateRequest;
import com.elderlycare.medicine.service.MedicineService;
import com.elderlycare.medicine.vo.MedicineVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用药计划控制器
 *
 * @author 郑
 */
@Slf4j
@RestController
@RequestMapping("/medicine")
@RequiredArgsConstructor
public class MedicineController {

    private final MedicineService medicineService;

    /**
     * 新增用药计划
     *
     * @param request 新增请求
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody MedicineAddRequest request) {
        return medicineService.add(request);
    }

    /**
     * 更新用药计划
     *
     * @param request 更新请求
     * @return 操作结果
     */
    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody MedicineUpdateRequest request) {
        return medicineService.update(request);
    }

    /**
     * 删除用药计划
     *
     * @param id 用药计划 ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        return medicineService.delete(id);
    }

    /**
     * 根据老人 ID 查询用药计划
     *
     * @param elderId 老人 ID
     * @return 用药计划列表
     */
    @GetMapping("/look/{elderId}")
    public Result<List<MedicineVO>> look(@PathVariable Integer elderId) {
        return medicineService.selectByElderId(elderId);
    }

    /**
     * 查看用药计划详情
     *
     * @param elderId 老人 ID
     * @return 用药计划列表
     */
    @GetMapping("/plan/{elderId}")
    public Result<List<MedicineVO>> plan(@PathVariable Integer elderId) {
        return medicineService.viewPlan(elderId);
    }
}
