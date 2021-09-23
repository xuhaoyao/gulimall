package com.scnu.gulimall.order.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.scnu.common.constant.order.OrderStatusEnum;
import com.scnu.gulimall.order.entity.OrderReturnApplyEntity;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.scnu.gulimall.order.entity.OrderEntity;
import com.scnu.gulimall.order.service.OrderService;
import com.scnu.common.utils.PageUtils;
import com.scnu.common.utils.R;



/**
 * 订单
 *
 * @author xhy
 * @email 623834276@qq.com
 * @date 2021-09-02 14:17:35
 */
@RestController
@RequestMapping("order/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    /**
     * 远程调用,查询用户的订单列表[付款成功时用于渲染页面]
     * @param params
     * @return
     */
    @PostMapping("/memberOrderList")
    public R memberOrderList(@RequestBody Map<String,Object> params){
        PageUtils page = orderService.memberOrderList(params);
        return R.ok().put("page", page);
    }

    /**
     * 远程调用,rabbitmq解锁库存时查询订单的状态
     * -1表示订单不存在
     */
    @GetMapping("/status/{orderSn}")
    public R orderStatus(@PathVariable("orderSn") String orderSn){
        OrderEntity order = orderService.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        Integer status = (order == null ? OrderStatusEnum.MISSING.getCode() : order.getStatus());
        return R.ok().put("data",status);
    }

    @GetMapping("/sendMsg/{num}")
    public String sendMsg(@PathVariable("num") Integer num){

        for (Integer i = 0; i < num; i++) {
            if(i % 2 == 0){
                OrderEntity orderEntity = new OrderEntity();
                orderEntity.setBillContent("222");
                orderEntity.setOrderSn("dsdsd");
                orderEntity.setCommentTime(new Date());
                rabbitTemplate.convertAndSend("hello.java.exchange","java.key1",orderEntity,new CorrelationData("" + i));
            }
            else{
                OrderReturnApplyEntity returnApplyEntity = new OrderReturnApplyEntity();
                returnApplyEntity.setOrderSn(UUID.randomUUID().toString());
                rabbitTemplate.convertAndSend("hello.java.exchange","java.key",returnApplyEntity,new CorrelationData("" + i));
            }
        }

        return "ok";
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("order:order:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = orderService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("order:order:info")
    public R info(@PathVariable("id") Long id){
		OrderEntity order = orderService.getById(id);

        return R.ok().put("order", order);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("order:order:save")
    public R save(@RequestBody OrderEntity order){
		orderService.save(order);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("order:order:update")
    public R update(@RequestBody OrderEntity order){
		orderService.updateById(order);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("order:order:delete")
    public R delete(@RequestBody Long[] ids){
		orderService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
