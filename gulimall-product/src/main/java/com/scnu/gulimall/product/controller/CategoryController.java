package com.scnu.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.scnu.gulimall.product.entity.CategoryEntity;
import com.scnu.gulimall.product.service.CategoryService;
import com.scnu.common.utils.PageUtils;
import com.scnu.common.utils.R;



/**
 * 商品三级分类
 *
 * @author xhy
 * @email 623834276@qq.com
 * @date 2021-09-02 13:07:58
 */
@RestController
@RequestMapping("product/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 查出所有商品分类,并组装成树形结构
     */
    @RequestMapping("/list/tree")
    //@RequiresPermissions("product:category:list")
    public R listTree(){
        List<CategoryEntity> categoryEntities = categoryService.listTree();

        return R.ok().put("data", categoryEntities);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{catId}")
    //@RequiresPermissions("product:category:info")
    public R info(@PathVariable("catId") Long catId){
		CategoryEntity category = categoryService.getById(catId);

        return R.ok().put("data", category);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:category:save")
    public R save(@RequestBody CategoryEntity category){
		categoryService.save(category);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:category:update")
    public R update(@RequestBody CategoryEntity category){
		categoryService.updateById(category);

        return R.ok();
    }

    /**
     * 批量修改
     */
    @RequestMapping("/update/sort")
    //@RequiresPermissions("product:category:update")
    public R updateSort(@RequestBody CategoryEntity[] category){
        categoryService.updateBatchById(Arrays.asList(category));
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:category:delete")
    public R delete(@RequestBody Long[] catIds){
		//categoryService.removeByIds(Arrays.asList(catIds));
		categoryService.removeBatch(Arrays.asList(catIds));
        return R.ok();
    }

}
