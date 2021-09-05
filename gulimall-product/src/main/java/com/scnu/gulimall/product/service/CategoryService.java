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
}

