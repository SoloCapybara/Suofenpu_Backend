package com.suofenpu.backend.admin;

import static com.suofenpu.backend.admin.UserManagementDtos.*;

import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.suofenpu.backend.admin.auth.*;
import com.suofenpu.backend.common.ApiException;
import com.suofenpu.backend.user.*;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/user-management")
public class UserManagementController {
    private static final Set<String> EMPLOYEE_ROLES = Set.of("OWNER", "MANAGER", "STAFF");
    private final AdminUserRepository adminUserRepository;
    private final AdminSessionRepository adminSessionRepository;
    private final AdminAuthService adminAuthService;
    private final UserRepository userRepository;

    public UserManagementController(AdminUserRepository adminUserRepository, AdminSessionRepository adminSessionRepository,
        AdminAuthService adminAuthService, UserRepository userRepository) {
        this.adminUserRepository = adminUserRepository;
        this.adminSessionRepository = adminSessionRepository;
        this.adminAuthService = adminAuthService;
        this.userRepository = userRepository;
    }

    @GetMapping("/employees")
    public List<EmployeeResponse> employees() {
        return adminUserRepository.findAll().stream().map(EmployeeResponse::from).toList();
    }

    @PostMapping("/employees")
    @Transactional
    public EmployeeResponse createEmployee(@RequestHeader("Authorization") String authorization,
        @Valid @RequestBody CreateEmployeeRequest request) {
        requireOwner(authorization);
        String username = request.username().trim();
        if (adminUserRepository.existsByUsername(username)) throw new ApiException(HttpStatus.CONFLICT, "登录账号已存在");
        String role = validateRole(request.role());
        AdminUser user = new AdminUser(username, adminAuthService.encodePassword(request.password()), request.displayName().trim());
        user.updateForManagement(null, role, true);
        return EmployeeResponse.from(adminUserRepository.save(user));
    }

    @PatchMapping("/employees/{id}")
    @Transactional
    public EmployeeResponse updateEmployee(@RequestHeader("Authorization") String authorization, @PathVariable Long id,
        @RequestBody UpdateEmployeeRequest request) {
        AdminUser operator = requireOwner(authorization);
        AdminUser user = findAdmin(id);
        if (operator.getId().equals(id) && Boolean.FALSE.equals(request.enabled()))
            throw new ApiException(HttpStatus.CONFLICT, "不能停用当前登录账号");
        String role = request.role() == null ? null : validateRole(request.role());
        if (operator.getId().equals(id) && role != null && !"OWNER".equals(role))
            throw new ApiException(HttpStatus.CONFLICT, "不能降低当前超管账号的权限");
        user.updateForManagement(request.displayName(), role, request.enabled());
        if (Boolean.FALSE.equals(request.enabled())) adminSessionRepository.deleteByAdminUserIdAndTokenNot(id, "");
        return EmployeeResponse.from(adminUserRepository.save(user));
    }

    @PostMapping("/employees/{id}/reset-password")
    @Transactional
    public void resetPassword(@RequestHeader("Authorization") String authorization, @PathVariable Long id,
        @Valid @RequestBody ResetPasswordRequest request) {
        requireOwner(authorization);
        AdminUser user = findAdmin(id);
        user.updatePasswordHash(adminAuthService.encodePassword(request.password()));
        adminUserRepository.save(user);
        adminSessionRepository.deleteByAdminUserIdAndTokenNot(id, "");
    }

    @GetMapping("/customers")
    public List<CustomerResponse> customers() {
        return userRepository.findAll().stream().map(CustomerResponse::from).toList();
    }

    @PatchMapping("/customers/{id}/status")
    @Transactional
    public CustomerResponse updateCustomerStatus(@RequestHeader("Authorization") String authorization, @PathVariable Long id,
        @Valid @RequestBody UpdateCustomerStatusRequest request) {
        requireOwner(authorization);
        UserStatus status;
        try { status = UserStatus.valueOf(request.status()); }
        catch (IllegalArgumentException exception) { throw new ApiException(HttpStatus.BAD_REQUEST, "用户状态不正确"); }
        User user = userRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "用户不存在"));
        user.updateStatus(status);
        return CustomerResponse.from(userRepository.save(user));
    }

    private AdminUser requireOwner(String authorization) {
        AdminUser user = adminAuthService.requireAdmin(authorization);
        if (!"OWNER".equals(user.getRole())) throw new ApiException(HttpStatus.FORBIDDEN, "仅超级管理员可以执行此操作");
        return user;
    }

    private AdminUser findAdmin(Long id) {
        return adminUserRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "员工账号不存在"));
    }

    private String validateRole(String role) {
        String normalized = role.trim().toUpperCase();
        if (!EMPLOYEE_ROLES.contains(normalized)) throw new ApiException(HttpStatus.BAD_REQUEST, "员工角色不正确");
        return normalized;
    }
}
