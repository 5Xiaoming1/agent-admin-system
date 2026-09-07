package com.tianji.agent.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 配置管理实体类
 * <p>
 * 映射数据库中的 setup 表，存储 AI 服务商配置信息，
 * 包括配置名称、API 密钥和模型地址等字段。
 * 使用 MyBatis-Plus 注解完成表名映射、主键策略和逻辑删除配置。
 * </p>
 */
@Data
@TableName("setup")
@Schema(description = "配置实体")
public class Setup {

    /** 主键ID，数据库自增 */
    @Schema(description = "配置ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 配置名称 */
    @Schema(description = "配置名称")
    private String name;

    /** API密钥 */
    @Schema(description = "API密钥")
    private String apiKey;

    /** 模型地址 */
    @Schema(description = "模型地址")
    private String baseUrl;

    /** 状态：1-正常，0-禁用 */
    @Schema(description = "状态：1-正常，0-禁用")
    private Integer status;

    /** 逻辑删除：0-未删除，1-已删除 */
    @Schema(description = "逻辑删除：0-未删除，1-已删除")
    @TableLogic
    private Integer isDeleted;

    /** 创建时间 */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /** 更新时间 */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}