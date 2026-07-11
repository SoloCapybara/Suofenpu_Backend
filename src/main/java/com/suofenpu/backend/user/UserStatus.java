package com.suofenpu.backend.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "用户状态：ACTIVE=正常；DISABLED=禁用")
public enum UserStatus {
    ACTIVE,
    DISABLED
}
