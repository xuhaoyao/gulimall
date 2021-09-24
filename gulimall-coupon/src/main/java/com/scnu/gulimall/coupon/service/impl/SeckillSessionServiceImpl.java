package com.scnu.gulimall.coupon.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.scnu.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.scnu.gulimall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scnu.common.utils.PageUtils;
import com.scnu.common.utils.Query;

import com.scnu.gulimall.coupon.dao.SeckillSessionDao;
import com.scnu.gulimall.coupon.entity.SeckillSessionEntity;
import com.scnu.gulimall.coupon.service.SeckillSessionService;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    private SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> seckillLastXDay(Integer x) {
        QueryWrapper<SeckillSessionEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("status",1).between("start_time",startDay(),endDay(x));
        List<SeckillSessionEntity> entities = baseMapper.selectList(wrapper);
        System.out.println(entities);
        if(ObjectUtils.isNotEmpty(entities)){
            entities.forEach(item -> {
                QueryWrapper<SeckillSkuRelationEntity> relationWrapper = new QueryWrapper<>();
                relationWrapper.eq("promotion_session_id",item.getId());
                item.setRelationSkus(seckillSkuRelationService.list(relationWrapper));
            });
        }
        return entities;
    }

    private String startDay(){
        LocalDate today = LocalDate.now();
        LocalTime min = LocalTime.MIN;
        LocalDateTime dateTime = LocalDateTime.of(today,min);
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String endDay(Integer x){
        x = x < 1 ? 1 : x;
        LocalDate xDay = LocalDate.now().plusDays(x - 1);
        LocalTime max = LocalTime.MAX;
        LocalDateTime dateTime = LocalDateTime.of(xDay,max);
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

}