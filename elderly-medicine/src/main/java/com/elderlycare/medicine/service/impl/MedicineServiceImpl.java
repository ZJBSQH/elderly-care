package com.elderlycare.medicine.service.impl;

import com.elderlycare.common.core.exception.BusinessException;
import com.elderlycare.common.core.result.Result;
import com.elderlycare.common.core.util.BeanUtil;
import com.elderlycare.medicine.dto.MedicineAddRequest;
import com.elderlycare.medicine.dto.MedicineErrorCode;
import com.elderlycare.medicine.dto.MedicineUpdateRequest;
import com.elderlycare.medicine.entity.Medicine;
import com.elderlycare.medicine.feign.UserAccessFeignClient;
import com.elderlycare.medicine.mapper.MedicineMapper;
import com.elderlycare.medicine.service.MedicineService;
import com.elderlycare.medicine.vo.MedicineVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用药计划服务实现
 *
 * @author 郑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MedicineServiceImpl implements MedicineService {

    private final MedicineMapper medicineMapper;
    /** 用户访问权限客户端 */
    private final UserAccessFeignClient userAccessFeignClient;

    /** 新增用药计划 */
    @Override
    public Result<Void> add(MedicineAddRequest request) {
        assertCanAccessElder(request.getElderId());
        Medicine medicine = new Medicine();
        BeanUtil.copyNonNullProperties(request, medicine);
        // 提醒时间默认 08:00
        if (medicine.getRemindTime() == null) {
            medicine.setRemindTime(java.time.LocalTime.of(8, 0));
        }
        medicine.setCreateTime(LocalDateTime.now());
        medicine.setUpdateTime(LocalDateTime.now());
        medicine.setStatus(1);
        medicine.setIsPublic(0);
        medicineMapper.insert(medicine);
        log.info("新增用药计划成功，elderId: {}, medicineName: {}", request.getElderId(), request.getMedicineName());
        return Result.success();
    }

    /** 更新用药计划 */
    @Override
    public Result<Void> update(MedicineUpdateRequest request) {
        Medicine medicine = medicineMapper.selectById(request.getId());
        if (medicine == null) {
            throw new BusinessException(MedicineErrorCode.MEDICINE_NOT_EXIST);
        }
        assertCanAccessElder(medicine.getElderId());
        BeanUtil.copyNonNullProperties(request, medicine);
        medicine.setUpdateTime(LocalDateTime.now());
        medicineMapper.updateById(medicine);
        log.info("更新用药计划成功，id: {}", request.getId());
        return Result.success();
    }

    /** 删除用药计划 */
    @Override
    public Result<Void> delete(Integer id) {
        Medicine medicine = medicineMapper.selectById(id);
        if (medicine == null) {
            throw new BusinessException(MedicineErrorCode.MEDICINE_NOT_EXIST);
        }
        assertCanAccessElder(medicine.getElderId());
        medicineMapper.deleteById(id);
        log.info("删除用药计划成功，id: {}", id);
        return Result.success();
    }

    /** 根据老人 ID 查询用药计划列表 */
    @Override
    public Result<List<MedicineVO>> selectByElderId(Integer elderId) {
        assertCanAccessElder(elderId);
        List<Medicine> medicineList = medicineMapper.selectByElderId(elderId);
        List<MedicineVO> voList = medicineList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    /** 查看用药计划详情 */
    @Override
    public Result<List<MedicineVO>> viewPlan(Integer elderId) {
        return selectByElderId(elderId);
    }

    /**
     * 将 Medicine 实体转换为 MedicineVO
     *
     * @param medicine 用药计划实体
     * @return 用药计划视图对象
     */
    private MedicineVO convertToVO(Medicine medicine) {
        MedicineVO vo = new MedicineVO();
        BeanUtil.copyProperties(medicine, vo);
        return vo;
    }

    /**
     * 校验当前用户是否可以访问指定老人档案。
     */
    private void assertCanAccessElder(Integer elderId) {
        Boolean canAccess = userAccessFeignClient.canAccessElder(elderId).getData();
        if (!Boolean.TRUE.equals(canAccess)) {
            throw new BusinessException(403, "无权访问该老人用药数据");
        }
    }
}
