package com.scnu.gulimall.search.vo;

import lombok.Data;

@Data
public class AttrRespVo extends AttrEntity {

    /**
     *          规格参数,即attr属性表查询时需要用到的字段
     * 			"catelogName": "手机/数码/手机", //所属分类名字
     * 			"groupName": "主体", //所属分组名字
     */
    private String catelogName;

    private String groupName;

    /**
     * 规格参数,即attr属性表修改时需要用到
     */
    private Long attrGroupId;

    private Long[] catelogPath;

}
