package com.scnu.gulimall.product.service.impl;

import com.scnu.common.utils.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scnu.common.utils.PageUtils;

import com.scnu.gulimall.product.dao.CategoryDao;
import com.scnu.gulimall.product.entity.CategoryEntity;
import com.scnu.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listTree() {
        //1.查所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);
        //2.找出父菜单,组装成树形结构
        List<CategoryEntity> tree = entities.stream()
                .filter(category -> category.getParentCid() == 0)
                .map(menu1 -> {
                    menu1.setChildren(getChildren(menu1,entities));
                    return menu1;
                })
                .sorted((o1, o2) -> o1.getSort() - o2.getSort())
                .collect(Collectors.toList());
        return tree;
    }

    @Transactional
    @Override
    public void removeBatch(List<Long> ids) {
        //TODO 该菜单被引用时不能删除
        baseMapper.deleteBatchIds(ids);
    }

    private List<CategoryEntity> getChildren(CategoryEntity menu, List<CategoryEntity> entities) {
        List<CategoryEntity> tree = entities.stream()
                .filter(subMenu -> subMenu.getParentCid().equals(menu.getCatId()))
                .map(subMenu -> {
                    subMenu.setChildren(getChildren(subMenu, entities));
                    return subMenu;
                })
                .sorted((o1, o2) -> o1.getSort() - o2.getSort())
                .collect(Collectors.toList());
        return tree;
    }


}