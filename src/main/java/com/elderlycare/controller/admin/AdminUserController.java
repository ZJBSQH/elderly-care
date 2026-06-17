package com.elderlycare.controller.admin;

import com.elderlycare.common.Result;
import com.elderlycare.pojo.dto.admin.AdminUserUpdateDTO;
import com.elderlycare.pojo.vo.admin.AdminUserVO;
import com.elderlycare.pojo.vo.PageResult;
import com.elderlycare.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员用户管理控制器
 */
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * 查询用户列表（分页 + 条件筛选）
     */
    @GetMapping
    public Result<PageResult<AdminUserVO>> getUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer userType,
            @RequestParam(required = false) Integer status
    ) {
        return Result.success(adminUserService.queryUsers(page, size, keyword, userType, status));
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public Result<AdminUserVO> getUserDetail(@PathVariable Integer id) {
        return Result.success(adminUserService.getUserDetail(id));
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    public Result<Void> updateUser(
            @PathVariable Integer id,
            @Valid @RequestBody AdminUserUpdateDTO request
    ) {
        boolean success = adminUserService.updateUser(id, request);
        return success ? Result.success() : Result.error("更新失败");
    }

    /**
     * 修改用户状态（启用/禁用）
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateUserStatus(
            @PathVariable Integer id,
            @RequestParam Integer status
    ) {
        boolean success = adminUserService.changeUserStatus(id, status);
        return success ? Result.success() : Result.error("修改失败");
    }

    /**
     * 修改用户类型
     */
    @PutMapping("/{id}/type")
    public Result<Void> updateUserType(
            @PathVariable Integer id,
            @RequestParam Integer userType
    ) {
        boolean success = adminUserService.changeUserType(id, userType);
        return success ? Result.success() : Result.error("修改失败");
    }

    /**
     * 统计用户数量
     */
    @GetMapping("/count")
    public Result<Long> countUsers(@RequestParam(required = false) Integer userType) {
        return Result.success(adminUserService.countUsers(userType));
    }
}
