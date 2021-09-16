package com.scnu.gulimall.auth.config;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * 核心原理[装饰者模式]
 * 核心类:
 * - RedisOperationsSessionRepository
 * - SessionRepositoryFilter
 * - DefaultCookieSerializer
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
