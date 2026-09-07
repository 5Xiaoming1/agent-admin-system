package com.tianji.agent.config.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一响应结果类
 * <p>
 * 封装所有 API 接口的返回数据，提供标准的响应格式：
 * <ul>
 *   <li>{@code code} — 状态码，200 表示成功，其他值表示异常</li>
 *   <li>{@code message} — 提示信息</li>
 *   <li>{@code data} — 响应数据，泛型支持任意类型</li>
 * </ul>
 * </p>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * // 成功返回数据
 * return Result.success(loginVO);
 *
 * // 成功返回自定义消息
 * return Result.success("登录成功", loginVO);
 *
 * // 失败返回错误信息
 * return Result.error("用户名或密码错误");
 *
 * // 失败返回自定义状态码
 * return Result.error(401, "未授权");
 * }</pre>
 *
 * @param <T> 响应数据的类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    /** 状态码，200 表示成功 */
    private Integer code;

    /** 提示信息 */
    private String message;

    /** 响应数据 */
    private T data;

    /**
     * 成功响应（无数据）
     *
     * @return 状态码 200，消息"操作成功"，无数据的 Result
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    /**
     * 成功响应（带数据）
     *
     * @param data 响应数据
     * @return 状态码 200，消息"操作成功"的 Result
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /**
     * 成功响应（自定义消息）
     *
     * @param message 自定义提示信息
     * @param data    响应数据
     * @return 状态码 200 的 Result
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    /**
     * 失败响应（默认状态码 500）
     *
     * @param message 错误提示信息
     * @return 状态码 500 的 Result
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }

    /**
     * 失败响应（自定义状态码）
     *
     * @param code    业务状态码
     * @param message 错误提示信息
     * @return 自定义状态码的 Result
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

}