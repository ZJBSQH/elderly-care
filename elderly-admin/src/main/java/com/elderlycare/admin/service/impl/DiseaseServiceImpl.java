package com.elderlycare.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elderlycare.admin.dto.AdminDiseaseAddDTO;
import com.elderlycare.admin.dto.AdminErrorCode;
import com.elderlycare.admin.entity.Disease;
import com.elderlycare.admin.mapper.DiseaseMapper;
import com.elderlycare.admin.service.DiseaseService;
import com.elderlycare.admin.vo.DiseaseVO;
import com.elderlycare.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 疾病字典服务实现
 *
 * @author 郑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiseaseServiceImpl implements DiseaseService {

    private final DiseaseMapper diseaseMapper;

    @Override
    public Result<List<DiseaseVO>> list(String category) {
        LambdaQueryWrapper<Disease> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(category)) {
            wrapper.eq(Disease::getCategory, category);
        }
        wrapper.orderByDesc(Disease::getCreateTime);
        List<Disease> diseases = diseaseMapper.selectList(wrapper);
        List<DiseaseVO> voList = diseases.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    @Override
    public Result<DiseaseVO> getById(Long id) {
        Disease disease = diseaseMapper.selectById(id);
        if (disease == null) {
            return Result.error(AdminErrorCode.DISEASE_NOT_FOUND.getCode(),
                    AdminErrorCode.DISEASE_NOT_FOUND.getMessage());
        }
        return Result.success(toVO(disease));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> add(AdminDiseaseAddDTO dto) {
        Disease disease = new Disease();
        disease.setDiseaseName(dto.getDiseaseName());
        disease.setCategory(dto.getCategory());
        disease.setSymptoms(dto.getSymptoms());
        disease.setTreatment(dto.getTreatment());
        disease.setPrevention(dto.getPrevention());
        disease.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        disease.setCreateTime(LocalDateTime.now());
        disease.setUpdateTime(LocalDateTime.now());
        diseaseMapper.insert(disease);
        log.info("添加疾病成功，diseaseName: {}, id: {}", dto.getDiseaseName(), disease.getId());
        return Result.success(null, "疾病添加成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> update(Long id, AdminDiseaseAddDTO dto) {
        Disease disease = diseaseMapper.selectById(id);
        if (disease == null) {
            return Result.error(AdminErrorCode.DISEASE_NOT_FOUND.getCode(),
                    AdminErrorCode.DISEASE_NOT_FOUND.getMessage());
        }
        disease.setDiseaseName(dto.getDiseaseName());
        disease.setCategory(dto.getCategory());
        disease.setSymptoms(dto.getSymptoms());
        disease.setTreatment(dto.getTreatment());
        disease.setPrevention(dto.getPrevention());
        if (dto.getStatus() != null) {
            disease.setStatus(dto.getStatus());
        }
        disease.setUpdateTime(LocalDateTime.now());
        diseaseMapper.updateById(disease);
        log.info("更新疾病成功，id: {}, diseaseName: {}", id, dto.getDiseaseName());
        return Result.success(null, "疾病更新成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> delete(Long id) {
        Disease disease = diseaseMapper.selectById(id);
        if (disease == null) {
            return Result.error(AdminErrorCode.DISEASE_NOT_FOUND.getCode(),
                    AdminErrorCode.DISEASE_NOT_FOUND.getMessage());
        }
        diseaseMapper.deleteById(id);
        log.info("删除疾病成功，id: {}", id);
        return Result.success(null, "疾病删除成功");
    }

    /**
     * 将实体转换为视图对象
     *
     * @param disease 疾病实体
     * @return 疾病VO
     */
    private DiseaseVO toVO(Disease disease) {
        DiseaseVO vo = new DiseaseVO();
        vo.setId(disease.getId());
        vo.setDiseaseName(disease.getDiseaseName());
        vo.setCategory(disease.getCategory());
        vo.setSymptoms(disease.getSymptoms());
        vo.setTreatment(disease.getTreatment());
        vo.setPrevention(disease.getPrevention());
        vo.setStatus(disease.getStatus());
        vo.setCreateTime(disease.getCreateTime());
        vo.setUpdateTime(disease.getUpdateTime());
        return vo;
    }
}
