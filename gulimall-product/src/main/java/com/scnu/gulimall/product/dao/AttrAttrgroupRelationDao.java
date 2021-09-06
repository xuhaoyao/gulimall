package com.scnu.gulimall.product.dao;

import com.scnu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scnu.gulimall.product.vo.AttrGroupVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 属性&属性分组关联
 * 
 * @author xhy
 * @email 623834276@qq.com
 * @date 2021-09-02 13:07:59
 */
@Mapper
public interface AttrAttrgroupRelationDao extends BaseMapper<AttrAttrgroupRelationEntity> {

    //delete from pms_attr_attrgroup_relation where
    //(attr_id = 1 and attr_group_id = 1)
    // or
    // (attr_id = 2 and attr_group_id = 2)
    // or ...
    void deleteRelation(@Param("attrGroupVos") AttrGroupVo[] attrGroupVos);

    void attrGroupRelation(@Param("attrGroupVos") AttrGroupVo[] attrGroupVos);
}
