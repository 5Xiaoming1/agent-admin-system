package com.tianji.agent.config.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "agent.jwt")
@Data
public class JwtProperties {

    private String userSecretKey;

    private long userTtl;

    private String userTokenName;

    private List<String> excludePaths;

}