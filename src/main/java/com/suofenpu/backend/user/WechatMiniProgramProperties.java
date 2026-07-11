package com.suofenpu.backend.user;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "wechat.mini-program")
public class WechatMiniProgramProperties {
    private String appId;
    private String appSecret;
    private boolean devLoginEnabled;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public boolean isDevLoginEnabled() {
        return devLoginEnabled;
    }

    public void setDevLoginEnabled(boolean devLoginEnabled) {
        this.devLoginEnabled = devLoginEnabled;
    }

    public boolean hasWechatCredentials() {
        return appId != null && !appId.isBlank() && appSecret != null && !appSecret.isBlank();
    }
}
