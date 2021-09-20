package com.scnu.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.scnu.common.utils.R;
import com.scnu.gulimall.ware.feign.MemberFeignService;
import com.scnu.gulimall.ware.to.MemberReceiveAddressEntity;
import com.scnu.gulimall.ware.vo.AddressFeeVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scnu.common.utils.PageUtils;
import com.scnu.common.utils.Query;

import com.scnu.gulimall.ware.dao.WareInfoDao;
import com.scnu.gulimall.ware.entity.WareInfoEntity;
import com.scnu.gulimall.ware.service.WareInfoService;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    private MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> wareInfoEntityQueryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wareInfoEntityQueryWrapper.eq("id",key).or()
                    .like("name",key)
                    .or().like("address",key)
                    .or().like("areacode",key);
        }

        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wareInfoEntityQueryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public AddressFeeVo addressFee(Long addrId) {
        AddressFeeVo vo = new AddressFeeVo();
        R info = memberFeignService.info(addrId);
        MemberReceiveAddressEntity memberReceiveAddress = info.getData("memberReceiveAddress", new TypeReference<MemberReceiveAddressEntity>() {});
        vo.setName(memberReceiveAddress.getName());
        vo.setPhone(memberReceiveAddress.getPhone());
        vo.setAddress(memberReceiveAddress.getProvince() + memberReceiveAddress.getDetailAddress());
        vo.setFee(new BigDecimal(vo.getPhone().substring(5,6)));
        vo.setDetail(memberReceiveAddress);
        return vo;
    }

}