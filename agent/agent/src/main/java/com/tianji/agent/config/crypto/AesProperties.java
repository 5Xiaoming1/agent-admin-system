package com.tianji.agent.config.crypto;

import com.tianji.agent.config.captcha.AesUtil;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AES 加密属性配置类
 * <p>
 * 绑定 {@code agent.crypto.aes} 前缀的配置项，用于读取 AES 加密密钥。
 * 密钥长度必须为 16 字节（128 位），对应 AES-128 算法。
 * </p>
 *
 * <p><b>配置示例（application.yaml）：</b></p>
 * <pre>
 * agent:
 *   crypto:
 *     aes:
 *       secret-key: TianJiAgent@2026
 * </pre>
 *
 * @see CryptoService
 * @see AesUtil
 */
@Data
@Component
@ConfigurationProperties(prefix = "agent.crypto.aes")
public class AesProperties {

    /** AES 加密密钥，长度必须为 16 字节（128 位） */
    private String secretKey = "TianJiAgent@2026";
}