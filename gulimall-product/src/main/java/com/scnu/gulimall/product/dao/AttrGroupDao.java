package com.scnu.gulimall.product.dao;

import com.scnu.gulimall.product.entity.AttrGroupEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scnu.gulimall.product.vo.SkuItemVo;
import com.scnu.gulimall.product.vo.SpuItemAttrGroupVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性分组
 * 
 * @author xhy
 * @email 623834276@qq.com
 * @date 2021-09-02 13:07:58
 */
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {

    /**
     * 此处有嵌套属性,需要自定义结果集resultMap
     * SELECT
     * 	ag.attr_group_id,
     * 	ag.attr_group_name,
     * 	aar.attr_id,
     * 	a.attr_name,
     * 	pav.attr_value
     * FROM
     * 	pms_attr_group ag
     * 	LEFT JOIN pms_attr_attrgroup_relation aar ON ag.attr_group_id = aar.attr_group_id
     * 	LEFT JOIN pms_attr a ON a.attr_id = aar.attr_id
     * 	LEFT JOIN pms_product_attr_value pav ON pav.attr_id = aar.attr_id
     * WHERE
     * 	ag.catelog_id = 225
     * 	AND pav.spu_id = 2
     * @param spuId
     * @param catalogId
     * @return
     */
    List<SpuItemAttrGroupVo> spuInfo(@Param("spuId") Long spuId, @Param("catalogId") Long catalogId);
}
