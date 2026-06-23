package com.itheima.aiagent01.exception;

import com.itheima.aiagent01.common.Result;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        return Result.error(400, message);
    }

    @ExceptionHandler(Exception.class)
    public Result<Object> handleException(Exception e) {
        e.printStackTrace();
        return Result.error(500, "服务器异常");
    }
}