package com.scnu.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scnu.common.utils.PageUtils;
import com.scnu.gulimall.product.entity.AttrEntity;
import com.scnu.gulimall.product.entity.AttrGroupEntity;
import com.scnu.gulimall.product.vo.AttrGroupVo;
import com.scnu.gulimall.product.vo.AttrGroupWithAttrsVo;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author xhy
 * @email 623834276@qq.com
 * @date 2021-09-02 13:07:58
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 根据catelogId查pms_attr_group表中的数据(三级分类对应的属性分组)
     */
    PageUtils queryPage(Map<String, Object> params, Long catelogId);

    /**
     * 根据分组id查找这个分组下的所有属性值
     * @param attrgroupId
     * @return
     */
    List<AttrEntity> attrWithGroup(Long attrgroupId);

    /**
     * 删除关联关系  [{"attrId":1,"attrGroupId":2}]
     * @param attrGroupVos
     */
    void deleteRelation(AttrGroupVo[] attrGroupVos);

    /**
     * 获取分类下所有分组&关联属性
     * @param catelogId
     * @return
     */
    List<AttrGroupWithAttrsVo> attrGroupWithAttr(Long catelogId);
}

