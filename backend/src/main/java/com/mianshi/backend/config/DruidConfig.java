package com.mianshi.backend.config;

import com.alibaba.druid.support.jakarta.StatViewServlet;
import com.alibaba.druid.support.jakarta.WebStatFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 手动注册 Druid 监控 Servlet 和 Filter，兼容 Spring Boot 3。
 */
@Configuration
public class DruidConfig {

    @Value("${spring.datasource.druid.stat-view-servlet.login-username:root}")
    private String loginUsername;

    @Value("${spring.datasource.druid.stat-view-servlet.login-password:123}")
    private String loginPassword;

    @Value("${spring.datasource.druid.stat-view-servlet.allow:127.0.0.1}")
    private String allow;

    @Value("${spring.datasource.druid.stat-view-servlet.reset-enable:false}")
    private String resetEnable;

    @Value("${spring.datasource.druid.web-stat-filter.exclusions:*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*}")
    private String exclusions;

    @Bean
    public ServletRegistrationBean<StatViewServlet> druidStatViewServlet() {
        ServletRegistrationBean<StatViewServlet> bean = new ServletRegistrationBean<>(new StatViewServlet());
        bean.addUrlMappings("/druid/*");
        Map<String, String> initParams = new HashMap<>(4);
        initParams.put("loginUsername", loginUsername);
        initParams.put("loginPassword", loginPassword);
        initParams.put("allow", allow);
        initParams.put("resetEnable", resetEnable);
        bean.setInitParameters(initParams);
        bean.setName("druidStatViewServlet");
        bean.setLoadOnStartup(1);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<WebStatFilter> druidWebStatFilter() {
        FilterRegistrationBean<WebStatFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new WebStatFilter());
        bean.addUrlPatterns("/*");
        bean.addInitParameter("exclusions", exclusions);
        bean.setName("druidWebStatFilter");
        return bean;
    }
}

