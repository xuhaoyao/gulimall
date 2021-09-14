package com.scnu.gulimall.member.to;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class UserRegisterTo {

    private String userName;

    private String password;

    private String email;

}
