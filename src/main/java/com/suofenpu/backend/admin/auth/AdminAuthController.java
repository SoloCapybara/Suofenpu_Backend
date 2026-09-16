package com.suofenpu.backend.admin.auth;

import static com.suofenpu.backend.admin.auth.AdminAuthDtos.AdminProfile;
import static com.suofenpu.backend.admin.auth.AdminAuthDtos.LoginRequest;
import static com.suofenpu.backend.admin.auth.AdminAuthDtos.LoginResponse;
import static com.suofenpu.backend.admin.auth.AdminAuthDtos.UpdateProfileRequest;
import static com.suofenpu.backend.admin.auth.AdminAuthDtos.ChangePasswordRequest;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin-auth")
public class AdminAuthController {
    private final AdminAuthService authService;

    public AdminAuthController(AdminAuthService authService) { this.authService = authService; }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) { return authService.login(request); }

    @GetMapping("/me")
    public AdminProfile me(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return authService.currentUser(authorization);
    }

    @PatchMapping("/me")
    public AdminProfile updateProfile(
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @Valid @RequestBody UpdateProfileRequest request
    ) {
        return authService.updateProfile(authorization, request);
    }

    @PostMapping("/me/avatar")
    public AdminProfile uploadAvatar(
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @RequestPart("file") MultipartFile file
    ) {
        return authService.uploadAvatar(authorization, file);
    }

    @DeleteMapping("/me/avatar")
    public AdminProfile removeAvatar(
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return authService.removeAvatar(authorization);
    }

    @PostMapping("/change-password")
    public void changePassword(
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @Valid @RequestBody ChangePasswordRequest request
    ) {
        authService.changePassword(authorization, request);
    }

    @PostMapping("/logout")
    public void logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.logout(authorization);
    }
}
