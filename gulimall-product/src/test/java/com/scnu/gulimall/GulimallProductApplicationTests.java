package com.scnu.gulimall;

import com.scnu.gulimall.product.entity.BrandEntity;
import com.scnu.gulimall.product.service.BrandService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GulimallProductApplicationTests {

    @Autowired
    private BrandService brandService;

    @Test
    public void contextLoads() {
        //BrandEntity brandEntity = new BrandEntity();
        //brandEntity.setDescript("测试...");
        //brandService.save(brandEntity);
        List<BrandEntity> list = brandService.list(null);
        list.forEach(System.out::println);
    }

}
