package com.suofenpu.backend.user;

import java.net.URI;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.suofenpu.backend.common.ApiException;

@Component
public class WechatClient {
    private static final Logger log = LoggerFactory.getLogger(WechatClient.class);
    private static final int MAX_WECHAT_BODY_LOG_LEN = 500;

    private final RestClient restClient;
    private final WechatMiniProgramProperties properties;
    private final ObjectMapper objectMapper;

    public WechatClient(WechatMiniProgramProperties properties) {
        this.restClient = RestClient.create();
        this.properties = properties;
        this.objectMapper = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public WechatSession codeToSession(String code) {
        if (!properties.hasWechatCredentials()) {
            if (!properties.isDevLoginEnabled()) {
                log.warn("微信登录失败：未配置 appId/appSecret，且开发登录已关闭");
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "未配置微信小程序 appId/appSecret");
            }
            log.info("微信登录使用开发模式：未配置真实 appId/appSecret，生成 dev_openid");
            return new WechatSession("dev_openid_" + code, null);
        }

        log.info("微信登录请求微信服务器：appId={}，code={}", properties.getAppId(), mask(code));

        URI uri = UriComponentsBuilder
            .fromUriString("https://api.weixin.qq.com/sns/jscode2session")
            .queryParam("appid", properties.getAppId())
            .queryParam("secret", properties.getAppSecret())
            .queryParam("js_code", code)
            .queryParam("grant_type", "authorization_code")
            .build()
            .toUri();

        String responseBody = restClient.get()
            .uri(uri)
            .retrieve()
            .body(String.class);

        WechatSessionResponse response = parseWechatResponse(responseBody);

        if (response == null) {
            log.warn("微信登录失败：微信 jscode2session 无响应");
            throw new ApiException(HttpStatus.BAD_GATEWAY, "微信登录接口无响应");
        }
        if (response.errcode() != null && response.errcode() != 0) {
            log.warn("微信登录失败：微信返回错误 errcode={} errmsg={}", response.errcode(), response.errmsg());
            throw new ApiException(HttpStatus.BAD_REQUEST, "微信登录失败：" + response.errmsg());
        }
        if (response.openid() == null || response.openid().isBlank()) {
            log.warn("微信登录失败：微信响应没有 openid");
            throw new ApiException(HttpStatus.BAD_GATEWAY, "微信登录未返回 openid");
        }
        log.info("微信登录请求微信服务器成功：openid={} unionid={}", mask(response.openid()), mask(response.unionid()));
        return new WechatSession(response.openid(), response.unionid());
    }

    private WechatSessionResponse parseWechatResponse(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(responseBody, WechatSessionResponse.class);
        } catch (JsonProcessingException ex) {
            log.warn("微信登录失败：微信响应解析失败 error={} body={}", ex.getOriginalMessage(), maskWechatBody(responseBody));
            throw new ApiException(HttpStatus.BAD_GATEWAY, "微信登录接口响应格式异常");
        }
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

    private String truncate(String value) {
        if (value == null) {
            return "null";
        }
        if (value.length() <= MAX_WECHAT_BODY_LOG_LEN) {
            return value;
        }
        return value.substring(0, MAX_WECHAT_BODY_LOG_LEN) + "...(truncated)";
    }

    private String maskWechatBody(String value) {
        return truncate(value)
            .replaceAll("(\"(?:session_key|openid|unionid)\"\\s*:\\s*\")([^\"]+)(\")", "$1***$3");
    }

    private record WechatSessionResponse(
        String openid,
        String unionid,
        @JsonProperty("session_key")
        String sessionKey,
        Integer errcode,
        String errmsg
    ) {
    }
}
