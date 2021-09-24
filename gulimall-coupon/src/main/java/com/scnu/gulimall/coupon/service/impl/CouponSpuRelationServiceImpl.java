package com.scnu.gulimall.coupon.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scnu.common.utils.PageUtils;
import com.scnu.common.utils.Query;

import com.scnu.gulimall.coupon.dao.CouponSpuRelationDao;
import com.scnu.gulimall.coupon.entity.CouponSpuRelationEntity;
import com.scnu.gulimall.coupon.service.CouponSpuRelationService;
import org.springframework.util.StringUtils;


@Service("couponSpuRelationService")
public class CouponSpuRelationServiceImpl extends ServiceImpl<CouponSpuRelationDao, CouponSpuRelationEntity> implements CouponSpuRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<CouponSpuRelationEntity> wrapper= new QueryWrapper<>();
        IPage<CouponSpuRelationEntity> page = this.page(
                new Query<CouponSpuRelationEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

}