package com.scnu.gulimall.product.service.impl;

import com.scnu.common.constant.product.SpuStatusEnum;
import com.scnu.common.to.SkuHasStockTo;
import com.scnu.common.to.SkuReductionTo;
import com.scnu.common.to.SpuBoundTo;
import com.scnu.common.to.es.SkuEsModel;
import com.scnu.common.utils.Query;
import com.scnu.common.utils.R;
import com.scnu.gulimall.product.dao.AttrDao;
import com.scnu.gulimall.product.dao.SpuInfoDescDao;
import com.scnu.gulimall.product.entity.*;
import com.scnu.gulimall.product.feign.CouponFeignService;
import com.scnu.gulimall.product.feign.SearchFeignService;
import com.scnu.gulimall.product.feign.WareFeignService;
import com.scnu.gulimall.product.service.*;
import com.scnu.gulimall.product.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scnu.common.utils.PageUtils;

import com.scnu.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("spuInfoService")
@Slf4j
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescDao spuInfoDescDao;

    @Autowired
    private SpuImagesService spuImagesService;

    @Autowired
    private AttrDao attrDao;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    //set session transaction isolation level read uncommitted;
    //TODO ?????????????????????
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {
        //1.??????spu???????????? pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo,spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        baseMapper.insert(spuInfoEntity);
        Long spuId = spuInfoEntity.getId();
        //2.??????spu??????????????? pms_spu_info_desc
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuId);
        spuInfoDescEntity.setDecript(String.join(",",vo.getDecript()));
        spuInfoDescDao.insert(spuInfoDescEntity);
        //3.??????spu???????????? pms_spu_images
        List<String> images = vo.getImages();
        spuImagesService.saveImages(spuId,images);
        //4.??????spu??????????????? pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            productAttrValueEntity.setSpuId(spuId);
            productAttrValueEntity.setAttrValue(attr.getAttrValues());
            AttrEntity attrEntity = attrDao.selectById(attr.getAttrId());
            productAttrValueEntity.setAttrName(attrEntity.getAttrName()); //???????????????
            productAttrValueEntity.setAttrId(attr.getAttrId());
            productAttrValueEntity.setQuickShow(attr.getShowDesc());
            return productAttrValueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveBatch(collect);

        //4.5 ??????spu??????????????? gulimall_sms->sms_spu_bounds
        Bounds bounds = vo.getBounds();
        SpuBoundTo boundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds,boundTo);
        boundTo.setSpuId(spuId);
        couponFeignService.saveSpuBounds(boundTo);
        //5.????????????spu???????????????sku??????
        List<Skus> skus = vo.getSkus();
        if(skus != null && skus.size() > 0){
            skus.forEach(item -> {
                String defaultImg = "";
                for (Images image : item.getImages()) {
                    if(image.getDefaultImg() == 1){
                        defaultImg = image.getImgUrl();
                    }
                }
                /**
                 *     private String skuName;
                 *     private BigDecimal price;
                 *     private String skuTitle;
                 *     private String skuSubtitle;
                 */
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item,skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuId);
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                //5.1 sku????????????  pms_sku_info
                skuInfoService.save(skuInfoEntity);
                //?????????skuInfo,????????????id,?????????????????????(skuId????????????)
                Long skuId = skuInfoEntity.getSkuId();
                List<SkuImagesEntity> imagesEntities = item.getImages().stream()
                        .filter(img -> StringUtils.hasLength(img.getImgUrl()))  //?????????????????????????????????
                        .map(img -> {
                            SkuImagesEntity skuImagesEntity = new SkuImagesEntity();

                            skuImagesEntity.setSkuId(skuId);
                            skuImagesEntity.setImgUrl(img.getImgUrl());
                            skuImagesEntity.setDefaultImg(img.getDefaultImg());
                            return skuImagesEntity;
                        }).collect(Collectors.toList());
                //5.2 sku??????????????? pms_sku_images
                skuImagesService.saveBatch(imagesEntities);

                //5.3 sku??????????????? pms_sku_sale_attr_value
                List<Attr> attr = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(a -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuId);
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                //5.4 sku?????????,??????????????? gulimall_sms -> sms_sku_ladder(?????????) -> sms_sku_full_reduction(?????????)
                //   sms_member_price(???????????????)
                if(item.getFullCount() > 0 || item.getFullPrice().compareTo(new BigDecimal("0")) > 0) {
                    SkuReductionTo skuReductionTo = new SkuReductionTo();
                    BeanUtils.copyProperties(item, skuReductionTo);
                    skuReductionTo.setSkuId(skuId);
                    couponFeignService.sveSkuReduction(skuReductionTo);
                }
            });
        }



    }

    @Override
    public PageUtils querySpuPage(Map<String, Object> params) {

        /**
         * status: 2
         * key:
         * brandId: 9
         * catelogId: 225
         */
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();

        // status=1 and (id=1 or spu_name like xxx)
        String status = (String) params.get("status");
        if(!StringUtils.isEmpty(status)){
            wrapper.eq("publish_status",status);
        }

        String brandId = (String) params.get("brandId");
        if(!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)){
            wrapper.eq("brand_id",brandId);
        }

        String catelogId = (String) params.get("catelogId");
        if(!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)){
            wrapper.eq("catalog_id",catelogId);
        }

        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and((w)->{
                w.eq("id",key).or().like("spu_name",key);
            });
        }
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    @Override
    public void up(Long spuId) {
        //1.????????????spuId???????????????sku??????,???????????????,???????????????
        List<SkuInfoEntity> skuInfoEntities = skuInfoService.getSkusBySpuId(spuId);

        //?????????spuId??????????????????????????????????????????,???????????????????????????,????????????.
        List<ProductAttrValueEntity> attrValueEntityList = productAttrValueService.list(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id",spuId));
        List<Long> attrIds = attrValueEntityList.stream().map(attrValue -> attrValue.getAttrId()).collect(Collectors.toList());
        List<Long> attrSearchAbleIds = attrDao.searchAbleIdList(attrIds);
        Set<Long> attrIdSet = new HashSet<>(attrSearchAbleIds);
        List<SkuEsModel.Attr> attrs = attrValueEntityList.stream()
                .filter(item -> attrIdSet.contains(item.getAttrId()))
                .map(item -> {
                    SkuEsModel.Attr attr = new SkuEsModel.Attr();
                    BeanUtils.copyProperties(item, attr);
                    return attr;
                }).collect(Collectors.toList());

        //????????????,??????????????????
        List<Long> skuIds = skuInfoEntities.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
        List<SkuHasStockTo> skuHasStockTos = wareFeignService.hasStock(skuIds);
        Map<Long, Boolean> hasStockMap = null;
        if(skuHasStockTos == null){
            hasStockMap = new HashMap<>(); //????????????????????????,??????????????????
        }
        else {
            hasStockMap = skuHasStockTos.stream().collect(Collectors.toMap(SkuHasStockTo::getSkuId, SkuHasStockTo::getHasStock));
        }
        Map<Long, Boolean> finalHasStockMap = hasStockMap;
        List<SkuEsModel> upProducts = skuInfoEntities.stream().map(sku -> {
            //??????????????????es?????????
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(sku,skuEsModel);
            /**skuPrice,hasStock,skuImg,hotScore,brandName,brandImg,catalogName
             *      attrs
             *         private Long attrId;
             *         private String attrName;
             *         private String attrValue;
             */
            skuEsModel.setSkuPrice(sku.getPrice());
            skuEsModel.setSkuImg(sku.getSkuDefaultImg());
            //TODO 1.????????????->?????????????????????
            Boolean flag = finalHasStockMap.get(sku.getSkuId());
            if(flag == null){
                flag = true;
            }
            skuEsModel.setHasStock(flag);
            //TODO 2.????????????,???????????????,?????????0
            BrandEntity brand = brandService.getById(skuEsModel.getBrandId());
            skuEsModel.setBrandName(brand.getName());
            skuEsModel.setBrandImg(brand.getLogo());
            skuEsModel.setHotScore(0L); //????????????
            CategoryEntity category = categoryService.getById(skuEsModel.getCatalogId());
            skuEsModel.setCatalogName(category.getName());
            skuEsModel.setAttrs(attrs);
            return skuEsModel;
        }).collect(Collectors.toList());

        //??????????????????gulimall-search
        R r = searchFeignService.productUp(upProducts);
        if(r.getCode() == 0){
            //??????????????????->??????pms_spu_info
            log.warn("spuId??????????????????:{}",spuId);
            baseMapper.updateStatus(spuId, SpuStatusEnum.UP.getCode());
        }
        else{
            //??????????????????
            //TODO ????????????????????????????
            /**
             * feign????????????
             * SynchronousMethodHandler.invoke(args)
             * 1.??????????????????,???????????????json
             *         RequestTemplate template = this.buildTemplateFromArgs.create(argv);
             * 2.????????????????????????(?????????????????????????????????)
             *         return this.executeAndDecode(template);
             * 3.??????????????????????????????(????????????)
             */
            log.error("??????????????????");
        }

    }

    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long id) {
        SkuInfoEntity byId = skuInfoService.getById(id);
        Long spuId = byId.getSpuId();
        return baseMapper.selectById(spuId);
    }

}