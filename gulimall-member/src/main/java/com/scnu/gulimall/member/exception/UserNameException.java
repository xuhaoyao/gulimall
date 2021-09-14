package com.scnu.gulimall.member.exception;

public class UserNameException extends RuntimeException{
    public UserNameException() {
        super();
    }

    public UserNameException(String message) {
        super(message);
    }
}
