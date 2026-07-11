package com.suofenpu.backend.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class WechatLoginConfigLogger implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(WechatLoginConfigLogger.class);

    private final WechatMiniProgramProperties properties;

    public WechatLoginConfigLogger(WechatMiniProgramProperties properties) {
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (properties.hasWechatCredentials()) {
            log.info("微信小程序登录配置已加载：appId={}，devLoginEnabled={}", mask(properties.getAppId()), properties.isDevLoginEnabled());
            return;
        }
        if (properties.isDevLoginEnabled()) {
            log.warn("微信小程序登录配置未加载：当前使用开发登录模式，登录会生成 dev_openid");
            return;
        }
        log.warn("微信小程序登录配置未加载：真实微信登录会失败，请配置 WECHAT_APP_ID/WECHAT_APP_SECRET 或使用 ./start-local.sh");
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
}
