package com.scnu.common.vo;

import lombok.Data;

/**
 * 商城用户登录之后存入session中的信息
 */
@Data
public class UserInfoVo {

    private Long id;
    private String nickname;

    public UserInfoVo setNickName(String nickname){
        this.nickname = nickname;
        return this;
    }

    public UserInfoVo setId(Long id){
        this.id = id;
        return this;
    }


    //...

}
