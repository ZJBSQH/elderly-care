package com.elderlycare.service;

import com.elderlycare.common.Result;
import com.elderlycare.pojo.dto.medicine.MedicineAddRequest;
import com.elderlycare.pojo.dto.medicine.MedicineRemoteUpdateRequest;
import com.elderlycare.pojo.dto.medicine.MedicineUpdateRequest;
import com.elderlycare.pojo.vo.MedicineVO;

import java.util.List;

public interface MedicineService {

    //添加用药计划
    Result<MedicineVO> add(MedicineAddRequest request);

    //修改用药计划
    Result<MedicineVO> update(MedicineUpdateRequest request);

    //删除用药计划
    Result<Void> delete(Integer id);

    //查看用药计划
    Result<List<MedicineVO>> selectByElderId(Integer elderId);

    Result<MedicineVO> remoteUpdate(MedicineRemoteUpdateRequest request);

    Result<List<MedicineVO>> viewPlanWithAuth(Integer elderId);
}
