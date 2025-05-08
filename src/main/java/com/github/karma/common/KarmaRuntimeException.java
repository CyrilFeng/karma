package com.github.karma.common;

public class KarmaRuntimeException extends RuntimeException {

    private final int code;

    public KarmaRuntimeException(int code, String message) {
        super(message);
        this.code = code;
    }

    public KarmaRuntimeException(ExceptionEnum exceptionEnum) {
        super(exceptionEnum.getMessage());
        this.code = exceptionEnum.getCode();
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return super.getMessage();
    }

}
