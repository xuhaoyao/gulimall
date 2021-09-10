package com.scnu.gulimall.ware.service.impl;

import com.scnu.common.to.SkuHasStockTo;
import com.scnu.common.utils.R;
import com.scnu.gulimall.ware.feign.ProductFeignService;
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


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private ProductFeignService productFeignService;

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

}