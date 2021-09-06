package com.scnu.common.constant.product;

public enum AttrEnum {

    SALE(0,"销售属性"),
    BASE(1,"基本属性");

    private int code;
    private String msg;

    AttrEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
