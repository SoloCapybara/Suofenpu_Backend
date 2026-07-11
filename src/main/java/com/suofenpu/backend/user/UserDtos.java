package com.suofenpu.backend.user;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public final class UserDtos {
    private UserDtos() {
    }

    @Schema(description = "微信小程序登录请求")
    public record WechatLoginRequest(
        @Schema(description = "wx.login 返回的临时 code。后端用它换取 openid/session_key", example = "081234abcd", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "code不能为空")
        String code,
        @Schema(description = "用户昵称，可选；前端拿不到时可不传", example = "粉友")
        String nickname,
        @Schema(description = "头像地址。当前版本后端会忽略该字段，新用户自动分配默认头像", example = "/assets/avatars/user-1.png")
        String avatarUrl
    ) {
    }

    @Schema(description = "开发环境手机号登录请求。仅用于本地联调，正式小程序应使用微信登录")
    public record DevLoginRequest(
        @Schema(description = "手机号。用于本地联调创建/查找用户", example = "13800008888", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "手机号不能为空")
        String phone,
        @Schema(description = "用户昵称", example = "测试用户")
        String nickname,
        @Schema(description = "头像地址。当前版本后端会忽略该字段，新用户自动分配默认头像", example = "/assets/avatars/user-1.png")
        String avatarUrl
    ) {
    }

    @Schema(description = "更新当前用户资料请求")
    public record UpdateProfileRequest(
        @Schema(description = "手机号。不传表示不修改", example = "13800008888")
        String phone,
        @Schema(description = "昵称。不传表示不修改", example = "粉友")
        String nickname,
        @Schema(description = "头像地址。当前版本不支持修改头像，后端会忽略该字段", example = "/assets/avatars/user-1.png")
        String avatarUrl
    ) {
    }

    @Schema(description = "登录响应")
    public record LoginResponse(
        @Schema(description = "后端登录凭证。小程序后续请求放到 Authorization: Bearer {token}", example = "4f2c...")
        String token,
        @Schema(description = "token 过期时间，ISO-8601 UTC 时间", example = "2026-07-31T04:00:00Z")
        Instant expiresAt,
        @Schema(description = "当前用户信息")
        UserResponse user
    ) {
    }

    @Schema(description = "用户信息响应")
    public record UserResponse(
        @Schema(description = "用户ID", example = "1")
        Long id,
        @Schema(description = "微信 openid。仅当前小程序内唯一；开发手机号登录时可能为空", example = "oAbcd1234")
        String openid,
        @Schema(description = "手机号", example = "13800008888")
        String phone,
        @Schema(description = "昵称", example = "粉友")
        String nickname,
        @Schema(description = "头像地址。当前版本为小程序本地默认头像路径，由后端首次注册时分配并保存到 MySQL", example = "/assets/avatars/user-1.png")
        String avatarUrl,
        @Schema(description = "用户状态：ACTIVE=正常；DISABLED=禁用", example = "ACTIVE")
        UserStatus status,
        @Schema(description = "注册时间", example = "2026-07-01T04:00:00Z")
        Instant createdAt
    ) {
        public static UserResponse from(User user) {
            return new UserResponse(
                user.getId(),
                user.getOpenid(),
                user.getPhone(),
                user.getNickname(),
                user.getAvatarUrl(),
                user.getStatus(),
                user.getCreatedAt()
            );
        }
    }
}
