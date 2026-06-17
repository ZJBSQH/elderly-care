package com.elderlycare.service.impl;

import com.elderlycare.common.util.SecurityUtil;
import com.elderlycare.mapper.ElderMapper;
import com.elderlycare.mapper.FamilyMapper;
import com.elderlycare.mapper.MedicineMapper;
import com.elderlycare.common.Result;
import com.elderlycare.common.exception.BusinessException;
import com.elderlycare.pojo.dto.medicine.BaseMedicineRequest;
import com.elderlycare.pojo.dto.medicine.MedicineAddRequest;
import com.elderlycare.pojo.dto.medicine.MedicineRemoteUpdateRequest;
import com.elderlycare.pojo.dto.medicine.MedicineUpdateRequest;
import com.elderlycare.pojo.entity.Elder;
import com.elderlycare.pojo.entity.Medicine;
import com.elderlycare.pojo.vo.MedicineVO;
import com.elderlycare.service.MedicineService;
import com.elderlycare.common.websocket.NotifyWebSocket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.elderlycare.common.util.BeanUtil.copyNonNullProperties;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicineServiceImpl implements MedicineService {

    private final MedicineMapper medicineMapper;
    private final ElderMapper elderMapper;
    private final FamilyMapper familyMapper;
    private final SecurityUtil securityUtil;

    //用药方案未停止
    private static final Integer MEDICINE_TYPE_ELDER = 1;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<MedicineVO> add(MedicineAddRequest request) {
        //验证老人是否存在
        if (elderMapper.selectById(request.getElderId()) == null) {
            throw new BusinessException("老人不存在");
        }

        Medicine medicine = new Medicine();
        BeanUtils.copyProperties(request, medicine);

        medicine.setStatus(MEDICINE_TYPE_ELDER);
        medicine.setCreateTime(LocalDateTime.now());

        //保存记录
        medicineMapper.insert(medicine);

        log.info("添加用药计划成功，ID: {}", medicine.getId());
        return Result.success(toMedicineVO(medicine));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<MedicineVO> update(MedicineUpdateRequest request) {

        if (request.getId() == null){throw new BusinessException("ID不能为空");}
        //查询原记录
        Medicine oldMedicine = medicineMapper.selectById(request.getId());
        if (oldMedicine == null){throw new BusinessException("记录不存在");}

        performMedicineUpdate(oldMedicine, request);

        log.info("更新用药计划成功，ID: {}", request.getId());
        return Result.success(toMedicineVO(oldMedicine));
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> delete(Integer id) {
        // 验证 ID 是否为空
        if (id == null) {
            throw new BusinessException("用药计划 ID 不能为空");
        }

        // 查询记录是否存在
        Medicine medicine = medicineMapper.selectById(id);
        if (medicine == null) {
            throw new BusinessException("用药计划不存在");
        }

        // 删除用药计划
        medicineMapper.deleteById(id);

        log.info("删除用药计划成功，ID: {}", id);
        return Result.success(null);
    }

    @Override
    public Result<List<MedicineVO>> selectByElderId(Integer elderId) {
        List<Medicine> medicinesList = medicineMapper.selectByElderId(elderId);
        log.info("查询老人 {} 的用药计划成功，数量: {}", elderId, medicinesList.size());
        return Result.success(medicinesList.stream().map(this::toMedicineVO).toList());
    }

    /**
     * 家属远程修改用药计划
     */
    @Override
    public Result<MedicineVO> remoteUpdate(MedicineRemoteUpdateRequest request) {
        // 1. 查询原计划
        Medicine medicine = medicineMapper.selectById(request.getMedicineId());
        if (medicine == null) {
            throw new BusinessException("用药计划不存在");
        }

        // 2. 验证权限：检查操作者是否为绑定的家属和时间
        Integer currentUserId = securityUtil.getCurrentUserId();
        boolean hasPermission = familyMapper.existsBinding(currentUserId, medicine.getElderId());
        if (!hasPermission) {
            throw new BusinessException("无权修改该老人的用药计划");
        }

        performMedicineUpdate(medicine, request);

        log.info("远程修改用药计划成功，medicineId: {}", request.getMedicineId());
        // 4. 推送通知给老人
        notifyElderAboutChange(medicine);

        return Result.success(toMedicineVO(medicine));
    }

    /**
     * 家属查看老人的用药计划（带权限验证）
     */
    @Override
    public Result<List<MedicineVO>> viewPlanWithAuth(Integer elderId) {
        Integer currentUserId = securityUtil.getCurrentUserId();

        // 验证权限
        boolean hasPermission = familyMapper.existsBinding(currentUserId, elderId);
        if (!hasPermission) {
            throw new BusinessException("无权查看该老人的用药计划");
        }

        // 查询用药计划
        List<Medicine> medicines = medicineMapper.selectByElderId(elderId);
        return Result.success(medicines.stream().map(this::toMedicineVO).toList());
    }

    /**
     * 推送用药计划变更通知给老人
     */
    private void notifyElderAboutChange(Medicine medicine) {
        try {
            Elder elder = elderMapper.selectById(medicine.getElderId());
            if (elder == null) {
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("action", "medicine_updated");
            data.put("medicineId", medicine.getId());
            data.put("medicineName", medicine.getMedicineName());
            data.put("remindTime", medicine.getRemindTime());
            data.put("updateTime", LocalDateTime.now());

            NotifyWebSocket.NotifyMessage message = new NotifyWebSocket.NotifyMessage(
                    200,
                    "用药计划已更新",
                    data
            );

            NotifyWebSocket.sendToUser(String.valueOf(elder.getUserId()), message);
            log.info("已推送用药计划变更通知给老人 userId: {}", elder.getUserId());
        } catch (Exception e) {
            log.error("推送通知失败", e);
        }
    }

    /**
     * 执行用药计划更新（通用逻辑）
     */
    private void performMedicineUpdate(Medicine medicine, BaseMedicineRequest request) {

        // 复制属性
        copyNonNullProperties(request, medicine);

        // 设置更新时间
        medicine.setUpdateTime(LocalDateTime.now());
        // 更新
        medicineMapper.updateById(medicine);
    }

    private MedicineVO toMedicineVO(Medicine medicine) {
        MedicineVO vo = new MedicineVO();
        BeanUtils.copyProperties(medicine, vo);
        return vo;
    }


}
