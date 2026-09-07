package com.tianji.agent.config.crypto;

import com.tianji.agent.config.captcha.AesUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 加密解密服务组件
 * <p>
 * 封装 AES 加密和解密操作，作为业务层与底层加密工具之间的桥梁。
 * 注入 {@link AesProperties} 获取密钥，对外提供统一的加密/解密接口，
 * 业务层无需关心密钥管理和底层实现细节。
 * </p>
 *
 * <p><b>数据流：</b></p>
 * <pre>
 * 前端 → Controller → Service → CryptoService.encrypt() → 数据库（密文）
 * 数据库（密文） → Service → CryptoService.decrypt() → Controller → 前端
 * </pre>
 *
 * @see AesProperties
 * @see AesUtil
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CryptoService {

    /** AES 加密配置属性，提供密钥 */
    private final AesProperties aesProperties;

    /**
     * 加密明文
     * <p>
     * 将明文使用 AES-128-ECB 算法加密后返回 Base64 编码的密文。
     * 如果明文为空或 null，则原样返回。
     * </p>
     *
     * @param plainText 待加密的明文
     * @return Base64 编码的密文，明文为空时返回原值
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        return AesUtil.encrypt(plainText, aesProperties.getSecretKey());
    }

    /**
     * 解密密文
     * <p>
     * 将 Base64 编码的密文解密为明文。
     * 如果密文为空或 null，则原样返回。
     * 如果解密失败（如数据库中存在未加密的历史明文数据），则返回原值。
     * </p>
     *
     * @param cipherText Base64 编码的密文
     * @return 解密后的明文，解密失败或密文为空时返回原值
     */
    public String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isEmpty()) {
            return cipherText;
        }
        try {
            return AesUtil.decrypt(cipherText, aesProperties.getSecretKey());
        } catch (Exception e) {
            log.warn("AES 解密失败，数据可能已是明文，返回原值");
            return cipherText;
        }
    }
}