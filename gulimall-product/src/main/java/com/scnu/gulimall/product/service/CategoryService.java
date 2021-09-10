package com.scnu.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scnu.common.utils.PageUtils;
import com.scnu.gulimall.product.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author xhy
 * @email 623834276@qq.com
 * @date 2021-09-02 13:07:58
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 查出所有商品分类,并组装成树形结构
     */
    List<CategoryEntity> listTree();

    /**
     * 批量删除前端展示的商品分类
     */
    void removeBatch(List<Long> ids);

    /**
     * 根据分类id递归找出这个分类的路径[父,子,孙]
     */
    Long[] getCatelogIdPath(Long catelogId);

    /**
     * 此处更新分类的时候,由于pms_category_brand_relation表中也关联了分类,因此也要更新这张表
     * @param category
     */
    void updateDetail(CategoryEntity category);

    /**
     * 商场首页展示一级分类
     * @return
     */
    List<CategoryEntity> getLevelOneList();

    /**
     * 商场首页展示的二级分类和三级分类,鼠标摸到一级分类展示相应内容
     */
    Map<String, Object> getcatalogJson();
}

