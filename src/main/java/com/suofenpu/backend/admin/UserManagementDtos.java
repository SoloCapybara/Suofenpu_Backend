package com.suofenpu.backend.admin;

import java.time.Instant;

import com.suofenpu.backend.admin.auth.AdminUser;
import com.suofenpu.backend.user.User;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class UserManagementDtos {
    private UserManagementDtos() {}

    public record EmployeeResponse(
        Long id,
        String username,
        String displayName,
        String avatarUrl,
        String role,
        boolean enabled,
        Instant createdAt
    ) {
        public static EmployeeResponse from(AdminUser user) {
            return new EmployeeResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt()
            );
        }
    }

    public record CustomerResponse(Long id, String nickname, String phone, String avatarUrl, String status, Instant createdAt) {
        public static CustomerResponse from(User user) {
            return new CustomerResponse(user.getId(), user.getNickname(), user.getPhone(), user.getAvatarUrl(), user.getStatus().name(), user.getCreatedAt());
        }
    }

    public record CreateEmployeeRequest(
        @NotBlank(message = "登录账号不能为空") @Size(max = 64, message = "登录账号不能超过64个字符") String username,
        @NotBlank(message = "显示名称不能为空") @Size(max = 80, message = "显示名称不能超过80个字符") String displayName,
        @NotBlank(message = "初始密码不能为空") @Size(min = 8, max = 72, message = "密码长度应为8到72位") String password,
        @NotBlank(message = "角色不能为空") String role
    ) {}

    public record UpdateEmployeeRequest(String displayName, String role, Boolean enabled) {}
    public record ResetPasswordRequest(@NotBlank @Size(min = 8, max = 72) String password) {}
    public record UpdateCustomerStatusRequest(@NotBlank String status) {}
}
