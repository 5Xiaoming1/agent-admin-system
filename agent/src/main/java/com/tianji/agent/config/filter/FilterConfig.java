package com.tianji.agent.config.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 过滤器注册配置
 * 参考 Gateway 的 FilterRegistration，适配为 Spring MVC 的 FilterRegistrationBean
 */

@Configuration
public class FilterConfig {

    private final CustomFilter customFilter;

    public FilterConfig(CustomFilter customFilter) {
        this.customFilter = customFilter;
    }

    @Bean
    public FilterRegistrationBean<CustomFilter> customFilterRegistration() {
        FilterRegistrationBean<CustomFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(customFilter);
        registration.addUrlPatterns("/api/*");
        registration.setOrder(1);
        registration.setName("customFilter");
        return registration;
    }

}