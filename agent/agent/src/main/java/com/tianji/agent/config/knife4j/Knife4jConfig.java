package com.tianji.agent.config.knife4j;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI agentOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Agent管理系统 API")
                        .version("v1.0.0")
                        .description("Agent管理系统 RESTful API 接口文档")
                        .contact(new Contact()
                                .name("徐永安")
                                .email("support@xuyongan.com")));
    }

    @Bean
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder()
                .group("系统管理")
                .pathsToMatch("/api/system/**")
                .build();
    }

    @Bean
    public GroupedOpenApi defaultApi() {
        return GroupedOpenApi.builder()
                .group("default")
                .pathsToMatch("/api/**")
                .pathsToExclude("/api/system/**")
                .build();
    }
}