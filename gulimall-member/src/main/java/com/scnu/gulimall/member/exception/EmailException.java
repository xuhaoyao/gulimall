package com.scnu.gulimall.member.exception;

public class EmailException extends RuntimeException{
    public EmailException() {
        super();
    }

    public EmailException(String message) {
        super(message);
    }
}
