package com.tianji.agent.config.redis;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Redis工具属性配置类
 * 绑定 redis.utils 前缀的配置项
 */
@Data
@Component
@ConfigurationProperties(prefix = "redis.utils")
public class RedisUtilsProperties {

    /** 默认过期时间：3600秒(1小时) */
    private long defaultTimeout = 3600L;
}