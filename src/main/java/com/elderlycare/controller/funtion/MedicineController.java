package com.elderlycare.controller.funtion;

import com.elderlycare.common.Result;
import com.elderlycare.pojo.dto.medicine.MedicineAddRequest;
import com.elderlycare.pojo.dto.medicine.MedicineRemoteUpdateRequest;
import com.elderlycare.pojo.dto.medicine.MedicineUpdateRequest;
import com.elderlycare.pojo.vo.MedicineVO;
import com.elderlycare.service.MedicineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/medicine")
public class MedicineController {
    private final MedicineService medicineService;

    //添加用药计划
    @PostMapping("/add")
    public Result<MedicineVO> add(@Valid @RequestBody MedicineAddRequest request){
        return medicineService.add(request);
    }

    //更新用药计划
    @PutMapping("/update")
    public Result<MedicineVO> update(@Valid @RequestBody MedicineUpdateRequest request){
        return medicineService.update(request);
    }

    //删除用药计划
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Integer id){
        return medicineService.delete(id);
    }

    //查询用药计划
    @GetMapping("/look/{elderId}")
    public Result<List<MedicineVO>> selectByElderId(@PathVariable Integer elderId){
        return medicineService.selectByElderId(elderId);
    }

    /**
     * 家属远程修改用药计划
     */
    @PutMapping("/remote/update")
    public Result<MedicineVO> remoteUpdate(@Valid @RequestBody MedicineRemoteUpdateRequest request) {
        return medicineService.remoteUpdate(request);
    }

    /**
     * 家属查看老人的用药计划（带权限验证）
     */
    @GetMapping("/plan/{elderId}")
    public Result<List<MedicineVO>> viewPlanWithAuth(@PathVariable Integer elderId) {
        return medicineService.viewPlanWithAuth(elderId);
    }
}
