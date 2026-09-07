package com.tianji.agent.config.captcha;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES 加密工具类
 * <p>
 * 使用 AES-128-ECB 算法对敏感字段（如 API Key）进行加密和解密。
 * 密钥长度固定为 16 字节（128 位）。
 * 加密结果使用 Base64 编码，便于存储和传输。
 * </p>
 *
 * <p><b>使用示例：</b></p>
 * <pre>
 * String cipherText = AesUtil.encrypt("sk-xxx", "TianJiAgent@2026");
 * String plainText  = AesUtil.decrypt(cipherText, "TianJiAgent@2026");
 * </pre>
 */
public class AesUtil {

    /** 加密算法 */
    private static final String ALGORITHM = "AES";

    /** 加密模式：ECB + PKCS5Padding 填充 */
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    /**
     * 加密明文，返回 Base64 编码的密文
     *
     * @param plainText 待加密的明文
     * @param key       16 字节的 AES 密钥
     * @return Base64 编码的密文字符串
     * @throws RuntimeException 加密失败时抛出
     */
    public static String encrypt(String plainText, String key) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(
                    plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("AES 加密失败", e);
        }
    }

    /**
     * 解密 Base64 编码的密文，返回明文
     *
     * @param cipherText Base64 编码的密文字符串
     * @param key        16 字节的 AES 密钥
     * @return 解密后的明文字符串
     * @throws RuntimeException 解密失败时抛出
     */
    public static String decrypt(String cipherText, String key) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decoded = Base64.getDecoder().decode(cipherText);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES 解密失败", e);
        }
    }
}