package com.scnu.gulimall.ware.vo;

import com.scnu.gulimall.ware.to.MemberReceiveAddressEntity;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddressFeeVo {

    private BigDecimal fee;
    private String name;
    private String phone;
    private String address;

    private MemberReceiveAddressEntity detail; //前面的name,phone,address应该保存在这里,但是又要改动之前写的页面,这里不改了

}
