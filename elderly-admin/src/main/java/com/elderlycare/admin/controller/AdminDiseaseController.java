package com.elderlycare.admin.controller;

import com.elderlycare.admin.dto.AdminDiseaseAddDTO;
import com.elderlycare.admin.service.DiseaseService;
import com.elderlycare.admin.vo.DiseaseVO;
import com.elderlycare.common.core.result.Result;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 疾病字典管理控制器
 *
 * @author 郑
 */
@Slf4j
@RestController
@RequestMapping("/admin/disease")
@RequiredArgsConstructor
public class AdminDiseaseController {

    private final DiseaseService diseaseService;

    /**
     * 获取疾病列表
     *
     * @param category 分类（可选）
     * @return 疾病列表
     */
    @GetMapping("/list")
    public Result<List<DiseaseVO>> list(@RequestParam(required = false) String category) {
        log.info("获取疾病列表请求，category: {}", category);
        return diseaseService.list(category);
    }

    /**
     * 根据ID获取疾病详情
     *
     * @param id 疾病ID
     * @return 疾病详情
     */
    @GetMapping("/{id}")
    public Result<DiseaseVO> getById(@PathVariable Long id) {
        log.info("获取疾病详情请求，id: {}", id);
        return diseaseService.getById(id);
    }

    /**
     * 添加疾病
     *
     * @param dto 疾病添加DTO
     * @return 添加结果
     */
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody AdminDiseaseAddDTO dto) {
        log.info("添加疾病请求，diseaseName: {}", dto.getDiseaseName());
        return diseaseService.add(dto);
    }

    /**
     * 更新疾病
     *
     * @param id  疾病ID
     * @param dto 疾病信息
     * @return 更新结果
     */
    @PutMapping("/update")
    public Result<Void> update(@RequestParam Long id, @Valid @RequestBody AdminDiseaseAddDTO dto) {
        log.info("更新疾病请求，id: {}, diseaseName: {}", id, dto.getDiseaseName());
        return diseaseService.update(id, dto);
    }

    /**
     * 删除疾病
     *
     * @param id 疾病ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除疾病请求，id: {}", id);
        return diseaseService.delete(id);
    }
}
