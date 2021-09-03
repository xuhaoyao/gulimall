package com.scnu.gulimall.product.dao;

import com.scnu.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author xhy
 * @email 623834276@qq.com
 * @date 2021-09-02 13:07:58
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
