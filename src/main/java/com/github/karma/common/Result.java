package com.github.karma.common;

import lombok.Data;

@Data
public class Result<T> {

    private int code;
    private String msg;
    private T data;


    public Result() {
    }

    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public Result<T> code(int code) {
        this.code = code;
        return this;
    }

    public Result<T> msg(String msg) {
        this.msg = msg;
        return this;
    }

    public Result<T> data(T data) {
        this.data = data;
        return this;
    }

    /**
     * 返回成功
     * @return
     */
    public static <T> Result<T> success() {
        return new Result<>(0, "success", null);
    }

    /**
     * 返回成功
     * @param data
     * @return
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(0, "success", data);
    }

    /**
     * 返回成功
     * @param data
     * @return
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(0, message, data);
    }

    /**
     * 返回失败
     * @param exception
     * @return
     */
    public static <T> Result<T> error(KarmaRuntimeException exception) {
        return new Result<>(exception.getCode(), exception.getMessage(), null);
    }

    /**
     * 返回失败
     * @param exceptionEnum
     * @return
     */
    public static <T> Result<T> error(ExceptionEnum exceptionEnum) {
        return new Result<>(exceptionEnum.getCode(), exceptionEnum.getMessage(), null);
    }

    /**
     * 返回失败
     * @param code
     * @param msg
     * @return
     */
    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg, null);
    }

    /**
     * 返回失败
     * @param code
     * @param msg
     * @param data
     * @param <T>
     * @return
     */
    public static <T> Result<T> error(int code, String msg, T data) {
        return new Result<>(code, msg, data);
    }
}
