package com.mianshi.backend.config;

import com.mianshi.backend.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置类
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                // 拦截所有请求
                .addPathPatterns("/**")
                // 排除不需要认证的路径
                .excludePathPatterns(
                        "/api/user/login", // 登录接口
                        "/api/user/create", // 注册接口（POST）
                        "/doc.html", // Knife4j 文档
                        "/webjars/**", // Knife4j 静态资源
                        "/swagger-resources/**", // Swagger 资源
                        "/v3/api-docs/**", // OpenAPI 文档
                        "/favicon.ico", // 网站图标
                        "/error" // 错误页面
                );
    }
}
