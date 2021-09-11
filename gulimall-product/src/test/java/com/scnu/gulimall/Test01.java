package com.scnu.gulimall;

import com.scnu.gulimall.product.service.CategoryService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Test01 {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redisson;

    @Test
    public void test1(){
        Long[] catelogIdPath = categoryService.getCatelogIdPath(225L);
        for (int i = 0; i < catelogIdPath.length; i++) {
            System.out.println(catelogIdPath[i]);
        }
    }

    @Test
    public void testRedis(){
        //保存
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        ops.set("hello","world" + UUID.randomUUID().toString());
        String hello = ops.get("hello");
        System.out.println(hello);
    }

    @Test
    public void testRedisson(){
        System.out.println(redisson);
    }
}
