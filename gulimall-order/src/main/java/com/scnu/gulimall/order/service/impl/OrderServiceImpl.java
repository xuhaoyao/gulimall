package com.scnu.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.scnu.common.to.SkuHasStockTo;
import com.scnu.common.utils.R;
import com.scnu.common.vo.UserInfoVo;
import com.scnu.gulimall.order.constant.RedisConstant;
import com.scnu.gulimall.order.dao.OrderItemDao;
import com.scnu.gulimall.order.entity.OrderItemEntity;
import com.scnu.gulimall.order.enume.OrderStatusEnum;
import com.scnu.gulimall.order.exception.NoStockException;
import com.scnu.gulimall.order.feign.CartFeignService;
import com.scnu.gulimall.order.feign.MemberFeignService;
import com.scnu.gulimall.order.feign.ProductFeignService;
import com.scnu.gulimall.order.feign.WareFeignService;
import com.scnu.gulimall.order.interceptor.UserInterceptor;
import com.scnu.gulimall.order.service.OrderItemService;
import com.scnu.gulimall.order.to.MemberInfoTo;
import com.scnu.gulimall.order.to.OrderCreateTo;
import com.scnu.gulimall.order.vo.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scnu.common.utils.PageUtils;
import com.scnu.common.utils.Query;

import com.scnu.gulimall.order.dao.OrderDao;
import com.scnu.gulimall.order.entity.OrderEntity;
import com.scnu.gulimall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private CartFeignService cartFeignService;

    @Autowired
    private ExecutorService pool;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() {
        UserInfoVo userInfoVo = UserInterceptor.userInfoThreadLocal.get();
        OrderConfirmVo vo = new OrderConfirmVo();

        /**
         * 异步任务下,由于一个线程只对应一个request请求,因此开了异步任务,request请求就不是原来请求了
         * RequestContextHolder -> ThreadLocal<RequestAttributes>
         * 为了让异步任务带上原请求,使用如下方法
         */
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> infoFuture = CompletableFuture.runAsync(() -> {
            /**
             * 开了异步任务的话,异步任务中的HttpRequestServlet是null,因为底层是ThreadLocal
             */
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //1.查用户相关信息 收货地址
            R userInfo = memberFeignService.orderConfrimInfo(userInfoVo.getId());
            MemberInfoTo memberInfo = userInfo.getData("memberInfo", new TypeReference<MemberInfoTo>() {
            });
            List<MemberAddressVo> address = userInfo.getData("address", new TypeReference<List<MemberAddressVo>>() {
            });
            vo.setAddress(address);
            vo.setIntegration(memberInfo.getIntegration());
        },pool);

        CompletableFuture<Void> cartFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //2.查选中的购物项信息
            List<OrderItemVo> items = cartFeignService.userCartItemsInfo();
            vo.setItems(items);
        },pool).thenRunAsync(()->{
            List<OrderItemVo> items = vo.getItems();
            List<Long> skuIds = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            List<SkuHasStockTo> skuHasStockTos = wareFeignService.hasStock(skuIds);
            Map<Long, Boolean> hasStock = skuHasStockTos.stream().collect(Collectors.toMap(SkuHasStockTo::getSkuId, SkuHasStockTo::getHasStock));
            vo.setHasStock(hasStock);
        },pool);

        //3.其他数据自动计算

        //4.TODO 防重令牌  30分钟过期
        String uuid = UUID.randomUUID().toString().replace("-","");
        vo.setOrderToken(uuid);
        redisTemplate.opsForValue().set(RedisConstant.ORDER_TOKEN_PREFIX + userInfoVo.getId(),uuid,30, TimeUnit.MINUTES);
        try {
            CompletableFuture.allOf(infoFuture,cartFuture).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return vo;
    }

    /**
     * 本地事务,只能控制自己事务的回滚,控制不了其他服务的回滚
     * 分布式事务: 网络问题+分布式机器
     */
    //@GlobalTransactional  高并发下,不采用seata
    @Transactional
    @Override
    public SubmitOrderRespVo submitOrder(OrderFormVo orderFormVo) {
        SubmitOrderRespVo vo = new SubmitOrderRespVo();
        vo.setCode(200);
        UserInfoVo userInfoVo = UserInterceptor.userInfoThreadLocal.get();
        //1.验证令牌[令牌的对比和删除保证原子性]
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        String orderToken = orderFormVo.getOrderToken();
        Long execute = redisTemplate.execute(new DefaultRedisScript<Long>(script,Long.class), Arrays.asList(RedisConstant.ORDER_TOKEN_PREFIX + userInfoVo.getId()), orderToken);
        if(execute == 1){
            //创建订单 订单项信息
            OrderCreateTo orderTo = createOrder(orderFormVo);
            //验价
            BigDecimal payAmount = orderTo.getOrder().getPayAmount();
            BigDecimal payPrice = orderFormVo.getPayPrice();
            if(Math.abs(payAmount.subtract(payAmount).doubleValue()) < 0.01){
                //保存订单数据
                saveOrder(orderTo);
                //锁定库存 只要有异常,就回滚订单数据
                //订单号 所有订单项
                WareSkuLockVo lockVo = new WareSkuLockVo();
                lockVo.setOrderSn(orderTo.getOrder().getOrderSn());
                List<OrderItemVo> locks = orderTo.getItems().stream().map(item -> {
                    OrderItemVo itemVo = new OrderItemVo();
                    itemVo.setSkuId(item.getSkuId());
                    itemVo.setCount(item.getSkuQuantity());
                    itemVo.setTitle(item.getSkuName());
                    return itemVo;
                }).collect(Collectors.toList());
                lockVo.setLocks(locks);
                //TODO 远程锁库存
                /**
                 * 分布式事务:
                 *      库存的事务提交了,返回数据的时候,网络原因超时
                 *      这个时候订单服务以为库存服务出异常,回滚,实际上库存服务没有异常
                 */
                R r = wareFeignService.orderLockStock(lockVo);
                if(r.getCode() == 0){
                    //锁定成功
                    vo.setOrder(orderTo.getOrder());
                    //TODO 远程扣减积分  假设出现了异常
                   // int a = 10 / 0;
                    /**
                     * 分布式事务:
                     *      int a = 10 / 0;
                     *      这个时候,订单回滚,库存不回滚,因此库存是远程调用,那边的事务已经提交了
                     */
                    //订单创建成功,发消息给订单的延迟队列,目的是:一定时间后用户没有操作这个订单,系统自动取消它。
                    rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",orderTo.getOrder());
                    return vo;
                }
                else{
                    //锁库存失败
                    throw new NoStockException("远程锁库存失败");
                }
            }
            else{
                //验价失败
                vo.setCode(501);
                return vo;
            }
        }else{
            //令牌验证失败
            vo.setCode(500);
            return vo;
        }
    }

    @Transactional
    @Override
    public void orderTryCancel(OrderEntity orderEntity) {
        //根据订单号得到最新的订单信息
        String orderSn = orderEntity.getOrderSn();
        OrderEntity lastOrder = baseMapper.selectOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        //如果是待付款状态,就取消订单
        if(lastOrder.getStatus() == OrderStatusEnum.CREATE_NEW.getCode()){
            OrderEntity update = new OrderEntity();
            update.setId(lastOrder.getId());
            update.setStatus(OrderStatusEnum.CANCLED.getCode());
            baseMapper.updateById(update);
            //注意:此处更新了订单状态之后,需要发一个消息给库存服务,告诉它订单已经取消了,需要把库存解锁
            //考虑这个情况,若订单超时了需要取消,但是由于网络等原因,订单超时的消息没有传到这个方法,那么订单就一直是
            //  新建状态,库存服务就不能解锁库存,导致了这个订单的库存就被锁死了
            rabbitTemplate.convertAndSend("order-event-exchange","order.release.other",orderSn);
        }
    }

    /**
     * 保存订单的所有数据到数据库
     * @param orderTo
     */
    private void saveOrder(OrderCreateTo orderTo) {

        OrderEntity order = orderTo.getOrder();
        order.setModifyTime(new Date());
        order.setCreateTime(new Date());
        baseMapper.insert(order);
        List<OrderItemEntity> items = orderTo.getItems();
        orderItemService.saveBatch(items);
    }

    /**
     * 创建一个订单 返回相关数据
     * @return
     */
    private OrderCreateTo createOrder(OrderFormVo orderFormVo){
        OrderCreateTo to = new OrderCreateTo();
        //构建订单
        OrderEntity entity = buildOrder(orderFormVo);
        //构建订单项
        List<OrderItemEntity> itemEntities = buildOrderItems(entity.getOrderSn());
        //验价
        computePrice(entity,itemEntities);

        to.setOrder(entity);
        to.setItems(itemEntities);

        return to;
    }

    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> itemEntities) {
        BigDecimal total = new BigDecimal("0.0");
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal integration_all = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");
        Integer growth = 0;
        Integer integration = 0;
        //订单的总额 叠加每一个订单项的总额信息
        for (OrderItemEntity itemEntity : itemEntities) {
            total = total.add(itemEntity.getRealAmount());
            coupon = coupon.add(itemEntity.getCouponAmount());
            integration_all = integration_all.add(itemEntity.getIntegrationAmount());
            promotion = promotion.add(itemEntity.getPromotionAmount());
            growth += itemEntity.getGiftGrowth();
            integration += itemEntity.getGiftIntegration();
        }
        orderEntity.setTotalAmount(total);
        //应付总额 算上运费
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        //一些优惠信息的总额
        orderEntity.setCouponAmount(coupon);
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(integration_all);
        //积分 成长值
        orderEntity.setIntegration(integration);
        orderEntity.setGrowth(growth);

        //未删除
        orderEntity.setDeleteStatus(0);

    }

    private OrderEntity buildOrder(OrderFormVo orderFormVo) {
        UserInfoVo userInfoVo = UserInterceptor.userInfoThreadLocal.get();
        OrderEntity entity = new OrderEntity();
        //0.会员信息
        entity.setMemberId(userInfoVo.getId());
        entity.setMemberUsername(userInfoVo.getNickname());
        //1.生成订单号
        String orderSn = IdWorker.getTimeId();
        entity.setOrderSn(orderSn);


        //2.获取收获地址等信息
        R r = wareFeignService.addressFee(orderFormVo.getAddrId());
        AddressFeeVo addressFeeVo = r.getData("data",new TypeReference<AddressFeeVo>(){});
        //设置收货人信息和运费
        entity.setFreightAmount(addressFeeVo.getFee());
        entity.setReceiverCity(addressFeeVo.getDetail().getCity());
        entity.setReceiverDetailAddress(addressFeeVo.getDetail().getDetailAddress());
        entity.setReceiverPhone(addressFeeVo.getDetail().getPhone());
        entity.setReceiverProvince(addressFeeVo.getDetail().getProvince());
        entity.setReceiverRegion(addressFeeVo.getDetail().getRegion());

        //订单的状态
        entity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        entity.setAutoConfirmDay(7); //自动确认
        return entity;
    }

    /**
     * 构建订单项
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        List<OrderItemVo> orderItemVos = cartFeignService.userCartItemsInfo();
        if(ObjectUtils.isNotEmpty(orderItemVos)){
            List<OrderItemEntity> collect = orderItemVos.stream().map(item -> {
                OrderItemEntity orderItem = buildOrderItem(item);
                orderItem.setOrderSn(orderSn);
                return orderItem;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    private OrderItemEntity buildOrderItem(OrderItemVo item) {
        OrderItemEntity orderItem = new OrderItemEntity();
        //1.订单信息 订单号
        //2.商品的SPU信息
        R r = null;
        try {
            r = productFeignService.getSpuInfoBySkuId(item.getSkuId());
        } catch (Exception e) {
            log.error("远程调用商品服务:--->取spu失败");
            SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
            spuInfoEntity.setSpuName("伪造...");
            spuInfoEntity.setId(123456L);
            r = R.error().put("data",spuInfoEntity);
        }
        SpuInfoEntity spuInfo = r.getData("data", new TypeReference<SpuInfoEntity>() {});
        orderItem.setSpuId(spuInfo.getId());
        orderItem.setSpuBrand(spuInfo.getId().toString());  //此处远程调用的时候,应该创建一个to类来封装这些信息的,懒得写了
        orderItem.setSpuName(spuInfo.getSpuName());
        orderItem.setCategoryId(spuInfo.getCatalogId());
        //3.商品的SKU信息
        orderItem.setSkuId(item.getSkuId());
        orderItem.setSkuName(item.getTitle());
        orderItem.setSkuPic(item.getImage());
        orderItem.setSkuPrice(item.getPrice());
        orderItem.setSkuAttrsVals(String.join(";",item.getSkuAttrs()));
        orderItem.setSkuQuantity(item.getCount());
        //4.优惠信息
        //5.积分信息
        item.setTotalPrice(item.getPrice().multiply(new BigDecimal(String.valueOf(item.getCount()))));
        orderItem.setGiftGrowth(item.getTotalPrice().intValue());
        orderItem.setGiftIntegration(item.getTotalPrice().intValue());
        //6.订单项的价格信息  一些优惠信息
        orderItem.setPromotionAmount(new BigDecimal("0.0"));
        orderItem.setCouponAmount(new BigDecimal("0.0"));
        orderItem.setIntegrationAmount(new BigDecimal("0.0"));
        BigDecimal origin = item.getTotalPrice();
        BigDecimal subtract = origin.subtract(orderItem.getPromotionAmount())
                                    .subtract(orderItem.getCouponAmount())
                                    .subtract(orderItem.getIntegrationAmount());
        //实际金额
        orderItem.setRealAmount(subtract);
        return orderItem;
    }

}