package com.suofenpu.backend.admin.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminAccountInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminAccountInitializer.class);
    private final AdminUserRepository userRepository;
    private final AdminAuthService authService;
    private final String username;
    private final String password;
    private final String displayName;

    public AdminAccountInitializer(
        AdminUserRepository userRepository,
        AdminAuthService authService,
        @Value("${admin.initial-username:admin}") String username,
        @Value("${admin.initial-password:admin123456}") String password,
        @Value("${admin.initial-display-name:\u5e97\u957f}") String displayName
    ) {
        this.userRepository = userRepository;
        this.authService = authService;
        this.username = username;
        this.password = password;
        this.displayName = displayName;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Only create the configured super administrator when that username is
        // absent. Never update an existing row here: in particular, a password
        // changed by the administrator must survive every application restart.
        if (userRepository.findByUsername(username.trim()).isPresent()) return;
        userRepository.save(new AdminUser(username.trim(), authService.encodePassword(password), displayName.trim()));
        log.warn("已初始化管理端账号 username={}，请通过 ADMIN_INITIAL_PASSWORD 设置并妥善保管初始密码", username);
    }
}
