package com.scnu.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.rabbitmq.client.Channel;
import com.scnu.common.constant.order.OrderStatusEnum;
import com.scnu.common.constant.ware.StockLockEnum;
import com.scnu.common.to.SkuHasStockTo;
import com.scnu.common.to.mq.StockDetailTo;
import com.scnu.common.to.mq.StockLockedTo;
import com.scnu.common.utils.R;
import com.scnu.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.scnu.gulimall.ware.entity.WareOrderTaskEntity;
import com.scnu.gulimall.ware.exception.NoStockException;
import com.scnu.gulimall.ware.feign.OrderFeignService;
import com.scnu.gulimall.ware.feign.ProductFeignService;
import com.scnu.gulimall.ware.service.WareOrderTaskDetailService;
import com.scnu.gulimall.ware.service.WareOrderTaskService;
import com.scnu.gulimall.ware.vo.LockStockResult;
import com.scnu.gulimall.ware.vo.OrderItemVo;
import com.scnu.gulimall.ware.vo.WareSkuLockVo;
import lombok.Data;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scnu.common.utils.PageUtils;
import com.scnu.common.utils.Query;

import com.scnu.gulimall.ware.dao.WareSkuDao;
import com.scnu.gulimall.ware.entity.WareSkuEntity;
import com.scnu.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;


@RabbitListener(queues = {"stock.release.stock.queue"})
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private WareOrderTaskService wareOrderTaskService;

    @Autowired
    private WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    private OrderFeignService orderFeignService;



    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("sku_id",skuId).eq("ware_id",wareId);
        Integer count = baseMapper.selectCount(wrapper);
        if(count == 0){
            //仓库中没有这条记录,则新增库存记录
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);
            try {
                String skuName = productFeignService.getSkuName(skuId);
                wareSkuEntity.setSkuName(skuName);
            }catch (Exception e){ //失败后可以不回滚,此数据不太重要
                e.printStackTrace();
            }
            baseMapper.insert(wareSkuEntity);
        }
        else{
            baseMapper.updateStock(skuId,wareId,skuNum);
        }
    }

    @Override
    public List<SkuHasStockTo> hasStock(List<Long> skuIds) {
        List<SkuHasStockTo> collect = skuIds.stream().map(skuId -> {
            SkuHasStockTo to = new SkuHasStockTo();
            Integer stock = baseMapper.hasStock(skuId);
            stock = (stock == null? 0 : stock);
            to.setSkuId(skuId);
            to.setHasStock(stock > 0);
            return to;
        }).collect(Collectors.toList());
        return collect;
    }

    /**
     * 库存解锁场景:
     *  1.下订单成功,订单过期没有支付被系统自动取消,用户手动取消订单
     *
     *  2.下订单成功,库存锁定成功,接下来的业务调用失败,导致订单回滚,之前锁定的库存就要解锁
     */
    @Transactional
    @Override
    public void orderLockStock(WareSkuLockVo vo) throws NoStockException {

        /**
         * 保存库存工作单的详情
         */
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskService.save(wareOrderTaskEntity);

        //找到每个商品在哪个仓库都有库存[一般是找附近的仓库,按照距离降序排列]
        List<OrderItemVo> locks = vo.getLocks();
        locks.forEach(item -> {
            Long wareId = baseMapper.selectWareIdHasStockBySkuId(item.getSkuId(),item.getCount());
            if(wareId == null){
                throw new NoStockException(item.getSkuId());
            }
            baseMapper.updateStockLock(item.getSkuId(),wareId,item.getCount());
            //库存锁定成功后,给MQ发消息
            WareOrderTaskDetailEntity entity =
                    new WareOrderTaskDetailEntity(null,item.getSkuId(),"",item.getCount(),wareOrderTaskEntity.getId(),wareId,
                            StockLockEnum.LOCKED.getCode());
            wareOrderTaskDetailService.save(entity);
            StockLockedTo lockedTo = new StockLockedTo();
            lockedTo.setId(wareOrderTaskEntity.getId());
            StockDetailTo detailTo = new StockDetailTo();
            BeanUtils.copyProperties(entity,detailTo);
            lockedTo.setDetailTo(detailTo);
            rabbitTemplate.convertAndSend("stock-event-exchange","stock.locked",lockedTo);
        });
    }

    /**
     * 库存自动解锁
     *      1.下订单成功,库存锁定成功,接下来的业务调用失败,导致订单回滚,之前锁定的库存就要解锁
     *      2.锁库存失败,导致锁库存的事务回滚
     *  查询数据库关于这个订单的锁定库存信息
     *      a.有:证明库存锁定成功了
     *          解锁还要看订单的情况:
     *          1.没有这个订单 --> 直接解锁(因为订单都回滚了)
     *          2.有这个订单,还要看订单的状态
     *              已取消 --> 直接解锁
     *              未取消 --> 不能解锁
     *      b.没有:库存锁定失败,库存事务已经回滚,此时无需做任何事
     */
    @Override
    public void releaseStock(StockLockedTo to) {
        Long id = to.getId();
        StockDetailTo detailTo = to.getDetailTo();
        WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(id);
        if(taskEntity == null){
            //没有这个库存工作单的信息,说明库存事务回滚了
            return;
        }
        //取得最新的工作单详情,因为需要获得最新的库存锁定状态,看库存状态来判断是否还需要解锁
        WareOrderTaskDetailEntity lastDetail = wareOrderTaskDetailService.getById(detailTo.getId());

        String orderSn = taskEntity.getOrderSn();
        //注意:因为订单模块必须用户登录才能访问,由于这是延迟队列,用户在此处是未登录状态
        //需要在订单模块的拦截器处放行这个请求
        R r = orderFeignService.orderStatus(orderSn);
        if(r.getCode() != 0){
            throw new RuntimeException("远程查询订单状态有异常");
        }
        Integer status = r.getData("data", new TypeReference<Integer>() {});
        if(status == OrderStatusEnum.MISSING.getCode() || status == OrderStatusEnum.CANCLED.getCode()){
            //订单不存在或者已取消,都需要解锁库存,以及锁定状态是已锁定才要解锁
            //此处判断了库存的锁定状态,再来考虑是否解锁库存,为一个幂等操作,有效的解决了消息重复的问题。
            //消息消费的时候,都尽量将代码写成幂等的,如果写不成幂等,就需要做额外判断,来防止消息重复消息
            //      防重表(redis/mysql) 消息message的redelivered字段(是否被重新投递过来)
            if(lastDetail.getLockStatus() == StockLockEnum.LOCKED.getCode()) {
                upLockedStock(lastDetail.getSkuId(),lastDetail.getWareId(),lastDetail.getSkuNum(),lastDetail.getId());
            }
        }
        else{
            //其他情况不需要解锁库存
        }
    }

    @Transactional
    @Override
    public void upLockedStock(Long skuId, Long wareId, Integer skuNum, Long detailId) {
        baseMapper.releaseStock(skuId, wareId, skuNum);
        //解锁完成后更新工作详情单的状态
        WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity();
        entity.setId(detailId);
        entity.setLockStatus(StockLockEnum.UN_LOCKED.getCode());
        wareOrderTaskDetailService.updateById(entity);
    }

    @Override
    public void releaseStock(String orderSn) {
        WareOrderTaskEntity taskEntity = wareOrderTaskService.getOne(new QueryWrapper<WareOrderTaskEntity>().eq("order_sn", orderSn));
        Long taskId = taskEntity.getId();
        List<WareOrderTaskDetailEntity> detailEntities =
                wareOrderTaskDetailService.list(
                        new QueryWrapper<WareOrderTaskDetailEntity>().eq("task_id", taskId).eq("lock_status", StockLockEnum.LOCKED.getCode()));
        if(ObjectUtils.isNotEmpty(detailEntities)){
            detailEntities.forEach(item -> {
                upLockedStock(item.getSkuId(),item.getWareId(),item.getSkuNum(),item.getId());
            });
        }
    }

}