package com.scnu.gulimall.product.service.impl;

import com.scnu.common.constant.product.AttrEnum;
import com.scnu.common.utils.Query;
import com.scnu.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.scnu.gulimall.product.dao.AttrGroupDao;
import com.scnu.gulimall.product.dao.CategoryDao;
import com.scnu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.scnu.gulimall.product.entity.AttrGroupEntity;
import com.scnu.gulimall.product.entity.CategoryEntity;
import com.scnu.gulimall.product.service.CategoryService;
import com.scnu.gulimall.product.vo.AttrRespVo;
import com.scnu.gulimall.product.vo.AttrVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scnu.common.utils.PageUtils;

import com.scnu.gulimall.product.dao.AttrDao;
import com.scnu.gulimall.product.entity.AttrEntity;
import com.scnu.gulimall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private AttrGroupDao attrGroupDao;

    @Autowired
    private CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveDetail(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr,attrEntity);
        baseMapper.insert(attrEntity);
        //基本属性才进关联表,销售属性不进关联表,因为它没有分组属性
        if(attr.getAttrType().equals(AttrEnum.BASE.getCode())) {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrId(attrEntity.getAttrId());
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            attrAttrgroupRelationDao.insert(relationEntity);
        }
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId,String type) {
        IPage<AttrEntity> page = null;
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if(catelogId != 0){
            wrapper.eq("catelog_id",catelogId);
        }
        if(StringUtils.hasLength(key)){
            wrapper.and(obj -> {
                obj.eq("attr_id",key).or().like("attr_name",key);
            });
        }
        wrapper.eq("attr_type","base".equals(type) ? AttrEnum.BASE.getCode() : AttrEnum.SALE.getCode());
        page = this.page(
                new Query<AttrEntity>().getPage(params),
                wrapper
        );
        List<AttrEntity> records = page.getRecords();
        //System.out.println(records);

        List<AttrRespVo> respVos = records.stream().map(attrEntity -> {
            AttrRespVo attrRespVo = new AttrRespVo();
            BeanUtils.copyProperties(attrEntity, attrRespVo);

            //1.查分类名字
            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            if(categoryEntity != null){
                attrRespVo.setCatelogName(categoryEntity.getName());
            }
            //规格参数才有分组属性,销售参数没有.
            if("base".equals(type)) {
                //2.查分组名字 从关联表查到分组的id,再查名字
                AttrAttrgroupRelationEntity relationEntity = attrAttrgroupRelationDao
                        .selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
                if (relationEntity != null) {
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(relationEntity.getAttrGroupId());
                    if (attrGroupEntity != null) {
                        attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
                    }
                }
            }

            return attrRespVo;
        }).collect(Collectors.toList());
        //System.out.println(respVos);
        PageUtils pageUtils = new PageUtils(page);
        //pageUtils接受设置泛型?,而这里IPage只接受具体的AttrEntity
        pageUtils.setList(respVos);
        return pageUtils;
    }

    @Override
    public AttrRespVo getAttrInfo(Long attrId) {
        AttrEntity attrEntity = baseMapper.selectById(attrId);
        AttrRespVo attrRespVo = new AttrRespVo();
        BeanUtils.copyProperties(attrEntity,attrRespVo);
        //1.所属分类
        Long[] catelogIdPath = categoryService.getCatelogIdPath(attrEntity.getCatelogId());
        attrRespVo.setCatelogPath(catelogIdPath);
        //销售属性才有分组信息
        if(attrEntity.getAttrType().equals(AttrEnum.BASE.getCode())) {
            //2.所属分组
            AttrAttrgroupRelationEntity relationEntity = attrAttrgroupRelationDao
                    .selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
            if (relationEntity != null) {
                attrRespVo.setAttrGroupId(relationEntity.getAttrGroupId());
            }
        }
        return attrRespVo;
    }

    @Transactional
    @Override
    public void updateAttr(AttrVo attrVo) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attrVo,attrEntity);
        baseMapper.updateById(attrEntity);
        Long attrGroupId = attrVo.getAttrGroupId();
        Long attrId = attrVo.getAttrId();
        //基本属性才进关联表,销售属性不进关联表,因为它没有分组属性
        if(attrVo.getAttrType().equals(AttrEnum.BASE.getCode())) {
            //先判断关联表中是否有了分组的记录,没有的话添加,有的话修改
            Integer count = attrAttrgroupRelationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>()
                    .eq("attr_id", attrId));
            AttrAttrgroupRelationEntity entity = new AttrAttrgroupRelationEntity();
            entity.setAttrId(attrId);
            entity.setAttrGroupId(attrGroupId);
            if (count == 0) {
                //添加
                attrAttrgroupRelationDao.insert(entity);
            } else {
                //更新
                attrAttrgroupRelationDao.update(entity, new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            }
        }
    }

    @Override
    public PageUtils noAttrRelation(Long attrgroupId, Map<String, Object> params) {
        //当前分组只能关联自己所属分类里面的属性
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        //当前分组只能关联别的分组没有关联的属性,同时自己关联的属性也不可以再关联
        //1.首先查出属于catelogId分类的所有分组,包括attrgroupId这个分组
        List<AttrGroupEntity> attrGroups = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId).select("attr_group_id"));
        //2.得到分组的id
        List<Long> attrGroupIds = attrGroups.stream().map(item -> item.getAttrGroupId()).collect(Collectors.toList());
        //3.从关联表中查到这些分组id的attr_id
        QueryWrapper<AttrAttrgroupRelationEntity> attrGroupWrapper = new QueryWrapper<>();
        attrGroupWrapper.select("attr_id");
        if(attrGroupIds.size() > 0){
            attrGroupWrapper.in("attr_group_id",attrGroupIds);
        }
        List<AttrAttrgroupRelationEntity> attrGroupRelations = attrAttrgroupRelationDao.selectList(attrGroupWrapper);
        List<Long> attrIds = attrGroupRelations.stream().map(item -> item.getAttrId()).collect(Collectors.toList());
        //4.从attr表中查attrgroupId所属分类下的全部属性值,剔除掉attrIds这些
        QueryWrapper<AttrEntity> attrWrapper = new QueryWrapper<>();
        attrWrapper.eq("catelog_id", catelogId);
        if(attrIds.size() > 0){
            attrWrapper.notIn("attr_id", attrIds);
        }

        String key = (String) params.get("key");
        if(StringUtils.hasLength(key)){
            attrWrapper.eq("attr_id",key).or().like("attr_name",key);
        }

        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                attrWrapper
        );

        return new PageUtils(page);
    }

}