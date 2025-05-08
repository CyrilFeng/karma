package com.github.karma.common;


public enum ExceptionEnum {

    SYSTEM_ERROR(-1, "系统异常"),
    NO_PERMISSION(-2, "权限异常"),
    PARAMS_MISSING(-3, "参数缺失"),
    AUTH_ERROR(-4, "认证失败"),
    TARGET_NOT_EXIST(-10, "目标不存在"),
    INCORRECT_PASSWORD(-11, "密码错误"),
    USER_NOT_EXIST(-11, "用户不存在"),
    USER_EXIST(-12, "用户已存在"),
    PROCESS_FAILED(-13, "处理失败"),
    DATASOURCE_NOT_EXIST(-15, "数据源不存在"),

    ;

    private final int code;
    private final String message;

    ExceptionEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }

}
