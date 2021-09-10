package com.scnu.gulimall.ware.handler;

import com.scnu.common.exception.ErrorCode;
import com.scnu.common.utils.R;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = RuntimeException.class)
    public R exceptionHandler(RuntimeException e){
        e.printStackTrace();
        return R.error(ErrorCode.UNKNOWN_ERROR.getCode(),e.getMessage());
    }

}
