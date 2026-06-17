package com.elderlycare.service;

import com.elderlycare.pojo.dto.admin.AdminUserUpdateDTO;
import com.elderlycare.pojo.vo.admin.AdminUserVO;
import com.elderlycare.pojo.vo.PageResult;

/**
 * 管理员用户服务接口
 */
public interface AdminUserService {

    /**
     * 查询用户列表（分页）
     *
     * @param page 页码
     * @param size 每页条数
     * @param keyword 搜索关键词（手机号/姓名）
     * @param userType 用户类型筛选
     * @param status 状态筛选
     * @return 分页结果
     */
    PageResult<AdminUserVO> queryUsers(Integer page, Integer size, String keyword, Integer userType, Integer status);

    /**
     * 获取用户详情
     *
     * @param userId 用户 ID
     * @return 用户详情
     */
    AdminUserVO getUserDetail(Integer userId);

    /**
     * 更新用户信息
     *
     * @param userId 用户 ID
     * @param request 更新请求
     * @return 操作结果
     */
    boolean updateUser(Integer userId, AdminUserUpdateDTO request);

    /**
     * 修改用户状态
     *
     * @param userId 用户 ID
     * @param status 新状态（0-禁用，1-正常）
     * @return 操作结果
     */
    boolean changeUserStatus(Integer userId, Integer status);

    /**
     * 修改用户类型
     *
     * @param userId 用户 ID
     * @param userType 新用户类型
     * @return 操作结果
     */
    boolean changeUserType(Integer userId, Integer userType);

    /**
     * 统计用户数量
     *
     * @param userType 用户类型（null 表示统计所有）
     * @return 用户数量
     */
    Long countUsers(Integer userType);
}
