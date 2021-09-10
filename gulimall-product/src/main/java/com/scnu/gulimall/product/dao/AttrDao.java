package com.scnu.gulimall.product.dao;

import com.scnu.gulimall.product.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scnu.gulimall.product.vo.Attr;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品属性
 * 
 * @author xhy
 * @email 623834276@qq.com
 * @date 2021-09-02 13:07:59
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {

    /**
     * 查出在attrIds中能被检索的记录
     */
    List<Long> searchAbleIdList(@Param("attrIds") List<Long> attrIds);
}
