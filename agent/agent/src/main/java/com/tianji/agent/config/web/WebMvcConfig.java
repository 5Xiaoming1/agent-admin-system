package com.tianji.agent.config.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Web MVC 配置
 * <p>
 * 配置静态资源映射，使上传的文件可以通过 HTTP 访问。
 * </p>
 */
@Configuration
@Slf4j
public class WebMvcConfig implements WebMvcConfigurer {

    /** 头像文件存储目录 */
    @Value("${agent.upload.avatar-dir:static/uploads/avatars}")
    private String avatarDir;

    /** 知识库文件存储目录 */
    @Value("${agent.knowledgebase.storage-dir:data/knowledgebase}")
    private String knowledgebaseDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射头像上传目录
        Path avatarPath = Paths.get(avatarDir).toAbsolutePath().normalize();
        String avatarLocation = "file:" + avatarPath.toString().replace("\\", "/") + "/";
        log.info("静态资源映射: /static/uploads/avatars/ -> {}", avatarLocation);
        registry.addResourceHandler("/static/uploads/avatars/**")
                .addResourceLocations(avatarLocation);

        // 映射知识库文件目录
        Path knowledgePath = Paths.get(knowledgebaseDir).toAbsolutePath().normalize();
        String knowledgeLocation = "file:" + knowledgePath.toString().replace("\\", "/") + "/";
        log.info("静态资源映射: /static/uploads/knowledgebase/ -> {}", knowledgeLocation);
        registry.addResourceHandler("/static/uploads/knowledgebase/**")
                .addResourceLocations(knowledgeLocation);
    }
}