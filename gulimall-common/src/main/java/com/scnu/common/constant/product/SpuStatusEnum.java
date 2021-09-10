package com.scnu.common.constant.product;

public enum SpuStatusEnum {

    CREATE(0,"新建"),
    UP(1,"上架"),
    DOWN(2,"下架");

    private Integer code;
    private String msg;

    SpuStatusEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
