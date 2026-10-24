package com.life.ledger.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / OpenAPI 文档配置
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI ledgerOpenAPI() {
        final String scheme = "Authorization";
        return new OpenAPI()
                .info(new Info()
                        .title("生活大作账 API")
                        .description("面向个人和家庭的纯记录型记账系统接口文档")
                        .version("v1.0"))
                .addSecurityItem(new SecurityRequirement().addList(scheme))
                .components(new Components()
                        .addSecuritySchemes(scheme, new SecurityScheme()
                                // 使用 APIKEY 类型：Knife4j 对 HTTP bearer 类型会错误地把方案名当作请求头名，
                                // 导致发出去的是 `Bearer: xxx` 而不是 `Authorization: Bearer xxx`
                                .name("Authorization")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .description("填写 Bearer {token}，注意 Bearer 后有一个空格；token 取自登录接口返回的 data.token")));
    }
}
