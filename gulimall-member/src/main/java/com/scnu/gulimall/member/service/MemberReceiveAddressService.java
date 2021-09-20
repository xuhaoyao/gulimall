package com.scnu.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scnu.common.utils.PageUtils;
import com.scnu.gulimall.member.entity.MemberReceiveAddressEntity;

import java.util.List;
import java.util.Map;

/**
 * 会员收货地址
 *
 * @author xhy
 * @email 623834276@qq.com
 * @date 2021-09-02 14:14:10
 */
public interface MemberReceiveAddressService extends IService<MemberReceiveAddressEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 查用户的收货地址
     * @param id
     * @return
     */
    List<MemberReceiveAddressEntity> memberAddress(Long id);
}

