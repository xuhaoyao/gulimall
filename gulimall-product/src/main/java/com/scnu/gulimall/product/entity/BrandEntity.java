package com.scnu.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import com.scnu.common.valid.SaveGroup;
import com.scnu.common.valid.StatusValid;
import com.scnu.common.valid.UpdateGroup;
import lombok.Data;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * 品牌
 * 
 * @author xhy
 * @email 623834276@qq.com
 * @date 2021-09-02 13:07:58
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */
	@NotNull(message = "修改时id值不能为空",groups = {UpdateGroup.class})
	@Null(message = "新增时id必须为空",groups = {SaveGroup.class})
	@TableId
	private Long brandId;
	/**
	 * 品牌名
	 */
	@NotBlank(message = "名牌名不能为空",groups = {UpdateGroup.class,SaveGroup.class})
	private String name;
	/**
	 * 品牌logo地址
	 */
	@Pattern(regexp = "https://(?!(\\.jpg|\\.png)).+?(\\.jpg|\\.png)",
			message = "logo地址必须是一个合法的图片URL",
			groups = {UpdateGroup.class,SaveGroup.class} )
	@NotNull(message = "logo不能为空",groups = {SaveGroup.class})
	private String logo;
	/**
	 * 介绍
	 */
	@NotBlank(message = "请填写介绍信息",groups = {SaveGroup.class})
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	//@Range(min = 0,max = 1,groups = {SaveGroup.class,UpdateGroup.class})
	@StatusValid(value = {0,1},groups = {SaveGroup.class,UpdateGroup.class})
	@NotNull(groups = {SaveGroup.class},message = "状态码不能为空")
	private Integer showStatus;
	/**
	 * 检索首字母
	 */
	@Pattern(regexp = "[a-zA-Z]",message = "请填写一个字母",groups = {SaveGroup.class,UpdateGroup.class})
	private String firstLetter;
	/**
	 * 排序
	 */
	@Min(value = 0,message = "排序字段必须大于等于0",groups = {SaveGroup.class,UpdateGroup.class})
	@NotNull(message = "排序字段不能为空",groups = {SaveGroup.class})
	private Integer sort;

}
/*
@NotEmpty 用在集合类上面
@NotBlank 用在String上面
@NotNull    用在基本类型上
 */