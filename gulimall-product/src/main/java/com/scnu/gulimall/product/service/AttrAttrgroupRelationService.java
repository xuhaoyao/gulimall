package com.scnu.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scnu.common.utils.PageUtils;
import com.scnu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.scnu.gulimall.product.vo.AttrGroupVo;

import java.util.Map;

/**
 * 属性&属性分组关联
 *
 * @author xhy
 * @email 623834276@qq.com
 * @date 2021-09-02 13:07:59
 */
public interface AttrAttrgroupRelationService extends IService<AttrAttrgroupRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 添加关联关系
     * @param attrGroupVos
     */
    void attrGroupRelation(AttrGroupVo[] attrGroupVos);
}

