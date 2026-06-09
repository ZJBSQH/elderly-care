package com.elderlycare.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elderlycare.admin.dto.AdminDiseaseAddDTO;
import com.elderlycare.admin.dto.AdminErrorCode;
import com.elderlycare.admin.entity.Disease;
import com.elderlycare.admin.mapper.DiseaseMapper;
import com.elderlycare.admin.service.DiseaseService;
import com.elderlycare.admin.vo.DiseaseVO;
import com.elderlycare.common.core.exception.BusinessException;
import com.elderlycare.common.core.util.BeanUtil;
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

    /**
     * 获取疾病列表，支持按分类筛选
     *
     * @param category 疾病分类（可选，为空则查询全部）
     * @return 疾病列表
     */
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

    /**
     * 根据ID获取疾病详情
     *
     * @param id 疾病ID
     * @return 疾病详情
     */
    @Override
    public Result<DiseaseVO> getById(Long id) {
        Disease disease = diseaseMapper.selectById(id);
        if (disease == null) {
            throw new BusinessException(AdminErrorCode.DISEASE_NOT_FOUND);
        }
        return Result.success(toVO(disease));
    }

    /**
     * 添加疾病到字典
     *
     * @param dto 疾病添加DTO
     * @return 添加结果
     */
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

    /**
     * 更新疾病信息
     *
     * @param id  疾病ID
     * @param dto 疾病添加DTO
     * @return 更新结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> update(Long id, AdminDiseaseAddDTO dto) {
        Disease disease = diseaseMapper.selectById(id);
        if (disease == null) {
            throw new BusinessException(AdminErrorCode.DISEASE_NOT_FOUND);
        }
        BeanUtil.copyNonNullProperties(dto, disease);
        disease.setUpdateTime(LocalDateTime.now());
        diseaseMapper.updateById(disease);
        log.info("更新疾病成功，id: {}, diseaseName: {}", id, dto.getDiseaseName());
        return Result.success(null, "疾病更新成功");
    }

    /**
     * 根据ID删除疾病
     *
     * @param id 疾病ID
     * @return 删除结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> delete(Long id) {
        Disease disease = diseaseMapper.selectById(id);
        if (disease == null) {
            throw new BusinessException(AdminErrorCode.DISEASE_NOT_FOUND);
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
        BeanUtil.copyProperties(disease, vo);
        return vo;
    }
}
