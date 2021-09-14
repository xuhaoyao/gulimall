package com.scnu.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * spu信息介绍
 * 
 * @author xhy
 * @email 623834276@qq.com
 * @date 2021-09-02 13:07:57
 */
@Data
@TableName("pms_spu_info_desc")
public class SpuInfoDescEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 商品id
	 */
	@TableId(type = IdType.INPUT)
	private Long spuId;  //此处的spuId需要我们手动输入,若不设置的话,mp认为是自增的,插入数据库的时候不带id字段,而数据库设计的时候spuId不自增,因此会报错
	/**
	 * 商品介绍->即图片集
	 */
	private String decript;

}
