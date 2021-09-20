package com.scnu.gulimall.order.exception;

public class NoStockException extends RuntimeException{
    public NoStockException() {
        super();
    }

    public NoStockException(String message) {
        super(message);
    }
}
