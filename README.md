## 高级篇总结

### 环境搭建

#### linux环境（阿里云1核2g centos7）

| 软件名          | 版本号 | 描述                              |
| --------------- | ------ | --------------------------------- |
| mysql           | 5.7.26 | 数据库                            |
| redis *         | 6.2.5  | 缓存数据库                        |
| rabbitmq *      | 3.9.5  | 消息队列                          |
| nacos           | 1.1.4  | 注册中心、配置中心[数据存于mysql] |
| elasticsearch * | 7.4.2  | 商城首页检索                      |
| kibana *        | 7.4.2  | elasticsearch可视化平台           |

#### Windows环境 （8g内存）

| 软件名              | 版本号        | 描述              |
| ------------------- | ------------- | ----------------- |
| idea                | 2020.3.2      | java开集成环境    |
| jdk                 | 1.8.0_271     |                   |
| SpringBoot          | 2.1.8.RELEASE |                   |
| SpringCloud         | Greenwich.SR3 |                   |
| SpringCloud alibaba | 2.1.0.RELEASE |                   |
| nginx               | 1.12.2        | 反向代理,动静分离 |
| natapp              |               | 内网穿透          |

**域名配置**

```bash
C:\Windows\System32\drivers\etc\hosts

127.0.0.1 gulimall.com
127.0.0.1 item.gulimall.com
127.0.0.1 search.gulimall.com
127.0.0.1 auth.gulimall.com
127.0.0.1 cart.gulimall.com
127.0.0.1 order.gulimall.com
127.0.0.1 member.gulimall.com
127.0.0.1 seckill.gulimall.com
```



### 模块介绍

| 模块名               | 模块名         | 描述                                                    |
| -------------------- | -------------- | ------------------------------------------------------- |
| gulimall-auth-server | 认证服务       | gitee登录、Oauth2.0                                     |
| gulimall-cart        | 购物车模块     | 购物车数据存redis                                       |
| gulimall-common      | 公共模块       | 保存常量、异常码、工具类、通用实体等                    |
| gulimall-coupon      | 优惠券模块     | 优惠券服务                                              |
| gulimall-gateway     | 网关模块       | 网关接收前端请求做统一转发                              |
| gulimall-member      | 会员模块       | 会员服务                                                |
| gulimall-order       | 订单模块       | 订单服务                                                |
| gulimall-product     | 商品模块       | 商品服务                                                |
| gulimall-search      | 检索服务       | Elasticsearch 检索                                      |
| gulimall-third-party | 第三方整合服务 | 阿里云OSS,QQ邮件发送                                    |
| gulimall-ware        | 库存模块       | 仓储服务                                                |
| gulimall-seckill     | 秒杀模块       | 秒杀服务[rabbitmq消息削峰]                              |
| renren-generator     | 代码生成器     | 后台crud代码一键生成                                    |
| renren-fast          | 人人后台生成   | [人人开源](https://gitee.com/renrenio) 后端快速开发平台 |
| renren-fast-vue      | 后台管理前端   | [人人开源](https://gitee.com/renrenio) 前端快速开发平台 |



### 配置记录

#### nginx配置

```bash
http{
	#限制请求体的大小，若超过所设定的大小，返回413错误。
	client_max_body_size 1024m;
	
	#前端请求统一发给网关,网关再转发给各个微服务
    upstream gulimall {
        server localhost:88;
    }

    server {
        listen       80;
        server_name  *.gulimall.com;

		#支付宝回调接口
        location /pay/callback {
           #内网穿透:支付宝的回调请求发送给订单模块可能会丢失请求头
           #因此携带上网关需要断定的请求头
           proxy_set_header Host order.gulimall.com;
           proxy_pass http://gulimall;
           #root   html;
        }
		
		#动静分离
        location /static/ {
            root   html;
        }

		#nginx代理给网关的时候,会丢失请求头的信息
        location / {
           proxy_set_header Host $host;
	       proxy_pass http://gulimall;
           #root   html;
        }
    }
}
```



#### nacos配置

**1.修改启动配置**

```bash
vim startup.sh
JAVA_OPT="${JAVA_OPT} -Xms64m -Xmx64m -Xmn40m"
JAVA_OPT="${JAVA_OPT} -Dnacos.standalone=true"
```

**2.账号密码问题**

```bash
Nacos采用SpringSecurity中的
    BCryptPasswordEncoder
进行密码加密
生成加密密码:
    new BCryptPasswordEncoder().encode("you password")
得到的结果插入nacos的数据库中,就可以用自定义的账号密码登录Nacos后台了
```

**3.持久化问题**

```bash
Nacos默认自带的是嵌入式数据库derby
可以切换为mysql
nacos-server-1.1.4\nacos\conf目录下找到sql脚本nacos-mysql.sql
按着sql文件创建相应的数据库和表

nacos-server-1.1.4\nacos\conf目录下找到application.properties

#在末尾处添加如下配置即可
spring.datasource.platform=mysql
db.num=1
db.url.0=jdbc:mysql://127.0.0.1:3306/nacos_config?characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true
db.user=[your username]
db.password=[your pwd]
```



#### redis配置

**1.docker配置如下**

```
mkdir -p /mydata/redis/conf
touch /mydata/redis/conf/redis.conf

docker run -p 6379:6379 --name redis -v /mydata/redis/data:/data \
-v /mydata/redis/conf/redis.conf:/etc/redis/redis.conf \
-d redis redis-server /etc/redis/redis.conf
```

**2.配置文件配置 redis.conf**

```
appendonly yes             
requirepass [input your password]
```



### 技术点总结

#### 1.跨域问题

**gulimall-gateway:在网关处统一解决**

```java
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter(){
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.setAllowCredentials(true);  //包含Cookie

        source.registerCorsConfiguration("/**",corsConfiguration);
        return new CorsWebFilter(source);
    }
}

```



#### 2.登录问题

- 同一个服务,三台机器,session不同步问题
- 不同服务,不同域名,session不能共享

**解决:使用SpringSession**

- 登录模块,以jsessionid为key将session存到redis中,并且将jsessionid以cookie的形式返回给浏览器,设置cookie的域名为父域名**(谷粒商城只有gulimall.com这个父域名,以及它众多的子域名)**

- 不同微服务发送请求时,都会携带jsessionid,根据这个jsessionid去redis拿到session

- 即order:100 给浏览器发了一个jsessionid,并且将这个session存入redis
- order:101 可以根据jsessionid从redis拿到order:100的session,实现了session共享
- 因此整合了SpringSession的模块,需要写一个拦截器来判断用户是否登录,且通过ThreadLocal传递登录用户的信息



```java
@EnableRedisHttpSession
@Configuration
public class SessionConfig {
    /**
     * 存入redis以json方式存入,否则每个存入session的类都需要序列化implements Serializable,否则报错
     * @return
     */
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new GenericFastJsonRedisSerializer();
    }

    /**
     * cookieName默认是SESSION
     * 域名一定要写父域名
     * cookie的作用域是domain本身以及domain下的所有子域名。
     * @return
     */
    @Bean
    public CookieSerializer cookieSerializer(){
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        cookieSerializer.setCookieName("GULIMALLCOOKIE");
        cookieSerializer.setDomainName("gulimall.com");
        return cookieSerializer;
    }

}
```

```java
@Component
public class UserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<UserInfoVo> userInfoThreadLocal = new ThreadLocal<>();

    /**
     * 只拦截订单相关的请求,在WebConfig中配置了
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        System.out.println(request.getRequestURI());

        UserInfoVo userInfo = (UserInfoVo) request.getSession().getAttribute(AuthConstant.SESSION_USER_NAME);
        if(userInfo == null){
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        }
        userInfoThreadLocal.set(userInfo);
        return true;
    }
}

```



**4.原理分析**

**核心类:**

- RedisOperationsSessionRepository
- SessionRepositoryFilter
- DefaultCookieSerializer

```java
/**
 * 核心原理[装饰者模式]
 * 1) @EnableRedisHttpSession
 *      @Import({RedisHttpSessionConfiguration.class})
 *      1.给容器中添加了 SessionRepository 【RedisOperationsSessionRepository】 :redis操作session,session的增删改查封装类
 *      2. SessionRepositoryFilter ->session存储过滤器,每个请求过来都必须经过filter
 *          SessionRepositoryFilter构造函数传入一个SessionRepository,即RedisOperationsSessionRepository
 *          原生的request,response被包装SessionRepositoryRequestWrapper
 *              SessionRepositoryRequestWrapper通过SessionRepository取session
 *
 *          通过如下方式取session
 *          private S getRequestedSession() {
 *             if (!this.requestedSessionCached) {
 *
 *                 List<String> sessionIds =
 *                      SessionRepositoryFilter.this.httpSessionIdResolver.resolveSessionIds(this){
 *                            //此方法找sessionId是通过cookie的名字找的this.cookieName.equals(cookie.getName())
 *                            //cookieName默认是SESSION,当可以自己指定cookieSerializer.setCookieName
 *                            //而cookieSerializer就是配置好了的DefaultCookieSerializer
 *                            //即前端只要传来的cookieName=GULIMALLCOOKIE,那么就可以拿到这个cookie携带的sessionId了
 *                            //1.拿到了sessionId,就可以去redis查到session
 *                            //2.再设置好cookie的域名是父域
 *                            //由1和2就实现了分布式下session共享
 *                            return this.cookieSerializer.readCookieValues(request);
 *                      }
 *                 Iterator var2 = sessionIds.iterator();
 *
 *                 while(var2.hasNext()) {
 *                     String sessionId = (String)var2.next();
 *                     if (this.requestedSessionId == null) {
 *                         this.requestedSessionId = sessionId;
 *                     }
 *                      //从redis找到这个session
 *                     S session = SessionRepositoryFilter.this.sessionRepository.findById(sessionId);
 *                     if (session != null) {
 *                         this.requestedSession = session;
 *                         this.requestedSessionId = sessionId;
 *                         break;
 *                     }
 *                 }
 *
 *                 this.requestedSessionCached = true;
 *             }
 *
 *             return this.requestedSession;
 *         }
 */
```



#### 3.订单问题[最终一致性]
![消息队列流程](https://user-images.githubusercontent.com/56396192/134756689-796dacbe-fbca-4eab-a20f-9737df1749f9.jpg)
