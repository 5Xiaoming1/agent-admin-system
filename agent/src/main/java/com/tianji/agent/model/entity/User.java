package com.tianji.agent.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * <p>
 * 映射数据库中的 user 表，包含用户基本信息、账号状态和管理员标识等字段。
 * 使用 MyBatis-Plus 注解完成表名映射、主键策略和逻辑删除配置。
 * </p>
 */
@Data
@TableName("user")
@Schema(description = "用户实体")
public class User {

    /** 主键ID，数据库自增 */
    @Schema(description = "用户ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名，用于登录账号 */
    @Schema(description = "用户名")
    private String username;

    /** 登录密码 */
    @Schema(description = "登录密码")
    private String password;

    /** 头像URL */
    @Schema(description = "头像URL")
    private String avatar;

    /** 管理员等级：2-超级管理员，1-管理员，0-普通用户 */
    @Schema(description = "管理员等级：2-超级管理员，1-管理员，0-普通用户")
    private Integer admin;

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