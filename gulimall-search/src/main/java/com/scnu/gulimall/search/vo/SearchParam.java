package com.scnu.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * 页面传过来的查询条件
 */
@Data
public class SearchParam {

    private String keyword; //页面传递过来的全文检索关键字

    private Long catalog3Id; //三级分类id

    /**
     * sort=saleCount_asc/desc
     * sort=skuPrice_asc/desc
     * sort=hotScore_asc/desc
     */
    private String sort; //排序条件

    /**
     * hasStock(是否有货),skuPrice(区间),brandId,
     *
     * 约定:
     *  skuPrice=1_500 1-500之间
     *  skuPrice=_500 小于等于500
     *  skuPrice=500_ 大于等于500
     *
     *  attrs=1_安卓:小米
     *  attrs=2_4G:8G
     */
    private Integer hasStock; //是否只显示有货
    private String skuPrice;  //价格区间查询
    private List<Long> brandId;//按照品牌查询(可以多选)
    private List<String> attrs; //按照属性进行筛选

    private Integer pageNum = 1; //页码

    private String queryParameter;

}
