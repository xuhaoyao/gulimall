package com.scnu.gulimall.seckill.constant;

public interface RedisConstant {

    String CACHE_SESSION_PREFIX = "seckill:session:";

    String CACHE_SKU_PREFIX = "seckill:skus:";

    String CACHE_STOCK_PREFIX = "seckill:stock:";

    String CACHE_USER_SECKILL_PREFIX = "user:sessionId:skuId:";

    String UPLOAD_LOCK = "seckill:upload:lock";

    String SECKILL_LOCK = "seckill:kill:lock";

}
