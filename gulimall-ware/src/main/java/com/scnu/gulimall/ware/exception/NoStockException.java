package com.scnu.gulimall.ware.exception;

public class NoStockException extends RuntimeException{

    private Long skuId;

    public NoStockException(Long skuId) {
        super("商品id:" + skuId + ",库存不足");
        this.skuId = skuId;
    }
}
