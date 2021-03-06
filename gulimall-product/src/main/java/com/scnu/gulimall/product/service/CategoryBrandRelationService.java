package com.scnu.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scnu.common.utils.PageUtils;
import com.scnu.gulimall.product.entity.BrandEntity;
import com.scnu.gulimall.product.entity.CategoryBrandRelationEntity;
import com.scnu.gulimall.product.vo.BrandVo;

import java.util.List;
import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author xhy
 * @email 623834276@qq.com
 * @date 2021-09-02 13:07:58
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 关联表中还有品牌名和分类名,一起添加
     */
    void saveDetail(CategoryBrandRelationEntity categoryBrandRelation);

    /**
     * 发布商品页面:根据此分类id查出关联的品牌信息
     * @param catId
     * @return
     */
    List<BrandEntity> brandsListByCatId(Long catId);
}

