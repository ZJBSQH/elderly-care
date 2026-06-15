package com.elderlycare.user.feign;

import com.elderlycare.common.vo.UserVO;
import com.elderlycare.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 认证服务Feign客户端
 */
@FeignClient(name = "elderly-auth")
public interface AuthFeignClient {

    /**
     * 根据用户标识查询用户信息
     * 支持通过手机号或用户ID查询
     *
     * @param identifier 用户标识（手机号或用户ID）
     * @param isPhone 是否为手机号查询（true: 手机号，false: 用户ID）
     * @return 用户信息
     */
    @GetMapping("/auth/user/byIdentifier")
    Result<UserVO> getUserByIdentifier(
            @RequestParam("identifier") String identifier,
            @RequestParam("isPhone") boolean isPhone);
}
