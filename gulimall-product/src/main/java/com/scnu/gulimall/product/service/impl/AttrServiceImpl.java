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
        //???????????????????????????,???????????????????????????,???????????????????????????
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

            //1.???????????????
            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            if(categoryEntity != null){
                attrRespVo.setCatelogName(categoryEntity.getName());
            }
            //??????????????????????????????,??????????????????.
            if("base".equals(type)) {
                //2.??????????????? ???????????????????????????id,????????????
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
        //pageUtils???????????????????,?????????IPage??????????????????AttrEntity
        pageUtils.setList(respVos);
        return pageUtils;
    }

    @Override
    public AttrRespVo getAttrInfo(Long attrId) {
        AttrEntity attrEntity = baseMapper.selectById(attrId);
        AttrRespVo attrRespVo = new AttrRespVo();
        BeanUtils.copyProperties(attrEntity,attrRespVo);
        //1.????????????
        Long[] catelogIdPath = categoryService.getCatelogIdPath(attrEntity.getCatelogId());
        attrRespVo.setCatelogPath(catelogIdPath);
        //??????????????????????????????
        if(attrEntity.getAttrType().equals(AttrEnum.BASE.getCode())) {
            //2.????????????
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
        //???????????????????????????,???????????????????????????,???????????????????????????
        if(attrVo.getAttrType().equals(AttrEnum.BASE.getCode())) {
            //????????????????????????????????????????????????,??????????????????,???????????????
            Integer count = attrAttrgroupRelationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>()
                    .eq("attr_id", attrId));
            AttrAttrgroupRelationEntity entity = new AttrAttrgroupRelationEntity();
            entity.setAttrId(attrId);
            entity.setAttrGroupId(attrGroupId);
            if (count == 0) {
                //??????
                attrAttrgroupRelationDao.insert(entity);
            } else {
                //??????
                attrAttrgroupRelationDao.update(entity, new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            }
        }
    }

    @Override
    public PageUtils noAttrRelation(Long attrgroupId, Map<String, Object> params) {
        //?????????????????????????????????????????????????????????
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        //?????????????????????????????????????????????????????????,????????????????????????????????????????????????
        //1.??????????????????catelogId?????????????????????,??????attrgroupId????????????
        List<AttrGroupEntity> attrGroups = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId).select("attr_group_id"));
        //2.???????????????id
        List<Long> attrGroupIds = attrGroups.stream().map(item -> item.getAttrGroupId()).collect(Collectors.toList());
        //3.?????????????????????????????????id???attr_id
        QueryWrapper<AttrAttrgroupRelationEntity> attrGroupWrapper = new QueryWrapper<>();
        attrGroupWrapper.select("attr_id");
        if(attrGroupIds.size() > 0){
            attrGroupWrapper.in("attr_group_id",attrGroupIds);
        }
        List<AttrAttrgroupRelationEntity> attrGroupRelations = attrAttrgroupRelationDao.selectList(attrGroupWrapper);
        List<Long> attrIds = attrGroupRelations.stream().map(item -> item.getAttrId()).collect(Collectors.toList());
        //4.???attr?????????attrgroupId?????????????????????????????????,?????????attrIds??????
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