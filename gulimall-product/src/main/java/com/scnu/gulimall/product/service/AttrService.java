package com.scnu.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scnu.common.utils.PageUtils;
import com.scnu.gulimall.product.entity.AttrEntity;
import com.scnu.gulimall.product.vo.AttrRespVo;
import com.scnu.gulimall.product.vo.AttrVo;

import java.util.Map;

/**
 * 商品属性
 *
 * @author xhy
 * @email 623834276@qq.com
 * @date 2021-09-02 13:07:59
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 保存一个属性值之后,由于属性值与属性组是多对多关系,因此需要额外给关联表加记录
     * @param attr
     */
    void saveDetail(AttrVo attr);

    /**
     *
     * @param params 分页属性
     * @param catelogId 0查所有,否则查具体分类对应的属性值
     * @return
     */
    PageUtils queryPage(Map<String, Object> params, Long catelogId, String type);

    /**
     * 规格参数页面中,修改参数值调用接口,主要回显所属分类与分属分组的id
     * @param attrId 属性id
     * @return
     */
    AttrRespVo getAttrInfo(Long attrId);

    /**
     *  规格参数页面中,保存修改操作,由于所属分组可能由先前的未填写,到这次的填写,因此分组表可能是更新,也可能是新增
     * @param attr
     */
    void updateAttr(AttrVo attr);

    /**
     * 查出该分组id没有关联的属性,同时要求是此分类下的
     */
    PageUtils noAttrRelation(Long attrgroupId,Map<String,Object> params);
}

