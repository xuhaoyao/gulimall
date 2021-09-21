package com.scnu.common.constant.ware;

public enum StockLockEnum {
    LOCKED(1,"已锁定"),
    UN_LOCKED(2,"已解锁"),
    DONE(3,"扣减");


    StockLockEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    private int code;
    private String msg;

}
