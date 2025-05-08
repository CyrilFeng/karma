package com.github.karma.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理 UGOSRuntimeException
     * @param e
     * @return
     */
    @ExceptionHandler(value = KarmaRuntimeException.class)
    public Result<?> handleException(KarmaRuntimeException e) {
        return Result.error(e);
    }

    /**
     * 处理未知异常，并打印error日志
     * @param e
     * @return
     */
    public Result<?> handleException(Exception e){
        log.error("系统异常",e);
        return Result.error(ExceptionEnum.SYSTEM_ERROR);
    }

}
