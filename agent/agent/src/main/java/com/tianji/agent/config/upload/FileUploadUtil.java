package com.tianji.agent.config.upload;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传工具类
 * <p>
 * 提供文件上传的通用方法，包括文件校验、生成文件名、保存文件等。
 * 可供头像上传、知识库文件上传等场景复用。
 * </p>
 *
 * <p><b>使用示例：</b></p>
 * <pre>
 * // 校验图片文件
 * FileUploadUtil.validateImage(file);
 * 
 * // 生成唯一文件名
 * String filename = FileUploadUtil.generateFilename("avatar", originalFilename);
 * 
 * // 保存文件
 * FileUploadUtil.saveFile(file, targetPath);
 * 
 * // 确保目录存在
 * FileUploadUtil.ensureDirectoryExists(dir);
 * </pre>
 */
@Slf4j
public class FileUploadUtil {

    /** 允许上传的图片 MIME 类型 */
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "image/bmp"
    );

    /** 默认最大文件大小：2MB */
    private static final long DEFAULT_MAX_FILE_SIZE = 2 * 1024 * 1024;

    /**
     * 校验图片文件
     * <p>
     * 校验文件是否为空、文件类型是否为图片、文件大小是否超限。
     * </p>
     *
     * @param file      上传的文件
     * @param maxSize   最大文件大小（字节）
     */
    public static void validateImage(MultipartFile file, long maxSize) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("请选择要上传的文件");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new RuntimeException("仅支持图片格式文件");
        }

        if (file.getSize() > maxSize) {
            throw new RuntimeException("图片大小不能超过" + (maxSize / 1024 / 1024) + "MB");
        }
    }

    /**
     * 校验图片文件（默认最大 2MB）
     *
     * @param file 上传的文件
     */
    public static void validateImage(MultipartFile file) {
        validateImage(file, DEFAULT_MAX_FILE_SIZE);
    }

    /**
     * 校验文件（不限制类型，仅校验大小）
     *
     * @param file    上传的文件
     * @param maxSize 最大文件大小（字节）
     */
    public static void validateFile(MultipartFile file, long maxSize) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("请选择要上传的文件");
        }

        if (file.getSize() > maxSize) {
            throw new RuntimeException("文件大小不能超过" + (maxSize / 1024 / 1024) + "MB");
        }
    }

    /**
     * 生成唯一文件名
     * <p>
     * 格式：前缀_yyyyMMdd_HHmmss_随机后缀.扩展名
     * </p>
     *
     * @param prefix           文件名前缀，如 "avatar"、"knowledge"
     * @param originalFilename 原始文件名
     * @return 生成的文件名
     */
    public static String generateFilename(String prefix, String originalFilename) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String randomSuffix = UUID.randomUUID().toString().substring(0, 8);
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return prefix + "_" + timestamp + "_" + randomSuffix + extension;
    }

    /**
     * 生成头像文件名
     *
     * @param originalFilename 原始文件名
     * @return 文件名，格式：avatar_yyyyMMdd_HHmmss_随机后缀.扩展名
     */
    public static String generateAvatarFilename(String originalFilename) {
        return generateFilename("avatar", originalFilename);
    }

    /**
     * 生成知识库文件名
     *
     * @param originalFilename 原始文件名
     * @return 文件名，格式：kb_yyyyMMdd_HHmmss_随机后缀.扩展名
     */
    public static String generateKnowledgeFilename(String originalFilename) {
        return generateFilename("kb", originalFilename);
    }

    /**
     * 保存文件到指定路径
     *
     * @param file       上传的文件
     * @param targetPath 目标路径
     */
    public static void saveFile(MultipartFile file, Path targetPath) {
        try {
            file.transferTo(targetPath.toFile());
            log.info("文件保存成功: {}", targetPath.getFileName());
        } catch (IOException e) {
            log.error("文件保存失败: {}", targetPath.getFileName(), e);
            throw new RuntimeException("文件保存失败: " + e.getMessage());
        }
    }

    /**
     * 确保目录存在，不存在则创建
     *
     * @param dir 目录路径
     */
    public static void ensureDirectoryExists(Path dir) {
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
                log.info("创建目录: {}", dir);
            }
        } catch (IOException e) {
            throw new RuntimeException("无法创建目录: " + dir, e);
        }
    }

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 扩展名（包含点号），如无扩展名则返回空字符串
     */
    public static String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * 获取文件大小描述（人类可读格式）
     *
     * @param size 文件大小（字节）
     * @return 文件大小描述，如 "1.5MB"、"256KB"
     */
    public static String formatFileSize(long size) {
        if (size < 1024) {
            return size + "B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1fKB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1fMB", size / (1024.0 * 1024));
        } else {
            return String.format("%.1fGB", size / (1024.0 * 1024 * 1024));
        }
    }
}