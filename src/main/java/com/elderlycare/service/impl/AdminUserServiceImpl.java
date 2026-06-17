package com.elderlycare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.common.exception.BusinessException;
import com.elderlycare.common.exception.ErrorCode;
import com.elderlycare.common.util.BeanUtil;
import com.elderlycare.mapper.UserMapper;
import com.elderlycare.pojo.dto.admin.AdminUserUpdateDTO;
import com.elderlycare.pojo.entity.User;
import com.elderlycare.pojo.vo.admin.AdminUserVO;
import com.elderlycare.pojo.vo.PageResult;
import com.elderlycare.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserMapper userMapper;

    @Override
    public PageResult<AdminUserVO> queryUsers(Integer page, Integer size, String keyword, Integer userType, Integer status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                    .like(User::getPhone, keyword)
                    .or()
                    .like(User::getName, keyword)
            );
        }

        if (userType != null) {
            wrapper.eq(User::getUserType, userType);
        }

        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }

        Page<User> userPage = userMapper.selectPage(new Page<>(page, size), wrapper);

        var adminUserVOList = userPage.getRecords().stream()
                .map(this::convertToAdminUserVO)
                .toList();

        return PageResult.of(adminUserVOList, userPage.getTotal(), page, size);
    }

    @Override
    public AdminUserVO getUserDetail(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_EXIST);
        }
        return convertToAdminUserVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(Integer userId, AdminUserUpdateDTO request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.error("用户不存在，userId: {}", userId);
            throw new BusinessException(ErrorCode.USER_NOT_EXIST);
        }

        BeanUtil.copyNonNullProperties(request, user);

        int rows = userMapper.updateById(user);
        log.info("更新用户信息成功，userId: {}", userId);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changeUserStatus(Integer userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.error("用户不存在，userId: {}", userId);
            throw new BusinessException(ErrorCode.USER_NOT_EXIST);
        }

        user.setStatus(status);
        int rows = userMapper.updateById(user);
        log.info("修改用户状态成功，userId: {}, status: {}", userId, status);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changeUserType(Integer userId, Integer userType) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.error("用户不存在，userId: {}", userId);
            throw new BusinessException(ErrorCode.USER_NOT_EXIST);
        }

        user.setUserType(userType);
        int rows = userMapper.updateById(user);
        log.info("修改用户类型成功，userId: {}, userType: {}", userId, userType);
        return rows > 0;
    }

    @Override
    public Long countUsers(Integer userType) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (userType != null) {
            wrapper.eq(User::getUserType, userType);
        }
        return userMapper.selectCount(wrapper);
    }

    private AdminUserVO convertToAdminUserVO(User user) {
        String maskedPhone = maskPhoneNumber(user.getPhone());

        return new AdminUserVO(
                user.getId(),
                maskedPhone,
                user.getName(),
                user.getAge(),
                user.getSex(),
                user.getAvatar(),
                user.getUserType(),
                getUserTypeDesc(user.getUserType()),
                user.getStatus(),
                getStatusDesc(user.getStatus()),
                user.getCreateTime()
        );
    }

    private String getUserTypeDesc(Integer userType) {
        if (userType == null) return "未知";
        return switch (userType) {
            case 0 -> "老人";
            case 1 -> "家属";
            case 2 -> "管理员";
            default -> "未知";
        };
    }

    private String getStatusDesc(Integer status) {
        if (status == null) return "未知";
        return status == 0 ? "禁用" : "正常";
    }

    private String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 11) {
            return phone;
        }
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }
}
