package com.suofenpu.backend.admin.auth;

import static com.suofenpu.backend.admin.auth.AdminAuthDtos.AdminProfile;
import static com.suofenpu.backend.admin.auth.AdminAuthDtos.LoginRequest;
import static com.suofenpu.backend.admin.auth.AdminAuthDtos.LoginResponse;
import static com.suofenpu.backend.admin.auth.AdminAuthDtos.UpdateProfileRequest;
import static com.suofenpu.backend.admin.auth.AdminAuthDtos.ChangePasswordRequest;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.suofenpu.backend.common.ApiException;
import com.suofenpu.backend.media.AdminAvatarStorageService;

@Service
public class AdminAuthService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();
    private final AdminUserRepository userRepository;
    private final AdminSessionRepository sessionRepository;
    private final AdminAvatarStorageService avatarStorageService;

    public AdminAuthService(
        AdminUserRepository userRepository,
        AdminSessionRepository sessionRepository,
        AdminAvatarStorageService avatarStorageService
    ) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.avatarStorageService = avatarStorageService;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        AdminUser user = userRepository.findByUsername(request.username().trim())
            .filter(AdminUser::isEnabled)
            .orElseThrow(this::invalidCredentials);
        if (!PASSWORD_ENCODER.matches(request.password(), user.getPasswordHash())) {
            throw invalidCredentials();
        }
        Instant expiresAt = Instant.now().plus(request.remember() ? 30 : 12, request.remember() ? ChronoUnit.DAYS : ChronoUnit.HOURS);
        AdminSession session = sessionRepository.save(new AdminSession(newToken(), user, expiresAt));
        return new LoginResponse(session.getToken(), expiresAt, AdminProfile.from(user));
    }

    @Transactional(readOnly = true)
    public AdminUser requireAdmin(String authorization) {
        String token = parseBearerToken(authorization);
        return sessionRepository.findByToken(token)
            .filter(session -> !session.isExpired())
            .map(AdminSession::getAdminUser)
            .filter(AdminUser::isEnabled)
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "登录已过期，请重新登录"));
    }

    @Transactional(readOnly = true)
    public AdminProfile currentUser(String authorization) {
        return AdminProfile.from(requireAdmin(authorization));
    }

    @Transactional
    public void logout(String authorization) {
        String token = parseBearerToken(authorization);
        sessionRepository.deleteByToken(token);
    }

    @Transactional
    public AdminProfile updateProfile(String authorization, UpdateProfileRequest request) {
        AdminUser user = requireAdmin(authorization);
        user.updateDisplayName(request.displayName().trim());
        return AdminProfile.from(userRepository.save(user));
    }

    @Transactional
    public AdminProfile uploadAvatar(String authorization, MultipartFile file) {
        AdminUser user = requireAdmin(authorization);
        String previousAvatarUrl = user.getAvatarUrl();
        String avatarUrl = avatarStorageService.upload(user.getId(), file);
        user.updateAvatarUrl(avatarUrl);
        AdminUser saved = userRepository.save(user);
        if (previousAvatarUrl != null && !previousAvatarUrl.equals(avatarUrl)) {
            avatarStorageService.deleteByUrl(previousAvatarUrl);
        }
        return AdminProfile.from(saved);
    }

    @Transactional
    public AdminProfile removeAvatar(String authorization) {
        AdminUser user = requireAdmin(authorization);
        String previousAvatarUrl = user.getAvatarUrl();
        user.clearAvatarUrl();
        AdminUser saved = userRepository.save(user);
        avatarStorageService.deleteByUrl(previousAvatarUrl);
        return AdminProfile.from(saved);
    }

    @Transactional
    public void changePassword(String authorization, ChangePasswordRequest request) {
        String currentToken = parseBearerToken(authorization);
        AdminUser user = requireAdmin(authorization);
        if (!PASSWORD_ENCODER.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "当前密码不正确");
        }
        if (PASSWORD_ENCODER.matches(request.newPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "新密码不能与当前密码相同");
        }
        user.updatePasswordHash(PASSWORD_ENCODER.encode(request.newPassword()));
        userRepository.save(user);
        sessionRepository.deleteByAdminUserIdAndTokenNot(user.getId(), currentToken);
    }

    public String encodePassword(String password) {
        return PASSWORD_ENCODER.encode(password);
    }

    private ApiException invalidCredentials() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
    }

    private String parseBearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "请先登录");
        }
        String token = authorization.substring(7).trim();
        if (token.isBlank()) throw new ApiException(HttpStatus.UNAUTHORIZED, "请先登录");
        return token;
    }

    private String newToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
