package com.scnu.gulimall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.scnu.common.to.SkuHasStockTo;
import com.scnu.gulimall.ware.exception.NoStockException;
import com.scnu.gulimall.ware.vo.LockStockResult;
import com.scnu.gulimall.ware.vo.WareSkuLockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.scnu.gulimall.ware.entity.WareSkuEntity;
import com.scnu.gulimall.ware.service.WareSkuService;
import com.scnu.common.utils.PageUtils;
import com.scnu.common.utils.R;



/**
 * 商品库存
 *
 * @author xhy
 * @email 623834276@qq.com
 * @date 2021-09-02 14:21:28
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;

    /**
     * 订单服务远程调用此方法,锁定库存
     * @param vo
     * @return
     */
    @PostMapping("/lock/order")
    public R orderLockStock(@RequestBody WareSkuLockVo vo){
        try {
            wareSkuService.orderLockStock(vo);
            return R.ok();
        } catch (NoStockException e) {
            e.printStackTrace();
            return R.error().put("msg",e.getMessage());
        }
    }

    /**
     * 远程调用,查询是否有库存
     */
    @PostMapping("/hasStock")
    public List<SkuHasStockTo> hasStock(@RequestBody List<Long> skuIds){
        List<SkuHasStockTo> skuHasStockTos = wareSkuService.hasStock(skuIds);
        return skuHasStockTos;
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:waresku:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:waresku:info")
    public R info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:waresku:save")
    public R save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:waresku:update")
    public R update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:waresku:delete")
    public R delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
