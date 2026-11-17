package com.yanggu.code.generator.controller;

import cn.hutool.v7.core.date.DateUtil;
import cn.hutool.v7.core.io.file.FileUtil;
import cn.hutool.v7.json.JSONUtil;
import com.yanggu.code.generator.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * 登录管理
 */
@Slf4j
@Validated
@RestController
@Tag(name = "登录管理")
@RequestMapping("/auth")
public class AuthController {

    /**
     * 登录
     */
    @PostMapping("/login")
    @Operation(summary = "登录")
    public LoginVO login(@RequestBody LoginDTO loginDTO) {
        log.info("登录参数: {}", loginDTO);
        LoginVO loginVO = new LoginVO();

        List<String> permissionList = List.of("user:add", "user:update", "user:delete");
        loginVO.setPermissionList(permissionList);

        List<String> roleList = List.of("admin", "user");
        loginVO.setRoleList(roleList);

        TokenVO tokenVO = new TokenVO();
        tokenVO.setAccessToken(DateUtil.formatDateTime(new Date()));
        tokenVO.setRefreshToken("refreshToken");
        tokenVO.setExpire(3600L);
        loginVO.setTokenInfo(tokenVO);

        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setUsername("admin");
        userInfoVO.setNickname("管理员");
        userInfoVO.setAvatar("https://wpimg.wallst");
        userInfoVO.setEmail("<EMAIL>");
        loginVO.setUserInfo(userInfoVO);

        List<MenuVO> menuList = JSONUtil.toList(FileUtil.readUtf8String("data.json"), MenuVO.class);
        loginVO.setMenuList(menuList);

        return loginVO;
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    public Result<String> logout() {
        return Result.success("退出成功");
    }

    /**
     * 返回401
     */
    @PostMapping("/401")
    @Operation(summary = "返回401")
    public Result<String> unauthorized() {
        return Result.fail(401, "未授权");
    }

    @Data
    public static class LoginDTO {

        private String username;

        private String password;
    }

    @Data
    public static class LoginVO {

        private List<String> permissionList;

        private List<String> roleList;

        private List<MenuVO> menuList;

        private TokenVO tokenInfo;

        private UserInfoVO userInfo;

    }

    @Data
    public static class MenuVO {

        private String name;

        private String path;

        private String component;

        private MetaVO meta;

        private List<MenuVO> children;

    }

    @Data
    public static class MetaVO {

        private String title;

        private String icon;

        private Boolean cache;

        private Boolean hidden;

        private Integer type;

    }

    @Data
    public static class TokenVO {

        private String tokenName;

        private String accessToken;

        private String refreshToken;

        private Long expire;

    }

    @Data
    public static class UserInfoVO {

        private String username;

        private String nickname;

        private String avatar;

        private String email;

        private String mobile;

    }

}
