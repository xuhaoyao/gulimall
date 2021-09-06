package com.scnu.gulimall;

import com.scnu.gulimall.product.service.CategoryService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Test01 {

    @Autowired
    private CategoryService categoryService;

    @Test
    public void test1(){
        Long[] catelogIdPath = categoryService.getCatelogIdPath(225L);
        for (int i = 0; i < catelogIdPath.length; i++) {
            System.out.println(catelogIdPath[i]);
        }
    }
}
