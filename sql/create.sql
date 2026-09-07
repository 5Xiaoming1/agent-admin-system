-- ============================================
-- 配置管理数据库表结构
-- 表名：setup（配置管理表）
-- ============================================
DROP TABLE IF EXISTS `setup`;
CREATE TABLE `setup` (
                         `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                         `name`        VARCHAR(128) NOT NULL                COMMENT '配置名称',
                         `api_key`     VARCHAR(512) NOT NULL                COMMENT 'API密钥',
                         `base_url`    VARCHAR(512) NOT NULL                COMMENT '模型地址',
                         `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1-正常，0-禁用',
                         `is_deleted`  TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除，1-已删除',
                         `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配置管理表';

-- ============================================
-- Tools工具数据库表结构
-- 表名：tools（工具表）
-- ============================================
DROP TABLE IF EXISTS `tools`;
CREATE TABLE `tools` (
                         `id`                     BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                         `name`                   VARCHAR(128)  NOT NULL                COMMENT '工具名称',
                         `description`            VARCHAR(512)  NOT NULL DEFAULT ''     COMMENT '工具描述',
                         `type`                   VARCHAR(64)   NOT NULL                COMMENT '工具类型',
                         `status`                 TINYINT       NOT NULL DEFAULT 1      COMMENT '状态：1-正常，0-禁用',
                         `is_deleted`             TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除，1-已删除',
                         `code_path`              VARCHAR(512)  NOT NULL DEFAULT ''     COMMENT '工具代码文件相对路径',
                         `parameter_descriptions` TEXT          NULL                    COMMENT '参数说明（JSON格式，如{"参数名":"参数说明"}）',
                         `endpoint`               VARCHAR(512)  NOT NULL DEFAULT ''     COMMENT '接口地址',
                         `parameters`             TEXT          NULL                    COMMENT '参数配置（JSON格式）',
                         `create_time`            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         `update_time`            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工具表';

-- ============================================
-- 知识库数据库表结构
-- 表名：knowledge（知识库表）
-- ============================================
DROP TABLE IF EXISTS `knowledge`;
CREATE TABLE `knowledge` (
                             `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                             `name`             VARCHAR(128) NOT NULL                COMMENT '知识库名称',
                             `description`      VARCHAR(512) NOT NULL DEFAULT ''     COMMENT '知识库描述',
                             `type`             VARCHAR(64)  NOT NULL                COMMENT '知识库类型',
                             `document_count`   INT          NOT NULL DEFAULT 0      COMMENT '文档数量',
                             `status`           TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1-正常，0-禁用',
                             `is_deleted`       TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除，1-已删除',
                             `content_path`     VARCHAR(512) NOT NULL DEFAULT ''     COMMENT '知识库内容存储路径（相对路径）',
                             `source_file_path` VARCHAR(512) NOT NULL DEFAULT ''     COMMENT '知识原数据上传文件路径',
                             `create_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `update_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库表';

-- ============================================
-- Agent数据库表结构
-- 表名：agent（智能体表）
-- 关联关系：
--   setup_id → setup 表（一对一，每个 Agent 对应一个配置）
--   knowledge_ids → knowledge 表（多对多，通过 agent_knowledge 中间表关联）
-- ============================================
DROP TABLE IF EXISTS `agent`;
CREATE TABLE `agent` (
                         `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                         `name`           VARCHAR(128) NOT NULL                COMMENT '智能体名称',
                         `description`    VARCHAR(512) NOT NULL DEFAULT ''     COMMENT '智能体描述',
                         `type`           VARCHAR(64)  NOT NULL                COMMENT '智能体类型',
                         `setup_id`       BIGINT       NOT NULL DEFAULT 0      COMMENT '指向setup表',
                         `tokens`         BIGINT       NOT NULL DEFAULT 0      COMMENT 'Token用量',
                         `status`         TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1-正常，0-禁用',
                         `is_deleted`     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除，1-已删除',
                         `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                         PRIMARY KEY (`id`),
                         INDEX `idx_setup_id` (`setup_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='智能体表';

-- ============================================
-- Agent知识库关联表
-- 表名：agent_knowledge（智能体知识库关联表）
-- ============================================
DROP TABLE IF EXISTS `agent_knowledge`;
CREATE TABLE `agent_knowledge` (
                                   `id`           BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                   `agent_id`     BIGINT NOT NULL COMMENT '智能体ID',
                                   `knowledge_id` BIGINT NOT NULL COMMENT '知识库ID',
                                   PRIMARY KEY (`id`),
                                   INDEX `idx_agent_id` (`agent_id`),
                                   INDEX `idx_knowledge_id` (`knowledge_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='智能体知识库关联表';

-- ============================================
-- 知识库工具关联表
-- 表名：knowledge_tool（知识库工具关联表）
-- ============================================
DROP TABLE IF EXISTS `knowledge_tool`;
CREATE TABLE `knowledge_tool` (
                                  `id`            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                  `knowledge_id`  BIGINT NOT NULL COMMENT '知识库ID',
                                  `tool_id`       BIGINT NOT NULL COMMENT '工具ID',
                                  PRIMARY KEY (`id`),
                                  INDEX `idx_knowledge_id` (`knowledge_id`),
                                  INDEX `idx_tool_id` (`tool_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库工具关联表';

-- ============================================
-- 用户数据库表结构
-- 表名：user（用户表）
-- admin 字段：2-超级管理员(不可删除)，1-管理员，0-普通用户
-- ============================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
                        `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                        `username`    VARCHAR(64)  NOT NULL                COMMENT '用户名',
                        `password`    VARCHAR(128) NOT NULL                COMMENT '密码',
                        `avatar`      VARCHAR(512) NOT NULL DEFAULT ''     COMMENT '头像URL',
                        `admin`       TINYINT      NOT NULL DEFAULT 0      COMMENT '管理员等级：2-超级管理员，1-管理员，0-普通用户',
                        `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1-正常，0-禁用',
                        `is_deleted`  TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除，1-已删除',
                        `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 插入默认超级管理员用户（密码：123456）
INSERT INTO `user` (`username`, `password`, `admin`, `status`, `is_deleted`) 
VALUES ('admin', '123456', 2, 1, 0);

-- ============================================
-- 登录日志数据库表结构
-- 表名：login_log（登录日志表）
-- ============================================
DROP TABLE IF EXISTS `login_log`;
CREATE TABLE `login_log` (
                             `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                             `user_id`     BIGINT       NOT NULL DEFAULT 0      COMMENT '用户ID',
                             `username`    VARCHAR(64)  NOT NULL                COMMENT '用户名',
                             `ip`          VARCHAR(64)  NOT NULL DEFAULT ''     COMMENT '登录IP',
                             `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1-成功，0-失败',
                             `message`     VARCHAR(256) NOT NULL DEFAULT ''     COMMENT '登录信息',
                             `is_deleted`  TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除，1-已删除',
                             `login_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
                             PRIMARY KEY (`id`),
                             INDEX `idx_user_id` (`user_id`),
                             INDEX `idx_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';

-- ============================================
-- 操作日志数据库表结构
-- 表名：operation_log（操作日志表）
-- ============================================
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
                                 `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 `user_id`     BIGINT       NOT NULL DEFAULT 0      COMMENT '用户ID',
                                 `username`    VARCHAR(64)  NOT NULL                COMMENT '用户名',
                                 `module`      VARCHAR(64)  NOT NULL                COMMENT '操作模块',
                                 `action`      VARCHAR(64)  NOT NULL                COMMENT '操作类型',
                                 `description` VARCHAR(512) NOT NULL DEFAULT ''     COMMENT '操作描述',
                                 `ip`          VARCHAR(64)  NOT NULL DEFAULT ''     COMMENT '操作IP',
                                 `is_deleted`  TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除，1-已删除',
                                 `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
                                 PRIMARY KEY (`id`),
                                 INDEX `idx_user_id` (`user_id`),
                                 INDEX `idx_module` (`module`),
                                 INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ============================================
-- 对话历史数据库表结构（会话级存储）
-- 表名：chat_history（对话历史表）
-- 说明：一次完整会话（多轮对话）作为一条数据库记录，
--       messages 字段以 JSON 数组存储该会话中所有消息
-- ============================================
DROP TABLE IF EXISTS `chat_history`;
CREATE TABLE `chat_history` (
                               `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                               `user_id`       BIGINT       NOT NULL                COMMENT '用户ID',
                               `session_id`    VARCHAR(128) NOT NULL                COMMENT '会话唯一标识',
                               `title`         VARCHAR(256) NOT NULL DEFAULT ''     COMMENT '会话标题（首条用户消息，超过30字截断）',
                               `messages`      MEDIUMTEXT   NULL                    COMMENT '会话消息列表（JSON数组格式）',
                               `message_count` INT          NOT NULL DEFAULT 0      COMMENT '消息总数',
                               `is_deleted`    TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除，1-已删除',
                               `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '会话创建时间',
                               `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '会话最后更新时间',
                               PRIMARY KEY (`id`),
                               UNIQUE KEY `uk_session_id` (`session_id`),
                               INDEX `idx_user_id` (`user_id`),
                               INDEX `idx_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对话历史表';