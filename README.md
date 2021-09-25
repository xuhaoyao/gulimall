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

**生成订单流程**

- 验证防重令牌,订单只能生成一次【lua脚本,redis保证令牌的对比与删除是原子操作】

- 创建订单

  - 远程调用查收货地址【1】

  - 远程调用查购物车:从redis再次查一遍购物车的数据,因为到了订单页面,就是用户勾选了购物车的项目跳转的,不必从前端传来购物项，从redis查的好处就是如果用户从一个页面勾选了两项商品跳转到订单页,又打开一个页面，从购物车选了三项,那么到了订单确认页，会显示用户将要购买三项【2】

  - 计算费用,优惠卷，运费等

- 订单创建好后,检验价格,看看后台计算的价格跟前台传来的价格是否一致

- 保存订单和订单项

- 远程调用:锁定库存【3】

- 远程调用:积分信息，优惠卷信息等杂七杂八..(假设做了这些功能)【4】

**发现问题**

- 多处涉及到远程调用,而且与数据库增删改查有关,这就产生了分布式事务的问题
- 库存的事务提交了,返回数据的时候,网络原因超时,这个时候订单服务以为库存服务出异常,回滚,实际上库存服务没有异常
- 假如【4】发生了异常，需要回滚数据,但【3】处数据库已经提交了

**解决**

- seata【订单是高并发场景，不适用】

- 最终一致性

  - 远程调用锁库存的时候,但锁定成功时,发一个消息【ttl = 2min】给延迟队列【stock.delay.queue】,这个消息的过期时间需要比订单消息的过期时间长上那么一点，保证了订单消息过期后，才能对应判断库存是否需要解锁
  - 写一个监听器监听【stock.release.stock.queue】,判断是否需要解锁的逻辑如下
    - 每一个订单会对应一个订单工作单,先查是否有这个订单工作单,如果没有,说明库存那边的事务回滚了【对应库存也肯定回滚了】,因此不必处理这条消息了
    - 查到订单工作单，获取最新的库存状态信息，如果库存状态是**已解锁**，说明订单那边取消了订单，顺带把库存解锁了，因此此条消息不必处理
    - 远程调用:查目前订单状态，如果订单是**不存在**或者**已取消**，都需要解锁库存
    - **幂等性保证**:由于先判断了库存的状态是不是锁定或者解锁的状态，因此解锁库存的操作是幂等的.

  - 订单创建成功后,发消息【ttl = 1min】给延迟队列【order.delay.queue】,告诉mq系统有这么一个订单，状态是待付款
  - 延迟队列的消息过期后,发给队列【order.release.order.queue】,写一个监听器监听这个队列，获取此时订单的最新状态，若订单状态还是待付款，那么取消这个订单，将订单状态置为已取消
  - 取消订单后，发一个消息给【order.release.stock.queue】,库存服务监听这个队列，拿到这个订单号就解锁对应的库存

- 最终一致性分析

  - 既然取消订单的时候，顺带解锁库存，为什么还需要在锁定库存的时候，发消息给MQ，来判断库存是否需要解锁呢？
  - 因为担心这个情况，取消订单的时候，给MQ发消息，这条消息因为网络等原因，阻塞或者丢失了，那么库存就被锁死了，锁定库存的时候发给MQ一条锁定库存的消息，用来保证库存一段时间后，能正确的锁定或者释放【确保最终一致性】
