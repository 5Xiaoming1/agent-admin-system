package com.tianji.agent.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工具实体类
 * <p>
 * 映射数据库中的 tools 表，存储工具集成的注册信息，
 * 包括工具名称、描述、类型、代码文件路径和参数说明等字段。
 * 工具代码不再存储在数据库中，而是以文件形式存储在 tools 目录下，
 * 数据库仅保存代码文件的相对路径。
 * 使用 MyBatis-Plus 注解完成表名映射、主键策略和逻辑删除配置。
 * </p>
 */
@Data
@TableName("tools")
@Schema(description = "工具实体")
public class Tools {

    /** 主键ID，数据库自增 */
    @Schema(description = "工具ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 工具名称 */
    @Schema(description = "工具名称")
    private String name;

    /** 工具描述 */
    @Schema(description = "工具描述")
    private String description;

    /** 工具类型：api/function/builtin，同时也作为文件存储的子目录名 */
    @Schema(description = "工具类型：api-接口，function-函数，builtin-内置")
    private String type;

    /** 状态：1-正常，0-禁用 */
    @Schema(description = "状态：1-正常，0-禁用")
    private Integer status;

    /** 逻辑删除：0-未删除，1-已删除 */
    @Schema(description = "逻辑删除：0-未删除，1-已删除")
    @TableLogic
    private Integer isDeleted;

    /** 工具代码文件相对路径，如 api/1.txt、office/2.txt */
    @Schema(description = "工具代码文件相对路径")
    private String codePath;

    /** 参数说明（JSON 字符串，格式：{"参数名": "参数说明"}） */
    @Schema(description = "参数说明（JSON字符串）")
    private String parameterDescriptions;

    /** 接口地址 */
    @Schema(description = "接口地址")
    private String endpoint;

    /** 参数配置（JSON 格式，如 {"city": "string"}） */
    @Schema(description = "参数配置（JSON格式）")
    private String parameters;

    /** 创建时间 */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /** 更新时间 */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}