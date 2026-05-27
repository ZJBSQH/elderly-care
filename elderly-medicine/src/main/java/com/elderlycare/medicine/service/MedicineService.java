package com.elderlycare.medicine.service;

import com.elderlycare.common.core.result.Result;
import com.elderlycare.medicine.dto.MedicineAddRequest;
import com.elderlycare.medicine.dto.MedicineUpdateRequest;
import com.elderlycare.medicine.vo.MedicineVO;

import java.util.List;

/**
 * 用药计划服务接口
 *
 * @author 郑
 */
public interface MedicineService {

    /**
     * 新增用药计划
     *
     * @param request 新增请求
     * @return 操作结果
     */
    Result<Void> add(MedicineAddRequest request);

    /**
     * 更新用药计划
     *
     * @param request 更新请求
     * @return 操作结果
     */
    Result<Void> update(MedicineUpdateRequest request);

    /**
     * 删除用药计划
     *
     * @param id 用药计划 ID
     * @return 操作结果
     */
    Result<Void> delete(Integer id);

    /**
     * 根据老人 ID 查询用药计划列表
     *
     * @param elderId 老人 ID
     * @return 用药计划列表
     */
    Result<List<MedicineVO>> selectByElderId(Integer elderId);

    /**
     * 查看用药计划详情
     *
     * @param elderId 老人 ID
     * @return 用药计划列表
     */
    Result<List<MedicineVO>> viewPlan(Integer elderId);
}
