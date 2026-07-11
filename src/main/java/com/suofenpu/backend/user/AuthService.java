package com.suofenpu.backend.user;

import static com.suofenpu.backend.user.UserDtos.DevLoginRequest;
import static com.suofenpu.backend.user.UserDtos.LoginResponse;
import static com.suofenpu.backend.user.UserDtos.UpdateProfileRequest;
import static com.suofenpu.backend.user.UserDtos.UserResponse;
import static com.suofenpu.backend.user.UserDtos.WechatLoginRequest;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suofenpu.backend.common.ApiException;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;
    private static final int SESSION_DAYS = 30;
    private static final String GUEST_AVATAR = "/assets/avatars/guest.png";
    private static final String[] DEFAULT_AVATARS = {
        "/assets/avatars/user-1.png",
        "/assets/avatars/user-2.png",
        "/assets/avatars/user-3.png",
        "/assets/avatars/user-4.png",
        "/assets/avatars/user-5.png",
        "/assets/avatars/user-6.png",
        "/assets/avatars/user-7.png",
        "/assets/avatars/user-8.png",
        "/assets/avatars/user-9.png",
        "/assets/avatars/user-10.png"
    };

    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepository;
    private final WechatClient wechatClient;
    private final WechatMiniProgramProperties properties;

    public AuthService(
        UserRepository userRepository,
        UserSessionRepository sessionRepository,
        WechatClient wechatClient,
        WechatMiniProgramProperties properties
    ) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.wechatClient = wechatClient;
        this.properties = properties;
    }

    @Transactional
    public LoginResponse wechatLogin(WechatLoginRequest request) {
        log.info("微信小程序登录开始：nickname={} code={}", safeText(request.nickname()), mask(request.code()));
        WechatSession wechatSession = wechatClient.codeToSession(request.code());
        Optional<User> existingUser = userRepository.findByOpenid(wechatSession.openid());
        boolean registeredNow = existingUser.isEmpty();
        User user = existingUser
            .orElseGet(() -> new User(wechatSession.openid(), wechatSession.unionid(), null, request.nickname(), randomDefaultAvatar()));
        user.mergeProfile(null, request.nickname(), wechatSession.unionid());
        ensureDefaultAvatar(user);
        user = userRepository.save(user);
        LoginResponse response = createLoginResponse(user);
        log.info(
            "微信小程序登录成功：userId={} openid={} result={} token={} expiresAt={}",
            user.getId(),
            mask(user.getOpenid()),
            registeredNow ? "自动注册新用户" : "老用户登录",
            mask(response.token()),
            response.expiresAt()
        );
        return response;
    }

    @Transactional
    public LoginResponse devLogin(DevLoginRequest request) {
        if (!properties.isDevLoginEnabled()) {
            log.warn("开发登录失败：开发登录已关闭 phone={}", mask(request.phone()));
            throw new ApiException(HttpStatus.FORBIDDEN, "开发登录已关闭");
        }
        log.info("开发登录开始：phone={} nickname={}", mask(request.phone()), safeText(request.nickname()));
        Optional<User> existingUser = userRepository.findByPhone(request.phone());
        boolean registeredNow = existingUser.isEmpty();
        User user = existingUser
            .orElseGet(() -> new User(null, null, request.phone(), request.nickname(), randomDefaultAvatar()));
        user.mergeProfile(request.phone(), request.nickname(), null);
        ensureDefaultAvatar(user);
        user = userRepository.save(user);
        LoginResponse response = createLoginResponse(user);
        log.info(
            "开发登录成功：userId={} phone={} result={} token={} expiresAt={}",
            user.getId(),
            mask(user.getPhone()),
            registeredNow ? "自动注册新用户" : "老用户登录",
            mask(response.token()),
            response.expiresAt()
        );
        return response;
    }

    @Transactional(readOnly = true)
    public User requireUser(String authorizationHeader) {
        return resolveUser(authorizationHeader)
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "请先登录"));
    }

    @Transactional(readOnly = true)
    public Optional<User> resolveUser(String authorizationHeader) {
        String token = parseBearerToken(authorizationHeader);
        if (token == null) {
            return Optional.empty();
        }
        return sessionRepository.findByToken(token)
            .filter(session -> !session.isExpired())
            .map(UserSession::getUser)
            .filter(user -> user.getStatus() == UserStatus.ACTIVE);
    }

    @Transactional
    public UserResponse currentUser(String authorizationHeader) {
        User user = requireUser(authorizationHeader);
        ensureDefaultAvatar(user);
        user = userRepository.save(user);
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateCurrentUser(String authorizationHeader, UpdateProfileRequest request) {
        User user = requireUser(authorizationHeader);
        user.mergeProfile(request.phone(), request.nickname(), null);
        return UserResponse.from(user);
    }

    private void ensureDefaultAvatar(User user) {
        if (!hasUsableAvatar(user.getAvatarUrl())) {
            user.setAvatarUrl(randomDefaultAvatar());
        }
    }

    private boolean hasUsableAvatar(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isBlank()) {
            return false;
        }
        String normalized = avatarUrl.trim();
        return !"-".equals(normalized) && !GUEST_AVATAR.equals(normalized);
    }

    private String randomDefaultAvatar() {
        return DEFAULT_AVATARS[SECURE_RANDOM.nextInt(DEFAULT_AVATARS.length)];
    }

    private LoginResponse createLoginResponse(User user) {
        Instant expiresAt = Instant.now().plus(SESSION_DAYS, ChronoUnit.DAYS);
        UserSession session = sessionRepository.save(new UserSession(newToken(), user, expiresAt));
        return new LoginResponse(session.getToken(), expiresAt, UserResponse.from(user));
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String mask(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        if (value.length() <= 8) {
            return value.charAt(0) + "***" + value.charAt(value.length() - 1);
        }
        return value.substring(0, 4) + "***" + value.substring(value.length() - 4);
    }

    private String newToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String parseBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return null;
        }
        String prefix = "Bearer ";
        if (!authorizationHeader.startsWith(prefix)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authorization 必须使用 Bearer token");
        }
        String token = authorizationHeader.substring(prefix.length()).trim();
        return token.isBlank() ? null : token;
    }
}
