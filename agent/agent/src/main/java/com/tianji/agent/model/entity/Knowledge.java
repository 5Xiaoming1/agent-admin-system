package com.tianji.agent.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库实体类
 * <p>
 * 映射数据库中的 knowledge 表，存储知识库的创建与维护信息，
 * 包括知识库名称、描述、类型、内容路径和文档数量等字段。
 * 知识库实际文档内容以文件形式存储在 data/knowledgebase 目录下，
 * 按类型分目录组织：data/knowledgebase/{type}/{id}/。
 * 使用 MyBatis-Plus 注解完成表名映射、主键策略和逻辑删除配置。
 * </p>
 */
@Data
@TableName("knowledge")
@Schema(description = "知识库实体")
public class Knowledge {

    /** 主键ID，数据库自增 */
    @Schema(description = "知识库ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 知识库名称 */
    @Schema(description = "知识库名称")
    private String name;

    /** 知识库描述 */
    @Schema(description = "知识库描述")
    private String description;

    /** 知识库类型：document/qa/web，同时也作为文件存储的子目录名 */
    @Schema(description = "知识库类型：document-文档，qa-问答，web-网页")
    private String type;

    /** 文档数量 */
    @Schema(description = "文档数量")
    private Integer documentCount;

    /** 状态：1-正常，0-禁用 */
    @Schema(description = "状态：1-正常，0-禁用")
    private Integer status;

    /** 逻辑删除：0-未删除，1-已删除 */
    @Schema(description = "逻辑删除：0-未删除，1-已删除")
    @TableLogic
    private Integer isDeleted;

    /** 知识库内容存储路径（相对路径），如 document/1/ */
    @Schema(description = "知识库内容存储路径")
    private String contentPath;

    /** 知识原数据上传文件路径 */
    @Schema(description = "知识原数据上传文件路径")
    private String sourceFilePath;

    /** 创建时间 */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /** 更新时间 */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}