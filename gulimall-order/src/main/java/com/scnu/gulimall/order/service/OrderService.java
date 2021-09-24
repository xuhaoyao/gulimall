package com.scnu.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scnu.common.to.mq.SeckillOrderTo;
import com.scnu.common.utils.PageUtils;
import com.scnu.gulimall.order.entity.OrderEntity;
import com.scnu.gulimall.order.vo.*;

import java.util.Map;

/**
 * 订单
 *
 * @author xhy
 * @email 623834276@qq.com
 * @date 2021-09-02 14:17:35
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 获取订单确认页的数据
     * @return
     */
    OrderConfirmVo confirmOrder();

    /**
     * 提交表单
     * @param orderFormVo
     * @return
     */
    SubmitOrderRespVo submitOrder(OrderFormVo orderFormVo);

    /**
     * 若这个订单是未付款的状态,那么取消这个订单
     * @param orderEntity
     */
    void orderTryCancel(OrderEntity orderEntity);

    /**
     * 根据订单号生成发送给支付宝的数据
     * @param orderSn
     * @return
     */
    PayVo payOrder(String orderSn);

    PageUtils memberOrderList(Map<String, Object> params);

    /**
     * 用户付款后,支付宝异步回调
     * @param vo
     */
    void payOrder(PayAsyncVo vo);

    /**
     * 处理秒杀订单
     * @param orderTo
     */
    void handleSeckillOrder(SeckillOrderTo orderTo);
}

