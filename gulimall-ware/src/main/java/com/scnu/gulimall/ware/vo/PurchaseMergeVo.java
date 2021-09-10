package com.scnu.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

@Data
public class PurchaseMergeVo {

    /**
     * {
     *   purchaseId: 1, //整单id
     *   items:[1,2,3,4] //合并项集合
     * }
     */

    private Long purchaseId;
    private List<Long> items;

}