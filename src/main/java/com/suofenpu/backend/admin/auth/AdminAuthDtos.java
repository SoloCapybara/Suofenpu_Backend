package com.suofenpu.backend.admin.auth;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AdminAuthDtos {
    private AdminAuthDtos() {}

    public record LoginRequest(
        @NotBlank(message = "用户名不能为空") String username,
        @NotBlank(message = "密码不能为空") String password,
        boolean remember
    ) {}

    public record AdminProfile(Long id, String username, String displayName, String avatarUrl, String role, Instant createdAt) {
        public static AdminProfile from(AdminUser user) {
            return new AdminProfile(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getRole(),
                user.getCreatedAt()
            );
        }
    }

    public record UpdateProfileRequest(
        @NotBlank(message = "显示名称不能为空")
        @Size(max = 80, message = "显示名称不能超过80个字符")
        String displayName
    ) {}

    public record ChangePasswordRequest(
        @NotBlank(message = "当前密码不能为空") String currentPassword,
        @NotBlank(message = "新密码不能为空")
        @Size(min = 8, max = 72, message = "新密码长度应为8到72位")
        String newPassword
    ) {}

    public record LoginResponse(String token, Instant expiresAt, AdminProfile user) {}
}
