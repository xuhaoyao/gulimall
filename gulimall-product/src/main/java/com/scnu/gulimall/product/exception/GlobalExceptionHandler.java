package com.scnu.gulimall.product.exception;

import com.scnu.common.exception.ErrorCode;
import com.scnu.common.utils.R;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.scnu.gulimall.product.controller")
public class GlobalExceptionHandler {

    /**
     * JSR303校验,校验失败的处理逻辑
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R validHandler(MethodArgumentNotValidException e){
        BindingResult result = e.getBindingResult();
        Map<String,String> map = new HashMap<>();
        result.getFieldErrors().forEach(fieldError -> {
            String name = fieldError.getField();
            String message = fieldError.getDefaultMessage();
            map.put(name,message);
        });
        return R.error(ErrorCode.VALID_PARAMETER.getCode(),ErrorCode.VALID_PARAMETER.getMsg()).put("data",map);
    }

    /**
     * 处理未知的异常
     */
    @ExceptionHandler(value = Throwable.class)
    public R unknownHandler(Throwable e){
        e.printStackTrace();
        return R.error(ErrorCode.UNKNOWN_ERROR.getCode(),ErrorCode.UNKNOWN_ERROR.getMsg()).put("data",e);
    }

}
