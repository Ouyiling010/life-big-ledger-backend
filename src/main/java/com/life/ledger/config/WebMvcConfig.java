package com.life.ledger.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import com.life.ledger.interceptor.AdminInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Web 配置：登录拦截、管理员拦截、静态资源（头像）、跨域
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AppProperties appProperties;
    private final AdminInterceptor adminInterceptor;

    /** 允许跨域的来源，逗号分隔；默认仅本机前端 */
    @Value("${app.cors.allowed-origins:*}")
    private String allowedOrigins;

    public WebMvcConfig(AppProperties appProperties, AdminInterceptor adminInterceptor) {
        this.appProperties = appProperties;
        this.adminInterceptor = adminInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 全局登录校验
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/register",
                        "/api/auth/send-email-code", "/api/auth/reset-password");

        // 隐藏后台管理员校验
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/api/console/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String dir = Paths.get(appProperties.getUpload().getDir()).toAbsolutePath().normalize()
                .toString().replace("\\", "/");
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + dir + "/");
    }

    /** 跨域过滤器：在拦截器之前处理 OPTIONS 预检，避免被登录拦截误伤 */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        for (String origin : allowedOrigins.split(",")) {
            if (!origin.isBlank()) {
                config.addAllowedOriginPattern(origin.trim());
            }
        }
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        // 认证走 Authorization 头（Sa-Token 未使用 Cookie），无需跨域携带凭证
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
