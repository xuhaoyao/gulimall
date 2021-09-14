package com.scnu.gulimall.controller;

import com.scnu.common.utils.R;
import com.scnu.gulimall.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/sendCode")
    public R sendCode(@RequestParam("email") String email,@RequestParam("code") String code){
        System.out.println(email);
        emailService.sendEmail(email,code);
        return R.ok();
    }


}
