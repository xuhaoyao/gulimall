package com.scnu.gulimall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.scnu.common.constant.auth.AuthConstant;
import com.scnu.common.exception.ErrorCode;
import com.scnu.common.utils.R;
import com.scnu.common.vo.UserInfoVo;
import com.scnu.gulimall.auth.constant.RedisConstant;
import com.scnu.gulimall.auth.feign.MemberFeignService;
import com.scnu.gulimall.auth.feign.ThirdPartyFeignService;
import com.scnu.gulimall.auth.vo.UserLoginVo;
import com.scnu.gulimall.auth.vo.UserRegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class AuthController {

    @Autowired
    private ThirdPartyFeignService thirdPartyFeignService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private MemberFeignService memberFeignService;

    @GetMapping("/sendCode")
    @ResponseBody
    public R sendCode(@RequestParam(value = "email") String email){
        //TODO 接口防刷
        String code = UUID.randomUUID().toString().substring(0,5);

        String oldCode = stringRedisTemplate.opsForValue().get(RedisConstant.PREFIX_EMAIL + email);
        //1分钟以内不能重复发送验证码
        if(StringUtils.hasLength(oldCode)){
            long oldTime = Long.parseLong(oldCode.split("_")[1]);
            long thisTime = System.currentTimeMillis();
            if(thisTime - oldTime < 60 * 1000){
                return R.error(ErrorCode.EMAIL_ERROR.getCode(),ErrorCode.EMAIL_ERROR.getMsg());
            }
        }
        String key = RedisConstant.PREFIX_EMAIL + email;
        String value = code + "_" + System.currentTimeMillis();
        //邮箱验证码五分钟有效
        stringRedisTemplate.opsForValue().set(key,value,5, TimeUnit.MINUTES);
        thirdPartyFeignService.sendCode(email,code);
        return R.ok();
    }

    /**
     *  bug1: Content type 'application/x-www-form-urlencoded;charset=UTF-8' not supported
     *  原因:
     *      一开始标注了@RequestBody UserRegisterVo vo,出现报错
     *  解决:
     *      1.去掉@RequestBody,解决问题
     *  分析:
     *      此处前端是表单提交,表单提交的请求头数据格式是application/x-www-form-urlencoded
     *      而@RequestBody需要接受一个json字符串,即它需要的数据格式是application/json
     *      数据格式不一致,因此报错
     *
     *  bug2: (type=Method Not Allowed, status=405).
     *  原因:
     *      一开始写的是转发,而不是重定向
     *          return "forward:/register.html";
     *      registry.addViewController("/register.html").setViewName("register");
     *      由于做了路径映射,转发的时候就能根据register.html找到templates下的register.html,然而是405
     *      若不做路径映射,那么是404
     *  解决:
     *      不用转发,直接用视图跳转,用thymeleaf渲染视图
     *      return "register";
     *  分析:
     *      路径映射和转发默认都是get方式访问的.而这个方式是post方法,转发就是原请求原封不动转给下一个,即post方式给get方式一个请求,
     *      就会出现(type=Method Not Allowed, status=405).
     *
     *  bug3: 渲染视图后,可能导致的表单重复提交,即用户刷新页面,使得表单又提交一次
     *  解决:
     *      重定向
     *
     *  //TODO 重定向携带数据,利用sesstion,将数据放在session中,只要跳到下一个页面取出这个数据以后,session里面的数据就会删掉
     *  //TODO 分布式下的session问题
     *  RedirectAttributes redirectAttributes:模拟重定向携带数据
     */
    @PostMapping(value = "/register")
    public String register(@Valid UserRegisterVo vo, BindingResult bindingResult, RedirectAttributes redirectAttributes){
        Map<String, String> errors = new HashMap<>();
        if(bindingResult.hasErrors()){
            errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            redirectAttributes.addFlashAttribute("errors",errors);
            /**
             *  默认返回的是:http://192.168.172.1:20000/register.html
             *  即当前服务器
             *     因此要写全路径跳转
             */
            //return "forward:/register.html";
            return "redirect:http://auth.gulimall.com/register.html";
        }

        //判断验证码有没有过期
        String redisCode = stringRedisTemplate.opsForValue().get(RedisConstant.PREFIX_EMAIL + vo.getEmail());
        if(StringUtils.isEmpty(redisCode)){
            errors.put("code","验证码已过期");
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/register.html";
        }else{
            if(redisCode.split("_")[0].equals(vo.getCode())){
                //远程调用:注册用户
                R register = memberFeignService.register(vo);
                //注册成功
                if(register.getCode() == 0) {
                    return "redirect:http://auth.gulimall.com/login.html";
                }
                //注册失败
                else{
                    errors.put("msg", (String) register.get("msg"));
                    redirectAttributes.addFlashAttribute("errors",errors);
                    return "redirect:http://auth.gulimall.com/register.html";
                }
            }
            else{
                errors.put("code","验证码错误");
                redirectAttributes.addFlashAttribute("errors",errors);
                return "redirect:http://auth.gulimall.com/register.html";
            }
        }
    }

    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes attributes, HttpSession session){

        R login = memberFeignService.login(vo);
        if(login.getCode() != 0){
            attributes.addFlashAttribute("msg",login.get("msg"));
            return "redirect:http://auth.gulimall.com/login.html";
        }
        UserInfoVo data = login.getData("data", new TypeReference<UserInfoVo>(){});
        session.setAttribute(AuthConstant.SESSION_USER_NAME,data);
        return "redirect:http://gulimall.com";
    }

}
