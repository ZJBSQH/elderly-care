package com.elderlycare.admin.service;

import com.elderlycare.admin.dto.AdminDiseaseAddDTO;
import com.elderlycare.admin.vo.DiseaseVO;
import com.elderlycare.common.core.result.Result;

import java.util.List;

/**
 * 疾病字典服务接口
 *
 * @author 郑
 */
public interface DiseaseService {

    /**
     * 获取疾病列表
     *
     * @param category 分类（可选，为空则查询全部）
     * @return 疾病列表
     */
    Result<List<DiseaseVO>> list(String category);

    /**
     * 根据ID获取疾病详情
     *
     * @param id 疾病ID
     * @return 疾病详情
     */
    Result<DiseaseVO> getById(Long id);

    /**
     * 添加疾病
     *
     * @param dto 疾病添加DTO
     * @return 添加结果
     */
    Result<Void> add(AdminDiseaseAddDTO dto);

    /**
     * 更新疾病
     *
     * @param dto 疾病添加DTO
     * @return 更新结果
     */
    Result<Void> update(Long id, AdminDiseaseAddDTO dto);

    /**
     * 删除疾病
     *
     * @param id 疾病ID
     * @return 删除结果
     */
    Result<Void> delete(Long id);
}
