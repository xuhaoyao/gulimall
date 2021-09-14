package com.scnu.common.exception;

/**
 * 错误码和错误信息定义类
 * 1. 错误码定义规则为 5 为数字
 * 2. 前两位表示业务场景，最后三位表示错误码。例如：10000。10:通用 000:系统未知异常
 * 3. 维护错误码后需要维护错误描述，将他们定义为枚举形式
 * 错误码列表：
 * 10: 通用
 *    001：参数格式校验
 *    002: 邮箱验证码发送频繁,请1分钟后再试
 * 11: 商品
 * 12: 订单
 * 13: 购物车
 * 14: 物流
 *
 *
 */
public enum ErrorCode {

    VALID_PARAMETER(10001,"参数格式校验失败"),
    UNKNOWN_ERROR(10000,"系统未知异常"),
    PRODUCT_UP_ERROR(11000,"商品上架错误"),
    EMAIL_ERROR(10002,"邮箱验证码发送频繁,请1分钟后再试");

    private Integer code;
    private String msg;

    ErrorCode(Integer code, String msg) {
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
