package com.scnu.gulimall.product.controller;

import java.util.Arrays;
import java.util.Map;

import com.scnu.gulimall.product.vo.SpuSaveVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.scnu.gulimall.product.entity.SpuInfoEntity;
import com.scnu.gulimall.product.service.SpuInfoService;
import com.scnu.common.utils.PageUtils;
import com.scnu.common.utils.R;



/**
 * spu信息
 *
 * @author xhy
 * @email 623834276@qq.com
 * @date 2021-09-02 13:07:57
 */
@RestController
@RequestMapping("product/spuinfo")
public class SpuInfoController {
    @Autowired
    private SpuInfoService spuInfoService;

    /**
     * 根据skuId得到spu信息
     */
    @GetMapping("/skuId/{id}")
    public R getSpuInfoBySkuId(@PathVariable("id") Long id){
        SpuInfoEntity entity = spuInfoService.getSpuInfoBySkuId(id);
        return R.ok().put("data",entity);
    }

    /**
     * 商品上架
     * /product/spuinfo/{spuId}/up
     */
    @PostMapping("/{spuId}/up")
    public R up(@PathVariable("spuId") Long spuId){
        spuInfoService.up(spuId);
        return R.ok();
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:spuinfo:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = spuInfoService.querySpuPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("product:spuinfo:info")
    public R info(@PathVariable("id") Long id){
		SpuInfoEntity spuInfo = spuInfoService.getById(id);

        return R.ok().put("spuInfo", spuInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:spuinfo:save")
    public R save(@RequestBody SpuSaveVo spuSaveVo){
		spuInfoService.saveSpuInfo(spuSaveVo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:spuinfo:update")
    public R update(@RequestBody SpuInfoEntity spuInfo){
		spuInfoService.updateById(spuInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:spuinfo:delete")
    public R delete(@RequestBody Long[] ids){
		spuInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
