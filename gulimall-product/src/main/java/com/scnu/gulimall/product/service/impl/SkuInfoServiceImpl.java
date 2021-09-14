package com.scnu.gulimall.product.service.impl;

import com.scnu.common.utils.Query;
import com.scnu.gulimall.product.entity.SkuImagesEntity;
import com.scnu.gulimall.product.entity.SpuInfoDescEntity;
import com.scnu.gulimall.product.entity.SpuInfoEntity;
import com.scnu.gulimall.product.service.*;
import com.scnu.gulimall.product.vo.SkuItemSaleAttrVo;
import com.scnu.gulimall.product.vo.SkuItemVo;
import com.scnu.gulimall.product.vo.SpuItemAttrGroupVo;
import com.scnu.gulimall.product.vo.SpuSaveVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scnu.common.utils.PageUtils;

import com.scnu.gulimall.product.dao.SkuInfoDao;
import com.scnu.gulimall.product.entity.SkuInfoEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private ExecutorService pool;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils querySkuInfo(Map<String, Object> params) {
        /**
         * {
         * page: 1,//当前页码
         * limit: 10,//每页记录数
         * sidx: 'id',//排序字段
         * order: 'asc/desc',//排序方式
         * key: '华为',//检索关键字
         * catelogId: 0,
         * brandId: 0,
         * min: 0,
         * max: 0
         * }
         */
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();

        String brandId = (String) params.get("brandId");
        if(!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)){
            wrapper.eq("brand_id",brandId);
        }

        String catelogId = (String) params.get("catelogId");
        if(!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)){
            wrapper.eq("catalog_id",catelogId);
        }

        String minPrice = (String) params.get("min");
        String maxPrice = (String) params.get("max");

        if(StringUtils.hasLength(minPrice) || StringUtils.hasLength(maxPrice)){
            if(!"0".equals(minPrice) || !"0".equals(maxPrice)) {
                wrapper.ge("price", minPrice);
                wrapper.le("price", maxPrice);
            }
        }

        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and((w)->{
                w.eq("sku_id",key).or().like("sku_name",key);
            });
        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("spu_id",spuId);
        List<SkuInfoEntity> list = this.list(wrapper);
        return list;
    }

    @Override
    public SkuItemVo item(Long skuId) {
        SkuItemVo vo = new SkuItemVo();

        CompletableFuture<SkuInfoEntity> skuInfoFuture = CompletableFuture.supplyAsync(() -> {
            //1.获取sku基本信息 pms_sku_info
            SkuInfoEntity skuInfoEntity = baseMapper.selectById(skuId);
            vo.setInfo(skuInfoEntity);
            return skuInfoEntity;
        }, pool);

        CompletableFuture<Void> skuImgFuture = CompletableFuture.runAsync(() -> {
            //2.获取sku图片信息 pms_sku_images
            List<SkuImagesEntity> images = skuImagesService.getSkuImagesBySkuId(skuId);
            vo.setImages(images);
        }, pool);

        CompletableFuture<Void> saleAttrsFuture = skuInfoFuture.thenAcceptAsync(info -> {
            //3.获取spu的销售属性组合
            List<SkuItemSaleAttrVo> saleAttrs = skuSaleAttrValueService.getSaleAttrsBySpuId(info.getSpuId());
            vo.setSaleAttrs(saleAttrs);
        }, pool);

        CompletableFuture<Void> spuInfoFuture = skuInfoFuture.thenAcceptAsync(info -> {
            //4.获取spu的介绍
            SpuInfoDescEntity desc = spuInfoDescService.getById(info.getSpuId());
            vo.setDesc(desc);
        }, pool);

        CompletableFuture<Void> groupAttrsFuture = skuInfoFuture.thenAcceptAsync(info -> {
            //5.获取spu的规格参数信息
            List<SpuItemAttrGroupVo> groupAttrs = attrGroupService.spuInfo(info.getSpuId(), info.getCatalogId());
            vo.setGroupAttrs(groupAttrs);
        }, pool);

        try {
            CompletableFuture.allOf(skuImgFuture,saleAttrsFuture,spuInfoFuture,groupAttrsFuture).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return vo;
    }



}