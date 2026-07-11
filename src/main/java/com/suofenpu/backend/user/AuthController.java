package com.suofenpu.backend.user;

import static com.suofenpu.backend.user.UserDtos.DevLoginRequest;
import static com.suofenpu.backend.user.UserDtos.LoginResponse;
import static com.suofenpu.backend.user.UserDtos.UpdateProfileRequest;
import static com.suofenpu.backend.user.UserDtos.UserResponse;
import static com.suofenpu.backend.user.UserDtos.WechatLoginRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "用户登录")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/wechat-login")
    @Operation(
        summary = "微信小程序登录/注册",
        description = """
            小程序调用 wx.login 获取 code 后传给后端。
            后端用 code 向微信换 openid；如果 openid 已存在则登录，否则自动注册用户。
            返回 token 后，前端后续请求放到 Authorization: Bearer {token}。
            """
    )
    public LoginResponse wechatLogin(@Valid @RequestBody WechatLoginRequest request) {
        return authService.wechatLogin(request);
    }

    @PostMapping("/dev-login")
    @Operation(
        summary = "开发环境手机号登录",
        description = "仅用于本地联调，没有微信 appId/appSecret 时可先用手机号创建用户并获取 token。生产环境可以关闭 WECHAT_DEV_LOGIN_ENABLED。"
    )
    public LoginResponse devLogin(@Valid @RequestBody DevLoginRequest request) {
        return authService.devLogin(request);
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前登录用户", description = "根据 Authorization: Bearer {token} 返回当前用户信息。")
    public UserResponse currentUser(
        @Parameter(description = "Bearer token，登录接口返回", example = "Bearer 4f2c...")
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return authService.currentUser(authorization);
    }

    @PatchMapping("/me")
    @Operation(summary = "更新当前用户资料", description = "更新手机号、昵称、头像。字段均可选，只传要改的字段。")
    public UserResponse updateCurrentUser(
        @Parameter(description = "Bearer token，登录接口返回", example = "Bearer 4f2c...")
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @RequestBody UpdateProfileRequest request
    ) {
        return authService.updateCurrentUser(authorization, request);
    }
}
