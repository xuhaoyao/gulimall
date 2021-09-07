package com.scnu.gulimall.product.service.impl;

import com.scnu.common.utils.Query;
import com.scnu.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.scnu.gulimall.product.dao.AttrDao;
import com.scnu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.scnu.gulimall.product.entity.AttrEntity;
import com.scnu.gulimall.product.service.AttrService;
import com.scnu.gulimall.product.vo.AttrGroupVo;
import com.scnu.gulimall.product.vo.AttrGroupWithAttrsVo;
import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scnu.common.utils.PageUtils;

import com.scnu.gulimall.product.dao.AttrGroupDao;
import com.scnu.gulimall.product.entity.AttrGroupEntity;
import com.scnu.gulimall.product.service.AttrGroupService;
import org.springframework.transaction.annotation.Transactional;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    private AttrDao attrDao;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        IPage<AttrGroupEntity> page = null;
        String key = (String) params.get("key");
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
        if(catelogId == 0){
            wrapper.eq("attr_group_id",key).or().like("attr_group_name",key);
            page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    wrapper
            );
        }
        else{
            //select * from pms_attr_group where catelog_id = ? and (attr_group_id = ? or attr_group_name like ?)
            wrapper.eq("catelog_id",catelogId);
            //前端有些参数不带key,若不加这一判断,则查不出数据,因为 catelog_id = ? and false
            if(key != null){
                wrapper.and(obj -> {
                    obj.eq("attr_group_id",key).or().like("attr_group_name",key);
                });
            }
            page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    wrapper
            );
        }
        return new PageUtils(page);
    }

    @Override
    public List<AttrEntity> attrWithGroup(Long attrgroupId) {
        List<AttrAttrgroupRelationEntity> relations = attrAttrgroupRelationDao.selectList(
                new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));
        List<Long> attrIds = relations.stream().map(relation -> relation.getAttrId()).collect(Collectors.toList());
        List<AttrEntity> attrEntities = null;
        if(attrIds.size() > 0) {
            attrEntities = attrDao.selectBatchIds(attrIds);
        }
        return attrEntities;
    }

    @Transactional
    @Override
    public void deleteRelation(AttrGroupVo[] attrGroupVos) {
        attrAttrgroupRelationDao.deleteRelation(attrGroupVos);
    }

    @Override
    public List<AttrGroupWithAttrsVo> attrGroupWithAttr(Long catelogId) {
        //1.根据此分类id得到所有分组
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("catelog_id",catelogId);
        List<AttrGroupEntity> attrGroupEntities = baseMapper.selectList(wrapper);
        //2.得到分组下关联的属性
        List<AttrGroupWithAttrsVo> collect = attrGroupEntities.stream().map(item -> {
            AttrGroupWithAttrsVo vo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(item, vo);
            List<AttrAttrgroupRelationEntity> relationEntities = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", item.getAttrGroupId()).select("attr_id"));
            List<Long> attrIds = relationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
            if (attrIds.size() > 0) {
                List<AttrEntity> attrEntities = attrDao.selectBatchIds(attrIds);
                vo.setAttrs(attrEntities);
            }
            return vo;
        }).collect(Collectors.toList());
        return collect;
    }


}