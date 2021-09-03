package com.scnu.gulimall.member.dao;

import com.scnu.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author xhy
 * @email 623834276@qq.com
 * @date 2021-09-02 14:14:11
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
