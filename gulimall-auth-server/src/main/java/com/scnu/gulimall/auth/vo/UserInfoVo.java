package com.scnu.gulimall.auth.vo;

import lombok.Data;

@Data
public class UserInfoVo {

    private String nickname;

    public UserInfoVo setNickName(String nickname){
        this.nickname = nickname;
        return this;
    }

    //...

}
