package com.suofenpu.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI suofenpuOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("嗦粉铺点餐系统 API")
                .version("v1")
                .description("客户扫码点餐小程序与商家后台的联调接口。"));
    }
}
