package com.scnu.gulimall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 1、整合MyBatis-Plus
 *      1）、导入依赖
 *      <dependency>
 *             <groupId>com.baomidou</groupId>
 *             <artifactId>mybatis-plus-boot-starter</artifactId>
 *             <version>3.2.0</version>
 *      </dependency>
 *      2）、配置
 *          1、配置数据源；
 *              1）、导入数据库的驱动。https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-versions.html
 *              2）、在application.yml配置数据源相关信息
 *          2、配置MyBatis-Plus；
 *              1）、使用@MapperScan
 *              2）、告诉MyBatis-Plus，sql映射文件位置
 *
 * 2、逻辑删除
 *  1）、配置全局的逻辑删除规则（省略）
 *  2）、配置逻辑删除的组件Bean（省略）
 *  3）、给Bean加上逻辑删除注解@TableLogic
 *
 * 3、JSR303
 *   1）、给Bean添加校验注解:javax.validation.constraints，并定义自己的message提示
 *   2)、开启校验功能@Valid
 *      效果：校验错误以后会有默认的响应；
 *   3）、给校验的bean后紧跟一个BindingResult，就可以获取到校验的结果
 *   4）、分组校验（多场景的复杂校验）
 *         1)、	@NotBlank(message = "品牌名必须提交",groups = {AddGroup.class,UpdateGroup.class})
 *          给校验注解标注什么情况需要进行校验
 *         2）、@Validated({AddGroup.class})
 *         3)、默认没有指定分组的校验注解@NotBlank，在分组校验情况@Validated({AddGroup.class})下不生效，只会在@Validated生效；
 *
 *   5）、自定义校验
 *      1）、编写一个自定义的校验注解
 *      2）、编写一个自定义的校验器 ConstraintValidator
 *      3）、关联自定义的校验器和自定义的校验注解
 *      @Documented
 * @Constraint(validatedBy = { ListValueConstraintValidator.class【可以指定多个不同的校验器，适配不同类型的校验】 })
 * @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
 * @Retention(RUNTIME)
 * public @interface ListValue {
 *
 * 4、统一的异常处理
 * @ControllerAdvice
 *  1）、编写异常处理类，使用@ControllerAdvice。
 *  2）、使用@ExceptionHandler标注方法可以处理的异常。
 *
 *  5、模板引擎
 *  1)thymeleaf-starter 关闭缓存
 *  2)静态资源放在static下就可直接按照路径访问
 *  3)页面放在templates下,直接访问不到,需要由controller跳转
 *  4)页面修改不重启服务器实时更新
 *      1)引入dev-tools
 *      2)修改完对应页面Ctrl+shift+F9
 *
 * 6、整合redis
 *  1)引入data-redis-starter
 *  2)简单配置host,port等信息
 *  3)使用redisTemplate或者stringRedisTemplate
 *
 *  7、整合redisson作为分布式锁等功能框架
 *  1.引入依赖
 *          <dependency>
 *             <groupId>org.redisson</groupId>
 *             <artifactId>redisson</artifactId>
 *             <version>3.12.0</version>
 *         </dependency>
 *  2.配置redisson
 *  3.https://github.com/redisson/redisson/wiki
 *
 *  8.整合SpringCache简化缓存开发
 *   1)、引入依赖
 *          spring-boot-starter-cache,spring-boot-starter-data-redis
 *   2）、配置
 *          1)自动配置了什么
 *              CacheAutoConfiguration会导入RedisCacheConfiguration
 *              自动配好了RedisCacheManager缓存管理器
 *          2)配置使用redis作为缓存
 *              spring.cache.type=redis
 *   3)注解
 *   首先在启动类上添加注解@EnableCaching
 *      @Cacheable: Triggers cache population.
 *      @CacheEvict: Triggers cache eviction.
 *      @CachePut: Updates the cache without interfering with the method execution.
 *      @Caching: Regroups multiple cache operations to be applied on a method.
 *      @CacheConfig: Shares some common cache-related settings at class-level.
 *
 *   4)原理
 *      CacheAutoConfiguration会导入RedisCacheConfiguration
 *      -> 自动配好了RedisCacheManager缓存管理器
 *      -> 如果redisCacheConfiguration没有配,就用默认的,否则使用我们自己配的
 *      -> 想要改缓存的配置,只需要我们注入一个redisCacheConfiguration即可
 *      -> 就会应用到当前redisCacheConfiguration管理的所有缓存分区
 *
 *   5)不足之处:
 *     1)读模式:
 *          缓存穿透:查询一个不存在的数据,缓存中没有,一直查数据库
 *              解决:缓存空数据,spring.cache.redis.cache-null-values=true(默认)
 *          缓存击穿:大量并发进来同时查询一个正好过期的数据
 *              解决:加锁(sync=true),虽然不是分布式锁,但一台机器只查一次数据库,也还行??
 *          缓存雪崩:大量的key同时过期
 *              解决:加上过期时间,spring.cache.redis.time-to-live=xxx(毫秒)
 *     2)写模式(缓存与数据库一致)
 *          1.读写加锁(读多写少的系统)
 *          2.引入Canal,感知MySQL的更新去更新redis
 *          3.读多写多,直接去数据库查询就行
 *
 */

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.scnu.gulimall.product.feign")
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}
