package com.scnu.gulimall.service.impl;

import com.scnu.gulimall.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSenderImpl mailSender;

    @Async
    @Override
    public void sendEmail(String email, String code) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();

        mailMessage.setSubject("邮箱验证码");
        mailMessage.setText("【谷粒商城】" + code + "(注册验证码)");

        mailMessage.setTo(email);
        mailMessage.setFrom("623834276@qq.com");
        mailSender.send(mailMessage);
    }
}
