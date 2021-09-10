package com.scnu.gulimall.ware.service.impl;

import com.scnu.common.constant.ware.PurchaseDetailStatusEnum;
import com.scnu.common.constant.ware.PurchaseStatusEnum;
import com.scnu.gulimall.ware.entity.PurchaseDetailEntity;
import com.scnu.gulimall.ware.exception.PurchaseException;
import com.scnu.gulimall.ware.service.PurchaseDetailService;
import com.scnu.gulimall.ware.service.WareSkuService;
import com.scnu.gulimall.ware.vo.ItemDoneVo;
import com.scnu.gulimall.ware.vo.PurchaseDoneVo;
import com.scnu.gulimall.ware.vo.PurchaseMergeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scnu.common.utils.PageUtils;
import com.scnu.common.utils.Query;

import com.scnu.gulimall.ware.dao.PurchaseDao;
import com.scnu.gulimall.ware.entity.PurchaseEntity;
import com.scnu.gulimall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDetailService purchaseDetailService;

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils unreceiveList(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status",0).or().eq("status",1)
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void merge(PurchaseMergeVo vo) {
        Long purchaseId = vo.getPurchaseId();
        List<Long> items = vo.getItems();
        if(items == null) {
            return;
        }
        else{
            //检查采购单的状态,0和1才能进行合并
            items.forEach(item -> {
                PurchaseDetailEntity byId = purchaseDetailService.getById(item);
                if(byId.getStatus() != PurchaseStatusEnum.CREATED.getCode() &&
                        byId.getStatus() != PurchaseStatusEnum.ASSIGNED.getCode()){
                    throw new PurchaseException("采购单不是新建或者已分配的,请确认");
                }
            });
        }
        if(purchaseId == null){
            //新建采购单
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setStatus(PurchaseStatusEnum.CREATED.getCode());
            baseMapper.insert(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }
        final Long pId = purchaseId;
        List<PurchaseDetailEntity> collect = items.stream().map(id -> {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setPurchaseId(pId);
            purchaseDetailEntity.setStatus(PurchaseDetailStatusEnum.ASSIGNED.getCode());
            purchaseDetailEntity.setId(id);
            return purchaseDetailEntity;
        }).collect(Collectors.toList());
        purchaseDetailService.updateBatchById(collect);

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        purchaseService.updateById(purchaseEntity);

    }

    @Transactional
    @Override
    public void received(List<Long> ids) {
        List<PurchaseEntity> purchaseEntities = baseMapper.selectBatchIds(ids);
        List<PurchaseEntity> collect = purchaseEntities.stream()
                //1.确认采购单是新建的或者已分配的
                .filter(item -> (item.getStatus() == PurchaseStatusEnum.CREATED.getCode()
                        || item.getStatus() == PurchaseStatusEnum.ASSIGNED.getCode()))
                .map(item -> {
                    //2.改变状态
                    PurchaseEntity p = new PurchaseEntity();
                    p.setStatus(PurchaseStatusEnum.RECEIVE.getCode());
                    p.setUpdateTime(new Date());
                    p.setId(item.getId());
                    return p;
                }).collect(Collectors.toList());
        this.updateBatchById(collect);

        //3.改变采购项的状态
        collect.forEach(item -> {
            Long id = item.getId();
            List<PurchaseDetailEntity> purchaseDetailEntities = purchaseDetailService.selectByPurchaseId(id);
            List<PurchaseDetailEntity> collect1 = purchaseDetailEntities.stream().map(item1 -> {
                PurchaseDetailEntity p = new PurchaseDetailEntity();
                p.setId(item1.getId());
                p.setStatus(PurchaseDetailStatusEnum.BUYING.getCode());
                return p;
            }).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(collect1);
        });
    }

    @Transactional
    @Override
    public void done(PurchaseDoneVo purchaseDoneVo) {
        /*
        逻辑:完成采购,就要改变采购单,采购细节单的状态
            只有采购细节单的状态全都是已完成,采购单的状态才设置为已完成
            若采购细节单的状态有采购失败的,那么采购单的状态设置为有异常
         */
        Boolean flag = true;   //判断采购单是否成功
        List<ItemDoneVo> items = purchaseDoneVo.getItems();
        if(items != null && items.size() > 0){
            List<PurchaseDetailEntity> updateDetails = new ArrayList<>();
            for (ItemDoneVo item : items) {
                Integer status = item.getStatus();
                PurchaseDetailEntity updateDetail = new PurchaseDetailEntity();
                updateDetail.setId(item.getItemId());
                if(status == PurchaseDetailStatusEnum.HASERROR.getCode()){
                    flag = false;  //采购单没有完成采购成功->有异常
                    updateDetail.setStatus(PurchaseDetailStatusEnum.HASERROR.getCode());
                }
                else{
                    //采购的货物入库
                    PurchaseDetailEntity byId = purchaseDetailService.getById(item.getItemId());
                    wareSkuService.addStock(byId.getSkuId(),byId.getWareId(),byId.getSkuNum());
                    //采购细节单的状态:成功
                    updateDetail.setStatus(PurchaseDetailStatusEnum.FINISH.getCode());
                }
                updateDetails.add(updateDetail);
            }
            purchaseDetailService.updateBatchById(updateDetails);

            //更新采购单的状态
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setId(purchaseDoneVo.getId());
            purchaseEntity.setStatus(flag ? PurchaseStatusEnum.FINISH.getCode() : PurchaseStatusEnum.HASERROR.getCode());
            purchaseEntity.setUpdateTime(new Date());
            baseMapper.updateById(purchaseEntity);
        }
    }

}