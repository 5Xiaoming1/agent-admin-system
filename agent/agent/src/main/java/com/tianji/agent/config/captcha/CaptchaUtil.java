package com.tianji.agent.config.captcha;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

/**
 * 验证码工具类
 * <p>
 * 基于 Java AWT 实现，无需第三方依赖，可生成包含干扰线、干扰点、字符旋转的验证码图片，
 * 并以 Base64 编码的 Data URI 格式输出，前端可直接嵌入 &lt;img&gt; 标签使用。
 * </p>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * CaptchaUtil.CaptchaResult result = CaptchaUtil.generate();
 * String code = result.code();           // 验证码文本，需存入 Session/Redis 供后续校验
 * String image = result.base64Image();   // Base64 图片，可直接返回给前端
 * }</pre>
 *
 * @author tianji
 */
public class CaptchaUtil {

    /** 验证码图片宽度 */
    private static final int WIDTH = 120;

    /** 验证码图片高度 */
    private static final int HEIGHT = 40;

    /** 验证码字符长度 */
    private static final int CODE_LENGTH = 4;

    /**
     * 验证码候选字符集
     * 排除了易混淆字符：0/O、1/I/l
     */
    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";

    /** 随机数生成器，复用避免频繁创建 */
    private static final Random RANDOM = new Random();

    private CaptchaUtil() {
        // 工具类禁止实例化
    }

    /**
     * 生成验证码
     * <p>
     * 一次调用完成验证码文本生成、图片绘制、Base64编码全部流程。
     * </p>
     *
     * @return 验证码结果对象，包含文本和 Base64 图片
     */
    public static CaptchaResult generate() {
        String code = generateCode();
        BufferedImage image = createImage(code);
        String base64Image = encodeToBase64(image);
        return new CaptchaResult(code, base64Image);
    }

    /**
     * 从候选字符集中随机选取指定长度的字符，生成验证码文本
     *
     * @return 验证码文本字符串
     */
    private static String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }

    /**
     * 创建验证码图片
     * <p>
     * 绘制流程：背景色 → 干扰线 → 验证码字符 → 干扰点
     * </p>
     *
     * @param code 验证码文本
     * @return 绘制完成的 BufferedImage 对象
     */
    private static BufferedImage createImage(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // 开启抗锯齿，使字符边缘更平滑
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 填充浅灰色背景
        g2d.setColor(new Color(240, 240, 240));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // 绘制干扰线，增加机器识别难度
        drawInterferenceLines(g2d);

        // 绘制验证码字符，带有随机旋转和偏移
        drawCodeChars(g2d, code);

        // 绘制干扰点，进一步增加噪点
        drawInterferenceDots(g2d);

        g2d.dispose();
        return image;
    }

    /**
     * 绘制干扰线
     * <p>
     * 在图片上随机绘制 5 条彩色线段，用于干扰 OCR 识别。
     * </p>
     *
     * @param g2d 图形上下文
     */
    private static void drawInterferenceLines(Graphics2D g2d) {
        for (int i = 0; i < 5; i++) {
            g2d.setColor(randomColor(100, 180));
            int x1 = RANDOM.nextInt(WIDTH);
            int y1 = RANDOM.nextInt(HEIGHT);
            int x2 = RANDOM.nextInt(WIDTH);
            int y2 = RANDOM.nextInt(HEIGHT);
            g2d.drawLine(x1, y1, x2, y2);
        }
    }

    /**
     * 绘制验证码字符
     * <p>
     * 每个字符使用随机颜色、随机字体，并施加小幅随机旋转和位置偏移，
     * 使字符之间产生错落感，提升反爬效果。
     * </p>
     *
     * @param g2d  图形上下文
     * @param code 验证码文本
     */
    private static void drawCodeChars(Graphics2D g2d, String code) {
        int charWidth = WIDTH / (code.length() + 1);
        for (int i = 0; i < code.length(); i++) {
            String ch = String.valueOf(code.charAt(i));
            g2d.setColor(randomColor(40, 100));
            g2d.setFont(randomFont());
            int x = charWidth * (i + 1) - 5 + RANDOM.nextInt(5);
            int y = 25 + RANDOM.nextInt(8);
            // 随机旋转角度，范围 ±0.25 弧度
            double angle = (RANDOM.nextDouble() - 0.5) * 0.5;
            g2d.rotate(angle, x, y);
            g2d.drawString(ch, x, y);
            g2d.rotate(-angle, x, y);
        }
    }

    /**
     * 绘制干扰点
     * <p>
     * 在图片上随机绘制 30 个彩色小圆点，增加背景噪点。
     * </p>
     *
     * @param g2d 图形上下文
     */
    private static void drawInterferenceDots(Graphics2D g2d) {
        for (int i = 0; i < 30; i++) {
            g2d.setColor(randomColor(100, 200));
            int x = RANDOM.nextInt(WIDTH);
            int y = RANDOM.nextInt(HEIGHT);
            g2d.fillOval(x, y, 2, 2);
        }
    }

    /**
     * 生成随机颜色
     *
     * @param min RGB 各通道最小值
     * @param max RGB 各通道最大值
     * @return 随机 Color 对象
     */
    private static Color randomColor(int min, int max) {
        int r = min + RANDOM.nextInt(max - min);
        int g = min + RANDOM.nextInt(max - min);
        int b = min + RANDOM.nextInt(max - min);
        return new Color(r, g, b);
    }

    /**
     * 生成随机字体
     * <p>
     * 在 Arial、Verdana、Tahoma 中随机选择，粗细随机，字号在 22~27 之间随机。
     * </p>
     *
     * @return 随机 Font 对象
     */
    private static Font randomFont() {
        String[] fontNames = {"Arial", "Verdana", "Tahoma"};
        String fontName = fontNames[RANDOM.nextInt(fontNames.length)];
        int style = RANDOM.nextBoolean() ? Font.BOLD : Font.PLAIN;
        int size = 22 + RANDOM.nextInt(6);
        return new Font(fontName, style, size);
    }

    /**
     * 将 BufferedImage 编码为 Base64 格式的 Data URI
     * <p>
     * 输出格式为 {@code data:image/png;base64,...}，前端可直接作为 img 标签的 src 属性使用。
     * </p>
     *
     * @param image 验证码图片
     * @return Base64 编码的 Data URI 字符串
     * @throws RuntimeException 图片编码失败时抛出
     */
    private static String encodeToBase64(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("验证码图片编码失败", e);
        }
    }

    /**
     * 验证码生成结果
     *
     * @param code        验证码文本，服务端需保存至 Session 或 Redis 用于后续校验
     * @param base64Image Base64 编码的验证码图片，格式为 {@code data:image/png;base64,...}
     */
    public record CaptchaResult(String code, String base64Image) {
    }

}